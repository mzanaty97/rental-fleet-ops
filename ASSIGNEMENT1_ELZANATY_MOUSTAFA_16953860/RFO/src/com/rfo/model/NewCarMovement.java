package com.rfo.model;

import java.time.LocalDateTime;

/**
 * A new car movement (NVM): adds a brand new vehicle to the fleet at a location (shown as
 * yarded).
 */
public class NewCarMovement extends Movement {

    public static final Location INTAKE_DEPOT = new Location("AKLC99", "Auckland Intake Depot", null);
    public static final int NEW_VEHICLE_ODO = 30;

    // new movement
    public NewCarMovement(Vehicle vehicle, String staffID) {
        super(vehicle, staffID, INTAKE_DEPOT, NEW_VEHICLE_ODO);
        checkIn(staffID, INTAKE_DEPOT, LocalDateTime.now());
        this.closeMovement();
    }

    // reconstructing new car movement from file
    public NewCarMovement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status) {

        super(movementID, vehicle, COStaff, CIStaff, checkOutDT, checkInDT,
                checkOutLoc, checkInLoc, odoOut, odoIn, status);
    }

    @Override
    public void execute() {
        vehicle.setCurrentLocation(checkInLoc);
        vehicle.setCurrentOdometer(NEW_VEHICLE_ODO);
        vehicle.updateStatus(VehicleStatus.AVAILABLE);
    }

    private void closeMovement() {
        this.status = MovementStatus.CLOSED;
    }
}
