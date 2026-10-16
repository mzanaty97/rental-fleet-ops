package com.rfo.gui.controller;

import com.rfo.gui.view.FleetView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleStatus;
import com.rfo.service.ModelListener;
import com.rfo.service.VehicleService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for the Fleet section. Fills the table from the VehicleService,
 * reacts to the filter boxes and the Apply button, and refreshes the table when
 * another session changes the data.
 */
public class FleetController implements ActionListener, ModelListener {

    private final FleetView view;
    private final VehicleService vehicleService;
    private final MessageSink console;

    public FleetController(FleetView view, VehicleService vehicleService, MessageSink console) {
        this.view = view;
        this.vehicleService = vehicleService;
        this.console = console;
        view.setStatusOptions(vehicleService.getManualStatuses());
        view.addController(this);
        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (FleetView.APPLY_COMMAND.equals(e.getActionCommand())) {
            applyStatus();
        } else {
            refresh();
        }
    }

    // some session saved vehicles, so show the latest
    @Override
    public void onModelChanged() {
        refresh();
    }

    private void refresh() {
        FleetView.Mode mode = view.getSelectedMode();
        List<Vehicle> vehicles;

        if (mode == FleetView.Mode.SERVICE_DUE) {
            vehicles = vehicleService.getServiceDueVehicles(view.getSelectedCategory());
        } else if (mode == FleetView.Mode.ALL) {
            vehicles = vehicleService.getAllVehicles(view.getSelectedCategory());
        } else {
            vehicles = vehicleService.getAvailableVehicles(view.getSelectedCategory());
        }
        view.setVehicles(vehicles);
    }

    private void applyStatus() {
        String plate = view.getSelectedPlate();
        VehicleStatus newStatus = view.getSelectedStatus();

        if (plate == null) {
            console.print("Select a vehicle in the table first.");
            return;
        }

        try {
            boolean changed = vehicleService.updateVehicleStatus(plate, newStatus);
            // saving already refreshed the table through onModelChanged
            if (changed) {
                console.print(plate + " is now " + newStatus + ".");
            } else {
                console.print(plate + " is already " + newStatus + ". No change made.");
            }
        } catch (IllegalArgumentException ex) {
            console.print(ex.getMessage());
        }
    }
}
