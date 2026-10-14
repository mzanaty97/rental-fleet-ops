package com.rfo.gui.controller;

import com.rfo.gui.view.LoginView;
import com.rfo.gui.view.MainWindow;
import com.rfo.gui.view.SessionView;
import com.rfo.model.Staff;
import com.rfo.service.AuthService;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller for the main window. Opens a login tab when New Session is
 * pressed, turns it into a session tab after a successful login, sends it back
 * to a login form on logout and closes tabs. All sessions share the one
 * DataStore, and the same staff member can be logged in on several tabs at
 * once.
 */
public class AppController implements ActionListener, LoginListener, SessionListener {

    private final MainWindow window;
    private final AuthService auth;
    private final StaffService staffService;
    private final DataStore dataStore;

    public AppController(MainWindow window, AuthService auth, StaffService staffService, DataStore dataStore) {
        this.window = window;
        this.auth = auth;
        this.staffService = staffService;
        this.dataStore = dataStore;
        window.getHomeView().addController(this);
    }

    // New Session button on the Home tab
    @Override
    public void actionPerformed(ActionEvent e) {
        LoginView loginView = createLoginView();
        window.addTab("New Session", loginView, ev -> closeTab(loginView));
        loginView.focusStaffId();
    }

    @Override
    public void onLoginSucceeded(LoginView view, Staff staff) {
        SessionView sessionView = new SessionView(staff, staffService.getMenuOptions(staff.getRole()));
        sessionView.addController(new SessionController(sessionView, this));
        dataStore.addListener(sessionView);

        window.replaceTab(view, sessionView, staff.getFName() + " (" + staff.getStaffID() + ")",
                ev -> closeTab(sessionView));
    }

    // logging out does not close the tab, it goes back to the login form
    @Override
    public void onLogout(SessionView view) {
        endSession(view);

        LoginView loginView = createLoginView();
        window.replaceTab(view, loginView, "New Session", ev -> closeTab(loginView));
        loginView.focusStaffId();
    }

    private LoginView createLoginView() {
        LoginView loginView = new LoginView();
        loginView.addController(new LoginController(loginView, auth, this));
        return loginView;
    }

    // the x on a tab: ends the session (if there is one) and removes the tab
    private void closeTab(Component content) {
        endSession(content);
        window.removeTab(content);
    }

    // stop the session view listening to the model once its tab is gone
    private void endSession(Component content) {
        if (content instanceof SessionView) {
            dataStore.removeListener((SessionView) content);
        }
    }
}
