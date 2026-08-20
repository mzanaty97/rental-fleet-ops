package com.rfo.ui;

import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.LocationService;
import java.util.List;
import java.util.Scanner;

public class ManageLocationsMenu extends Menu {

    private LocationService service;

    public ManageLocationsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.service = new LocationService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Manage Locations";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("View Locations", "Add Location", "Close Location");
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "View Locations":
                viewLocations();
                break;
            case "Add Location":
                addLocation();
                break;
            case "Close Location":
                closeLocation();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }

    private void viewLocations() {
        List<Location> locations = dataStore.getLocations();

        if (locations.isEmpty()) {
            System.out.println("No locations on record.");
            return;
        }

        System.out.println("\n--- Locations ---");
        System.out.println("Location ID\t|\tName\t|\tActive\t|\tAddress");
        for (Location l : locations) {
            System.out.println(l.getLocationID() + "\t|\t" + l.getName() + "\t|\t"
                    + l.isActive() + "\t|\t" + l.getAddress().getFullAddress());
        }
    }

    private void addLocation() {
        System.out.print("Location ID: ");
        String locationID = scanner.nextLine();

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Street: ");
        String street = scanner.nextLine();

        System.out.print("City: ");
        String city = scanner.nextLine();

        System.out.print("Country code (e.g. NZ): ");
        String countryInput = scanner.nextLine().trim().toUpperCase();
        CountryCode country;
        try {
            country = CountryCode.valueOf(countryInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid country code.");
            return;
        }

        System.out.print("Postal Code: ");
        String postalCode = scanner.nextLine();

        Address address = new Address(street, city, country, postalCode);
        Location location = service.addLocation(locationID, name, address);

        if (location == null) {
            System.out.println("Failed to add location. See message above.");
            return;
        }

        System.out.println("Location added successfully: " + location.getLocationID());
    }

    private void closeLocation() {
        System.out.print("Location ID to close: ");
        String locationID = scanner.nextLine();

        Location location = dataStore.getBy(dataStore.getLocations(), l -> l.getLocationID().equals(locationID));

        if (location == null) {
            System.out.println("Location not found.");
            return;
        }

        System.out.print("Destination location ID for any vehicles there: ");
        String newLocationID = scanner.nextLine();

        boolean closed = service.closeLocation(location, newLocationID, staff.getStaffID());

        if (closed) {
            System.out.println("Location closed: " + location.getLocationID());
        }
    }
}
