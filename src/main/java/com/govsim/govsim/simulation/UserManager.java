package com.govsim.govsim.simulation;

import com.govsim.govsim.database.UserDAO;
import com.govsim.govsim.model.User;
import java.util.Scanner;

/** Handles user authentication (login/register) */
public class UserManager {

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    /** Authenticate user via console (login or register) */
    public User authenticate(Scanner scanner) {

        while (currentUser == null) {

            // Simple menu
            System.out.println("\nGOVSIM — Government Sim");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.print("Choose (1 or 2): ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                // Clean input (same line style)
                System.out.print("Username: ");
                String username = scanner.nextLine();

                System.out.print("Password: ");
                String password = scanner.nextLine();

                if (choice == 2) {
                    User newUser = new User(username, password);

                    if (userDAO.register(newUser)) {
                        System.out.println("Registered successfully. Please login.");
                    } else {
                        System.out.println("Registration failed. Try again.");
                    }

                } else if (choice == 1) {

                    currentUser = userDAO.login(username, password);

                    if (currentUser == null) {
                        System.out.println("Wrong username or password.");
                    } else {
                        System.out.println("Welcome, " + currentUser.getUsername());
                    }

                } else {
                    System.out.println("Invalid choice.");
                }

            } catch (Exception e) {
                System.out.println("Invalid input.");
                scanner.nextLine();
            }
        }

        return currentUser;
    }

    /** Get currently authenticated user */
    public User getCurrentUser() {
        return currentUser;
    }
}