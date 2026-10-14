package com.rfo.service;

import com.rfo.model.Address;
import com.rfo.model.CarTransferMovement;
import com.rfo.model.Location;
import com.rfo.model.Vehicle;
import java.util.List;

public class LocationService {

    private static final int GENERIC_DRIVER_ID = 0; // reserved placeholder driver for administrative fleet moves

    private DataStore dataStore;

    public LocationService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Location addLocation(String locationID, String name, Address address) {
        Location existing = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(locationID));
        if (existing != null) {
            System.out.println("Duplicate locationID: " + locationID + " already exists.");
            return null;
        }
        Location newLocation = new Location(locationID, name, address);
        dataStore.getLocations().add(newLocation);
        dataStore.saveLocations();
        return newLocation;
    }

    public boolean closeLocation(Location location, String newLocationID, String staffID) {
        if (!location.isActive()) {
            System.out.println("Location is already closed.");
            return false;
        }

        List<Vehicle> stranded = dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentLocation() != null && v.getCurrentLocation().getLocationID().equals(location.getLocationID()));

        if (!stranded.isEmpty()) {
            Location newLocation = dataStore.getBy(dataStore.getLocations(),
                    l -> l.getLocationID().equals(newLocationID));

            if (newLocation == null) {
                System.out.println("Destination location not found. Close cancelled.");
                return false;
            }

            MovementService movementService = new MovementService(dataStore);
            List<Integer> genericDriver = List.of(GENERIC_DRIVER_ID);

            for (Vehicle v : stranded) {
                CarTransferMovement ctm = movementService.startCTM(v.getPlateNumber(), staffID, location.getLocationID(),
                        genericDriver, java.time.LocalDateTime.now(), newLocationID);

                if (ctm != null) {
                    movementService.checkIn(ctm, staffID, newLocationID);
                    movementService.closeDrivenMovement(ctm, v.getCurrentOdometer());
                }
            }
        }

        location.setActive(false);
        dataStore.saveLocations();
        return true;
    }
}
