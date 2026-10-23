package com.rfo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A rental station with an ID, a name, an address and an active flag.
 */
public class Location {

    private String locationID;
    private String name;
    private Address address;
    private boolean active = true;

    public Location(String locationID, String name, Address address) {
        this.locationID = locationID;
        this.name = name;
        this.address = address;
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        return vehicles;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
