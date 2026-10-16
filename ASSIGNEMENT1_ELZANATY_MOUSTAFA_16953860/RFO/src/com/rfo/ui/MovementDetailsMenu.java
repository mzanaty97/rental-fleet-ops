package com.rfo.ui;

import com.rfo.model.DrivenMovement;
import com.rfo.model.Driver;
import com.rfo.model.Movement;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

// Generated with assistance from Claude (modified existing class): I wrote this menu in Part 1; Claude helped me change it to call the service classes and show their error messages.
/**
 * The console menu for looking up movement details, drivers and a vehicle's movements.
 */
public class MovementDetailsMenu extends Menu {

    private StaffService staffService;
    private MovementService movementService;

    public MovementDetailsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.staffService = new StaffService(dataStore);
        this.movementService = new MovementService(dataStore);
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
            case "Show Vehicle Movements":
                showVehicleMovements();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void queryMovementDetails() {
        System.out.print("Movement ID: ");
        String movementID = scanner.nextLine();

        Movement movement;
        try {
            movement = movementService.findMovement(movementID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
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

    // Generated with assistance from Claude: console version of Show Vehicle Movements.
    private void showVehicleMovements() {
        System.out.print("Rego: ");
        String rego = scanner.nextLine();

        List<MovementService.HistoryRow> rows;
        try {
            rows = movementService.vehicleHistory(rego);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        DateTimeFormatter when = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String line = "%-5s %-10s %-22s %-17s %9s  %-22s %-17s %9s%n";
        System.out.println("\n--- Vehicle Movements (newest first) ---");
        System.out.printf(line, "Type", "Movement", "Check-out location", "Check-out time", "Odo out",
                "Check-in location", "Check-in time", "Odo in");
        boolean anyExpected = false;
        for (MovementService.HistoryRow r : rows) {
            String mark = r.expected ? "*" : "";
            anyExpected |= r.expected;
            System.out.printf(line, r.type, r.movementID, r.outLocation == null ? "" : r.outLocation,
                    r.outTime == null ? "" : r.outTime.format(when), r.odoOut == null ? "" : r.odoOut,
                    (r.inLocation == null ? "" : r.inLocation) + mark,
                    r.inTime == null ? "" : r.inTime.format(when) + mark, r.odoIn == null ? "" : r.odoIn);
        }
        if (anyExpected) {
            System.out.println("* expected check-in, the vehicle has not been checked in yet");
        }
    }

    private void queryDrivers() {
        System.out.print("Driver ID: ");
        String input = scanner.nextLine();

        Driver driver;
        try {
            driver = movementService.findDriver(input);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.println("\n--- Driver Details ---");
        System.out.println("Driver ID: " + driver.getDriverID());
        System.out.println("Name: " + driver.getName());
        System.out.println("License Number: " + driver.getLicenseNumber());
        System.out.println("Contact Info: " + driver.getContactInfo());
    }
}
