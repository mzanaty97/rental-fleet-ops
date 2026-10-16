package com.rfo.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Helper methods shared by the console movement menus, such as asking for driver IDs and
 * for a date and time.
 */
public class MovementMenuUtils {

    public static List<Integer> promptForDriverIDs(Scanner scanner) {
        List<Integer> driverIDs = new ArrayList<>();
        System.out.println("Enter driver IDs (one per line, blank line to finish):");
        while (true) {
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                break;
            }
            try {
                driverIDs.add(Integer.parseInt(input));
            } catch (NumberFormatException e) {
                System.out.println("Invalid driver ID, skipped.");
            }
        }
        return driverIDs;
    }

    public static LocalDateTime promptForDateTime(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        try {
            return LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date/time format.");
            return null;
        }
    }

    public static <T extends Enum<T>> T promptForEnum(Scanner scanner, String label, Class<T> enumType) {
        T[] values = enumType.getEnumConstants();
        System.out.println(label + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }
        System.out.print("Choose: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > values.length) {
                System.out.println("Invalid choice.");
                return null;
            }
            return values[choice - 1];
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return null;
        }
    }
}
