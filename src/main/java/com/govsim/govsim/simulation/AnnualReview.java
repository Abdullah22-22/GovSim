package com.govsim.govsim.simulation;

import com.govsim.govsim.model.*;
import com.govsim.govsim.president.*;
import java.util.List;
import java.util.Scanner;

/** Annual minister review — evaluate and keep/fire */
public class AnnualReview {

    private final President president;
    private final Scanner   scanner;

    public AnnualReview(President president, Scanner scanner) {
        this.president = president;
        this.scanner   = scanner;
    }

    /** Run annual review for all ministers */
    public void run(List<Minister> ministers, int year) {
        System.out.println("\n========================================");
        System.out.println("ANNUAL MINISTER REVIEW — Year " + year);
        System.out.println("========================================");

        for (Minister minister : ministers) {
            System.out.println("\n========================================");
            System.out.println("Minister: " + minister.getName() +
                    " | Ministry: " + minister.getMinistry());
            System.out.println("----------------------------------------");

            double totalRating = 0;
            for (Report r : minister.getMonthlyReports()) {
                System.out.printf("Month %-2d | Events: %-3d | Resolved: %-3d | Rating: %.0f%%%n",
                        r.getMonth(), r.getTotalEvents(),
                        r.getResolved(), r.getRating());
                totalRating += r.getRating();
            }

            double avgRating = minister.getMonthlyReports().isEmpty()
                    ? 0 : totalRating / minister.getMonthlyReports().size();

            System.out.println("----------------------------------------");
            System.out.printf("Average Rating: %.0f%%%n", avgRating);
            System.out.println("========================================");

            // Get AI suggestion
            DecisionOption option = president.getAnnualOption(minister, avgRating);

            System.out.println("AI Advisor suggests:");
            System.out.println("  1. KEEP - " + (option.title.equalsIgnoreCase("KEEP")
                    ? option.description : "Solid performance overall"));
            System.out.println("  2. FIRE - " + (option.title.equalsIgnoreCase("FIRE")
                    ? option.description : "Poor performance detected"));
            System.out.println("========================================");

            // Player chooses
            int choice = -1;
            while (choice != 0 && choice != 1) {
                System.out.print("Choose option (1 or 2): ");
                try {
                    choice = scanner.nextInt() - 1;
                    if (choice != 0 && choice != 1)
                        System.out.println("Invalid! Enter 1 or 2.");
                } catch (Exception e) {
                    System.out.println("Invalid! Enter 1 or 2.");
                    scanner.nextLine();
                }
            }

            if (choice == 0) {
                minister.setStatus("ACTIVE");
                System.out.println("  -> KEPT");
            } else {
                minister.setStatus("FIRED");
                System.out.println("  -> FIRED");
            }
        }
    }
}