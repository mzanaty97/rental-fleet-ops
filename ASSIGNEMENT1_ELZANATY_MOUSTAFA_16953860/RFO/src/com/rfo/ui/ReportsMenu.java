package com.rfo.ui;

import com.rfo.model.Location;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.DataStore;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import com.rfo.service.VehicleService;
import java.util.List;
import java.util.Scanner;

public class ReportsMenu extends Menu {

    private StaffService staffService;
    private VehicleService vehicleService;
    private MovementService movementService;

    public ReportsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.staffService = new StaffService(dataStore);
        this.vehicleService = new VehicleService(dataStore);
        this.movementService = new MovementService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Reports";
    }

    @Override
    protected List<String> getOptions() {
        return staffService.getReportsOptions(staff.getRole());
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Out on Rental Report":
                outOnRentalReport();
                break;
            case "Location Fleet":
                locationFleet();
                break;
            case "New Vehicles":
                newVehicles();
                break;
            case "Staff Rental Reports":
                staffRentalReport();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void outOnRentalReport() {
        List<RentalMovement> open;
        try {
            open = movementService.getOutOnRental(staff.getCurrentLocation());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }
        if (open.isEmpty()) {
            System.out.println("No open movements found");
            return;
        }

        System.out.println("\n--- Out on Rental Report ---");
        System.out.println("Rego\tExpected Return\tCategory");
        for (RentalMovement rm : open) {
            System.out.println(rm.getVehicle().getPlateNumber() + "\t"
                    + rm.getExpectedCIDT() + "\t" + rm.getVehicle().getCategory());
        }
    }

    private void locationFleet() {
        System.out.println("Location ID: ");
        String input = scanner.nextLine().trim().toUpperCase();

        Location location = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(input));

        if (location == null) {
            System.out.println("Location not found");
            return;
        }

        List<Vehicle> fleet = vehicleService.getLocationFleet(input);
        if (fleet.isEmpty()) {
            System.out.println("No vehicles at this location");
            return;
        }

        System.out.println("\n--- " + input + " Fleet ---");
        System.out.println("Rego\t|Odometer\t|Status");
        for (Vehicle v : fleet) {
            System.out.println(v.getPlateNumber() + "\t|" + v.getCurrentOdometer() + "\t|" + v.getStatus());
        }
    }

    private void newVehicles() {
        List<Vehicle> newCar = vehicleService.getNewVehicles();
        if (newCar.isEmpty()) {
            System.out.println("No new cars available.");
            return;
        }

        System.out.println("\n--- New Cars ---");
        System.out.println("Rego\t|\tLocation\t");
        for (Vehicle v : newCar) {
            String locationName = v.getCurrentLocation() != null ? v.getCurrentLocation().getName() : "N/A";
            System.out.println(v.getPlateNumber() + "\t|\t" + locationName);
        }
    }

    private void staffRentalReport() {
        System.out.print("Staff ID: ");
        String staffID = scanner.nextLine();

        List<RentalMovement> rentals = movementService.getStaffRentals(staffID);
        if (rentals.isEmpty()) {
            System.out.println("No rentals found for this staff member.");
            return;
        }

        System.out.println("\n--- Staff Rental Report: " + staffID + " ---");
        System.out.println("Movement ID\t|\tCO Date\t|\tCI Date\t|\tStatus");
        for (RentalMovement rm : rentals) {
            String ciDate = rm.getCheckInDT() != null
                    ? rm.getCheckInDT().toString()
                    : rm.getExpectedCIDT() + " (expected)";
            String flag = rm.getStatus() == MovementStatus.CLOSED ? "C" : "O";
            System.out.println(rm.getMovementID() + "\t|\t" + rm.getCheckOutDT() + "\t|\t" + ciDate + "\t|\t" + flag);
        }
    }

}
