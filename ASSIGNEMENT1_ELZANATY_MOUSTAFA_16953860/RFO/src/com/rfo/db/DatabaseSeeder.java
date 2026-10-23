package com.rfo.db;

import com.rfo.io.DriverDAO;
import com.rfo.io.DriverFileHandler;
import com.rfo.io.LocationDAO;
import com.rfo.io.LocationFileHandler;
import com.rfo.io.MovementDAO;
import com.rfo.io.MovementFileHandler;
import com.rfo.io.StaffDAO;
import com.rfo.io.StaffFileHandler;
import com.rfo.io.VehicleDAO;
import com.rfo.io.VehicleFileHandler;
import com.rfo.model.Driver;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Generated with assistance from Claude. I designed the structure and logic
// (following my Part 1 design); Claude wrote the repeated methods and boilerplate.
/**
 * One-time migration of the Part 1 data/*.txt files into the Derby database.
 *
 * Runs only when the database is empty (no locations and no staff), so it is
 * safe to call on every start-up: once the tables hold data it does nothing.
 * The old FileHandlers are used purely as readers here; the DAOs do the
 * writing.
 */
public class DatabaseSeeder {

    private static final String STAFF_FILE = "data/staff.txt";

    public void seedIfEmpty() {
        if (!isDatabaseEmpty()) {
            return;
        }

        // read everything from the old text files (same order as DataStore used)
        List<Location> locations = new LocationFileHandler().loadAll();
        List<Driver> drivers = new DriverFileHandler().loadAll();
        List<Vehicle> vehicles = new VehicleFileHandler().loadAll(locations);
        List<Staff> staff = new StaffFileHandler().loadAll(locations);
        List<Movement> movements = new MovementFileHandler().loadAll(vehicles, locations);

        // write in foreign-key order: parents before the rows that reference them
        new LocationDAO().saveAll(locations);
        new DriverDAO().saveAll(drivers);
        new VehicleDAO().saveAll(vehicles);
        seedStaff(staff);
        new MovementDAO().saveAll(movements);

        System.out.println("Database seeded from data/*.txt files.");
    }

    // Staff objects never hold passwords, so they are read straight from the file here
    private void seedStaff(List<Staff> staff) {
        Map<String, String> passwords = readPasswords();
        StaffDAO staffDAO = new StaffDAO();
        for (Staff member : staff) {
            staffDAO.addNewStaffLine(member, passwords.getOrDefault(member.getStaffID(), ""));
        }
    }

    private Map<String, String> readPasswords() {
        Map<String, String> passwords = new HashMap<>();
        File file = new File(STAFF_FILE);
        if (!file.exists()) {
            return passwords;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                String[] fields = ln.split("\\|", -1);
                if (fields.length > 1) {
                    passwords.put(fields[0], fields[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }
        return passwords;
    }

    private boolean isDatabaseEmpty() {
        return countRows("LOCATIONS") == 0 && countRows("STAFF") == 0;
    }

    private int countRows(String table) {
        try (Statement st = DBConnection.getInstance().getConnection().createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error counting rows in " + table + ": " + e.getMessage());
            return -1; // unknown state: treat as not empty so nothing gets overwritten
        }
    }
}
