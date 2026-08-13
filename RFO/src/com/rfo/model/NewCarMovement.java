package com.rfo.model;

import java.time.LocalDateTime;

public class NewCarMovement extends Movement {

    public static final Location INTAKE_DEPOT = new Location("AKLC99", "Auckland Intake Depot", null);
    public static final int NEW_VEHICLE_ODO = 30;

    public NewCarMovement(Vehicle vehicle, String staffID) {
        super(vehicle, staffID, INTAKE_DEPOT, NEW_VEHICLE_ODO);
        checkIn(staffID, INTAKE_DEPOT, LocalDateTime.now());
        this.closeMovement();
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
