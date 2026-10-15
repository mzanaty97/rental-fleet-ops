package com.rfo.service;

import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Driver;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Business rules for creating a driver (a customer who rents a vehicle). The
 * checks return one message per field so the screen can show each problem next
 * to its own box. Nothing is saved unless every check passes.
 */
public class DriverService {

    // the keys of the fields, used by the screen to show each error in the right place
    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String LICENCE = "licence";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String STREET = "street";
    public static final String CITY = "city";
    public static final String POSTAL = "postal";

    // driver IDs from here up are placeholders from the seed data, not real customers
    private static final int PLACEHOLDER_FROM = 1_000_000;

    private final DataStore dataStore;

    public DriverService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Checks the typed details. Returns a message for every field that is wrong
     * (empty if all are fine).
     */
    public Map<String, String> check(String first, String last, String licence, String email, String phone,
            String street, String city, String postal) {
        Map<String, String> errors = new LinkedHashMap<>();
        String f = clean(first), l = clean(last), lic = clean(licence).toUpperCase(), e = clean(email),
                p = clean(phone), s = clean(street), c = clean(city), z = clean(postal);

        checkName(errors, FIRST, "First name", f);
        checkName(errors, LAST, "Last name", l);

        if (!lic.matches("[A-Z0-9]{5,15}")) {
            errors.put(LICENCE, "5 to 15 letters and digits, e.g. AB123456.");
        } else {
            Driver same = dataStore.getBy(dataStore.getDrivers(), d -> d.getLicenseNumber().equalsIgnoreCase(lic));
            if (same != null) {
                errors.put(LICENCE, "Already used by driver " + same.getDriverID() + " (" + same.getName() + ").");
            }
        }

        if (e.length() > 100 || !e.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            errors.put(EMAIL, "Enter a valid email, e.g. sam@example.com.");
        }

        int digits = p.replaceAll("\\D", "").length();
        if (!p.matches("[0-9+()\\- ]+") || digits < 7 || digits > 15 || p.length() > 30) {
            errors.put(PHONE, "7 to 15 digits, e.g. 021 123 4567.");
        }

        if (s.isEmpty() || s.length() > 100) {
            errors.put(STREET, "Enter the street address.");
        }
        if (c.isEmpty() || c.length() > 50) {
            errors.put(CITY, "Enter the city.");
        }
        if (!z.matches("[A-Za-z0-9 \\-]{3,10}")) {
            errors.put(POSTAL, "3 to 10 letters or digits.");
        }
        return errors;
    }

    /**
     * Creates the driver, gives it the next driver ID and saves it. Throws with
     * the first problem if any detail is wrong. Driver ID 0 is never used.
     */
    public Driver createDriver(String first, String last, String licence, String email, String phone,
            String street, String city, CountryCode country, String postal) {
        Map<String, String> errors = check(first, last, licence, email, phone, street, city, postal);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.values().iterator().next());
        }
        if (country == null) {
            throw new IllegalArgumentException("Choose the country.");
        }

        // continue from the highest real driver ID; the very large ID of the seed placeholder is ignored
        int next = 1;
        for (Driver d : dataStore.getDrivers()) {
            if (d.getDriverID() < PLACEHOLDER_FROM) {
                next = Math.max(next, d.getDriverID() + 1);
            }
        }
        Driver.setLatestDriverID(next);

        Driver driver = new Driver(clean(first), clean(last), clean(licence).toUpperCase(), clean(email), clean(phone),
                new Address(clean(street), clean(city), country, clean(postal)));
        dataStore.getDrivers().add(driver);
        dataStore.saveDrivers();
        return driver;
    }

    private void checkName(Map<String, String> errors, String key, String label, String value) {
        if (value.isEmpty() || value.length() > 40 || !value.matches("[\\p{L}][\\p{L} '.\\-]*")) {
            errors.put(key, "Letters only, up to 40.");
        }
    }

    private String clean(String text) {
        return text == null ? "" : text.trim().replaceAll("\\s+", " ");
    }
}
