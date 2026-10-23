package com.rfo.service;

import com.rfo.model.DefleetMovement;
import com.rfo.model.FuelType;
import com.rfo.model.NewCarMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VehicleService {

    private DataStore dataStore;

    public VehicleService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Vehicle addVehicle(String plateNumber, String make, String model, int year,
            VehicleCategory category, FuelType fuelType, boolean hasGPS, String staffID) {

        Vehicle existing = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plateNumber));

        if (existing != null) {
            System.out.println("Duplicate plateNumber: " + plateNumber + " already exists.");
            return null;
        }

        Vehicle newVehicle = new Vehicle(plateNumber, make, model, year, category, fuelType, hasGPS);
        newVehicle.setRate(getDefaultRate(category));

        NewCarMovement ncm = new NewCarMovement(newVehicle, staffID);
        ncm.execute();

        dataStore.getVehicles().add(newVehicle);
        dataStore.getMovements().add(ncm);

        dataStore.saveVehicles();
        dataStore.saveMovements();

        return newVehicle;
    }

    public boolean defleetVehicle(String plateNumber, String staffID) {
        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plateNumber));

        if (vehicle == null) {
            System.out.println("Vehicle not found: " + plateNumber);
            return false;
        }

        DefleetMovement dvm = new DefleetMovement(vehicle, staffID);
        dvm.execute();

        dataStore.getMovements().add(dvm);

        dataStore.saveVehicles();
        dataStore.saveMovements();

        return true;
    }

    // available vehicles, optionally for one category (null = every category)
    public List<Vehicle> getAvailableVehicles(VehicleCategory category) {
        List<Vehicle> available = dataStore.search(dataStore.getVehicles(), Vehicle::isAvailable);

        if (category != null) {
            available = dataStore.search(available, v -> v.getCategory() == category);
        }

        Comparator<Vehicle> comparator;
        if (category == null) {
            comparator = Comparator.comparing(Vehicle::getCategory)
                    .thenComparingInt(Vehicle::getCurrentOdometer)
                    .thenComparing(Vehicle::getPlateNumber);
        } else {
            comparator = Comparator.comparingInt(Vehicle::getCurrentOdometer)
                    .thenComparing(Vehicle::getPlateNumber);
        }
        available.sort(comparator);
        return available;
    }

    // vehicles flagged as due for service, optionally for one category
    public List<Vehicle> getServiceDueVehicles(VehicleCategory category) {
        List<Vehicle> due = dataStore.search(dataStore.getVehicles(), Vehicle::isDueForService);

        if (category != null) {
            due = dataStore.search(due, v -> v.getCategory() == category);
        }
        return due;
    }

    // the whole fleet, optionally for one category
    public List<Vehicle> getAllVehicles(VehicleCategory category) {
        List<Vehicle> all = dataStore.search(dataStore.getVehicles(),
                v -> category == null || v.getCategory() == category);

        all.sort(Comparator.comparing(Vehicle::getCategory)
                .thenComparing(Vehicle::getPlateNumber));
        return all;
    }

    /**
     * The statuses a person may set by hand. IN_USE is left out because only a
     * rental movement puts a vehicle in use.
     */
    public List<VehicleStatus> getManualStatuses() {
        List<VehicleStatus> statuses = new ArrayList<>();
        for (VehicleStatus status : VehicleStatus.values()) {
            if (status != VehicleStatus.IN_USE) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    /**
     * Changes the status of the vehicle with the given plate and saves it.
     *
     * @return true if the status changed, false if it was already that status
     * @throws IllegalArgumentException if the plate or status is missing or no
     * such vehicle exists
     */
    public boolean updateVehicleStatus(String plateNumber, VehicleStatus newStatus) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Plate number is required.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("A new status is required.");
        }

        if (newStatus == VehicleStatus.IN_USE) {
            throw new IllegalArgumentException("IN_USE is set by rental movements and cannot be chosen here.");
        }

        String plate = plateNumber.trim().toUpperCase();
        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plate));

        if (vehicle == null) {
            throw new IllegalArgumentException("No vehicle with plate number " + plate + ".");
        }

        if (vehicle.getStatus() == newStatus) {
            return false;
        }

        vehicle.updateStatus(newStatus);
        dataStore.saveVehicles();
        return true;
    }

    private double getDefaultRate(VehicleCategory category) {
        switch (category) {
            case COMPACT:
                return 0.75;
            case INTERMEDIATE:
                return 0.90;
            case STANDARD:
                return 1.0;
            case PREMIUM:
                return 1.6;
            case SPECIAL:
                return 1.8;
            default:
                return 1.0;
        }
    }
}
