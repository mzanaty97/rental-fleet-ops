package com.rfo.ui;

import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleStatus;
import com.rfo.service.DataStore;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class ReportsMenu extends Menu {

    public ReportsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
    }

    @Override
    protected String getTitle() {
        return "Reports";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Out on Rental Report", "Location Fleet", "New Vehicles", "Staff Rental Reports");
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
        List<Movement> ora = dataStore.search(dataStore.getMovements(), m -> m instanceof RentalMovement
                && m.getStatus().equals(MovementStatus.OPEN));

        String currentLocID = staff.getCurrentLocation() != null
                ? staff.getCurrentLocation().getLocationID() : null;

        if (currentLocID == null) {
            System.out.println("You have no current location set.");
            return;
        }

        ora = dataStore.search(ora, m -> {
            RentalMovement rm = (RentalMovement) m;
            boolean checkedOutHere = m.getCheckOutLoc().getLocationID().equals(currentLocID);
            boolean returningHere = rm.getExpectedCILoc() != null
                    && rm.getExpectedCILoc().getLocationID().equals(currentLocID);
            return checkedOutHere || returningHere;
        });

        if (ora.isEmpty()) {
            System.out.println("No open movements found");
            return;
        }

        ora.sort(Comparator
                .comparing((Movement m) -> ((RentalMovement) m).getExpectedCIDT())
                .thenComparing(m -> m.getVehicle().getCategory()));

        System.out.println("\n--- Out on Rental Report ---");
        System.out.println("Rego\tExpected Return\tCategory");
        for (Movement m : ora) {
            RentalMovement rm = (RentalMovement) m;
            System.out.println(m.getVehicle().getPlateNumber() + "\t"
                    + rm.getExpectedCIDT() + "\t" + m.getVehicle().getCategory());
        }
    }

    private void locationFleet() {
        System.out.println("Location ID: ");
        String input = scanner.nextLine();

        Location location = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(input));

        if (location == null) {
            System.out.println("Location not found");
            return;
        }

        List<Vehicle> fleet = dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentLocation() != null
                && v.getCurrentLocation().getLocationID().equals(input));

        if (fleet.isEmpty()) {
            System.out.println("No vehicles at this location");
            return;
        }

        fleet.sort(Comparator.comparingInt(Vehicle::getCurrentOdometer)
                .thenComparing(Vehicle::getCategory));

        System.out.println("\n--- " + input + " Fleet ---");
        System.out.println("Rego\t|Odometer\t|Status");
        for (Vehicle v : fleet) {
            System.out.println(v.getPlateNumber() + "\t|" + v.getCurrentOdometer() + "\t|" + v.getStatus());
        }
    }

    private void newVehicles() {
        List<Vehicle> newCar = dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentOdometer() <= 30
                && v.getStatus().equals(VehicleStatus.AVAILABLE));

        if (newCar.isEmpty()) {
            System.out.println("No new cars available.");
            return;
        }

        newCar.sort(Comparator
                .comparing((Vehicle v) -> v.getCurrentLocation() != null ? v.getCurrentLocation().getLocationID() : "")
                .thenComparingInt(Vehicle::getCurrentOdometer)
                .thenComparing(Vehicle::getPlateNumber));

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

        List<Movement> rentals = dataStore.search(dataStore.getMovements(),
                m -> m instanceof RentalMovement && m.getCOStaff().equals(staffID));

        if (rentals.isEmpty()) {
            System.out.println("No rentals found for this staff member.");
            return;
        }

        rentals.sort(Comparator.comparingInt(m -> Integer.parseInt(m.getMovementID())));

        System.out.println("\n--- Staff Rental Report: " + staffID + " ---");
        System.out.println("Movement ID\t|\tCO Date\t|\tCI Date\t|\tStatus");
        for (Movement m : rentals) {
            RentalMovement rm = (RentalMovement) m;
            String ciDate = m.getCheckInDT() != null
                    ? m.getCheckInDT().toString()
                    : rm.getExpectedCIDT() + " (expected)";
            String flag = m.getStatus() == MovementStatus.CLOSED ? "C" : "O";
            System.out.println(m.getMovementID() + "\t|\t" + m.getCheckOutDT() + "\t|\t" + ciDate + "\t|\t" + flag);
        }
    }

}
