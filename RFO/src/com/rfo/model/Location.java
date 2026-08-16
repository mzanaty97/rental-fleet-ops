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
}
