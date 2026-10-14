package com.rfo.ui;

import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class FleetMenu extends Menu {

    private StaffService staffService;

    public FleetMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.staffService = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Fleet";
    }

    @Override
    protected List<String> getOptions() {
        return staffService.getFleetOptions(staff.getRole());
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Query Available Vehicles":
                queryAvailableVehicles();
                break;
            case "Query Service Due Vehicles":
                queryServiceDueVehicles();
                break;
            case "Update Vehicle Status":
                updateVehicleStatus();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void queryAvailableVehicles() {
        VehicleCategory filter = promptForModeAndCategory();

        List<Vehicle> available = dataStore.search(dataStore.getVehicles(), Vehicle::isAvailable);

        if (filter != null) {
            available = dataStore.search(available, v -> v.getCategory() == filter);
        }

        if (available.isEmpty()) {
            System.out.println("No Vehicles Available!");
            return;
        }

        Comparator<Vehicle> comparator;
        if (filter == null) {
            comparator = Comparator.comparing(Vehicle::getCategory)
                    .thenComparingInt(Vehicle::getCurrentOdometer)
                    .thenComparing(Vehicle::getPlateNumber);
        } else {
            comparator = Comparator.comparingInt(Vehicle::getCurrentOdometer)
                    .thenComparing(Vehicle::getPlateNumber);
        }

        available.sort(comparator);

        System.out.println("\n--- Available Vehicles ---");
        for (Vehicle v : available) {
            System.out.println(v.getCategory() + " | " + v.getPlateNumber() + " | "
                    + v.getMake() + " " + v.getModel() + " | " + v.getCurrentOdometer() + " km | "
                    + v.getCurrentLocation().getName());
        }
    }

    private void queryServiceDueVehicles() {
        List<Vehicle> due = dataStore.search(dataStore.getVehicles(), Vehicle::isDueForService);

        if (due.isEmpty()) {
            System.out.println("No due for service vehicles.");
            return;
        }

        System.out.println("\n--- Due for Service Vehicles ---");
        for (Vehicle v : due) {
            System.out.println(v.getPlateNumber() + " | " + v.getMake() + " " + v.getModel()
                    + " | Overdue by " + v.getOverdueKm() + " km");
        }
    }

    private void updateVehicleStatus() {
        System.out.println("Plate number: ");
        String input = scanner.nextLine().trim().toUpperCase();

        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(input));

        if (vehicle == null) {
            System.out.println("No vehicle with given plate number.");
            return;
        }

        System.out.println("Current Status: " + vehicle.getStatus());

        VehicleStatus newStatus = MovementMenuUtils.promptForEnum(scanner, "New status", VehicleStatus.class);

        if (newStatus == null) {
            return;
        }

        if (vehicle.getStatus().equals(newStatus)) {
            System.out.println("No change done.");
            return;
        }

        vehicle.updateStatus(newStatus);
        dataStore.saveVehicles();
        System.out.println("Status changed to: " + newStatus);
    }

    private VehicleCategory promptForModeAndCategory() {
        System.out.println("1. General (all categories)");
        System.out.println("2. Specific (choose a category)");
        System.out.print("Choose mode: ");
        String input = scanner.nextLine();

        if (input.equals("1")) {
            return null;
        }

        if (input.equals("2")) {
            return MovementMenuUtils.promptForEnum(scanner, "Category", VehicleCategory.class);
        }

        System.out.println("Invalid choice. Defaulting to general mode.");
        return null;

    }

}
