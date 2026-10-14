package com.rfo.gui.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The one-line description shown under each menu option, and which options are
 * not built yet. This is only about how the GUI presents the menus; the menus
 * themselves still come from MenuService.
 */
public final class MenuInfo {

    private static final Map<String, String> DESCRIPTIONS = new HashMap<>();
    private static final Set<String> COMING_SOON = Set.of("Amend Rental", "Changeover", "Print Invoice");

    static {
        // main menu
        put("Admin", "Change your location or password");
        put("Movements", "Rentals, transfers, workshop and movement lookups");
        put("Fleet", "Find available or service due vehicles, update status");
        put("Reports", "Out on rental, location fleet, new vehicles, staff rentals");
        put("Location Manager", "Staff and depot locations");
        // admin
        put("Change Location", "Switch to another depot you have access to");
        put("Change Password", "Set a new password for your account");
        // movements
        put("Revenue Movements", "Checkout, check in and close rentals");
        put("Non-Revenue Movements", "New vehicles, transfers, workshop and defleet");
        put("Movement Details", "Look up a movement or a driver");
        put("Checkout", "Start a rental and hand over a vehicle");
        put("Check In", "Receive a returned rental vehicle");
        put("Close Rental", "Take payment and close a checked-in rental");
        put("New Vehicle", "Add a vehicle to the fleet");
        put("Car Transfer", "Move a vehicle between locations");
        put("Workshop", "Send a vehicle to the workshop");
        put("Defleet", "Remove a vehicle from the fleet");
        put("Query Movement Details", "Look up a movement by its ID");
        put("Query Drivers", "Look up a driver by their ID");
        // fleet
        put("Query Available Vehicles", "Vehicles ready to rent, filter by category");
        put("Query Service Due Vehicles", "Vehicles over their service distance");
        put("Update Vehicle Status", "Mark vehicles available, workshop, cleaning and more");
        // reports
        put("Out on Rental Report", "Open rentals at your location");
        put("Location Fleet", "Vehicles at a chosen location");
        put("New Vehicles", "Near-new vehicles ready to rent");
        put("Staff Rental Reports", "Rentals handled by a staff member");
        // location manager
        put("Manage Staff", "View staff, add staff and change their location access");
        put("Manage Locations", "View, add and close depots");
        put("View Staff", "Everyone and their roles");
        put("Add Staff", "Create a new staff account");
        put("Grant Location Access", "Let a staff member use another location");
        put("Revoke Location Access", "Remove a staff member's location access");
        put("View Locations", "All depots and their vehicle counts");
        put("Add Location", "Create a new depot");
        put("Close Location", "Close a depot and move its vehicles");
    }

    private MenuInfo() {
    }

    private static void put(String label, String description) {
        DESCRIPTIONS.put(label, description);
    }

    public static boolean isComingSoon(String label) {
        return COMING_SOON.contains(label);
    }

    public static String describe(String label) {
        if (isComingSoon(label)) {
            return "Not available yet";
        }
        return DESCRIPTIONS.getOrDefault(label, "");
    }
}
