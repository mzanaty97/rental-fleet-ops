package com.rfo.gui.controller;

import com.rfo.gui.view.DriverView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Driver;
import com.rfo.service.MovementService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Query Drivers. Finds the driver with the MovementService and
 * gives the view the text to show.
 */
public class DriverController implements ActionListener {

    private final DriverView view;
    private final MovementService movementService;
    private final MessageSink console;

    public DriverController(DriverView view, MovementService movementService, MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Driver d = movementService.findDriver(view.getDriverText());
            String initials = initial(d.getFName()) + initial(d.getLName());
            view.showDriver(initials, d.getName(), String.valueOf(d.getDriverID()), d.getLicenseNumber(),
                    d.getEmail(), d.getPhoneNumber(), d.getAddress() == null ? "N/A" : d.getAddress().getFullAddress());
            console.ok("Showing driver " + d.getDriverID() + ".");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private String initial(String name) {
        return name == null || name.isEmpty() ? "" : name.substring(0, 1).toUpperCase();
    }
}
