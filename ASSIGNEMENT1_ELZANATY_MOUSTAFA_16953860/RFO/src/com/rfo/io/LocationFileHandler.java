package com.rfo.io;

import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Location;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocationFileHandler {

    private static final String FILE_PATH = "data/locations.txt";
    // locations saved in "locationID|name|street|city|country|postalCode" format

    public List<Location> loadAll() {
        List<Location> locations = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return locations;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                locations.add(parseLine(ln));
            }
        } catch (IOException e) {
            System.out.println("Error reading locations file: " + e.getMessage());
        }

        return locations;
    }

    public void saveAll(List<Location> locations) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Location location : locations) {
                writer.write(toLine(location));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing locations into file: " + e.getMessage());
        }
    }

    private Location parseLine(String ln) {
        String[] fields = ln.split("\\|", -1);

        String locationID = fields[0];
        String name = fields[1];
        String street = fields[2];
        String city = fields[3];
        CountryCode country = CountryCode.valueOf(fields[4]);
        String postalCode = fields[5];
        boolean active = fields.length > 6 ? Boolean.parseBoolean(fields[6]) : true;

        Address address = new Address(street, city, country, postalCode);
        Location location = new Location(locationID, name, address);
        location.setActive(active);
        return location;
    }

    private String toLine(Location location) {
        Address address = location.getAddress();
        return String.join("|",
                location.getLocationID(),
                location.getName(),
                address.getStreet(),
                address.getCity(),
                address.getCountry().toString(),
                address.getPostalCode(),
                String.valueOf(location.isActive())
        );
    }
}
