package com.rfo.io;

import com.rfo.db.DBConnection;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Generated with assistance from Claude. I designed the structure and logic
// (following my Part 1 design); Claude wrote the repeated methods and boilerplate.
/**
 * DAO replacement for StaffFileHandler. Same public contract as the file-based
 * version (loadAll/saveAll/authenticate/addNewStaffLine/updatePassword), now
 * backed by the STAFF + STAFF_LOCATIONS tables.
 *
 * Matches the locked decisions from Part 1: - passwords never enter memory as
 * Staff object state (mapRow never reads PASSWORD) - saveAll() never touches
 * the PASSWORD column, so it can't wipe existing passwords
 */
public class StaffDAO {

    public List<Staff> loadAll(List<Location> allLocations) {
        List<Staff> staffList = new ArrayList<>();
        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        String sql = "SELECT STAFF_ID, FNAME, LNAME, ROLE, CURRENT_LOCATION_ID FROM STAFF";

        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                staffList.add(mapRow(rs, locationsById));
            }
        } catch (SQLException e) {
            System.out.println("Error reading staff table: " + e.getMessage());
        }

        return staffList;
    }

    public void saveAll(List<Staff> staffList) {
        String update = "UPDATE STAFF SET FNAME=?, LNAME=?, ROLE=?, CURRENT_LOCATION_ID=? WHERE STAFF_ID=?";

        try {
            Connection conn = getConnection();
            for (Staff staff : staffList) {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setString(1, staff.getFName());
                    ps.setString(2, staff.getLName());
                    ps.setString(3, staff.getRole().toString());
                    ps.setString(4, staff.getCurrentLocation() != null ? staff.getCurrentLocation().getLocationID() : null);
                    ps.setString(5, staff.getStaffID());
                    ps.executeUpdate();
                }
                saveAccessibleLocations(conn, staff);
            }
        } catch (SQLException e) {
            System.out.println("Error writing staff table: " + e.getMessage());
        }
    }

    // Reads the table directly every call - password is never cached in memory
    public boolean authenticate(String staffID, String password) {
        String sql = "SELECT PASSWORD FROM STAFF WHERE STAFF_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PASSWORD").equals(password);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading staff table: " + e.getMessage());
        }
        System.out.println("Staff not found: " + staffID);
        return false;
    }

    public void addNewStaffLine(Staff staff, String password) {
        String insert = "INSERT INTO STAFF (STAFF_ID, PASSWORD, FNAME, LNAME, ROLE, CURRENT_LOCATION_ID) VALUES (?,?,?,?,?,?)";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, staff.getStaffID());
                ps.setString(2, password);
                ps.setString(3, staff.getFName());
                ps.setString(4, staff.getLName());
                ps.setString(5, staff.getRole().toString());
                ps.setString(6, staff.getCurrentLocation() != null ? staff.getCurrentLocation().getLocationID() : null);
                ps.executeUpdate();
            }
            saveAccessibleLocations(conn, staff);
        } catch (SQLException e) {
            System.out.println("Error adding staff: " + e.getMessage());
        }
    }

    public boolean updatePassword(String staffID, String newPassword) {
        String sql = "UPDATE STAFF SET PASSWORD=? WHERE STAFF_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, staffID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    private void saveAccessibleLocations(Connection conn, Staff staff) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM STAFF_LOCATIONS WHERE STAFF_ID=?")) {
            del.setString(1, staff.getStaffID());
            del.executeUpdate();
        }
        String insert = "INSERT INTO STAFF_LOCATIONS (STAFF_ID, LOCATION_ID) VALUES (?,?)";
        try (PreparedStatement ins = conn.prepareStatement(insert)) {
            for (Location loc : staff.getAccessibleLocations()) {
                ins.setString(1, staff.getStaffID());
                ins.setString(2, loc.getLocationID());
                ins.executeUpdate();
            }
        }
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    private Map<String, Location> buildLocationLookup(List<Location> allLocations) {
        Map<String, Location> map = new HashMap<>();
        for (Location loc : allLocations) {
            map.put(loc.getLocationID(), loc);
        }
        return map;
    }

    private Staff mapRow(ResultSet rs, Map<String, Location> locationsById) throws SQLException {
        String staffID = rs.getString("STAFF_ID");
        Location currentLocation = locationsById.get(rs.getString("CURRENT_LOCATION_ID"));
        List<Location> accessibleLocations = loadAccessibleLocations(staffID, locationsById);
        return new Staff(staffID, rs.getString("FNAME"), rs.getString("LNAME"),
                StaffRole.valueOf(rs.getString("ROLE")), accessibleLocations, currentLocation);
    }

    private List<Location> loadAccessibleLocations(String staffID, Map<String, Location> locationsById) throws SQLException {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT LOCATION_ID FROM STAFF_LOCATIONS WHERE STAFF_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, staffID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Location loc = locationsById.get(rs.getString("LOCATION_ID"));
                    if (loc != null) {
                        locations.add(loc);
                    }
                }
            }
        }
        return locations;
    }
}
