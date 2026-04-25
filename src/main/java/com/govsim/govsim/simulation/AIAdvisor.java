package com.govsim.govsim.simulation;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Minister;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.president.DecisionOption;
import com.govsim.govsim.president.DecisionType;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * AI Advisor — connects to Gemini and returns DecisionOption arrays:
 * 1. Dangerous event  — 3 DecisionOptions with cost
 * 2. Monthly report   — DecisionOptions per ministry (ADD/CUT/KEEP)
 * 3. Annual review    — 1 DecisionOption (KEEP/FIRE)
 */
public class AIAdvisor {

    // Load API key and model from .env
    private static final Dotenv  dotenv  = Dotenv.load();
    private static final String  API_KEY = dotenv.get("GEMINI_API_KEY");
    private static final String  MODEL   = dotenv.get("GEMINI_MODEL");
    private static final String  API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/" +
        MODEL + ":generateContent?key=" + API_KEY;

    private final HttpClient client = HttpClient.newHttpClient();

    // ─────────────────────────────────────────────────────
    // CASE 1 — Dangerous Event
    // Sends event + city state to Gemini
    // Returns 3 DecisionOptions with realistic costs
    // ─────────────────────────────────────────────────────

    public DecisionOption[] suggestForEvent(Event event, City city) {
        String prompt = String.format(
            "You are an AI advisor in a city simulation game. " +
            "A DANGEROUS event occurred in the %s ministry: '%s'. " +
            "City state: Budget=%.0f euros, Satisfaction=%.0f%%. " +
            "Suggest exactly 3 options. Adjust costs based on budget. " +
            "Format EXACTLY as (3 lines, no extra text):\n" +
            "TITLE: [title] | DESC: [description] | COST: [number]\n" +
            "TITLE: [title] | DESC: [description] | COST: [number]\n" +
            "TITLE: [title] | DESC: [description] | COST: [number]\n" +
            "Line 1 = cheapest (0). Line 3 = most expensive.",
            event.getMinistry(), event.getDescription(),
            city.getBudget(), city.getSatisfaction()
        );

        String raw = callGemini(prompt);
        return parseOptions(raw, DecisionType.DANGEROUS_EVENT, 3);
    }

    // ─────────────────────────────────────────────────────
    // CASE 2 — Monthly Report
    // Sends all ministry reports + city state to Gemini
    // Returns ADD / CUT / KEEP suggestion per ministry
    // ─────────────────────────────────────────────────────

    public DecisionOption[] suggestForMonthlyReport(List<Report> reports, City city) {
        // Build ministry summary to send to Gemini
        StringBuilder summary = new StringBuilder();
        for (Report r : reports) {
            summary.append(String.format(
                "%s: rating=%.0f%% resolved=%d ignored=%d. ",
                r.getMinistry(), r.getRating(),
                r.getResolved(), r.getUnresolved()
            ));
        }

        String prompt = String.format(
            "You are an AI advisor in a city simulation game. " +
            "Monthly ministry performance: %s " +
            "City budget: %.0f euros. Satisfaction: %.0f%%. " +
            "Suggest exactly %d budget actions (one per ministry). " +
            "Format EXACTLY as (one line per ministry, no extra text):\n" +
            "TITLE: [ADD/CUT/KEEP] | DESC: [ministry - short reason] | COST: [number]\n" +
            "COST = amount to add or cut. 0 if KEEP.",
            summary.toString(), city.getBudget(),
            city.getSatisfaction(), reports.size()
        );

        String raw = callGemini(prompt);
        return parseOptions(raw, DecisionType.MONTHLY_BUDGET, reports.size());
    }

    // ─────────────────────────────────────────────────────
    // CASE 3 — Annual Review
    // Sends minister performance to Gemini
    // Returns KEEP or FIRE with reason
    // ─────────────────────────────────────────────────────

    public DecisionOption suggestForAnnualReview(Minister minister, double avgRating) {
        String prompt = String.format(
            "You are an AI advisor in a city simulation game. " +
            "Minister '%s' manages the %s ministry. " +
            "Average performance this year: %.0f%%. Warnings: %d. " +
            "Should the president KEEP or FIRE this minister? " +
            "Format EXACTLY as (one line, no extra text):\n" +
            "TITLE: [KEEP/FIRE] | DESC: [one sentence reason] | COST: 0",
            minister.getName(), minister.getMinistry(),
            avgRating, minister.getWarnings()
        );

        String raw = callGemini(prompt);
        DecisionOption[] result = parseOptions(raw, DecisionType.ANNUAL_REVIEW, 1);
        return result[0];
    }

    // ─────────────────────────────────────────────────────
    // GEMINI API — sends prompt and returns raw text
    // ─────────────────────────────────────────────────────

    private String callGemini(String prompt) {
        try {
            // Build JSON request body
            String body = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "%s"
                    }]
                  }]
                }
                """.formatted(prompt.replace("\"", "'").replace("\n", "\\n"));

            // Send POST request to Gemini
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

            return parseRawText(response.body());

        } catch (Exception e) {
            System.out.println("[AIAdvisor] Gemini error: " + e.getMessage());
            return "";
        }
    }

    // Extract raw text from Gemini JSON response
    private String parseRawText(String json) {
        try {
            int start = json.indexOf("\"text\": \"") + 9;
            int end   = json.lastIndexOf("\"");
            return json.substring(start, end)
                .replace("\\n", "\n").trim();
        } catch (Exception e) {
            return "";
        }
    }

    // Parse Gemini response into DecisionOption array
    // Expected format per line: "TITLE: x | DESC: y | COST: 1234"
    private DecisionOption[] parseOptions(String raw, DecisionType type, int count) {
        DecisionOption[] options = new DecisionOption[count];
        String[] lines = raw.split("\n");
        int idx = 0;

        for (String line : lines) {
            if (line.isBlank() || idx >= count) continue;
            try {
                String title = extract(line, "TITLE:", "|").trim();
                String desc  = extract(line, "DESC:",  "|").trim();
                int cost     = Integer.parseInt(
                    extract(line, "COST:", "\n")
                        .replaceAll("[^0-9]", "").trim()
                );
                options[idx++] = new DecisionOption(type, title, desc, cost);
            } catch (Exception e) {
                options[idx++] = new DecisionOption(
                    type, "OPTION " + (idx + 1), "No suggestion", 0
                );
            }
        }

        // Fallback if Gemini returned less than expected
        while (idx < count) {
            options[idx] = new DecisionOption(
                type, "OPTION " + (idx + 1), "No suggestion", 0
            );
            idx++;
        }

        return options;
    }

    // Extracts text between two markers in a line
    private String extract(String line, String from, String to) {
        int start = line.indexOf(from) + from.length();
        int end   = line.indexOf(to, start);
        if (end == -1) end = line.length();
        return line.substring(start, end);
    }
}