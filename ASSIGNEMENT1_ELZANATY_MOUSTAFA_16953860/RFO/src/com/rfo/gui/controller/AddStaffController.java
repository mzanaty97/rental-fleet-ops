package com.rfo.gui.controller;

import com.rfo.gui.view.AddStaffView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Staff;
import com.rfo.service.StaffService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller for Add Staff. The service checks the details and says why it refuses; this controller puts
 * that reason next to the box it is about and in the console.
 */
public class AddStaffController implements ActionListener {

    private final AddStaffView view;
    private final StaffService staffService;
    private final MessageSink console;

    public AddStaffController(AddStaffView view, StaffService staffService, MessageSink console) {
        this.view = view;
        this.staffService = staffService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Staff added = staffService.addStaff(view.getStaffId(), view.getPassword(),
                    view.getFirstName(), view.getLastName(), view.getRole());
            console.ok("Staff added successfully: " + added.getStaffID());
            view.clearForm();
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage();
            if (message.startsWith("Staff ID") || message.startsWith("Duplicate")) {
                view.showIdError(message);
            } else if (message.startsWith("Password")) {
                view.showPasswordError(message);
            } else if (message.startsWith("Please choose")) {
                view.showRoleError(message);
            } else {
                view.showFirstNameError(message);
            }
            console.error(message);
        }
    }
}
