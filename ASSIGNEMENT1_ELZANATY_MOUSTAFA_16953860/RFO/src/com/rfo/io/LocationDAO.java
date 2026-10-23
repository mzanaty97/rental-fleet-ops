package com.rfo.io;

import com.rfo.db.DBConnection;
import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO replacement for LocationFileHandler. Same public contract
 * (loadAll/saveAll) as the file-based version, now backed by the LOCATIONS
 * table instead of data/locations.txt.
 *
 * saveAll() updates existing rows and inserts any new ones - it does not delete
 * rows that are missing from the list, matching how the app only deactivates
 * locations (via the `active` flag) rather than removing them.
 */
public class LocationDAO {

    public List<Location> loadAll() {
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT LOCATION_ID, NAME, STREET, CITY, COUNTRY, POSTAL_CODE, ACTIVE FROM LOCATIONS";

        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                locations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error reading locations table: " + e.getMessage());
        }

        return locations;
    }

    public void saveAll(List<Location> locations) {
        String update = "UPDATE LOCATIONS SET NAME=?, STREET=?, CITY=?, COUNTRY=?, POSTAL_CODE=?, ACTIVE=? WHERE LOCATION_ID=?";
        String insert = "INSERT INTO LOCATIONS (LOCATION_ID, NAME, STREET, CITY, COUNTRY, POSTAL_CODE, ACTIVE) VALUES (?,?,?,?,?,?,?)";

        try {
            Connection conn = getConnection();
            for (Location location : locations) {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    bindUpdate(ps, location);
                    if (ps.executeUpdate() == 0) {
                        try (PreparedStatement ins = conn.prepareStatement(insert)) {
                            bindInsert(ins, location);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error writing locations table: " + e.getMessage());
        }
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    private Location mapRow(ResultSet rs) throws SQLException {
        Address address = new Address(
                rs.getString("STREET"),
                rs.getString("CITY"),
                CountryCode.valueOf(rs.getString("COUNTRY")),
                rs.getString("POSTAL_CODE")
        );
        Location location = new Location(rs.getString("LOCATION_ID"), rs.getString("NAME"), address);
        location.setActive(rs.getBoolean("ACTIVE"));
        return location;
    }

    private void bindUpdate(PreparedStatement ps, Location location) throws SQLException {
        Address a = location.getAddress();
        ps.setString(1, location.getName());
        ps.setString(2, a.getStreet());
        ps.setString(3, a.getCity());
        ps.setString(4, a.getCountry().toString());
        ps.setString(5, a.getPostalCode());
        ps.setBoolean(6, location.isActive());
        ps.setString(7, location.getLocationID());
    }

    private void bindInsert(PreparedStatement ps, Location location) throws SQLException {
        Address a = location.getAddress();
        ps.setString(1, location.getLocationID());
        ps.setString(2, location.getName());
        ps.setString(3, a.getStreet());
        ps.setString(4, a.getCity());
        ps.setString(5, a.getCountry().toString());
        ps.setString(6, a.getPostalCode());
        ps.setBoolean(7, location.isActive());
    }
}
