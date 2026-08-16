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

public class StaffFileHandler {

    private static final String FILE_PATH = "data/staff.txt";
    // staff saved in "staffID|password|fName|lName|role|currentLocationID|accessibleLocationID1,accessibleLocationID2,..." format
    // password is never loaded into a Staff object — only read directly here for authentication

    private List<String[]> loadRawLines() {
        List<String[]> lines = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                lines.add(ln.split("\\|"));
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }

        return lines;
    }

    private void saveAllRaw(List<String[]> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String[] fields : lines) {
                writer.write(String.join("|", fields));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing staff file: " + e.getMessage());
        }
    }

    public boolean authenticate(String staffID, String password) {
        for (String[] fields : loadRawLines()) {
            if (fields[0].equals(staffID)) {
                return fields[1].equals(password);
            }
        }
        System.out.println("Staff not found: " + staffID);
        return false;
    }

    public Staff getBy(String staffID, List<Location> allLocations) {
        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        for (String[] fields : loadRawLines()) {
            if (fields[0].equals(staffID)) {
                return parseFields(fields, locationsById);
            }
        }

        System.out.println("Staff not found: " + staffID);
        return null;
    }

    public List<Staff> search(String fName, String lName, List<Location> allLocations) {
        List<Staff> results = new ArrayList<>();
        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        for (String[] fields : loadRawLines()) {
            if (fields[2].equalsIgnoreCase(fName) && fields[3].equalsIgnoreCase(lName)) {
                results.add(parseFields(fields, locationsById));
            }
        }

        return results;
    }

    public Staff addStaff(String staffID, String password, String fName, String lName, StaffRole role) {
        List<String[]> lines = loadRawLines();

        for (String[] fields : lines) {
            if (fields[0].equals(staffID)) {
                System.out.println("Duplicate staffID: " + staffID + " already exists.");
                return null;
            }
        }

        Staff newStaff = new Staff(staffID, fName, lName, role);

        String[] newLine = {
            staffID, password, fName, lName, role.toString(), "", ""
        };
        lines.add(newLine);
        saveAllRaw(lines);

        return newStaff;
    }

    private Map<String, Location> buildLocationLookup(List<Location> allLocations) {
        Map<String, Location> locationsById = new HashMap<>();
        for (Location location : allLocations) {
            locationsById.put(location.getLocationID(), location);
        }
        return locationsById;
    }

    private Staff parseFields(String[] fields, Map<String, Location> locationsById) {
        String staffID = fields[0];
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
}
