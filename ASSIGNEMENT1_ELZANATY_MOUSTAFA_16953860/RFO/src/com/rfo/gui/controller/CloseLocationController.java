package com.rfo.gui.controller;

import com.rfo.gui.view.CloseLocationView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Location;
import com.rfo.service.LocationService;
import com.rfo.service.ModelListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Close Location screen. It lists the active locations,
 * closes the selected one (moving its vehicles to the chosen destination) and
 * prints the outcome in the console. It refreshes when any session changes a
 * location.
 */
public class CloseLocationController implements ActionListener, ModelListener {

    private final CloseLocationView view;
    private final LocationService locationService;
    private final MessageSink console;
    private final String staffID;

    public CloseLocationController(CloseLocationView view, LocationService locationService,
            MessageSink console, String staffID) {
        this.view = view;
        this.locationService = locationService;
        this.console = console;
        this.staffID = staffID;
        view.addController(this);
        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String locationID = view.getSelectedLocationID();

        if (locationID == null) {
            console.print("Select the location to close in the table first.");
            return;
        }

        try {
            Location location = locationService.findLocation(locationID);
            locationService.closeLocation(location, view.getSelectedDestinationID(), staffID);
            console.print("Location closed: " + locationID);
        } catch (IllegalArgumentException ex) {
            console.print(ex.getMessage());
        }
    }

    @Override
    public void onModelChanged() {
        refresh();
    }

    private void refresh() {
        List<Location> active = locationService.getActiveLocations();
        Map<String, Integer> counts = new HashMap<>();
        for (Location l : active) {
            counts.put(l.getLocationID(), locationService.countVehiclesAt(l.getLocationID()));
        }
        view.setLocations(active, counts);
        view.setDestinations(active);
    }
}
