package com.rfo.gui.controller;

import com.rfo.gui.view.CheckoutView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Location;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.LocationService;
import com.rfo.service.DateTimeInput;
import com.rfo.service.ModelListener;
import com.rfo.service.MovementService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Checkout. It checks each part of the form with the
 * MovementService, shows the reasons next to the field they belong to, and
 * starts the rental only when the customer has been through the confirm card.
 * The staff member's current location is read fresh each time, so Change
 * Location in another tab is respected.
 */
public class CheckoutController implements ActionListener, ModelListener {

    private final CheckoutView view;
    private final MovementService movementService;
    private final LocationService locationService;
    private final Staff staff;
    private final MessageSink console;

    public CheckoutController(CheckoutView view, MovementService movementService,
            LocationService locationService, Staff staff, MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.locationService = locationService;
        this.staff = staff;
        this.console = console;
        view.addController(this);
        refreshLocations();
    }

    @Override
    public void onModelChanged() {
        refreshLocations();
    }

    private void refreshLocations() {
        view.setFromName(staff.getCurrentLocation() == null ? "" : staff.getCurrentLocation().getName());
        view.setReturnLocations(locationService.getActiveLocations(), staff.getCurrentLocation());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case CheckoutView.PLATE:
                checkPlate();
                break;
            case CheckoutView.ADD_DRIVER:
                addDriver();
                break;
            case CheckoutView.NEW_DRIVER:
                newDriver();
                break;
            case CheckoutView.REVIEW:
                review();
                break;
            case CheckoutView.CONFIRM:
                confirm();
                break;
            case CheckoutView.QUOTE:
                updateQuote();
                break;
            case CheckoutView.LIST_AVAILABLE:
                listAvailable();
                break;
            case CheckoutView.RETURN_DATE:
                readReturnDate();
                break;
            case CheckoutView.RETURN_TIME:
                readReturnTime();
                break;
            default:
                console.info("Checkout cancelled.");
                view.reset();
                view.goToMenu();
        }
    }

    /**
     * Works out the price for every insurance tier, quietly: nothing is shown
     * if the plate or return time is not ready.
     */
    private void updateQuote() {
        java.util.Map<com.rfo.model.Insurance, RentalMovement.Quote> byTier = null;
        try {
            Vehicle v = movementService.findVehicleForCheckout(view.getPlate(), staff.getCurrentLocation());
            LocalDateTime when = LocalDateTime.of(LocalDate.parse(view.getReturnDateText()),
                    LocalTime.parse(view.getReturnTimeText()));
            movementService.validateExpectedReturn(when);
            byTier = new java.util.EnumMap<>(com.rfo.model.Insurance.class);
            for (com.rfo.model.Insurance tier : com.rfo.model.Insurance.values()) {
                byTier.put(tier, movementService.quoteRental(v, when, view.isRsa(), tier));
            }
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            byTier = null;
        }
        view.setQuotes(byTier);
    }

    /**
     * Returns the vehicle if the plate is fine, otherwise shows why not and
     * returns null.
     */
    private Vehicle checkPlate() {
        view.setFromName(staff.getCurrentLocation() == null ? "" : staff.getCurrentLocation().getName());
        try {
            Vehicle v = movementService.findVehicleForCheckout(view.getPlate(), staff.getCurrentLocation());
            view.setVehicleOk(v.getMake() + " " + v.getModel(), v.getCategory() + " · "
                    + String.format("%,d", v.getCurrentOdometer()) + " km · at your location, available");
            return v;
        } catch (IllegalArgumentException ex) {
            view.setVehicleError(ex.getMessage());
            return null;
        }
    }

    /**
     * Enter on an empty plate box: lists what can be rented from the staff
     * member's current location.
     */
    private void listAvailable() {
        Location here = staff.getCurrentLocation();
        if (here == null) {
            console.error("You have no current location set. Cannot check out a vehicle.");
            return;
        }
        List<Vehicle> vehicles = movementService.getVehiclesForCheckout(here);
        if (vehicles.isEmpty()) {
            console.warn("No vehicles are available to rent at " + here.getName() + ".");
            return;
        }
        List<String[]> rows = new ArrayList<>();
        for (Vehicle v : vehicles) {
            rows.add(new String[]{v.getPlateNumber(), v.getMake() + " " + v.getModel(), v.getCategory().toString(),
                String.format("%,d", v.getCurrentOdometer()) + " km"});
        }
        view.showAvailableVehicles(rows);
    }

    /**
     * Turns the typed return date (and time, if it was typed with it) into the
     * boxes' format.
     */
    private void readReturnDate() {
        try {
            DateTimeInput.Result typed = DateTimeInput.parseDateTime(view.getReturnDateText());
            if (typed.date.isBefore(LocalDate.now())) {
                view.setReturnDate(null);
                view.setReturnError("Return date is in the past.");
                console.error("The return date " + typed.date + " is in the past. Enter today's date or a later one.");
                return;
            }
            view.setReturnDate(typed.date);
            if (typed.time != null) {
                if (DateTimeInput.isPast(typed.date, typed.time)) {
                    view.setReturnTime(null);
                    view.setTimeError("That time has already passed today.");
                    console.error("The return time " + typed.time + " has already passed today. Enter a later time.");
                    return;
                }
                view.setReturnTime(typed.time);
            } else {
                checkTimeAgainstDate();
            }
        } catch (IllegalArgumentException ex) {
            view.setReturnError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private void readReturnTime() {
        try {
            java.time.LocalTime time = DateTimeInput.parseTime(view.getReturnTimeText());
            view.setReturnTime(time);
            checkTimeAgainstDate();
        } catch (IllegalArgumentException ex) {
            view.setTimeError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    /**
     * If both boxes are filled and the moment has already gone by, empties the
     * time and says why.
     */
    private void checkTimeAgainstDate() {
        try {
            LocalDate date = LocalDate.parse(view.getReturnDateText());
            java.time.LocalTime time = LocalTime.parse(view.getReturnTimeText());
            if (DateTimeInput.isPast(date, time)) {
                view.setReturnTime(null);
                view.setTimeError("That time has already passed today.");
                console.error("The return time " + time + " has already passed today. Enter a later time.");
            }
        } catch (DateTimeParseException ex) {
            // one of the boxes is still empty: nothing to compare yet
        }
    }

    private void addDriver() {
        try {
            int id = movementService.parseDriverID(view.getDriverText());
            if (view.getDriverIds().contains(id)) {
                throw new IllegalArgumentException("Driver " + id + " is already added.");
            }
            // the driver must already exist; a new customer is created with the New driver button
            movementService.findDriver(String.valueOf(id));
            view.addDriver(id);
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage().equals("Driver not found.")
                    ? "Driver not found. Press New driver to create one." : ex.getMessage();
            view.setDriverError(message);
            console.error(message);
        }
    }

    /**
     * Opens the New driver pop-up; a driver that gets created is added to this
     * screen's drivers.
     */
    private void newDriver() {
        NewDriverController.open(view, movementService.drivers(), console, id -> {
            if (!view.getDriverIds().contains(id)) {
                view.addDriver(id);
            }
        });
    }

    private void review() {
        List<String> problems = new ArrayList<>();

        // make sure what is typed in the date and time boxes has been turned into the proper format
        if (!view.getReturnDateText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            readReturnDate();
        }
        if (!view.getReturnTimeText().isEmpty() && !view.getReturnTimeText().matches("\\d{2}:\\d{2}")) {
            readReturnTime();
        }

        if (checkPlate() == null) {
            problems.add("Vehicle: check the plate number.");
        }
        if (view.getDriverIds().isEmpty()) {
            view.setDriverError("At least one driver is required.");
            problems.add("At least one driver is required.");
        }
        LocalDateTime when = parseReturn();
        if (when == null) {
            problems.add("Enter a valid return date and time.");
        } else {
            try {
                movementService.validateExpectedReturn(when);
                view.setReturnOk();
            } catch (IllegalArgumentException ex) {
                view.setReturnError(ex.getMessage());
                problems.add(ex.getMessage());
            }
        }
        if (view.getReturnLocation() == null) {
            problems.add("Choose a return location.");
        }

        view.setProblemCount(problems.size());
        if (!problems.isEmpty()) {
            console.error(problems.get(0));
            return;
        }
        console.info("Review the details, then confirm.");
        view.showConfirm();
    }

    private LocalDateTime parseReturn() {
        if (view.getReturnDateText().isEmpty()) {
            view.setReturnError("Enter the return date.");
            return null;
        }
        if (view.getReturnTimeText().isEmpty()) {
            view.setTimeError("Enter the return time.");
            return null;
        }
        try {
            return LocalDateTime.of(LocalDate.parse(view.getReturnDateText()), LocalTime.parse(view.getReturnTimeText()));
        } catch (DateTimeParseException ex) {
            view.setReturnError("Check the return date and time.");
            return null;
        }
    }

    private void confirm() {
        Location from = staff.getCurrentLocation();
        try {
            RentalMovement rental = movementService.startRA(view.getPlate(), staff.getStaffID(),
                    from == null ? null : from.getLocationID(), view.getDriverIds(), parseReturn(),
                    view.getReturnLocation() == null ? null : view.getReturnLocation().getLocationID(),
                    view.isRsa(), view.getInsurance());
            String cost = "$" + String.format("%,.2f", rental.getRentalEstimate());
            console.ok("Checkout complete. Movement ID: " + rental.getMovementID() + ". Estimated total cost: " + cost);
            view.showDone(view.getPlate(), rental.getMovementID(), cost);
        } catch (IllegalArgumentException ex) {
            // for example the car was taken in another tab since the plate was checked
            console.error(ex.getMessage());
            view.showForm();
            view.setVehicleError(ex.getMessage());
            view.setProblemCount(1);
        }
    }
}
