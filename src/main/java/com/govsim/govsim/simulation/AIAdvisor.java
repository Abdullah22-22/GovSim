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
 * AI Advisor — connects to Groq API and returns DecisionOption arrays:
 * 1. Dangerous event  — 3 DecisionOptions with cost
 * 2. Monthly report   — DecisionOptions per ministry (ADD/CUT/KEEP)
 * 3. Annual review    — 1 DecisionOption (KEEP/FIRE)
 */
public class AIAdvisor {

    // Load API keys and model from .env
    private static final Dotenv   dotenv  = Dotenv.load();
    private static final String[] API_KEYS = {
            dotenv.get("GROQ_API_KEY_1"),
            dotenv.get("GROQ_API_KEY_2")
    };
    private static final String   MODEL   = dotenv.get("GROQ_MODEL");
    private static final String   API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private int currentKeyIndex = 0;
    private final HttpClient client = HttpClient.newHttpClient();

    // ─────────────────────────────────────────────────────
    // CASE 1 — Dangerous Event
    // Sends event + city state to Groq
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
                event.getMinistry(),
                event.getDescription()
                        .replace("—", "-")
                        .replace("€", "EUR")
                        .replace("\\", "/"),
                city.getBudget(), city.getSatisfaction()
        );

        String raw = callGroq(prompt);
        return parseOptions(raw, DecisionType.DANGEROUS_EVENT, 3);
    }

    // ─────────────────────────────────────────────────────
    // CASE 2 — Monthly Report
    // Sends all ministry reports + city state to Groq
    // Returns ADD / CUT / KEEP suggestion per ministry
    // ─────────────────────────────────────────────────────

    public DecisionOption[] suggestForMonthlyReport(List<Report> reports, City city) {
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

        String raw = callGroq(prompt);
        return parseOptions(raw, DecisionType.MONTHLY_BUDGET, reports.size());
    }

    // ─────────────────────────────────────────────────────
    // CASE 3 — Ministry Monthly Review
    // Sends one ministry report + city state to Groq
    // Returns exactly ADD, KEEP, CUT options
    // ─────────────────────────────────────────────────────

    public DecisionOption[] suggestForMinistryReview(Report report, City city) {
        String prompt = String.format(
                "You are an AI advisor in a city simulation game. " +
                        "Ministry: %s. Rating: %.0f%%. Resolved: %d. Ignored: %d. " +
                        "City budget: %.0f euros. Satisfaction: %.0f%%. " +
                        "Respond with EXACTLY 3 lines, no extra text, no numbering:\n" +
                        "TITLE: ADD | DESC: [reason to invest more] | COST: [number above 0]\n" +
                        "TITLE: KEEP | DESC: [reason to keep same] | COST: 0\n" +
                        "TITLE: CUT | DESC: [reason to cut] | COST: [number above 0]",
                report.getMinistry(), report.getRating(),
                report.getResolved(), report.getUnresolved(),
                city.getBudget(), city.getSatisfaction()
        );

        String raw = callGroq(prompt);
        return parseOptions(raw, DecisionType.MONTHLY_BUDGET, 3);
    }

    // ─────────────────────────────────────────────────────
    // CASE 4 — Annual Review
    // Sends minister performance to Groq
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

        String raw = callGroq(prompt);
        return parseOptions(raw, DecisionType.ANNUAL_REVIEW, 1)[0];
    }

    // ─────────────────────────────────────────────────────
    // Groq API — sends prompt and returns raw text
    // Rotates between API keys on rate limit
    // ─────────────────────────────────────────────────────

    private String callGroq(String prompt) {
        String cleanPrompt = prompt
                .replace("\"", "'")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\\", "/")
                .replace("—", "-")
                .replace("€", "EUR");

        int attempts = 0;

        while (attempts < API_KEYS.length) {
            try { Thread.sleep(1500); }
            catch (InterruptedException ignored) {}
            String apiKey = API_KEYS[currentKeyIndex];
            try {
                String body = String.format("""
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "temperature": 0.7,
                  "max_tokens": 300
                }
                """, MODEL, cleanPrompt);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseRawText(response.body());
                }

                // Rate limit or error — switch key
                System.out.println("[AIAdvisor] Key #" + (currentKeyIndex + 1) +
                        " failed (" + response.statusCode() + ") — switching...");
                currentKeyIndex = (currentKeyIndex + 1) % API_KEYS.length;

            } catch (Exception e) {
                System.out.println("[AIAdvisor] Key #" + (currentKeyIndex + 1) +
                        " error: " + e.getMessage());
                currentKeyIndex = (currentKeyIndex + 1) % API_KEYS.length;
            }

            attempts++;
        }

        System.out.println("[AIAdvisor] All API keys failed.");
        return "";
    }

    // Extract raw text from Groq JSON response
    private String parseRawText(String json) {
        try {
            int start = json.indexOf("\"content\":\"") + 11;
            if (start == 10) {
                start = json.indexOf("\"content\": \"") + 12;
            }
            int end = json.indexOf("\"}]", start);

            return json.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\"", "")
                    .trim();
        } catch (Exception e) {
            return "";
        }
    }

    // Parse Groq response into DecisionOption array
    // Expected format per line: "TITLE: x | DESC: y | COST: 1234"
    private DecisionOption[] parseOptions(String raw, DecisionType type, int count) {
        DecisionOption[] options = new DecisionOption[count];
        String[] lines = raw.split("\n");
        int idx = 0;

        for (String line : lines) {
            if (line.isBlank() || idx >= count) continue;
            try {
                String title, desc;
                int cost;

                if (line.contains("TITLE:")) {
                    title = extract(line, "TITLE:", "|").trim();
                    desc  = extract(line, "DESC:",  "|").trim();
                } else if (line.contains("|")) {
                    title = line.substring(0, line.indexOf("|")).trim();
                    desc  = extract(line, "DESC:", "|").trim();
                } else {
                    title = "OPTION " + (idx + 1);
                    desc  = line.trim();
                }

                cost = Integer.parseInt(
                        extract(line, "COST:", "\n")
                                .replaceAll("[^0-9]", "").trim()
                );

                options[idx++] = new DecisionOption(type, title, desc, cost);

            } catch (Exception e) {
                if (type == DecisionType.MONTHLY_BUDGET) {
                    options[idx] = new DecisionOption(
                            type,
                            idx == 0 ? "ADD"  : idx == 1 ? "KEEP" : "CUT",
                            idx == 0 ? "Invest more in ministry" : idx == 1 ? "No change needed" : "Reduce spending",
                            0
                    );
                } else {
                    options[idx] = new DecisionOption(
                            type,
                            idx == 0 ? "IGNORE"     : idx == 1 ? "INVESTIGATE" : "EMERGENCY",
                            idx == 0 ? "Do nothing" : idx == 1 ? "Send a team" : "Full response",
                            0
                    );
                }
                idx++;
            }
        }

        // Fallback if Groq returned less than expected
        while (idx < count) {
            if (type == DecisionType.MONTHLY_BUDGET) {
                options[idx] = new DecisionOption(
                        type,
                        idx == 0 ? "ADD"  : idx == 1 ? "KEEP" : "CUT",
                        idx == 0 ? "Invest more in ministry" : idx == 1 ? "No change needed" : "Reduce spending",
                        0
                );
            } else {
                options[idx] = new DecisionOption(
                        type,
                        idx == 0 ? "IGNORE"     : idx == 1 ? "INVESTIGATE" : "EMERGENCY",
                        idx == 0 ? "Do nothing" : idx == 1 ? "Send a team" : "Full response",
                        0
                );
            }
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