package com.rfo.model;

import java.time.LocalDateTime;

/**
 * A defleet movement (DVM): retires a vehicle from the fleet. It is checked in at the
 * retail depot and closed straight away.
 */
public class DefleetMovement extends Movement {

    public static final Location RETAIL_DEPOT = new Location("AKLC88", "Auckland Retail Depot", null);

    // new movement
    public DefleetMovement(Vehicle vehicle, String staffID) {
        super(vehicle, staffID, vehicle.getCurrentLocation(), vehicle.getCurrentOdometer());
        checkIn(staffID, RETAIL_DEPOT, LocalDateTime.now());
        this.closeMovement();
    }

    // reconstructing defleet movement from file
    public DefleetMovement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status) {

        super(movementID, vehicle, COStaff, CIStaff, checkOutDT, checkInDT,
                checkOutLoc, checkInLoc, odoOut, odoIn, status);
    }

    @Override
    public void execute() {
        vehicle.updateStatus(VehicleStatus.DEFLEETED);
        vehicle.setCurrentOdometer(odoOut);
    }

    private void closeMovement() {
        this.status = MovementStatus.CLOSED;
    }
}
