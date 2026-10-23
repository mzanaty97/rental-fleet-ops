package com.rfo.gui.controller;

import com.rfo.gui.view.DefleetView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.VehicleService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Defleet: look the vehicle up, ask "are you sure", then defleet
 * it.
 */
public class DefleetController implements ActionListener {

    private final DefleetView view;
    private final VehicleService vehicleService;
    private final Staff staff;
    private final MessageSink console;

    public DefleetController(DefleetView view, VehicleService vehicleService, Staff staff, MessageSink console) {
        this.view = view;
        this.vehicleService = vehicleService;
        this.staff = staff;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case DefleetView.LOOKUP:
                lookup();
                break;
            case DefleetView.LIST_VEHICLES:
                list();
                break;
            case DefleetView.ASK:
                if (lookup() != null) {
                    view.showConfirm(view.getPlate());
                }
                break;
            default:
                confirm();
        }
    }

    private Vehicle lookup() {
        try {
            Vehicle v = vehicleService.findVehicleForDefleet(view.getPlate());
            view.showVehicle(v.getMake() + " " + v.getModel(), v.getCategory().toString(),
                    String.format("%,d", v.getCurrentOdometer()) + " km", v.getStatus().toString());
            return v;
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            return null;
        }
    }

    private void list() {
        List<Vehicle> vehicles = vehicleService.getVehiclesForDefleet();
        if (vehicles.isEmpty()) {
            console.warn("There are no vehicles that can be defleeted right now.");
            return;
        }
        List<String[]> rows = new ArrayList<>();
        for (Vehicle v : vehicles) {
            rows.add(new String[]{v.getPlateNumber(), v.getMake() + " " + v.getModel(), v.getCategory().toString(),
                v.getCurrentLocation() == null ? "no location" : v.getCurrentLocation().getName()});
        }
        view.showVehicleChoices(vehicles.size() + (vehicles.size() == 1 ? " vehicle" : " vehicles") + " can be defleeted.", rows);
    }

    private void confirm() {
        try {
            Vehicle v = vehicleService.defleetVehicle(view.getPlate(), staff.getStaffID());
            console.ok("Vehicle defleeted: " + v.getPlateNumber() + ".");
            view.showDone(v.getPlateNumber());
        } catch (IllegalArgumentException ex) {
            console.error(ex.getMessage());
            view.hideConfirm();
            view.showError(ex.getMessage());
        }
    }
}
