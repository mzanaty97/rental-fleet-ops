package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.NewVehicleView;
import com.rfo.model.NewCarMovement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import com.rfo.service.VehicleService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller for New Vehicle. It checks each box with the VehicleService, shows
 * the reasons next to the box they belong to, and adds the vehicle only when
 * everything is fine.
 */
public class NewVehicleController implements ActionListener {

    private final NewVehicleView view;
    private final VehicleService vehicleService;
    private final Staff staff;
    private final MessageSink console;

    public NewVehicleController(NewVehicleView view, VehicleService vehicleService, Staff staff, MessageSink console) {
        this.view = view;
        this.vehicleService = vehicleService;
        this.staff = staff;
        this.console = console;
        view.addController(this);
        view.setFooter("Added at " + NewCarMovement.INTAKE_DEPOT.getName() + " as AVAILABLE");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (NewVehicleView.PLATE.equals(e.getActionCommand())) {
            checkPlate();
        } else {
            add();
        }
    }

    private boolean checkPlate() {
        try {
            vehicleService.validatePlate(view.getPlate());
            return true;
        } catch (IllegalArgumentException ex) {
            view.setPlateError(ex.getMessage());
            return false;
        }
    }

    private void add() {
        String first = null;
        if (!checkPlate()) {
            first = "Plate: check the plate number.";
        }
        int year = 0;
        try {
            year = vehicleService.parseYear(view.getYearText());
        } catch (IllegalArgumentException ex) {
            view.setYearError(ex.getMessage());
            first = first == null ? ex.getMessage() : first;
        }
        if (view.getMake().isEmpty()) {
            view.setMakeError("Enter the make.");
            first = first == null ? "Enter the make." : first;
        }
        if (view.getModel().isEmpty()) {
            view.setModelError("Enter the model.");
            first = first == null ? "Enter the model." : first;
        }
        if (first != null) {
            console.error(first);
            return;
        }
        try {
            Vehicle v = vehicleService.addVehicle(view.getPlate(), view.getMake(), view.getModel(), year,
                    view.getCategory(), view.getFuelType(), view.hasGps(), staff.getStaffID());
            console.ok("Vehicle added: " + v.getPlateNumber() + " (" + v.getYear() + " " + v.getMake() + " "
                    + v.getModel() + ") at " + NewCarMovement.INTAKE_DEPOT.getName() + ".");
            view.clearForm();
        } catch (IllegalArgumentException ex) {
            view.setPlateError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }
}
