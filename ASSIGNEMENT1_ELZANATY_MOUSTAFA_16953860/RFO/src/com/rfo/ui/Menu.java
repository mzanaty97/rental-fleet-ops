package com.rfo.ui;

import com.rfo.model.Staff;
import com.rfo.service.DataStore;
import java.util.List;
import java.util.Scanner;

/**
 * Base class for the console menus. Shows the title and the options and keeps asking
 * until the user chooses to exit; each menu fills in its own options and what happens on
 * each choice.
 */
public abstract class Menu {

    protected Scanner scanner;
    protected String exit;
    protected Staff staff;
    protected DataStore dataStore;

    public Menu(Scanner scanner, String exit, Staff staff, DataStore dataStore) {
        this.scanner = scanner;
        this.exit = exit;
        this.staff = staff;
        this.dataStore = dataStore;
    }

    protected abstract String getTitle();

    protected abstract List<String> getOptions();

    protected abstract void handleSelection(String select);

    public void run() {
        boolean active = true;

        while (active) {
            List<String> options = getOptions();
            System.out.println("\n--- " + getTitle() + " ---");
            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + options.get(i));
            }
            int exitOption = options.size() + 1;
            System.out.println(exitOption + ". " + exit);

            System.out.println("Choose an optiom: ");
            String input = scanner.nextLine();

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid option.");
                continue;
            }

            if (choice == exitOption) {
                active = false;
                continue;
            }

            if (choice < 1 || choice > options.size()) {
                System.out.println("Please enter a valid option.");
                continue;
            }

            handleSelection(options.get(choice - 1));
        }
    }
}
