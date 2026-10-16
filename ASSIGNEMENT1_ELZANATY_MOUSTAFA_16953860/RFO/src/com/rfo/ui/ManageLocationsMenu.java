package com.rfo.ui;

import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.LocationService;
import java.util.List;
import java.util.Scanner;

// Generated with assistance from Claude (modified existing class): I wrote this menu in Part 1; Claude helped me change it to call the service classes and show their error messages.
/**
 * The console menu for viewing, adding and closing locations.
 */
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
        System.out.println(String.format("%-12s | %-30s | %-8s | %s",
                "Location ID", "Name", "Active", "Address"));
        for (Location l : locations) {
            System.out.println(String.format("%-12s | %-30s | %-8s | %s",
                    l.getLocationID(), l.getName(), l.isActive(), l.getAddress().getFullAddress()));
        }
    }

    private void addLocation() {
        System.out.print("Location ID: ");
        String locationID = scanner.nextLine().trim().toUpperCase();

        System.out.print("Name: ");
        String name = scanner.nextLine().trim().toUpperCase();

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

        try {
            Location location = service.addLocation(locationID, name, address);
            System.out.println("Location added successfully: " + location.getLocationID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void closeLocation() {
        System.out.print("Location ID to close: ");
        String locationID = scanner.nextLine().trim().toUpperCase();

        Location location = service.findLocation(locationID);

        if (location == null) {
            System.out.println("Location not found.");
            return;
        }

        System.out.print("Destination location ID for any vehicles there: ");
        String newLocationID = scanner.nextLine().trim().toUpperCase();

        try {
            service.closeLocation(location, newLocationID, staff.getStaffID());
            System.out.println("Location closed: " + location.getLocationID());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
