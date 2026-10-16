package com.rfo.ui;

import com.rfo.model.CarTransferMovement;
import com.rfo.model.FuelType;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.WorkshopMovement;
import com.rfo.service.DataStore;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import com.rfo.service.VehicleService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

// Generated with assistance from Claude (modified existing class): I wrote this menu in Part 1; Claude helped me change it to call the service classes and show their error messages.
/**
 * The console menu for non-revenue movements: new vehicle, car transfer, workshop and
 * defleet.
 */
public class NonRevenueMovementsMenu extends Menu {

    private VehicleService vehicleService;
    private MovementService movementService;
    private StaffService staffService;

    public NonRevenueMovementsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.vehicleService = new VehicleService(dataStore);
        this.movementService = new MovementService(dataStore);
        this.staffService = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Non-Revenue Movements";
    }

    @Override
    protected List<String> getOptions() {
        return staffService.getNonRevenueMovementsOptions(staff.getRole());
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "New Vehicle":
                newVehicle();
                break;
            case "Car Transfer":
                carTransfer();
                break;
            case "Workshop":
                workshop();
                break;
            case "Defleet":
                defleet();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void newVehicle() {
        System.out.print("Plate number: ");
        String plateNumber = scanner.nextLine();

        System.out.print("Make: ");
        String make = scanner.nextLine();

        System.out.print("Model: ");
        String model = scanner.nextLine();

        System.out.print("Year: ");
        int year;
        try {
            year = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid year.");
            return;
        }

        VehicleCategory category = MovementMenuUtils.promptForEnum(scanner, "Vehicle category", VehicleCategory.class);
        if (category == null) {
            return;
        }

        FuelType fuelType = MovementMenuUtils.promptForEnum(scanner, "Fuel type", FuelType.class);
        if (fuelType == null) {
            return;
        }

        System.out.print("Has GPS? (yes/no): ");
        boolean hasGPS = scanner.nextLine().trim().equalsIgnoreCase("yes");

        try {
            Vehicle vehicle = vehicleService.addVehicle(plateNumber, make, model, year,
                    category, fuelType, hasGPS, staff.getStaffID());
            System.out.println("Vehicle added successfully: " + vehicle.getPlateNumber());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void carTransfer() {
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
                "Expected arrival date/time (yyyy-MM-dd HH:mm): ");
        if (expectedCIDT == null) {
            return;
        }

        System.out.print("Expected arrival location ID: ");
        String expectedCILocID = scanner.nextLine();

        try {
            CarTransferMovement ctm = movementService.startCTM(plateNumber, staff.getStaffID(), checkOutLocID,
                    driverIDs, expectedCIDT, expectedCILocID);
            System.out.println("Transfer started. Movement ID: " + ctm.getMovementID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void workshop() {
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
                "Expected return date/time (yyyy-MM-dd HH:mm): ");
        if (expectedCIDT == null) {
            return;
        }

        System.out.print("Expected return location ID: ");
        String expectedCILocID = scanner.nextLine();

        try {
            WorkshopMovement wsm = movementService.startWSM(plateNumber, staff.getStaffID(), checkOutLocID,
                    driverIDs, expectedCIDT, expectedCILocID);
            System.out.println("Workshop movement started. Movement ID: " + wsm.getMovementID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void defleet() {
        System.out.print("Plate number: ");
        String plateNumber = scanner.nextLine();

        try {
            vehicleService.defleetVehicle(plateNumber, staff.getStaffID());
            System.out.println("Vehicle defleeted successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
