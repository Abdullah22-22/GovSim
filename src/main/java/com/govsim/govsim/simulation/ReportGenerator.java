package com.govsim.govsim.simulation;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Report;
import com.govsim.govsim.model.Event;
import java.util.List;

public class ReportGenerator {

    public void printMonthlyReport(City city, List<Report> reports) {
        int totalEvents = 0;
        int totalResolved = 0;
        double combinedRating = 0;

        System.out.println("========================================");
        System.out.println("MONTHLY GOVERNMENT REPORT");
        System.out.println("Month: " + city.getMonth() + " / Year: " + city.getYear());
        System.out.println("========================================");

        System.out.println("Ministry\tEvents\tResolved\tRating");
        System.out.println("----------------------------------------");

        for (Report r : reports) {
            String name = r.getMinistry();
            int count = r.getTotalEvents();
            int solved = r.getResolved();
            double score = r.getRating();


            System.out.println(name + "\t\t" + count + "\t" + solved + "\t\t" + (int)score + "%");


            totalEvents += count;
            totalResolved += solved;
            combinedRating += score;
        }

        double overallRating = reports.isEmpty() ? 0 : combinedRating / reports.size();

        System.out.println("----------------------------------------");
        System.out.println("Total Events:    " + totalEvents);
        System.out.println("Total Resolved:  " + totalResolved);
        System.out.println("Total Ignored:   " + (totalEvents - totalResolved));



        System.out.println("Overall Rating:  " + (int)overallRating + "%");
        System.out.println("========================================");

        System.out.println("Budget:       EUR " + (int)city.getBudget());
        System.out.println("Satisfaction: " + (int)city.getSatisfaction() + "%");
                System.out.println("Population:" + city.getPopulation());

        System.out.println("========================================");
    }
}