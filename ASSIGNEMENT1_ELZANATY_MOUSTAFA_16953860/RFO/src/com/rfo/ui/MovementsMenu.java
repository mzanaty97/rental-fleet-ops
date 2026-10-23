package com.rfo.ui;

import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The console movements menu. Opens the revenue, non-revenue and movement details menus.
 */
public class MovementsMenu extends Menu {

    private StaffService service;

    public MovementsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
        this.service = new StaffService(dataStore);
    }

    @Override
    protected String getTitle() {
        return "Movements";
    }

    @Override
    protected List<String> getOptions() {
        List<String> options = new ArrayList<>();

        if (!service.getRevenueMovementsOptions(staff.getRole()).isEmpty()) {
            options.add("Revenue Movements");
        }
        if (!service.getNonRevenueMovementsOptions(staff.getRole()).isEmpty()) {
            options.add("Non-Revenue Movements");
        }
        if (!service.getMovementDetailsOptions(staff.getRole()).isEmpty()) {
            options.add("Movement Details");
        }

        return options;
    }

    @Override
    protected void handleSelection(String select) {
        switch (select) {
            case "Revenue Movements":
                new RevenueMovementsMenu(scanner, staff, dataStore).run();
                break;
            case "Non-Revenue Movements":
                new NonRevenueMovementsMenu(scanner, staff, dataStore).run();
                break;
            case "Movement Details":
                new MovementDetailsMenu(scanner, staff, dataStore).run();
                break;
            default:
                System.out.println(select + " not implemented yet.");
                break;
        }
    }
}
