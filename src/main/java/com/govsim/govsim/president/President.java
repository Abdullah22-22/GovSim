package com.govsim.govsim.president;

import com.govsim.govsim.model.City;
import com.govsim.govsim.model.Event;

public class President {

    private City city;

    public President(City city) {
        this.city = city;
    }

    public void decide(Event event) {

        System.out.println("\n[President] ⚠ DANGEROUS EVENT!");
        System.out.println(event.getDescription());

        int choice = (int)(Math.random() * 3) + 1;

        System.out.println("Decision chosen: " + choice);

        switch (choice) {

            case 1:
                System.out.println("Action: Spend money (costly fix)");
                city.setBudget(city.getBudget() - 1000);
                event.setResolved(true);
                break;

            case 2:
                System.out.println("Action: Ignored (bad for society)");
                city.setSatisfaction(city.getSatisfaction() - 10);
                event.setResolved(false);
                break;

            case 3:
                System.out.println("Action: Partial solution");
                city.setBudget(city.getBudget() - 500);
                city.setSatisfaction(city.getSatisfaction() - 3);
                event.setResolved(true);
                break;
        }

        System.out.println();
    }
}