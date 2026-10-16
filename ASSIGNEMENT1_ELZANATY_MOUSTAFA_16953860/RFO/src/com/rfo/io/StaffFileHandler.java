package com.rfo.io;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 1 text-file storage for staff and their passwords (data/staff.txt). In Part 2 it
 * is only used to fill an empty database the first time the app runs; StaffDAO does the
 * saving now.
 */
public class StaffFileHandler {

    private static final String FILE_PATH = "data/staff.txt";
    // staff saved in "staffID|password|fName|lName|role|currentLocationID|accessibleLocationID1,accessibleLocationID2,..." format
    // password is never loaded into a Staff object - only read directly here for authentication

    public List<Staff> loadAll(List<Location> allLocations) {
        List<Staff> staffList = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return staffList;
        }

        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                staffList.add(parseLine(ln, locationsById));
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }

        return staffList;
    }

    public void saveAll(List<Staff> staffList) {
        Map<String, String> existingPasswords = loadPasswordsByStaffID();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Staff staff : staffList) {
                String password = existingPasswords.getOrDefault(staff.getStaffID(), "");
                writer.write(toLine(staff, password));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing staff file: " + e.getMessage());
        }
    }

    // Reads the file directly every call - password is never cached in memory
    public boolean authenticate(String staffID, String password) {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                String[] fields = ln.split("\\|", -1);
                if (fields[0].equals(staffID)) {
                    return fields[1].equals(password);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }

        System.out.println("Staff not found: " + staffID);
        return false;
    }

    public void addNewStaffLine(Staff staff, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) { // append mode
            writer.write(toLine(staff, password));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error adding staff: " + e.getMessage());
        }
    }

    private Map<String, String> loadPasswordsByStaffID() {
        Map<String, String> passwords = new HashMap<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return passwords;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                String[] fields = ln.split("\\|", -1);
                passwords.put(fields[0], fields[1]);
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }

        return passwords;
    }

    private Map<String, Location> buildLocationLookup(List<Location> allLocations) {
        Map<String, Location> locationsById = new HashMap<>();
        for (Location location : allLocations) {
            locationsById.put(location.getLocationID(), location);
        }
        return locationsById;
    }

    private Staff parseLine(String ln, Map<String, Location> locationsById) {
        String[] fields = ln.split("\\|", -1);

        String staffID = fields[0];
        // fields[1] = password - intentionally skipped, never loaded into Staff
        String fName = fields[2];
        String lName = fields[3];
        StaffRole role = StaffRole.valueOf(fields[4]);
        String currentLocationID = fields[5];

        Location currentLocation = locationsById.get(currentLocationID);

        List<Location> accessibleLocations = new ArrayList<>();
        if (fields.length > 6 && !fields[6].isEmpty()) {
            for (String id : fields[6].split(",")) {
                Location loc = locationsById.get(id);
                if (loc != null) {
                    accessibleLocations.add(loc);
                }
            }
        }

        return new Staff(staffID, fName, lName, role, accessibleLocations, currentLocation);
    }

    private String toLine(Staff staff, String password) {
        StringBuilder accessibleIDs = new StringBuilder();
        List<Location> accessibleLocations = staff.getAccessibleLocations();
        for (int i = 0; i < accessibleLocations.size(); i++) {
            accessibleIDs.append(accessibleLocations.get(i).getLocationID());
            if (i < accessibleLocations.size() - 1) {
                accessibleIDs.append(",");
            }
        }

        String currentLocationID = staff.getCurrentLocation() != null
                ? staff.getCurrentLocation().getLocationID()
                : "";

        return String.join("|",
                staff.getStaffID(),
                password,
                staff.getFName(),
                staff.getLName(),
                staff.getRole().toString(),
                currentLocationID,
                accessibleIDs.toString()
        );
    }

    public boolean updatePassword(String staffID, String newPassword) {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return false;
        }

        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                String[] fields = ln.split("\\|", -1);
                if (fields[0].equals(staffID)) {
                    fields[1] = newPassword;
                    ln = String.join("|", fields);
                    found = true;
                }
                lines.add(ln);
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
            return false;
        }

        if (!found) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String ln : lines) {
                writer.write(ln);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing staff file: " + e.getMessage());
            return false;
        }

        return true;
    }
}
