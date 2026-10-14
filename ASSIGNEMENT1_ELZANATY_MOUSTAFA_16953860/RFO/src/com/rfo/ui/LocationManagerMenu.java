package com.rfo.ui;

import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.List;
import java.util.Scanner;

public class LocationManagerMenu extends Menu {

    private StaffService service;

    public LocationManagerMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.service = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Location Manager";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Manage Staff", "Manage Locations");
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Manage Staff":
                new ManageStaffMenu(scanner, staff, dataStore).run();
                break;
            case "Manage Locations":
                new ManageLocationsMenu(scanner, staff, dataStore).run();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }
}
