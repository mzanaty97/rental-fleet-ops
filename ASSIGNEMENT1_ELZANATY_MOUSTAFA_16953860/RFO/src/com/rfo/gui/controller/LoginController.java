package com.rfo.gui.controller;

import com.rfo.gui.view.LoginView;
import com.rfo.model.Staff;
import com.rfo.service.AuthService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Handles the login form: validates the input, asks AuthService, reports the
 * result.
 */
public class LoginController implements ActionListener {

    private static final int MAX_ATTEMPTS = 5;

    private final LoginView view;
    private final AuthService auth;
    private final LoginListener listener;
    private int attempts = 0;

    public LoginController(LoginView view, AuthService auth, LoginListener listener) {
        this.view = view;
        this.auth = auth;
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String staffId = view.getStaffId().trim().toUpperCase();
        String password = view.getPassword();

        // input validation
        if (staffId.isEmpty() || password.isEmpty()) {
            view.showMissing(staffId.isEmpty(), password.isEmpty(), "Please enter both your Staff ID and password.");
            return;
        }

        Staff staff = auth.login(staffId, password);

        if (staff == null) {
            attempts++;
            view.clearPassword();
            view.showAttempts(attempts, MAX_ATTEMPTS);
            if (attempts >= MAX_ATTEMPTS) {
                view.lockForm("Too many failed attempts. Login locked.");
                listener.onLoginLocked(view);
            } else {
                view.showWrongLogin("Staff ID or password is incorrect.", "Invalid staff ID or password.");
            }
            return;
        }

        listener.onLoginSucceeded(view, staff);
    }
}
