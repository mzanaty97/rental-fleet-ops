package com.rfo.model;

import java.util.ArrayList;
import java.util.List;

public class Staff implements Person {

    private String staffID;
    private StaffRole role;
    private String fName;
    private String lName;
    private List<Location> accessibleLocations;
    private Location currentLocation;

    public Staff(String staffID, String fName, String lName, StaffRole role) {
        this(staffID, fName, lName, role, new ArrayList<>(), null);
    }

    public Staff(String staffId, String fName, String lName, StaffRole role,
            List<Location> accessibleLocations, Location currentLocation) {
        this.staffID = staffId;
        this.fName = fName;
        this.lName = lName;
        this.role = role;
        this.accessibleLocations = accessibleLocations;
        this.currentLocation = currentLocation;
    }

    @Override
    public String getName() {
        return fName + " " + lName;
    }

    @Override
    public String getContactInfo() {
        return "name: "
                + this.getName()
                + " Staff ID: "
                + getStaffID();
    }

    public List<String> getMenuOptions() {
        List<String> menu = new ArrayList<>();

        return menu;
    }

    public boolean hasPermission(String action) {
        return true;
    }

    public void switchLocation(Location location) {

        this.currentLocation = location;
    }

    public List<Location> getAccessibleLocations() {
        return accessibleLocations;
    }

    public void updateAccessibleLocations(List<Location> locations) {
        this.accessibleLocations = locations;
    }

    public String getStaffID() {
        return staffID;
    }

    public StaffRole getRole() {
        return role;
    }

}
