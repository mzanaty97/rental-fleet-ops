package com.rfo.ui;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.List;
import java.util.Scanner;

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
        System.out.println("Staff ID\t|\tName\t|\tRole\t|\tCurrent Location");
        for (Staff s : allStaff) {
            String loc = s.getCurrentLocation() != null ? s.getCurrentLocation().getName() : "N/A";
            System.out.println(s.getStaffID() + "\t|\t" + s.getName() + "\t|\t" + s.getRole() + "\t|\t" + loc);
        }
    }

    private void addStaff() {
        System.out.print("Staff ID: ");
        String staffID = scanner.nextLine();

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

        Staff newStaff = service.addStaff(staffID, password, fName, lName, role);

        if (newStaff == null) {
            System.out.println("Failed to add staff. See message above.");
            return;
        }

        System.out.println("Staff added successfully: " + newStaff.getStaffID());
    }

    private void grantLocationAccess() {
        Staff target = findStaffByID();
        if (target == null) {
            return;
        }

        System.out.print("Location ID to grant: ");
        String locationID = scanner.nextLine();

        Location location = dataStore.getBy(dataStore.getLocations(), l -> l.getLocationID().equals(locationID));

        if (location == null) {
            System.out.println("Location not found.");
            return;
        }

        service.grantLocationAccess(target, location);
    }

    private void revokeLocationAccess() {
        Staff target = findStaffByID();
        if (target == null) {
            return;
        }

        boolean revoked = service.revokeAllLocationAccess(target);
        if (revoked) {
            System.out.println("All location access revoked for: " + target.getStaffID());
        }
    }

    private Staff findStaffByID() {
        System.out.print("Staff ID: ");
        String staffID = scanner.nextLine();

        Staff target = dataStore.getBy(dataStore.getStaff(), s -> s.getStaffID().equals(staffID));

        if (target == null) {
            System.out.println("Staff not found.");
        }

        return target;
    }
}
