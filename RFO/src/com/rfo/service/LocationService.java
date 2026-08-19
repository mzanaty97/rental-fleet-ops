package com.rfo.service;

import com.rfo.model.Address;
import com.rfo.model.Location;

public class LocationService {

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
}
