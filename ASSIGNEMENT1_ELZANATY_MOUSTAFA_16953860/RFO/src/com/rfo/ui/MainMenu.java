package com.rfo.ui;

import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.List;
import java.util.Scanner;

/**
 * The console main menu. Opens the admin, movements, fleet, reports and location manager
 * menus.
 */
public class MainMenu extends Menu {

    private StaffService service;

    public MainMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Logout", staff, dataStore);
        this.service = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Main Menu.";
    }

    @Override
    protected List<String> getOptions() {
        return service.getMenuOptions(staff.getRole());
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Admin":
                new AdminMenu(scanner, staff, dataStore).run();
                break;
            case "Movements":
                new MovementsMenu(scanner, staff, dataStore).run();
                break;
            case "Fleet":
                new FleetMenu(scanner, staff, dataStore).run();
                break;
            case "Reports":
                new ReportsMenu(scanner, staff, dataStore).run();
                break;
            case "Location Manager":
                new LocationManagerMenu(scanner, staff, dataStore).run();
                break;
            default:
                System.out.println(select + " not implemented yet.");
        }
    }

}
