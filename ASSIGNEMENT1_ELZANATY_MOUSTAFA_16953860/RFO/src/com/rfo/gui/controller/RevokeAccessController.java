package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.RevokeAccessView;
import com.rfo.model.Location;
import com.rfo.model.Staff;
import com.rfo.service.ModelListener;
import com.rfo.service.StaffService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Revoke Location Access. Shows what would be removed and
 * revokes it all only on confirm.
 */
public class RevokeAccessController implements ActionListener, ModelListener {

    private final RevokeAccessView view;
    private final StaffService staffService;
    private final MessageSink console;

    public RevokeAccessController(RevokeAccessView view, StaffService staffService, MessageSink console) {
        this.view = view;
        this.staffService = staffService;
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
        Staff staff = view.getSelectedStaff();
        switch (e.getActionCommand()) {
            case RevokeAccessView.STAFF_CHANGED:
                view.clearSelection();
                showAccess();
                break;
            case RevokeAccessView.KEEP:
                view.clearSelection();
                if (staff != null) {
                    console.info("Access kept for " + staff.getStaffID());
                }
                break;
            default:
                if (staff == null) {
                    return;
                }
                List<Location> chosen = view.getSelectedLocations();
                try {
                    staffService.revokeLocationAccess(staff, chosen);
                    console.ok("Access revoked for " + staff.getStaffID() + ": " + names(chosen));
                    view.clearSelection();
                    showAccess();
                } catch (IllegalArgumentException ex) {
                    console.error(ex.getMessage());
                }
        }
    }

    private static String names(List<Location> locations) {
        StringBuilder sb = new StringBuilder();
        for (Location l : locations) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(l.getLocationID());
        }
        return sb.toString();
    }

    private void refresh() {
        view.setStaff(staffService.getStaffWithAccessRules());
        showAccess();
    }

    private void showAccess() {
        Staff staff = view.getSelectedStaff();
        view.setAccess(staff, staff == null ? new ArrayList<>() : new ArrayList<>(staff.getAccessibleLocations()));
    }
}
