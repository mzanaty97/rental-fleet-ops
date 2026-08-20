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
                return List.of("Admin", "Movements", "Fleet", "Reports", "Location Manager");
            case AGENT:
                return List.of("Admin", "Movements", "Fleet", "Reports");
            case MECHANIC:
                return List.of("Movements", "Fleet", "Reports");
            case FLEET_OPERATOR:
                return List.of("Movements", "Fleet", "Reports");
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
        dataStore.saveStaff();
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

    public boolean changePassword(Staff staff, String currentPassword, String newPassword) {
        boolean verified = dataStore.authenticate(staff.getStaffID(), currentPassword);
        if (!verified) {
            System.out.println("Current password is incorrect.");
            return false;
        }
        return dataStore.updateStaffPassword(staff.getStaffID(), newPassword);
    }

    public void grantLocationAccess(Staff target, Location location) {
        if (target.getAccessibleLocations().contains(location)) {
            System.out.println("Staff already has access to this location.");
            return;
        }
        target.getAccessibleLocations().add(location);
        dataStore.saveStaff();
        System.out.println("Access granted.");
    }

    public boolean revokeAllLocationAccess(Staff target) {
        if (target.getAccessibleLocations().isEmpty() && target.getCurrentLocation() == null) {
            System.out.println("Staff already has no location access.");
            return false;
        }
        target.getAccessibleLocations().clear();
        target.switchLocation(null);
        dataStore.saveStaff();
        return true;
    }
}
