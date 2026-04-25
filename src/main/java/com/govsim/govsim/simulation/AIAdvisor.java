package com.govsim.govsim.simulation;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Event;
import com.govsim.govsim.model.Minister;
import com.govsim.govsim.model.Report;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * AI Advisor — connects to Gemini API and gives suggestions in 3 cases:
 * 1. Dangerous event — suggests 3 options with cost based on city state
 * 2. Monthly report — suggests ADD / CUT / KEEP budget per ministry
 * 3. Annual review — suggests KEEP or FIRE minister
 */
public class AIAdvisor {

    // Load API key and model from .env file
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GEMINI_API_KEY");
    private static final String MODEL = dotenv.get("GEMINI_MODEL");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" +
            MODEL + ":generateContent?key=" + API_KEY;

    private final HttpClient client = HttpClient.newHttpClient();

    // ─────────────────────────────────────────────────────
    // CASE 1 — Dangerous Event
    // Sends event + city state to Gemini
    // Returns 3 options with realistic cost based on budget
    // ─────────────────────────────────────────────────────

    public String[] suggestForEvent(Event event, City city) {
        String prompt = String.format(
                "You are an AI advisor in a city simulation game. " +
                        "A DANGEROUS event occurred in the %s ministry: '%s'. " +
                        "Current city state: Budget=%.0f euros, Satisfaction=%.0f%%. " +
                        "Based on the budget, suggest exactly 3 realistic options for the president. " +
                        "If budget is low, keep costs small. If budget is high, allow bigger responses. " +
                        "Format EXACTLY as (no extra text):\n" +
                        "Option 1: [title] - [description] - Cost: [number]\n" +
                        "Option 2: [title] - [description] - Cost: [number]\n" +
                        "Option 3: [title] - [description] - Cost: [number]\n" +
                        "Option 1 must be cheapest (0 or very low). Option 3 must be most expensive.",
                event.getMinistry(),
                event.getDescription(),
                city.getBudget(),
                city.getSatisfaction());

        return callGemini(prompt, 3);
    }

    // ─────────────────────────────────────────────────────
    // CASE 2 — Monthly Report
    // Sends all ministry reports + city state to Gemini
    // Returns ADD / CUT / KEEP suggestion per ministry
    // ─────────────────────────────────────────────────────

    public String[] suggestForMonthlyReport(List<Report> reports, City city) {
        // Build report summary to send to Gemini
        StringBuilder summary = new StringBuilder();
        for (Report r : reports) {
            summary.append(String.format(
                    "%s ministry: rating=%.0f%%, resolved=%d, ignored=%d. ",
                    r.getMinistry(),
                    r.getRating(),
                    r.getResolved(),
                    r.getUnresolved()));
        }
        String prompt = String.format(
                "You are an AI advisor in a city simulation game. " +
                        "Monthly ministry performance: %s " +
                        "City state: Budget=%.0f euros, Satisfaction=%.0f%%. " +
                        "For each ministry, suggest one of: ADD budget, CUT budget, or KEEP as is. " +
                        "Base your suggestion on performance rating and current city budget. " +
                        "Format EXACTLY as (one line per ministry, no extra text):\n" +
                        "[MinistryName]: [ADD/CUT/KEEP] - [amount in euros if ADD or CUT] - [short reason]",
                summary.toString(),
                city.getBudget(),
                city.getSatisfaction());
        return callGemini(prompt, reports.size());
    }

    // ─────────────────────────────────────────────────────
    // CASE 3 — Annual Review
    // Sends minister performance to Gemini
    // Returns KEEP or FIRE decision with reason
    // ─────────────────────────────────────────────────────

    public String tForAnnualReview(Minister minister, double avgRating) {
        String prompt = String.format(
                "You are an AI advisor in a city simulation game. " +
                        "Minister '%s' manages the %s ministry. " +
                        "Their average performance rating this year: %.0f%%. " +
                        "Warnings received: %d. " +
                        "Should the president KEEP or FIRE this minister? " +
                        "Format EXACTLY as (no extra text):\n" +
                        "Decision: [KEEP/FIRE] - [one sentence reason]",
                minister.getName(), minister.getMinistry(),
                avgRating, minister.getWarnings());

        String[] result = callGemini(prompt, 1);
        return result[0];
    }

    // ─────────────────────────────────────────────────────
    // GEMINI API — sends prompt and returns response lines
    // ─────────────────────────────────────────────────────
    private String[] callGemini(String prompt, int expectedLines) {
        try {
            // Build request body
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

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return parseResponse(response.body(), expectedLines);

        } catch (Exception e) {
            System.out.println("[AIAdvisor] Gemini error: " + e.getMessage());
            return fallback(expectedLines);
        }
    }

    // Parse Gemini JSON response and extract text lines
    private String[] parseResponse(String json, int expectedLines) {
        try {
            int start = json.indexOf("\"text\": \"") + 9;
            int end = json.lastIndexOf("\"");
            String text = json.substring(start, end)
                    .replace("\\n", "\n").trim();

            if (expectedLines == 1)
                return new String[] { text.split("\n")[0].trim() };

            // Split into lines and return expected count
            String[] lines = text.split("\n");
            String[] options = new String[expectedLines];
            int count = 0;
            for (String line : lines) {
                if (!line.isBlank() && count < expectedLines) {
                    options[count++] = line.trim();
                }
            }

            if (count < expectedLines)
                return fallback(expectedLines);
            return options;

        } catch (Exception e) {
            return fallback(expectedLines);
        }
    }

    // Extract cost number from Gemini response line
    // Example: "Option 2: LOCKDOWN - Lock area - Cost: 4000" -> returns 4000
    public double extractCost(String optionLine) {
        try {
            String lower = optionLine.toLowerCase();
            int idx = lower.lastIndexOf("cost:") + 5;
            String numStr = lower.substring(idx).replaceAll("[^0-9]", "").trim();
            return Double.parseDouble(numStr);
        } catch (Exception e) {
            return 5000; // default cost if parsing fails
        }
    }

    // Fallback if Gemini fails or is unreachable
    private String[] fallback(int count) {
        if (count == 1)
            return new String[] { "Decision: KEEP - No data available." };
        String[] f = new String[count];
        for (int i = 0; i < count; i++)
            f[i] = "Option " + (i + 1) + ": No suggestion available. - Cost: 0";
        return f;
    }
}