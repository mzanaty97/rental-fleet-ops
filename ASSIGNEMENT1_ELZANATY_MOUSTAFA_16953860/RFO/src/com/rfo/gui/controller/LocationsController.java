package com.rfo.gui.controller;

import com.rfo.gui.view.LocationsView;
import com.rfo.model.Location;
import com.rfo.service.LocationService;
import com.rfo.service.ModelListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the View Locations screen. It shows every location and
 * refreshes the table when any session adds or closes one.
 */
public class LocationsController implements ModelListener {

    private final LocationsView view;
    private final LocationService locationService;

    public LocationsController(LocationsView view, LocationService locationService) {
        this.view = view;
        this.locationService = locationService;
        refresh();
    }

    @Override
    public void onModelChanged() {
        refresh();
    }

    private void refresh() {
        List<Location> locations = locationService.getAllLocations();
        Map<String, Integer> counts = new HashMap<>();
        for (Location l : locations) {
            counts.put(l.getLocationID(), locationService.countVehiclesAt(l.getLocationID()));
        }
        view.setLocations(locations, counts);
    }
}
