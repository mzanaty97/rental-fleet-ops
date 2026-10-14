package com.rfo.gui.controller;

import com.rfo.gui.view.GrantAccessView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.service.LocationService;
import com.rfo.service.ModelListener;
import com.rfo.service.StaffService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Grant Location Access. It fills the lists for the chosen
 * person (what they can already use, and the active locations they could be
 * given) and asks the service to grant the chosen one.
 */
public class GrantAccessController implements ActionListener, ModelListener {

    private final GrantAccessView view;
    private final StaffService staffService;
    private final LocationService locationService;
    private final MessageSink console;

    public GrantAccessController(GrantAccessView view, StaffService staffService,
            LocationService locationService, MessageSink console) {
        this.view = view;
        this.staffService = staffService;
        this.locationService = locationService;
        this.console = console;
        view.addController(this);
        refresh();
    }

    @Override
    public void onModelChanged() {
        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (GrantAccessView.STAFF_CHANGED.equals(e.getActionCommand())) {
            showLists();
            return;
        }
        Staff staff = view.getSelectedStaff();
        Location location = view.getSelectedLocation();
        if (staff == null || location == null) {
            console.error("Choose a staff member and a location first.");
            return;
        }
        try {
            staffService.grantLocationAccess(staff, location);
            console.ok("Access granted: " + staff.getStaffID() + " can now use " + location.getName());
            showLists();
        } catch (IllegalArgumentException ex) {
            console.error(ex.getMessage());
        }
    }

    private void refresh() {
        view.setStaff(staffService.getStaffWithAccessRules());
        showLists();
    }

    private void showLists() {
        Staff staff = view.getSelectedStaff();
        List<Location> has = new ArrayList<>();
        List<Location> grantable = new ArrayList<>();
        if (staff != null) {
            has.addAll(staff.getAccessibleLocations());
            for (Location l : locationService.getActiveLocations()) {
                if (!has.contains(l)) {
                    grantable.add(l);
                }
            }
        }
        view.setCurrentAccess(has);
        view.setGrantable(grantable);
    }
}
