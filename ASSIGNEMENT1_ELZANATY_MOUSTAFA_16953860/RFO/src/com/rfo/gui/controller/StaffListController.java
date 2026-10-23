package com.rfo.gui.controller;

import com.rfo.gui.view.StaffView;
import com.rfo.service.ModelListener;
import com.rfo.service.StaffService;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for View Staff. Shows every staff member and refreshes when any
 * session changes the staff.
 */
public class StaffListController implements ModelListener {

    private final StaffView view;
    private final StaffService staffService;

    public StaffListController(StaffView view, StaffService staffService) {
        this.view = view;
        this.staffService = staffService;
        refresh();
    }

    @Override
    public void onModelChanged() {
        refresh();
    }

    private void refresh() {
        view.setStaff(staffService.getAllStaff());
    }
}
