package com.rfo.io;

import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Driver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 1 text-file storage for drivers (data/drivers.txt). In Part 2 it is only used to
 * fill an empty database the first time the app runs; DriverDAO does the saving now.
 */
public class DriverFileHandler {

    private static final String FILE_PATH = "data/drivers.txt";
    // drivers saved in "driverID|fName|lName|licenseNumber|email|phoneNumber|street|city|country|postalCode" format
    // file is always kept in ascending driverID order

    public List<Driver> loadAll() {
        List<Driver> drivers = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return drivers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                Driver driver = parseLine(ln);
                drivers.add(driver);
            }
        } catch (IOException e) {
            System.out.println("Error reading drivers file: " + e.getMessage());
        }

        if (!drivers.isEmpty()) {
            int latestID = drivers.get(drivers.size() - 1).getDriverID();
            Driver.setLatestDriverID(latestID + 1);
        }

        return drivers;
    }

    public void saveAll(List<Driver> drivers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Driver driver : drivers) {
                writer.write(toLine(driver));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing drivers file: " + e.getMessage());
        }
    }

    private Driver parseLine(String ln) {
        String[] fields = ln.split("\\|", -1);

        int driverID = Integer.parseInt(fields[0]);
        String fName = fields[1];
        String lName = fields[2];
        String licenseNumber = fields[3];
        String email = fields[4];
        String phoneNumber = fields[5];
        String street = fields[6];
        String city = fields[7];
        CountryCode country = CountryCode.valueOf(fields[8]);
        String postalCode = fields[9];

        Address address = new Address(street, city, country, postalCode);
        return new Driver(driverID, fName, lName, licenseNumber, email, phoneNumber, address);
    }

    private String toLine(Driver driver) {
        Address address = driver.getAddress();
        return String.join("|",
                String.valueOf(driver.getDriverID()),
                driver.getFName(),
                driver.getLName(),
                driver.getLicenseNumber(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                address.getStreet(),
                address.getCity(),
                address.getCountry().toString(),
                address.getPostalCode()
        );
    }
}
