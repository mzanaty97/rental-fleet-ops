package com.rfo.gui.controller;

import com.rfo.gui.view.AmendRentalView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.RentalMovement;
import com.rfo.service.LocationService;
import com.rfo.service.ModelListener;
import com.rfo.service.MovementService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Amend Rental. Finds the open rental with the MovementService,
 * shows it, and when the user presses Amend rental asks the service to change
 * the expected return. All the rules live in the service; this class only moves
 * text between the screen and the service.
 */
public class AmendRentalController implements ActionListener, ModelListener {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a");

    private final AmendRentalView view;
    private final MovementService movementService;
    private final LocationService locationService;
    private final MessageSink console;
    private RentalMovement rental;
    private String pendingDate;
    private String pendingLocation;

    public AmendRentalController(AmendRentalView view, MovementService movementService,
            LocationService locationService, MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.locationService = locationService;
        this.console = console;
        view.addController(this);
        view.setLocations(locationService.getActiveLocations());
    }

    @Override
    public void onModelChanged() {
        view.setLocations(locationService.getActiveLocations());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case AmendRentalView.AMEND:
                review();
                break;
            case AmendRentalView.CONFIRM:
                confirm();
                break;
            case AmendRentalView.CANCEL:
                console.info("Amendment cancelled. Nothing was changed.");
                view.showForm();
                break;
            default:
                find();
        }
    }

    private void find() {
        try {
            rental = movementService.findRentalForAmend(view.getLookupText());
            show();
            console.ok("Showing rental " + rental.getMovementID() + ". Type the new return and press Amend rental.");
        } catch (IllegalArgumentException ex) {
            rental = null;
            view.showLookupError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private void show() {
        String[][] details = {
            {"Vehicle", rental.getVehicle().getPlateNumber() + " · " + rental.getVehicle().getMake() + " "
                + rental.getVehicle().getModel()},
            {"Drivers", String.valueOf(rental.getDriverIDs().size())},
            {"Picked up", rental.getCheckOutDT().format(WHEN) + " · " + rental.getCheckOutLoc().getName()},
            {"Expected return", rental.getExpectedCIDT().format(WHEN)},
            {"Return location", rental.getExpectedCILoc().getName()},
            {"Estimated price", String.format("$%,.2f", rental.getRentalEstimate())}
        };
        view.showRental("Rental " + rental.getMovementID(), "Status " + rental.getStatus(), details,
                rental.getExpectedCILoc());
    }

    // step 1: check the typed change and show it on the confirm card; nothing is saved yet
    private void review() {
        if (rental == null) {
            console.error("Find a rental first.");
            return;
        }
        pendingDate = view.getDateText();
        pendingLocation = view.getChosenLocation() == null ? null : view.getChosenLocation().getLocationID();
        try {
            MovementService.AmendPreview p = movementService.previewAmend(rental, pendingDate, pendingLocation);
            view.showConfirm(new String[][]{
                {"Expected return", rental.getExpectedCIDT().format(WHEN), p.newReturn.format(WHEN)},
                {"Return location", rental.getExpectedCILoc().getName(), p.newLocation.getName()}
            }, String.format("$%,.2f", p.newEstimate), String.format("$%,.2f", p.oldEstimate));
            console.info("Check the change with the customer, then press Confirm amendment.");
        } catch (IllegalArgumentException ex) {
            handleProblem(ex);
        }
    }

    // step 2: the user confirmed, so the change is checked again and saved
    private void confirm() {
        if (rental == null) {
            console.error("Find a rental first.");
            return;
        }
        double before = rental.getRentalEstimate();
        String oldReturn = rental.getExpectedCIDT().format(WHEN) + " at " + rental.getExpectedCILoc().getName();
        try {
            movementService.amendRental(rental, pendingDate, pendingLocation);
        } catch (IllegalArgumentException ex) {
            handleProblem(ex);
            return;
        }
        console.ok("Rental " + rental.getMovementID() + " amended. Return was " + oldReturn + ", now "
                + rental.getExpectedCIDT().format(WHEN) + " at " + rental.getExpectedCILoc().getName()
                + ". Estimate " + String.format("$%,.2f", before) + " to " + String.format("$%,.2f", rental.getRentalEstimate()) + ".");
        show();
    }

    private void handleProblem(IllegalArgumentException ex) {
        // for example the rental was checked in from another tab: start again
        if (ex.getMessage().contains("cannot be amended")) {
            rental = null;
            view.reset();
        } else {
            view.showForm();
            view.showDateError(ex.getMessage());
        }
        console.error(ex.getMessage());
    }
}
