package com.rfo.ui;

import com.rfo.model.Insurance;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.MovementService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class RevenueMovementsMenu extends Menu {

    private MovementService movementService;

    public RevenueMovementsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.movementService = new MovementService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Revenue Movements";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Checkout", "Check In", "Close Rental", "Amend Rental", "Changeover", "Print Invoice");
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Checkout":
                checkout();
                break;
            case "Check In":
                checkin();
                break;
            case "Close Rental":
                closeRental();
                break;
            case "Amend Rental":
                System.out.println("Not built yet. Coming soon!");
                break;
            case "Changeover":
                System.out.println("Not built yet. Coming soon!");
                break;
            case "Print Invoice":
                System.out.println("Not built yet. Coming soon!");
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void checkout() {
        System.out.print("Plate number: ");
        String plateNumber = scanner.nextLine();

        System.out.print("Checkout location ID: ");
        String checkOutLocID = scanner.nextLine();

        List<Integer> driverIDs = MovementMenuUtils.promptForDriverIDs(scanner);

        if (driverIDs.isEmpty()) {
            System.out.println("At least one driver is required.");
            return;
        }

        LocalDateTime expectedCIDT = MovementMenuUtils.promptForDateTime(scanner,
                "Expected check-in date/time (yyyy-MM-dd HH:mm): ");
        if (expectedCIDT == null) {
            return;
        }

        System.out.print("Expected check-in location ID: ");
        String expectedCILocID = scanner.nextLine();

        System.out.print("RSA? (yes/no): ");
        boolean rsa = scanner.nextLine().trim().equalsIgnoreCase("yes");

        Insurance insuranceTier = MovementMenuUtils.promptForEnum(scanner, "Insurance tier", Insurance.class);
        if (insuranceTier == null) {
            return;
        }

        RentalMovement rental = movementService.startRA(plateNumber, staff.getStaffID(), checkOutLocID,
                driverIDs, expectedCIDT, expectedCILocID, rsa, insuranceTier);

        if (rental == null) {
            System.out.println("Checkout failed. Please check the details above and try again.");
            return;
        }

        System.out.println("Rental started successfully. Movement ID: " + rental.getMovementID());
        double estimate = rental.getRentalEstimate();
        System.out.println("Estimated cost: $" + String.format("%.2f", estimate));
    }

    private void checkin() {
        System.out.print("Movement ID: ");
        String movementID = scanner.nextLine();

        Movement movement = dataStore.getBy(dataStore.getMovements(),
                m -> m.getMovementID().equals(movementID));

        if (movement == null) {
            System.out.println("Movement not found.");
            return;
        }

        if (!(movement instanceof RentalMovement)) {
            System.out.println("That movement is not a rental.");
            return;
        }

        if (movement.getStatus() != MovementStatus.OPEN) {
            System.out.println("This rental has already been checked in or closed.");
            return;
        }

        System.out.print("Check-in location ID: ");
        String checkInLocID = scanner.nextLine();

        movementService.checkIn(movement, staff.getStaffID(), checkInLocID);
        System.out.println("Checked in successfully. Movement status is now: " + movement.getStatus());
    }

    private void closeRental() {
        System.out.print("Movement ID: ");
        String movementID = scanner.nextLine();

        Movement movement = dataStore.getBy(dataStore.getMovements(),
                m -> m.getMovementID().equals(movementID));

        if (movement == null) {
            System.out.println("Movement not found.");
            return;
        }

        if (!(movement instanceof RentalMovement)) {
            System.out.println("That movement is not a rental.");
            return;
        }

        RentalMovement rental = (RentalMovement) movement;

        if (rental.getStatus() != MovementStatus.INCOMPLETE) {
            System.out.println("This rental must be checked in before it can be closed.");
            return;
        }

        System.out.print("Odometer reading at check-in: ");
        String odoInput = scanner.nextLine();
        int odoIn;
        try {
            odoIn = Integer.parseInt(odoInput);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        double finalCost = movementService.getRentalCost(rental);
        System.out.println("Final rental cost: $" + String.format("%.2f", finalCost));

        System.out.print("Confirm payment and close rental? (yes/no): ");
        String confirm = scanner.nextLine();

        if (!confirm.trim().equalsIgnoreCase("yes")) {
            System.out.println("Closing cancelled.");
            return;
        }

        movementService.closeRA(rental, odoIn, finalCost);
        System.out.println("Rental closed. Vehicle status: " + rental.getVehicle().getStatus());
    }
}
