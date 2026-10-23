package com.rfo.io;

import com.rfo.db.DBConnection;
import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO replacement for DriverFileHandler. Same public contract as the file-based
 * version, now backed by the DRIVERS table.
 */
public class DriverDAO {

    public List<Driver> loadAll() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT DRIVER_ID, FNAME, LNAME, LICENSE_NUMBER, EMAIL, PHONE_NUMBER, STREET, CITY, COUNTRY, POSTAL_CODE "
                + "FROM DRIVERS ORDER BY DRIVER_ID ASC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                drivers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error reading drivers table: " + e.getMessage());
        }

        if (!drivers.isEmpty()) {
            int latestID = drivers.get(drivers.size() - 1).getDriverID();
            Driver.setLatestDriverID(latestID + 1);
        }

        return drivers;
    }

    public void saveAll(List<Driver> drivers) {
        String update = "UPDATE DRIVERS SET FNAME=?, LNAME=?, LICENSE_NUMBER=?, EMAIL=?, PHONE_NUMBER=?, STREET=?, CITY=?, COUNTRY=?, POSTAL_CODE=? WHERE DRIVER_ID=?";
        String insert = "INSERT INTO DRIVERS (DRIVER_ID, FNAME, LNAME, LICENSE_NUMBER, EMAIL, PHONE_NUMBER, STREET, CITY, COUNTRY, POSTAL_CODE) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try {
            Connection conn = getConnection();
            for (Driver driver : drivers) {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    bindUpdate(ps, driver);
                    if (ps.executeUpdate() == 0) {
                        try (PreparedStatement ins = conn.prepareStatement(insert)) {
                            bindInsert(ins, driver);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error writing drivers table: " + e.getMessage());
        }
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    private Driver mapRow(ResultSet rs) throws SQLException {
        Address address = new Address(
                rs.getString("STREET"),
                rs.getString("CITY"),
                CountryCode.valueOf(rs.getString("COUNTRY")),
                rs.getString("POSTAL_CODE")
        );
        return new Driver(rs.getInt("DRIVER_ID"), rs.getString("FNAME"), rs.getString("LNAME"),
                rs.getString("LICENSE_NUMBER"), rs.getString("EMAIL"), rs.getString("PHONE_NUMBER"), address);
    }

    private void bindUpdate(PreparedStatement ps, Driver driver) throws SQLException {
        Address a = driver.getAddress();
        ps.setString(1, driver.getFName());
        ps.setString(2, driver.getLName());
        ps.setString(3, driver.getLicenseNumber());
        ps.setString(4, driver.getEmail());
        ps.setString(5, driver.getPhoneNumber());
        ps.setString(6, a.getStreet());
        ps.setString(7, a.getCity());
        ps.setString(8, a.getCountry().toString());
        ps.setString(9, a.getPostalCode());
        ps.setInt(10, driver.getDriverID());
    }

    private void bindInsert(PreparedStatement ps, Driver driver) throws SQLException {
        Address a = driver.getAddress();
        ps.setInt(1, driver.getDriverID());
        ps.setString(2, driver.getFName());
        ps.setString(3, driver.getLName());
        ps.setString(4, driver.getLicenseNumber());
        ps.setString(5, driver.getEmail());
        ps.setString(6, driver.getPhoneNumber());
        ps.setString(7, a.getStreet());
        ps.setString(8, a.getCity());
        ps.setString(9, a.getCountry().toString());
        ps.setString(10, a.getPostalCode());
    }
}
