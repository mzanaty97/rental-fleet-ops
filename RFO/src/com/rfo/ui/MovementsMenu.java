package com.rfo.ui;

import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import java.util.List;
import java.util.Scanner;

public class MovementsMenu extends Menu {

    public MovementsMenu(Scanner scanner, Staff staff, DataStore dataStore) {
        super(scanner, "Previous Menu", staff, dataStore);
    }

    @Override
    protected String getTitle() {
        return "Movements";
    }

    @Override
    protected List<String> getOptions() {
        return List.of("Revenue Movements", "Non-Revenue Movements", "Movement Details");
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
