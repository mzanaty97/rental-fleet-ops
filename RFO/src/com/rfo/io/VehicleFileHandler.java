package com.rfo.io;

import com.rfo.model.FuelType;
import com.rfo.model.Location;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleFileHandler {

    private static final String FILE_PATH = "data/vehicles.txt";
    // vehicles saved in "plateNumber|make|model|year|category|rate|hasGPS|fuelType|currentOdometer|
    //                     lastServiceOdometer|status|currentLocationID|serviceFlag" format

    public List<Vehicle> loadAll(List<Location> allLocations) {
        List<Vehicle> vehicles = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return vehicles;
        }

        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                vehicles.add(parseLine(ln, locationsById));
            }
        } catch (IOException e) {
            System.out.println("Error reading vehicles file: " + e.getMessage());
        }

        return vehicles;
    }

    public void saveAll(List<Vehicle> vehicles) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Vehicle vehicle : vehicles) {
                writer.write(toLine(vehicle));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing vehicles file: " + e.getMessage());
        }
    }

    private Map<String, Location> buildLocationLookup(List<Location> allLocations) {
        Map<String, Location> locationsById = new HashMap<>();
        for (Location location : allLocations) {
            locationsById.put(location.getLocationID(), location);
        }
        return locationsById;
    }

    private Vehicle parseLine(String ln, Map<String, Location> locationsById) {
        String[] fields = ln.split("\\|");

        String plateNumber = fields[0];
        String make = fields[1];
        String model = fields[2];
        int year = Integer.parseInt(fields[3]);
        VehicleCategory category = VehicleCategory.valueOf(fields[4]);
        double rate = Double.parseDouble(fields[5]);
        boolean hasGPS = Boolean.parseBoolean(fields[6]);
        FuelType fuelType = FuelType.valueOf(fields[7]);
        int currentOdometer = Integer.parseInt(fields[8]);
        int lastServiceOdometer = Integer.parseInt(fields[9]);
        VehicleStatus status = VehicleStatus.valueOf(fields[10]);
        Location currentLocation = locationsById.get(fields[11]);
        boolean serviceFlag = Boolean.parseBoolean(fields[12]);

        return new Vehicle(plateNumber, make, model, year, category, fuelType, hasGPS,
                rate, currentOdometer, lastServiceOdometer, status, currentLocation, serviceFlag);
    }

    private String toLine(Vehicle vehicle) {
        String currentLocationID = vehicle.getCurrentLocation() != null
                ? vehicle.getCurrentLocation().getLocationID()
                : "";

        return String.join("|",
                vehicle.getPlateNumber(),
                vehicle.getMake(),
                vehicle.getModel(),
                String.valueOf(vehicle.getYear()),
                vehicle.getCategory().toString(),
                String.valueOf(vehicle.getRate()),
                String.valueOf(vehicle.hasGPS()),
                vehicle.getFuelType().toString(),
                String.valueOf(vehicle.getCurrentOdometer()),
                String.valueOf(vehicle.getLastServiceOdometer()),
                vehicle.getStatus().toString(),
                currentLocationID,
                String.valueOf(vehicle.isDueForService())
        );
    }
}
