package com.rfo.service;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import java.util.List;

public class StaffService {

    private DataStore dataStore;

    public StaffService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<String> getMenuOptions(StaffRole role) {
        switch (role) {
            case MANAGER:
                return List.of("Add Vehicle", "Add Staff", "View Reports", "Manage Locations");
            case AGENT:
                return List.of("Start Rental", "Check In Rental", "Check Vehicle Status");
            case FLEET_OPERATOR:
                return List.of("Transfer Vehicle", "Defleet Vehicle", "Check Vehicle Status");
            case MECHANIC:
                return List.of("Start Workshop Movement", "Check In Workshop Movement");
            default:
                return List.of();
        }
    }

    public boolean hasPermission(StaffRole role, String action) {
        return getMenuOptions(role).contains(action);
    }

    public void switchLocation(Staff staff, Location requested) {
        if (!staff.getAccessibleLocations().contains(requested)) {
            System.out.println("Staff does not have access to this location.");
            return;
        }
        staff.switchLocation(requested);
    }

    public Staff addStaff(String staffID, String password, String fName, String lName, StaffRole role) {
        Staff existing = dataStore.getBy(dataStore.getStaff(), s -> s.getStaffID().equals(staffID));

        if (existing != null) {
            System.out.println("Duplicate staffID: " + staffID + " already exists.");
            return null;
        }

        Staff newStaff = new Staff(staffID, fName, lName, role);

        dataStore.getStaff().add(newStaff);
        dataStore.addNewStaffLine(newStaff, password);

        return newStaff;
    }
}
