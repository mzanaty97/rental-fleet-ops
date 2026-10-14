package com.rfo.service;

import com.rfo.model.Address;
import com.rfo.model.CarTransferMovement;
import com.rfo.model.Location;
import com.rfo.model.Vehicle;
import java.util.ArrayList;
import java.util.List;

public class LocationService {

    private static final int GENERIC_DRIVER_ID = 0; // reserved placeholder driver for administrative fleet moves
    private static final int MAX_ID_LENGTH = 10;

    private DataStore dataStore;

    public LocationService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Location> getAllLocations() {
        return new ArrayList<>(dataStore.getLocations());
    }

    public List<Location> getActiveLocations() {
        return dataStore.search(dataStore.getLocations(), Location::isActive);
    }

    public Location findLocation(String locationID) {
        if (locationID == null) {
            return null;
        }
        String id = locationID.trim().toUpperCase();
        return dataStore.getBy(dataStore.getLocations(), l -> l.getLocationID().equals(id));
    }

    public int countVehiclesAt(String locationID) {
        return dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentLocation() != null
                && v.getCurrentLocation().getLocationID().equals(locationID)).size();
    }

    /**
     * Adds a new, active location.
     *
     * @throws IllegalArgumentException with the reason if anything is missing
     * or invalid, or the location ID already exists
     */
    public Location addLocation(String locationID, String name, Address address) {
        String id = locationID == null ? "" : locationID.trim().toUpperCase();
        String cleanName = name == null ? "" : name.trim().toUpperCase();

        if (id.isEmpty()) {
            throw new IllegalArgumentException("Location ID is required.");
        }
        if (id.length() > MAX_ID_LENGTH || !id.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("Location ID must be letters and digits only, up to "
                    + MAX_ID_LENGTH + " characters.");
        }
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (address == null || isBlank(address.getStreet()) || isBlank(address.getCity())
                || address.getCountry() == null || isBlank(address.getPostalCode())) {
            throw new IllegalArgumentException("Street, city, country and postal code are all required.");
        }

        if (findLocation(id) != null) {
            throw new IllegalArgumentException("Duplicate locationID: " + id + " already exists.");
        }

        Location newLocation = new Location(id, cleanName, address);
        dataStore.getLocations().add(newLocation);
        dataStore.saveLocations();
        return newLocation;
    }

    /**
     * Closes a location, first moving any vehicles there to the destination.
     *
     * @throws IllegalArgumentException with the reason if the location is
     * already closed or the destination is missing, unknown, closed or the same
     * location
     */
    public void closeLocation(Location location, String newLocationID, String staffID) {
        if (location == null) {
            throw new IllegalArgumentException("Location not found.");
        }
        if (!location.isActive()) {
            throw new IllegalArgumentException("Location is already closed.");
        }

        List<Vehicle> stranded = dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentLocation() != null && v.getCurrentLocation().getLocationID().equals(location.getLocationID()));

        if (!stranded.isEmpty()) {
            Location newLocation = findLocation(newLocationID);

            if (newLocation == null) {
                throw new IllegalArgumentException("Destination location not found. Close cancelled.");
            }
            if (!newLocation.isActive() || newLocation.getLocationID().equals(location.getLocationID())) {
                throw new IllegalArgumentException(
                        "Destination must be an active location other than " + location.getLocationID() + ". Close cancelled.");
            }

            MovementService movementService = new MovementService(dataStore);
            List<Integer> genericDriver = List.of(GENERIC_DRIVER_ID);

            for (Vehicle v : stranded) {
                CarTransferMovement ctm = movementService.startCTM(v.getPlateNumber(), staffID, location.getLocationID(),
                        genericDriver, java.time.LocalDateTime.now(), newLocation.getLocationID());

                if (ctm != null) {
                    movementService.checkIn(ctm, staffID, newLocation.getLocationID());
                    movementService.closeDrivenMovement(ctm, v.getCurrentOdometer());
                }
            }
        }

        location.setActive(false);
        dataStore.saveLocations();
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
