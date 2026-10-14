package com.rfo.gui.controller;

import com.rfo.gui.view.FleetView;
import com.rfo.gui.view.PlaceholderView;
import com.rfo.gui.view.SessionView;
import com.rfo.service.VehicleService;
import javax.swing.JComponent;

/**
 * Factory for feature screens. Given the name of a menu option it builds the
 * matching screen together with its controller. Options that are not built yet
 * get a placeholder, so adding a section later means adding one case here.
 */
public class ScreenFactory {

    private final VehicleService vehicleService;

    public ScreenFactory(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    public JComponent createScreen(String option, SessionView sessionView) {
        switch (option) {
            case "Query Available Vehicles":
                return fleetScreen(new FleetView(FleetView.Mode.AVAILABLE, true, false), sessionView);
            case "Query Service Due Vehicles":
                return fleetScreen(new FleetView(FleetView.Mode.SERVICE_DUE, false, false), sessionView);
            case "Update Vehicle Status":
                return fleetScreen(new FleetView(FleetView.Mode.ALL, true, true), sessionView);
            default:
                return new PlaceholderView(option);
        }
    }

    // the controller listens to the model through the session, so the table refreshes itself
    private JComponent fleetScreen(FleetView view, SessionView sessionView) {
        sessionView.addModelListener(new FleetController(view, vehicleService));
        return view;
    }
}
