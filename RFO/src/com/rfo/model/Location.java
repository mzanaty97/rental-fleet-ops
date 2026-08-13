package com.rfo.model;

import java.util.ArrayList;
import java.util.List;

public class Location {

    private String locationID;
    private String name;
    private Address address;

    public Location(String locationID, String name, Address address) {
        this.locationID = locationID;
        this.name = name;
        this.address = address;
    }

    public String getLocationID() {
        return locationID;
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {
        return address;
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        return vehicles;
    }
}
