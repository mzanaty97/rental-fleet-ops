package com.rfo.gui.controller;

import com.rfo.gui.view.LoginView;
import com.rfo.gui.view.MainWindow;
import com.rfo.gui.view.SessionView;
import com.rfo.model.Staff;
import com.rfo.service.AuthService;
import com.rfo.service.DataStore;
import com.rfo.service.LocationService;
import com.rfo.service.MenuService;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import com.rfo.service.VehicleService;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
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
    private final MenuService menuService;
    private final ScreenFactory screenFactory;

    public AppController(MainWindow window, AuthService auth, StaffService staffService, DataStore dataStore) {
        this.window = window;
        this.auth = auth;
        this.staffService = staffService;
        this.dataStore = dataStore;
        this.menuService = new MenuService(staffService);
        this.screenFactory = new ScreenFactory(new VehicleService(dataStore), new LocationService(dataStore), staffService,
                new MovementService(dataStore));
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
        SessionView sessionView = new SessionView(staff);
        sessionView.addController(new SessionController(sessionView, this));
        new NavigationController(sessionView, menuService.getMenuFor(staff.getRole()), screenFactory, staff);
        dataStore.addListener(sessionView);

        window.replaceTabWithSession(view, sessionView, staff, ev -> closeTab(sessionView));
    }

    @Override
    public void onLoginLocked(LoginView view) {
        window.markTabLocked(view);
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
