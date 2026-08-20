package com.rfo.ui;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.List;
import java.util.Scanner;

public class AdminMenu extends Menu {

    private StaffService service;

    public AdminMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.service = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Admin Menu";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Change Location", "Change Password");
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Change Location":
                changeLocation();
                break;
            case "Change Password":
                changePassword();
                break;
            default:
                System.out.println(select + " Not implemented yet.");
        }
    }

    private void changeLocation() {
        List<Location> access = staff.getAccessibleLocations();

        if (access.isEmpty()) {
            System.out.println("You have no accessible locations.");
            return;
        }

        System.out.println("Accessible locations: ");
        for (int i = 0; i < access.size(); i++) {
            System.out.println((i + 1) + ". (" + access.get(i).getLocationID() + ") " + access.get(i).getName());
        }

        System.out.println("Choose location");
        String input = scanner.nextLine();

        int choice;

        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid input");
            return;
        }

        if (choice < 1 || choice > access.size()) {
            System.out.println("Please choose a valid input");
            return;
        }

        Location selected = access.get(choice - 1);
        service.switchLocation(staff, selected);
        System.out.println("Location changed to (" + selected.getLocationID() + ") " + selected.getName());
    }

    private void changePassword() {
        System.out.print("Enter current password: ");
        String current = scanner.nextLine();

        System.out.print("Enter new password: ");
        String newPass = scanner.nextLine();

        System.out.print("Confirm new password: ");
        String confirm = scanner.nextLine();

        if (!newPass.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }

        boolean success = service.changePassword(staff, current, newPass);
        if (success) {
            System.out.println("Password changed successfully.");
        }
    }
}
