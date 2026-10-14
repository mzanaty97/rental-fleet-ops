package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.TransferView;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.DateTimeInput;
import com.rfo.service.LocationService;
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
 * Controller for Car Transfer and Workshop. They share this class because the
 * steps are the same: find the vehicle at the staff member's current location,
 * collect drivers and a due date and place, then start the movement. The staff
 * member's location is read fresh each time.
 */
public class TransferController implements ActionListener, ModelListener {

    private final TransferView view;
    private final TransferView.Kind kind;
    private final MovementService movementService;
    private final LocationService locationService;
    private final Staff staff;
    private final MessageSink console;

    public TransferController(TransferView view, TransferView.Kind kind, MovementService movementService,
            LocationService locationService, Staff staff, MessageSink console) {
        this.view = view;
        this.kind = kind;
        this.movementService = movementService;
        this.locationService = locationService;
        this.staff = staff;
        this.console = console;
        view.addController(this);
        refresh();
    }

    @Override
    public void onModelChanged() {
        refresh();
    }

    private void refresh() {
        view.setFromName(staff.getCurrentLocation() == null ? "" : staff.getCurrentLocation().getName());
        view.setLocations(locationService.getActiveLocations());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case TransferView.PLATE:
                checkPlate();
                break;
            case TransferView.LIST_AVAILABLE:
                listAvailable();
                break;
            case TransferView.ADD_DRIVER:
                addDriver();
                break;
            case TransferView.DATE:
                readDate();
                break;
            case TransferView.TIME:
                readTime();
                break;
            default:
                start();
        }
    }

    private Vehicle checkPlate() {
        view.setFromName(staff.getCurrentLocation() == null ? "" : staff.getCurrentLocation().getName());
        try {
            Vehicle v = movementService.findVehicleForNonRevenue(view.getPlate(), staff.getCurrentLocation());
            view.setVehicleOk(v.getMake() + " " + v.getModel() + " · " + v.getStatus());
            return v;
        } catch (IllegalArgumentException ex) {
            view.setVehicleError(ex.getMessage());
            return null;
        }
    }

    private void listAvailable() {
        Location here = staff.getCurrentLocation();
        if (here == null) {
            console.error("You have no current location set.");
            return;
        }
        List<Vehicle> vehicles = movementService.getVehiclesForNonRevenue(here);
        if (vehicles.isEmpty()) {
            console.warn("No vehicles are available to move at " + here.getName() + ".");
            return;
        }
        List<String[]> rows = new ArrayList<>();
        for (Vehicle v : vehicles) {
            rows.add(new String[]{v.getPlateNumber(), v.getMake() + " " + v.getModel(), v.getCategory().toString(),
                String.format("%,d", v.getCurrentOdometer()) + " km"});
        }
        view.showVehicleChoices(vehicles.size() + (vehicles.size() == 1 ? " vehicle" : " vehicles")
                + " available at " + here.getName() + ".", rows);
    }

    private void addDriver() {
        try {
            int id = movementService.parseDriverID(view.getDriverText());
            if (view.getDriverIds().contains(id)) {
                throw new IllegalArgumentException("Driver " + id + " is already added.");
            }
            view.addDriver(id);
        } catch (IllegalArgumentException ex) {
            view.setDriverError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    /**
     * Turns the typed date (and time, if typed with it) into the boxes' format.
     * A date in the past is erased.
     */
    private void readDate() {
        try {
            DateTimeInput.Result typed = DateTimeInput.parseDateTime(view.getDateText());
            if (typed.date.isBefore(LocalDate.now())) {
                view.setDate(null);
                view.setDateError("Date is in the past.");
                console.error("The date " + typed.date + " is in the past. Enter today's date or a later one.");
                return;
            }
            view.setDate(typed.date);
            if (typed.time != null) {
                if (DateTimeInput.isPast(typed.date, typed.time)) {
                    view.setTime(null);
                    view.setTimeError("That time has already passed today.");
                    console.error("The time " + typed.time + " has already passed today. Enter a later time.");
                    return;
                }
                view.setTime(typed.time);
            } else {
                checkTimeAgainstDate();
            }
        } catch (IllegalArgumentException ex) {
            view.setDateError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private void readTime() {
        try {
            view.setTime(DateTimeInput.parseTime(view.getTimeText()));
            checkTimeAgainstDate();
        } catch (IllegalArgumentException ex) {
            view.setTimeError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private void checkTimeAgainstDate() {
        try {
            LocalDate date = LocalDate.parse(view.getDateText());
            LocalTime time = LocalTime.parse(view.getTimeText());
            if (DateTimeInput.isPast(date, time)) {
                view.setTime(null);
                view.setTimeError("That time has already passed today.");
                console.error("The time " + time + " has already passed today. Enter a later time.");
            }
        } catch (DateTimeParseException ex) {
            // one of the boxes is still empty: nothing to compare yet
        }
    }

    private LocalDateTime readWhen() {
        if (view.getDateText().isEmpty()) {
            view.setDateError("Enter the date.");
            return null;
        }
        if (view.getTimeText().isEmpty()) {
            view.setTimeError("Enter the time.");
            return null;
        }
        try {
            return LocalDateTime.of(LocalDate.parse(view.getDateText()), LocalTime.parse(view.getTimeText()));
        } catch (DateTimeParseException ex) {
            view.setDateError("Check the date and time.");
            return null;
        }
    }

    private void start() {
        String first = null;
        if (checkPlate() == null) {
            first = "Check the plate number.";
        }
        if (view.getDriverIds().isEmpty()) {
            view.setDriverError("At least one driver is required.");
            first = first == null ? "At least one driver is required." : first;
        }
        if (!view.getDateText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            readDate();
        }
        if (!view.getTimeText().isEmpty() && !view.getTimeText().matches("\\d{2}:\\d{2}")) {
            readTime();
        }
        LocalDateTime when = readWhen();
        if (when == null) {
            first = first == null ? "Enter a valid date and time." : first;
        } else {
            try {
                movementService.validateExpectedReturn(when);
            } catch (IllegalArgumentException ex) {
                view.setDateError(ex.getMessage());
                first = first == null ? ex.getMessage() : first;
            }
        }
        Location destination = view.getChosenLocation();
        Location here = staff.getCurrentLocation();
        if (destination == null) {
            first = first == null ? "Choose a location." : first;
        } else if (kind == TransferView.Kind.TRANSFER && here != null
                && destination.getLocationID().equals(here.getLocationID())) {
            first = first == null ? "The arrival location must be different from where the car is now." : first;
            console.error(first);
            return;
        }
        if (first != null) {
            console.error(first);
            return;
        }
        try {
            Movement m;
            if (kind == TransferView.Kind.TRANSFER) {
                m = movementService.startCTM(view.getPlate(), staff.getStaffID(), here.getLocationID(),
                        view.getDriverIds(), when, destination.getLocationID());
            } else {
                m = movementService.startWSM(view.getPlate(), staff.getStaffID(), here.getLocationID(),
                        view.getDriverIds(), when, destination.getLocationID());
            }
            console.ok(kind == TransferView.Kind.TRANSFER ? "Transfer started. Movement ID: " + m.getMovementID()
                    : "Workshop movement started. Movement ID: " + m.getMovementID());
            view.showDone(m.getMovementID());
        } catch (IllegalArgumentException ex) {
            console.error(ex.getMessage());
            view.setVehicleError(ex.getMessage());
        }
    }
}
