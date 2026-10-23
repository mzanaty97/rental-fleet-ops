package com.rfo.gui.controller;

import com.rfo.gui.view.AddLocationView;
import com.rfo.gui.view.CloseLocationView;
import com.rfo.gui.view.FleetView;
import com.rfo.gui.view.LocationsView;
import com.rfo.gui.view.PlaceholderView;
import com.rfo.gui.view.SessionView;
import com.rfo.model.Staff;
import com.rfo.service.LocationService;
import com.rfo.service.VehicleService;
import javax.swing.JComponent;

/**
 * Factory for feature screens. Given the name of a menu option it builds the
 * matching screen together with its controller. Options that are not built yet
 * get a placeholder, so adding a section later means adding one case here.
 */
public class ScreenFactory {

    private final VehicleService vehicleService;
    private final LocationService locationService;

    public ScreenFactory(VehicleService vehicleService, LocationService locationService) {
        this.vehicleService = vehicleService;
        this.locationService = locationService;
    }

    public JComponent createScreen(String option, SessionView sessionView, Staff staff) {
        switch (option) {
            case "Query Available Vehicles":
                return fleetScreen(new FleetView(FleetView.Mode.AVAILABLE, true, false), sessionView);
            case "Query Service Due Vehicles":
                return fleetScreen(new FleetView(FleetView.Mode.SERVICE_DUE, false, false), sessionView);
            case "Update Vehicle Status":
                return fleetScreen(new FleetView(FleetView.Mode.ALL, true, true), sessionView);
            case "View Locations":
                LocationsView locations = new LocationsView();
                sessionView.addModelListener(new LocationsController(locations, locationService));
                return locations;
            case "Add Location":
                AddLocationView form = new AddLocationView();
                new AddLocationController(form, locationService, sessionView.getConsole());
                return form;
            case "Close Location":
                CloseLocationView close = new CloseLocationView();
                sessionView.addModelListener(new CloseLocationController(close, locationService,
                        sessionView.getConsole(), staff.getStaffID()));
                return close;
            default:
                return new PlaceholderView(option);
        }
    }

    // the controller listens to the model through the session, so the table refreshes itself
    private JComponent fleetScreen(FleetView view, SessionView sessionView) {
        sessionView.addModelListener(new FleetController(view, vehicleService, sessionView.getConsole()));
        return view;
    }
}
