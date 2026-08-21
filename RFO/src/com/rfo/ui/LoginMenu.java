package com.rfo.ui;

import com.rfo.model.Staff;
import com.rfo.service.AuthService;
import java.util.Scanner;

public class LoginMenu {

    private static final int MAX_ATTEMPTS = 5;

    private AuthService auth;
    private Scanner scanner;

    public LoginMenu(AuthService auth, Scanner scanner) {
        this.auth = auth;
        this.scanner = scanner;
    }

    public Staff run() {
        int attempts = 0;

        String staffId;
        String pwd;
        while (attempts < MAX_ATTEMPTS) {
            System.out.println("Staff ID: ");
            staffId = scanner.nextLine().trim().toUpperCase();

            System.out.println("Password: ");
            pwd = scanner.nextLine();

            Staff staff = auth.login(staffId, pwd);

            if (staff != null) {
                System.out.println("Welcome " + staff.getFName() + "!");
                return staff;
            }

            attempts++;
            System.out.println("Attempts remaining: " + (MAX_ATTEMPTS - attempts));
        }

        System.out.println("Too many failed attempts! Exiting program!");
        System.exit(0);
        return null;
    }
}
