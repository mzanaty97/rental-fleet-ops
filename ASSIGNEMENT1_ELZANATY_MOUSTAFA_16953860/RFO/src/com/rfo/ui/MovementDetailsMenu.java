package com.rfo.ui;

import com.rfo.model.DrivenMovement;
import com.rfo.model.Driver;
import com.rfo.model.Movement;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.List;
import java.util.Scanner;

public class MovementDetailsMenu extends Menu {

    private StaffService staffService;

    public MovementDetailsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.staffService = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Movement Details";
    }

    @Override
    protected List<String> getOptions() {
        return staffService.getMovementDetailsOptions(staff.getRole());
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Query Movement Details":
                queryMovementDetails();
                break;
            case "Query Drivers":
                queryDrivers();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void queryMovementDetails() {
        System.out.print("Movement ID: ");
        String movementID = scanner.nextLine();

        Movement movement = dataStore.getBy(dataStore.getMovements(),
                m -> m.getMovementID().equals(movementID));

        if (movement == null) {
            System.out.println("Movement not found.");
            return;
        }

        System.out.println("\n--- Movement Details ---");
        System.out.println("Movement ID: " + movement.getMovementID());
        System.out.println("Type: " + movement.getClass().getSimpleName());
        System.out.println("Vehicle: " + movement.getVehicle().getPlateNumber());
        System.out.println("Checkout Staff: " + movement.getCOStaff());
        System.out.println("Checkin Staff: " + (movement.getCIStaff() != null ? movement.getCIStaff() : "N/A"));
        System.out.println("Checkout Date/Time: " + movement.getCheckOutDT());
        System.out.println("Checkin Date/Time: " + (movement.getCheckInDT() != null ? movement.getCheckInDT() : "N/A"));
        System.out.println("Checkout Location: " + movement.getCheckOutLoc().getName());
        System.out.println("Checkin Location: " + (movement.getCheckInLoc() != null ? movement.getCheckInLoc().getName() : "N/A"));
        System.out.println("Odometer Out: " + movement.getOdoOut());
        System.out.println("Odometer In: " + movement.getOdoIn());
        System.out.println("Status: " + movement.getStatus());

        if (movement instanceof DrivenMovement dm) {
            System.out.println("Driver IDs: " + dm.getDriverIDs());
            System.out.println("Expected Checkin: " + dm.getExpectedCIDT());
            System.out.println("Expected Checkin Location: " + dm.getExpectedCILoc().getName());
        }

        if (movement instanceof RentalMovement rm) {
            System.out.println("Insurance Tier: " + rm.getInsuranceTier());
            System.out.println("RSA: " + rm.hasRSA());
            System.out.println("Equipment: " + rm.getEquipment());
            System.out.println("Price: $" + String.format("%.2f", rm.getPrice()));
        }
    }

    private void queryDrivers() {
        System.out.print("Driver ID: ");
        String input = scanner.nextLine();

        int driverID;
        try {
            driverID = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        Driver driver = dataStore.getBy(dataStore.getDrivers(), d -> d.getDriverID() == driverID);

        if (driver == null) {
            System.out.println("Driver not found.");
            return;
        }

        System.out.println("\n--- Driver Details ---");
        System.out.println("Driver ID: " + driver.getDriverID());
        System.out.println("Name: " + driver.getName());
        System.out.println("License Number: " + driver.getLicenseNumber());
        System.out.println("Contact Info: " + driver.getContactInfo());
    }
}
