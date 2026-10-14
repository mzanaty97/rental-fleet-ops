package com.rfo.gui.controller;

import com.rfo.gui.view.FleetView;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleStatus;
import com.rfo.service.ModelListener;
import com.rfo.service.VehicleService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Controller for the Fleet section. Fills the table from the VehicleService,
 * reacts to the filter boxes and the Apply button, and refreshes the table when
 * another session changes the data.
 */
public class FleetController implements ActionListener, ModelListener {

    private final FleetView view;
    private final VehicleService vehicleService;

    public FleetController(FleetView view, VehicleService vehicleService) {
        this.view = view;
        this.vehicleService = vehicleService;
        view.setStatusOptions(vehicleService.getManualStatuses());
        view.addController(this);
        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (FleetView.APPLY_COMMAND.equals(e.getActionCommand())) {
            applyStatus();
        } else {
            view.clearMessage();
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
            view.showError("Select a vehicle in the table first.");
            return;
        }

        try {
            boolean changed = vehicleService.updateVehicleStatus(plate, newStatus);
            // saving already refreshed the table through onModelChanged
            if (changed) {
                view.showMessage(plate + " is now " + newStatus + ".");
            } else {
                view.showError(plate + " is already " + newStatus + ". No change made.");
            }
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        }
    }
}
