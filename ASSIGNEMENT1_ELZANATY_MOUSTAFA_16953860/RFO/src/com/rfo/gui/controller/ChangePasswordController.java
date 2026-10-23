package com.rfo.gui.controller;

import com.rfo.gui.view.ChangePasswordView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Staff;
import com.rfo.service.StaffService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Change Password. Checks the three boxes, asks the StaffService
 * to change the password and shows the result in the console. The reason for a
 * refusal comes from the service.
 */
public class ChangePasswordController implements ActionListener {

    private final ChangePasswordView view;
    private final StaffService staffService;
    private final Staff staff;
    private final MessageSink console;

    public ChangePasswordController(ChangePasswordView view, StaffService staffService, Staff staff, MessageSink console) {
        this.view = view;
        this.staffService = staffService;
        this.staff = staff;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String current = view.getCurrentPassword();
        String newPass = view.getNewPassword();
        String confirm = view.getConfirmPassword();

        // input validation
        if (current.isEmpty()) {
            fail("Enter your current password.", "current");
            return;
        }
        if (newPass.isEmpty()) {
            fail("Enter a new password.", "new");
            return;
        }
        if (!newPass.equals(confirm)) {
            fail("Passwords do not match.", "confirm");
            return;
        }

        try {
            if (staffService.changePassword(staff, current, newPass)) {
                console.ok("Password changed successfully.");
                view.clearForm();
            } else {
                console.error("The password could not be saved.");
            }
        } catch (IllegalArgumentException ex) {
            // the service says which box is at fault by its message
            String message = ex.getMessage();
            fail(message, message.startsWith("Current") ? "current" : "new");
        }
    }

    private void fail(String message, String box) {
        switch (box) {
            case "current":
                view.showCurrentError(message);
                break;
            case "new":
                view.showNewError(message);
                break;
            default:
                view.showConfirmError(message);
        }
        console.error(message);
    }
}
