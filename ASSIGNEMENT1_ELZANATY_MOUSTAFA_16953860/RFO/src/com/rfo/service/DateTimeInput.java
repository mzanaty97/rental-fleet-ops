package com.rfo.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Turns typed dates and times into real ones. A date can be typed in many ways:
 * 181026, 18102026, 1810261000, 181020261000, 18.10.26 10:00, 18/10/2026 and so
 * on (day, month, year, then the time if wanted). Two-digit years mean 20xx.
 * The format yyyy-MM-dd is accepted too. Every method throws an
 * IllegalArgumentException with a message the user can read.
 */
public final class DateTimeInput {

    /**
     * A typed date, and the time as well when one was typed with it (otherwise
     * null).
     */
    public static final class Result {

        public final LocalDate date;
        public final LocalTime time;

        private Result(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }
    }

    private static final String HELP = "Type the date as day, month, year, then the time if you like, "
            + "for example 18102026 1000 or 18.10.26 10:00.";

    private DateTimeInput() {
    }

    public static Result parseDateTime(String text) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Enter the return date.");
        }
        if (t.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException(HELP);
        }
        if (t.matches("\\d{4}-\\d{1,2}-\\d{1,2}.*")) {
            String[] parts = t.split("[^0-9]+");
            return build(parts[2], parts[1], parts[0], parts, 3);
        }
        String[] tokens = t.split("[^0-9]+");
        if (tokens.length >= 3 && tokens[0].length() <= 2 && tokens[1].length() <= 2 && !tokens[0].isEmpty()) {
            return build(tokens[0], tokens[1], tokens[2], tokens, 3);
        }
        String d = t.replaceAll("\\D", "");
        switch (d.length()) {
            case 6:
                return build(d.substring(0, 2), d.substring(2, 4), d.substring(4, 6), new String[0], 0);
            case 8:
                return build(d.substring(0, 2), d.substring(2, 4), d.substring(4, 8), new String[0], 0);
            case 10:
                return build(d.substring(0, 2), d.substring(2, 4), d.substring(4, 6),
                        new String[]{d.substring(6, 10)}, 0);
            case 12:
                return build(d.substring(0, 2), d.substring(2, 4), d.substring(4, 8),
                        new String[]{d.substring(8, 12)}, 0);
            default:
                throw new IllegalArgumentException(HELP);
        }
    }

    private static Result build(String day, String month, String year, String[] rest, int from) {
        LocalDate date;
        try {
            int y = Integer.parseInt(year);
            if (year.length() == 2) {
                y += 2000;
            } else if (year.length() != 4) {
                throw new IllegalArgumentException(HELP);
            }
            date = LocalDate.of(y, Integer.parseInt(month), Integer.parseInt(day));
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException("That is not a real date. " + HELP);
        }
        LocalTime time = null;
        if (rest.length > from) {
            StringBuilder sb = new StringBuilder();
            for (int i = from; i < rest.length; i++) {
                sb.append(i > from ? ":" : "").append(rest[i]);
            }
            time = parseTime(sb.toString());
        }
        return new Result(date, time);
    }

    /**
     * Accepts 1000, 10:00, 10.00, 930, 9:30 or just 10 (meaning 10:00).
     */
    public static LocalTime parseTime(String text) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Enter the return time.");
        }
        String message = "That is not a real time. Type it like 1000 or 10:00.";
        if (!t.matches("[0-9:. ]+")) {
            throw new IllegalArgumentException(message);
        }
        String[] parts = t.split("[^0-9]+");
        int hour, minute;
        try {
            if (parts.length == 2) {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            } else if (parts.length == 1) {
                String d = parts[0];
                if (d.length() <= 2) {
                    hour = Integer.parseInt(d);
                    minute = 0;
                } else if (d.length() == 3 || d.length() == 4) {
                    hour = Integer.parseInt(d.substring(0, d.length() - 2));
                    minute = Integer.parseInt(d.substring(d.length() - 2));
                } else {
                    throw new IllegalArgumentException(message);
                }
            } else {
                throw new IllegalArgumentException(message);
            }
            return LocalTime.of(hour, minute);
        } catch (DateTimeException | NumberFormatException e) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * True when the date (and the time, if given) has already gone by. A date
     * of today with no time is not past.
     */
    public static boolean isPast(LocalDate date, LocalTime time) {
        LocalDateTime now = LocalDateTime.now();
        if (date.isBefore(now.toLocalDate())) {
            return true;
        }
        return time != null && !LocalDateTime.of(date, time).isAfter(now);
    }
}
