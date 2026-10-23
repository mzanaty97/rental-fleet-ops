package com.rfo.gui.controller;

import com.rfo.gui.view.ChangeLocationView;
import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.SessionView;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.service.ModelListener;
import com.rfo.service.StaffService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Change Location. Fills the list from the staff member's
 * accessible locations, switches the working location through the StaffService
 * and tells the session header to update.
 */
public class ChangeLocationController implements ActionListener, ModelListener {

    private final ChangeLocationView view;
    private final StaffService staffService;
    private final Staff staff;
    private final SessionView session;
    private final MessageSink console;

    public ChangeLocationController(ChangeLocationView view, StaffService staffService, Staff staff, SessionView session) {
        this.view = view;
        this.staffService = staffService;
        this.staff = staff;
        this.session = session;
        this.console = session.getConsole();
        view.addController(this);
        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Location chosen = view.getSelectedLocation();
        if (chosen == null) {
            console.warn("Select a location first.");
            return;
        }
        try {
            staffService.switchLocation(staff, chosen);
            console.ok("Location changed to (" + chosen.getLocationID() + ") " + chosen.getName());
            session.refreshHeader();
            refresh();
        } catch (IllegalArgumentException ex) {
            console.error(ex.getMessage());
        }
    }

    // another session may have changed locations or access
    @Override
    public void onModelChanged() {
        refresh();
    }

    private void refresh() {
        view.setLocations(staff.getCurrentLocation(), staffService.getUsableLocations(staff));
    }
}
