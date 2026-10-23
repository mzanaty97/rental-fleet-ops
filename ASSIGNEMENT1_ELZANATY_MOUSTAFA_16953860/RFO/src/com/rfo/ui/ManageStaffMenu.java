package com.rfo.ui;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.List;
import java.util.Scanner;

// Generated with assistance from Claude (modified existing class): I wrote this menu in Part 1; Claude helped me change it to call the service classes and show their error messages.
/**
 * The console menu for viewing and adding staff and granting or revoking their location
 * access.
 */
public class ManageStaffMenu extends Menu {

    private StaffService service;

    public ManageStaffMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.service = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Manage Staff";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("View Staff", "Add Staff", "Grant Location Access", "Revoke Location Access");
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "View Staff":
                viewStaff();
                break;
            case "Add Staff":
                addStaff();
                break;
            case "Grant Location Access":
                grantLocationAccess();
                break;
            case "Revoke Location Access":
                revokeLocationAccess();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void viewStaff() {
        List<Staff> allStaff = dataStore.getStaff();

        if (allStaff.isEmpty()) {
            System.out.println("No staff on record.");
            return;
        }

        System.out.println("\n--- Staff ---");
        System.out.println(String.format("%-10s | %-20s | %-16s | %s",
                "Staff ID", "Name", "Role", "Current Location"));
        for (Staff s : allStaff) {
            String loc = s.getCurrentLocation() != null ? s.getCurrentLocation().getName() : "N/A";
            System.out.println(String.format("%-10s | %-20s | %-16s | %s",
                    s.getStaffID(), s.getName(), s.getRole(), loc));
        }
    }

    private void addStaff() {
        System.out.print("Staff ID: ");
        String staffID = scanner.nextLine().trim().toUpperCase();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("First Name: ");
        String fName = scanner.nextLine();

        System.out.print("Last Name: ");
        String lName = scanner.nextLine();

        StaffRole role = MovementMenuUtils.promptForEnum(scanner, "Role", StaffRole.class);
        if (role == null) {
            return;
        }

        try {
            Staff newStaff = service.addStaff(staffID, password, fName, lName, role);
            System.out.println("Staff added successfully: " + newStaff.getStaffID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to add staff. See message above.");
        }
    }

    private void grantLocationAccess() {
        Staff target = findStaffByID();
        if (target == null) {
            return;
        }

        System.out.print("Location ID to grant: ");
        String locationID = scanner.nextLine().trim().toUpperCase();

        Location location = dataStore.getBy(dataStore.getLocations(), l -> l.getLocationID().equals(locationID));

        if (location == null) {
            System.out.println("Location not found.");
            return;
        }

        try {
            service.grantLocationAccess(target, location);
            System.out.println("Access granted.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void revokeLocationAccess() {
        Staff target = findStaffByID();
        if (target == null) {
            return;
        }

        try {
            service.revokeAllLocationAccess(target);
            System.out.println("All location access revoked for: " + target.getStaffID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private Staff findStaffByID() {
        System.out.print("Staff ID: ");
        String staffID = scanner.nextLine().trim().toUpperCase();

        Staff target = dataStore.getBy(dataStore.getStaff(), s -> s.getStaffID().equals(staffID));

        if (target == null) {
            System.out.println("Staff not found.");
        }

        return target;
    }
}
