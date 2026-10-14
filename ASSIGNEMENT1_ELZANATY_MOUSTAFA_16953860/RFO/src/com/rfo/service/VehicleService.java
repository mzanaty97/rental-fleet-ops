package com.rfo.service;

import com.rfo.model.DefleetMovement;
import com.rfo.model.FuelType;
import com.rfo.model.NewCarMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;

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
