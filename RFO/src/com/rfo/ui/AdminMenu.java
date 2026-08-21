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

        String currentName = staff.getCurrentLocation() != null
                ? staff.getCurrentLocation().getName() + " (" + staff.getCurrentLocation().getLocationID() + ")"
                : "None set";
        System.out.println("Current location: " + currentName);

        System.out.println("Accessible locations: ");
        for (Location loc : access) {
            System.out.println("- (" + loc.getLocationID() + ") " + loc.getName());
        }

        System.out.print("Enter location ID: ");
        String input = scanner.nextLine().trim().toUpperCase();

        Location selected = access.stream()
                .filter(l -> l.getLocationID().equals(input))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            System.out.println("Invalid location ID, or you do not have access to it.");
            return;
        }

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
