package com.rfo.ui;

import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.DataStore;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RevenueMovementsMenu extends Menu {

    private DataStore dataStore;
    private Staff staff;
    private MovementService movementService;
    private StaffService staffService;

    public RevenueMovementsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.dataStore = dataStore;
        this.staff = staff;
        this.movementService = new MovementService(dataStore);
        this.staffService = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Revenue Movements";
    }

    @Override
    protected List<String> getOptions() {
        return staffService.getRevenueMovementsOptions(staff.getRole());
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
        if (staff.getCurrentLocation() == null) {
            System.out.println("You have no current location set. Cannot check out a vehicle.");
            return;
        }

        if (!new com.rfo.service.StaffService(dataStore).canUseLocation(staff, staff.getCurrentLocation())) {
            System.out.println("You do not have access to your current location. Cannot check out a vehicle.");
            return;
        }

        String checkOutLocID = staff.getCurrentLocation().getLocationID();

        String plateNumber = promptForValidatedPlateNumber(checkOutLocID);
        if (plateNumber == null) {
            return;
        }

        List<Integer> driverIDs = promptForDriverIDsWithCancel();
        if (driverIDs == null) {
            return;
        }
        if (driverIDs.isEmpty()) {
            System.out.println("At least one driver is required.");
            return;
        }

        LocalDateTime expectedCIDT = promptForDateTimeWithCancel(
                "Expected check-in date/time (yyyy-MM-dd HH:mm), or CANCEL: ");
        if (expectedCIDT == null) {
            return;
        }

        String expectedCILocID = promptForLocationID(
                "Expected check-in location ID (or LIST to browse, or CANCEL): ");
        if (expectedCILocID == null) {
            return;
        }

        Boolean rsa = promptForYesNoWithCancel("RSA? (yes/no, or CANCEL): ");
        if (rsa == null) {
            return;
        }

        Insurance insuranceTier = promptForInsuranceWithCancel();
        if (insuranceTier == null) {
            return;
        }

        Location checkOutLoc = staff.getCurrentLocation();
        Location expectedCILoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(expectedCILocID));

        System.out.println("\n--- Confirm Checkout ---");
        System.out.println("Plate: " + plateNumber);
        System.out.println("Checkout: " + checkOutLoc.getName() + " (now)");
        System.out.println("Expected return: " + expectedCIDT + " at "
                + (expectedCILoc != null ? expectedCILoc.getName() : expectedCILocID));
        System.out.println("Drivers: " + driverIDs);
        System.out.println("RSA: " + (rsa ? "Yes" : "No"));
        System.out.println("Insurance: " + insuranceTier);

        if (!confirmOrCancel()) {
            System.out.println("Checkout cancelled.");
            return;
        }

        RentalMovement rental;
        try {
            rental = movementService.startRA(plateNumber, staff.getStaffID(), checkOutLocID,
                    driverIDs, expectedCIDT, expectedCILocID, rsa, insuranceTier);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Checkout failed. Please check the details above and try again.");
            return;
        }

        System.out.println("\nRental started successfully.");
        System.out.println("Movement ID: " + rental.getMovementID());
        double estimate = rental.getRentalEstimate();
        System.out.println("Estimated total cost: $" + String.format("%.2f", estimate));
    }

    private void checkin() {
        System.out.print("Movement ID: ");
        String movementID = scanner.nextLine();

        try {
            movementService.checkInRental(movementID, staff.getStaffID(), staff.getCurrentLocation());
            System.out.println("Checked in at " + staff.getCurrentLocation().getName()
                    + ". Movement status is now: " + MovementStatus.INCOMPLETE);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void closeRental() {
        System.out.print("Movement ID: ");
        String movementID = scanner.nextLine();

        RentalMovement rental;
        try {
            rental = movementService.findRentalForClose(movementID);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        System.out.print("Odometer reading at check-in: ");
        String odoInput = scanner.nextLine();
        int odoIn;
        try {
            odoIn = movementService.parseOdometer(odoInput, rental.getOdoOut());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
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

        try {
            movementService.closeRA(rental, odoIn, finalCost);
            System.out.println("Rental closed. Vehicle status: " + rental.getVehicle().getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---- Shared helpers below (CANCEL-aware retry-until-valid prompts) ----
    private String promptForValidatedPlateNumber(String checkOutLocID) {
        while (true) {
            String plateNumber = promptForPlateNumber("Plate number (or CANCEL): ");
            if (plateNumber == null) {
                return null;
            }

            Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                    v -> v.getPlateNumber().equals(plateNumber));

            if (vehicle == null) {
                System.out.println("No vehicle found with that plate number. Try again.");
                continue;
            }

            if (vehicle.getCurrentLocation() == null
                    || !vehicle.getCurrentLocation().getLocationID().equals(checkOutLocID)) {
                System.out.println("That vehicle is not at your current location ("
                        + staff.getCurrentLocation().getName() + "). Try again.");
                continue;
            }

            if (!vehicle.isAvailable()) {
                System.out.println("That vehicle is not available (status: " + vehicle.getStatus() + "). Try again.");
                continue;
            }

            if (vehicle.isDueForService()) {
                System.out.println("That vehicle is overdue for service by "
                        + vehicle.getOverdueKm() + " km. Try again.");
                continue;
            }

            return plateNumber;
        }
    }

    private String promptForPlateNumber(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equalsIgnoreCase("CANCEL")) {
                if (confirmAbandon()) {
                    return null;
                }
                continue;
            }

            return input;
        }
    }

    private String promptForLocationID(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equalsIgnoreCase("CANCEL")) {
                if (confirmAbandon()) {
                    return null;
                }
                continue;
            }

            if (input.equalsIgnoreCase("LIST")) {
                printActiveLocations();
                continue;
            }

            String locationID = input;
            Location loc = dataStore.getBy(dataStore.getLocations(), l -> l.getLocationID().equals(locationID));

            if (loc == null) {
                System.out.println("Location not found. Try again, or LIST to browse.");
                continue;
            }

            return locationID;
        }
    }

    private void printActiveLocations() {
        List<Location> active = dataStore.search(dataStore.getLocations(), Location::isActive);
        System.out.println("--- Active Locations ---");
        for (Location l : active) {
            System.out.println(l.getLocationID() + " - " + l.getName());
        }
    }

    private List<Integer> promptForDriverIDsWithCancel() {
        while (true) {
            List<Integer> driverIDs = new ArrayList<>();
            System.out.println("Enter driver IDs (one per line, blank line to finish, or CANCEL):");
            boolean cancelled = false;

            while (true) {
                String input = scanner.nextLine().trim().toUpperCase();
                if (input.isEmpty()) {
                    break;
                }
                if (input.equalsIgnoreCase("CANCEL")) {
                    cancelled = true;
                    break;
                }
                try {
                    driverIDs.add(Integer.parseInt(input));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid driver ID, skipped.");
                }
            }

            if (cancelled) {
                if (confirmAbandon()) {
                    return null;
                }
                continue;
            }

            return driverIDs;
        }
    }

    private LocalDateTime promptForDateTimeWithCancel(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equalsIgnoreCase("CANCEL")) {
                if (confirmAbandon()) {
                    return null;
                }
                continue;
            }

            LocalDateTime parsed;
            try {
                parsed = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date/time format. Try again.");
                continue;
            }

            if (!parsed.isAfter(LocalDateTime.now())) {
                System.out.println("Expected check-in must be in the future. Try again.");
                continue;
            }

            return parsed;
        }
    }

    private Boolean promptForYesNoWithCancel(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equalsIgnoreCase("CANCEL")) {
                if (confirmAbandon()) {
                    return null;
                }
                continue;
            }
            if (input.equalsIgnoreCase("YES")) {
                return true;
            }
            if (input.equalsIgnoreCase("NO")) {
                return false;
            }

            System.out.println("Please answer yes or no.");
        }
    }

    private Insurance promptForInsuranceWithCancel() {
        while (true) {
            Insurance[] tiers = Insurance.values();
            System.out.println("Insurance tier (or CANCEL):");
            for (int i = 0; i < tiers.length; i++) {
                System.out.println((i + 1) + ". " + tiers[i]);
            }
            System.out.print("Choose: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equalsIgnoreCase("CANCEL")) {
                if (confirmAbandon()) {
                    return null;
                }
                continue;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice < 1 || choice > tiers.length) {
                    System.out.println("Invalid choice. Try again.");
                    continue;
                }
                return tiers[choice - 1];
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private boolean confirmAbandon() {
        while (true) {
            System.out.print("Abandon checkout? (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equalsIgnoreCase("yes")) {
                return true;
            }
            if (input.equalsIgnoreCase("no")) {
                return false;
            }
            System.out.println("Please answer yes or no.");
        }
    }

    private boolean confirmOrCancel() {
        while (true) {
            System.out.print("Type CONFIRM to start this rental, or CANCEL to abandon: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equalsIgnoreCase("CONFIRM")) {
                return true;
            }
            if (input.equalsIgnoreCase("CANCEL")) {
                return false;
            }
            System.out.println("Please type CONFIRM or CANCEL.");
        }
    }
}
