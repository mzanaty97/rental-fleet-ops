package com.rfo.service;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import java.util.ArrayList;
import java.util.List;

// Generated with assistance from Claude (modified existing class): I wrote this class in Part 1; Claude helped me move more of the business rules into it in Part 2 (see the method notes) so the GUI and the console menus share them.
/**
 * Business rules for staff: login-related changes, passwords, which locations a staff
 * member can use, and granting or revoking access. Problems are reported with
 * IllegalArgumentException.
 */
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
                return List.of("Admin", "Movements", "Fleet", "Reports");
            case FLEET_OPERATOR:
                return List.of("Admin", "Movements", "Fleet", "Reports");
            default:
                return List.of();
        }
    }

    public boolean hasPermission(StaffRole role, String action) {
        return getMenuOptions(role).contains(action);
    }

    // Generated with assistance from Claude: lists all staff.
    /**
     * Everyone on record, in file order.
     */
    public List<Staff> getAllStaff() {
        return new ArrayList<>(dataStore.getStaff());
    }

    /**
     * The staff member with this ID (any letter case), or null.
     */
    // Generated with assistance from Claude: lists the staff whose location access can be changed.
    /**
     * Everyone whose location access can be changed: managers are left out
     * because they can use every location.
     */
    public List<Staff> getStaffWithAccessRules() {
        List<Staff> list = new ArrayList<>();
        for (Staff s : dataStore.getStaff()) {
            if (s.getRole() != StaffRole.MANAGER) {
                list.add(s);
            }
        }
        return list;
    }

    // Generated with assistance from Claude: finds a staff member by ID.
    public Staff findStaff(String staffID) {
        String wanted = staffID == null ? "" : staffID.trim().toUpperCase();
        return dataStore.getBy(dataStore.getStaff(), s -> s.getStaffID().equals(wanted));
    }

    // Generated with assistance from Claude: lists the locations a staff member is allowed to work at.
    /**
     * Locations the staff member may work at: a manager can use every active
     * location, others only the ones granted.
     */
    public List<Location> getUsableLocations(Staff staff) {
        if (staff.getRole() == StaffRole.MANAGER) {
            return new ArrayList<>(dataStore.getLocations().stream().filter(Location::isActive)
                    .collect(java.util.stream.Collectors.toList()));
        }
        return new ArrayList<>(staff.getAccessibleLocations());
    }

    // Generated with assistance from Claude: checks whether a staff member may work at a location.
    public boolean canUseLocation(Staff staff, Location location) {
        return location != null && getUsableLocations(staff).contains(location);
    }

    /**
     * Switches the staff member's working location. Throws with the reason if
     * it is not allowed.
     */
    public void switchLocation(Staff staff, Location requested) {
        if (!canUseLocation(staff, requested)) {
            throw new IllegalArgumentException("Staff does not have access to this location.");
        }
        staff.switchLocation(requested);
        dataStore.saveStaff();
    }

    /**
     * Adds a staff member after checking every field. Throws with the reason if
     * anything is wrong.
     */
    public Staff addStaff(String staffID, String password, String fName, String lName, StaffRole role) {
        if (staffID == null || !staffID.matches("[A-Za-z0-9]{1,10}")) {
            throw new IllegalArgumentException("Staff ID must be letters and digits only, up to 10 characters.");
        }
        staffID = staffID.toUpperCase();
        if (fName == null || fName.trim().isEmpty() || lName == null || lName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name are required.");
        }
        checkPasswordRules(password);
        if (role == null) {
            throw new IllegalArgumentException("Please choose a role.");
        }
        if (findStaff(staffID) != null) {
            throw new IllegalArgumentException("Duplicate staffID: " + staffID + " already exists.");
        }
        Staff newStaff = new Staff(staffID, fName.trim(), lName.trim(), role);
        dataStore.getStaff().add(newStaff);
        dataStore.addNewStaffLine(newStaff, password);
        return newStaff;
    }

    /**
     * Changes the password after checking the current one. Throws with the
     * reason if it is wrong or too weak.
     */
    public boolean changePassword(Staff staff, String currentPassword, String newPassword) {
        boolean verified = dataStore.authenticate(staff.getStaffID(), currentPassword);
        if (!verified) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        checkPasswordRules(newPassword);
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("New password must be different from the current one.");
        }
        return dataStore.updateStaffPassword(staff.getStaffID(), newPassword);
    }

    // Generated with assistance from Claude: checks a new password follows the password rules.
    private void checkPasswordRules(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    /**
     * Lets the staff member use another location. Throws if they already can.
     */
    public void grantLocationAccess(Staff target, Location location) {
        if (target.getRole() == StaffRole.MANAGER) {
            throw new IllegalArgumentException("Managers can already use every location.");
        }
        if (target.getAccessibleLocations().contains(location)) {
            throw new IllegalArgumentException("Staff already has access to this location.");
        }
        target.getAccessibleLocations().add(location);
        dataStore.saveStaff();
    }

    // Generated with assistance from Claude: removes a staff member's access to some locations.
    /**
     * Removes the chosen locations from the staff member and keeps the rest.
     * Throws with the reason if nothing can be removed.
     */
    public void revokeLocationAccess(Staff target, List<Location> locations) {
        if (target.getRole() == StaffRole.MANAGER) {
            throw new IllegalArgumentException("Managers can use every location, so their access cannot be revoked.");
        }
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one location to remove.");
        }
        for (Location l : locations) {
            if (!target.getAccessibleLocations().contains(l)) {
                throw new IllegalArgumentException("Staff does not have access to " + l.getLocationID() + ".");
            }
        }
        target.getAccessibleLocations().removeAll(locations);
        if (target.getCurrentLocation() != null && locations.contains(target.getCurrentLocation())) {
            target.switchLocation(null);
        }
        dataStore.saveStaff();
    }

    /**
     * Removes every location from the staff member. Throws if they have none.
     */
    public boolean revokeAllLocationAccess(Staff target) {
        if (target.getRole() == StaffRole.MANAGER) {
            throw new IllegalArgumentException("Managers can use every location, so their access cannot be revoked.");
        }
        if (target.getAccessibleLocations().isEmpty() && target.getCurrentLocation() == null) {
            throw new IllegalArgumentException("Staff already has no location access.");
        }
        target.getAccessibleLocations().clear();
        target.switchLocation(null);
        dataStore.saveStaff();
        return true;
    }

    public List<String> getMovementsOptions(StaffRole role) {
        return List.of("Revenue Movements", "Non-Revenue Movements", "Movement Details");
    }

    public List<String> getRevenueMovementsOptions(StaffRole role) {
        if (role == StaffRole.MANAGER || role == StaffRole.AGENT) {
            return List.of("Checkout", "Check In", "Close Rental", "Amend Rental", "Changeover", "Print Invoice");
        }
        return List.of();
    }

    public List<String> getNonRevenueMovementsOptions(StaffRole role) {
        List<String> options = new ArrayList<>();
        if (role == StaffRole.MANAGER || role == StaffRole.FLEET_OPERATOR) {
            options.add("New Vehicle");
        }
        if (role == StaffRole.MANAGER || role == StaffRole.AGENT || role == StaffRole.FLEET_OPERATOR) {
            options.add("Car Transfer");
        }
        if (role == StaffRole.MANAGER || role == StaffRole.AGENT || role == StaffRole.MECHANIC) {
            options.add("Workshop");
        }
        if (role == StaffRole.MANAGER || role == StaffRole.FLEET_OPERATOR) {
            options.add("Defleet");
        }
        return options;
    }

    public List<String> getMovementDetailsOptions(StaffRole role) {
        List<String> options = new ArrayList<>();
        options.add("Query Movement Details");
        if (role == StaffRole.MANAGER || role == StaffRole.AGENT) {
            options.add("Query Drivers");
        }
        options.add("Show Vehicle Movements");
        return options;
    }

    public List<String> getFleetOptions(StaffRole role) {
        List<String> options = new ArrayList<>();
        if (role == StaffRole.MANAGER || role == StaffRole.AGENT || role == StaffRole.FLEET_OPERATOR) {
            options.add("Query Available Vehicles");
        }
        if (role == StaffRole.MANAGER || role == StaffRole.AGENT || role == StaffRole.MECHANIC) {
            options.add("Query Service Due Vehicles");
        }
        if (role == StaffRole.MANAGER || role == StaffRole.FLEET_OPERATOR) {
            options.add("Update Vehicle Status");
        }
        return options;
    }

    public List<String> getReportsOptions(StaffRole role) {
        List<String> options = new ArrayList<>();
        options.add("Out on Rental Report");
        options.add("Location Fleet");
        options.add("New Vehicles");
        if (role == StaffRole.MANAGER) {
            options.add("Staff Rental Reports");
        }
        return options;
    }
}
