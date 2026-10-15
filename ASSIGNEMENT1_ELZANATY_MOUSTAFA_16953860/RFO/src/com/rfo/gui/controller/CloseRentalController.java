package com.rfo.gui.controller;

import com.rfo.gui.view.CloseRentalView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.DrivenMovement;
import com.rfo.model.RentalMovement;
import com.rfo.service.MovementService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller for Close Rental. Step 1 looks the rental up and checks the
 * odometer with the MovementService; step 2 shows the final cost and closes the
 * rental only when the user confirms.
 */
public class CloseRentalController implements ActionListener {

    private final CloseRentalView view;
    private final MovementService movementService;
    private final MessageSink console;
    private DrivenMovement rental;
    private int odoIn;
    private double finalCost;

    public CloseRentalController(CloseRentalView view, MovementService movementService, MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case CloseRentalView.LOOKUP:
                lookup();
                break;
            case CloseRentalView.CONTINUE:
                toPayment();
                break;
            case CloseRentalView.CONFIRM:
                close();
                break;
            default:
                console.info("Closing cancelled.");
                view.reset();
        }
    }

    private DrivenMovement lookup() {
        try {
            DrivenMovement r = movementService.findMovementForClose(view.getMovementId());
            view.showMovementOk(movementService.kindOf(r) + " · " + r.getVehicle().getPlateNumber() + " " + r.getVehicle().getMake() + " "
                    + r.getVehicle().getModel() + " · " + r.getStatus() + " (checked in) · odometer at checkout "
                    + String.format("%,d", r.getOdoOut()) + " km");
            return r;
        } catch (IllegalArgumentException ex) {
            view.showMovementError(ex.getMessage());
            return null;
        }
    }

    private void toPayment() {
        DrivenMovement r = lookup();
        if (r == null) {
            console.error("Check the movement ID or plate number first.");
            return;
        }
        try {
            odoIn = movementService.parseOdometer(view.getOdometerText(), r.getOdoOut());
        } catch (IllegalArgumentException ex) {
            view.showOdometerError(ex.getMessage());
            console.error(ex.getMessage());
            return;
        }
        rental = r;
        boolean isRental = r instanceof RentalMovement;
        String text = "No charge";
        if (isRental) {
            finalCost = movementService.getRentalCost((RentalMovement) r);
            text = "$" + String.format("%,.2f", finalCost);
            console.info("Final rental cost: " + text);
        } else {
            console.info("No charge for a " + movementService.kindOf(r).toLowerCase() + ". Confirm to close it.");
        }
        view.showPayment(r.getMovementID() + " · " + r.getVehicle().getPlateNumber(), r.getOdoOut(), odoIn, text,
                isRental);
    }

    private void close() {
        try {
            if (rental instanceof RentalMovement) {
                movementService.closeRA((RentalMovement) rental, odoIn, finalCost);
            } else {
                movementService.closeDrivenMovement(rental, odoIn);
            }
            console.ok(movementService.kindOf(rental) + " closed. Vehicle status: " + rental.getVehicle().getStatus());
            view.reset();
        } catch (IllegalArgumentException ex) {
            // for example it was closed in another tab
            console.error(ex.getMessage());
            view.reset();
        }
    }
}
