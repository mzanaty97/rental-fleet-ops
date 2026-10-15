package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.ReportView;
import com.rfo.model.Location;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.LocationService;
import com.rfo.service.ModelListener;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import com.rfo.service.VehicleService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the four reports. It asks the services for the rows, turns
 * them into text for the ReportView, and refreshes when another session changes
 * the data. One class serves all four reports; the {@link Report} says which
 * one.
 */
public class ReportController implements ActionListener, ModelListener {

    /**
     * The four reports in the Reports menu.
     */
    public enum Report {
        OUT_ON_RENTAL, LOCATION_FLEET, NEW_VEHICLES, STAFF_RENTALS
    }

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ReportView view;
    private final Report report;
    private final VehicleService vehicleService;
    private final MovementService movementService;
    private final Staff staff;
    private final MessageSink console;
    // what the drop down holds, in the same order
    private List<Location> locations = new ArrayList<>();
    private List<Staff> staffList = new ArrayList<>();

    public ReportController(ReportView view, Report report, VehicleService vehicleService,
            MovementService movementService, LocationService locationService, StaffService staffService,
            Staff staff, MessageSink console) {
        this.view = view;
        this.report = report;
        this.vehicleService = vehicleService;
        this.movementService = movementService;
        this.staff = staff;
        this.console = console;

        if (report == Report.LOCATION_FLEET) {
            locations = locationService.getAllLocations();
            List<String> labels = new ArrayList<>();
            int selected = 0;
            for (int i = 0; i < locations.size(); i++) {
                Location l = locations.get(i);
                labels.add(l.getLocationID() + " · " + l.getName());
                if (staff.getCurrentLocation() != null
                        && l.getLocationID().equals(staff.getCurrentLocation().getLocationID())) {
                    selected = i;
                }
            }
            view.setPickerItems(labels, selected);
        } else if (report == Report.STAFF_RENTALS) {
            staffList = staffService.getAllStaff();
            List<String> labels = new ArrayList<>();
            int selected = 0;
            for (int i = 0; i < staffList.size(); i++) {
                Staff s = staffList.get(i);
                labels.add(s.getStaffID() + " · " + s.getFName() + " " + s.getLName());
                if (s.getStaffID().equals(staff.getStaffID())) {
                    selected = i;
                }
            }
            view.setPickerItems(labels, selected);
        }
        view.addController(this);
        // every screen is built once at login; the view tells us each time it is opened, and we refresh then
        refresh(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        refresh(true);
    }

    // another session saved something, so show the latest without printing messages again
    @Override
    public void onModelChanged() {
        refresh(false);
    }

    private void refresh(boolean speak) {
        switch (report) {
            case OUT_ON_RENTAL:
                outOnRental(speak);
                break;
            case LOCATION_FLEET:
                locationFleet(speak);
                break;
            case NEW_VEHICLES:
                newVehicles(speak);
                break;
            default:
                staffRentals(speak);
        }
    }

    private void outOnRental(boolean speak) {
        Location here = staff.getCurrentLocation();
        List<RentalMovement> open;
        try {
            open = movementService.getOutOnRental(here);
        } catch (IllegalArgumentException ex) {
            view.setRows(new ArrayList<>(), "No location", "", ex.getMessage(), "Pick a location with Change Location.");
            if (speak) {
                console.error(ex.getMessage());
            }
            return;
        }
        List<Object[]> rows = new ArrayList<>();
        for (RentalMovement rm : open) {
            rows.add(new Object[]{rm.getVehicle().getPlateNumber(), rm.getExpectedCIDT().format(WHEN),
                rm.getVehicle().getCategory().toString()});
        }
        view.setRows(rows, "Open rentals from " + here.getName() + " · " + rows.size(), "Soonest return first",
                "No open movements found", "Every rental from this location has been checked in. This list refreshes by itself.");
        if (speak && rows.isEmpty()) {
            console.info("No open movements found");
        }
    }

    private void locationFleet(boolean speak) {
        int index = view.getPickedIndex();
        if (index < 0 || index >= locations.size()) {
            view.setRows(new ArrayList<>(), "0 vehicles", "", "Location not found", "Pick a location above.");
            return;
        }
        Location location = locations.get(index);
        List<Vehicle> fleet = vehicleService.getLocationFleet(location.getLocationID());
        List<Object[]> rows = new ArrayList<>();
        for (Vehicle v : fleet) {
            rows.add(new Object[]{v.getPlateNumber(), v.getCurrentOdometer(), v.getStatus().toString()});
        }
        view.setRows(rows, fleet.size() + (fleet.size() == 1 ? " vehicle" : " vehicles"),
                "Updates by itself when another session changes a vehicle",
                "No vehicles at this location", "Nothing is parked at " + location.getName() + " right now.");
        if (speak && rows.isEmpty()) {
            console.info("No vehicles at this location");
        }
    }

    private void newVehicles(boolean speak) {
        List<Vehicle> cars = vehicleService.getNewVehicles();
        List<Object[]> rows = new ArrayList<>();
        for (Vehicle v : cars) {
            String where = v.getCurrentLocation() != null ? v.getCurrentLocation().getName() : "N/A";
            rows.add(new Object[]{v.getPlateNumber(), v.getCurrentOdometer(), where});
        }
        view.setRows(rows, cars.size() + (cars.size() == 1 ? " near new car" : " near new cars"),
                "Available, 30 km or less on the odometer",
                "No new cars available.", "Cars with 30 km or less that are AVAILABLE show up here.");
        if (speak && rows.isEmpty()) {
            console.info("No new cars available.");
        }
    }

    private void staffRentals(boolean speak) {
        int index = view.getPickedIndex();
        if (index < 0 || index >= staffList.size()) {
            view.setRows(new ArrayList<>(), "0 rentals", "", "Staff member not found", "Pick a staff member above.");
            return;
        }
        Staff chosen = staffList.get(index);
        List<RentalMovement> rentals = movementService.getStaffRentals(chosen.getStaffID());
        List<Object[]> rows = new ArrayList<>();
        int closed = 0;
        for (RentalMovement rm : rentals) {
            String checkIn = rm.getCheckInDT() != null ? rm.getCheckInDT().format(WHEN)
                    : rm.getExpectedCIDT().format(WHEN) + " expected";
            if (rm.getStatus() == MovementStatus.CLOSED) {
                closed++;
            }
            rows.add(new Object[]{rm.getMovementID(), rm.getCheckOutDT().format(WHEN), checkIn,
                rm.getStatus().toString()});
        }
        view.setRows(rows, rentals.size() + (rentals.size() == 1 ? " rental" : " rentals") + " · "
                + (rentals.size() - closed) + " open · " + closed + " closed", "",
                "No rentals found for this staff member.", chosen.getFName() + " has not checked out a rental yet.");
        if (speak && rows.isEmpty()) {
            console.info("No rentals found for this staff member.");
        }
    }
}
