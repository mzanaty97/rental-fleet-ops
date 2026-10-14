package com.rfo.gui.controller;

import com.rfo.gui.view.CheckInView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.service.MovementService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Check In. Asks the MovementService to check the rental in at
 * the staff member's current location.
 */
public class CheckInController implements ActionListener {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CheckInView view;
    private final MovementService movementService;
    private final Staff staff;
    private final MessageSink console;

    public CheckInController(CheckInView view, MovementService movementService, Staff staff, MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.staff = staff;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            RentalMovement rental = movementService.checkInRental(view.getMovementId(), staff.getStaffID(),
                    staff.getCurrentLocation());
            String where = staff.getCurrentLocation().getName();
            console.ok("Checked in at " + where + ". Movement status is now: " + rental.getStatus() + ".");
            view.showResult(where, rental.getMovementID(), rental.getVehicle().getPlateNumber(),
                    rental.getVehicle().getMake() + " " + rental.getVehicle().getModel(),
                    rental.getCheckOutDT().format(WHEN), rental.getCheckInDT().format(WHEN),
                    rental.getExpectedCIDT().format(WHEN));
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }
}
