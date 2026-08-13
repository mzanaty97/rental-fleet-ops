package com.rfo.model;

import java.time.LocalDateTime;

public class DefleetMovement extends Movement {

    public static final Location RETAIL_DEPOT = new Location("AKLC88", "Auckland Retail Depot", null);

    public DefleetMovement(Vehicle vehicle, String staffID) {
        super(vehicle, staffID, vehicle.getCurrentLocation(), vehicle.getCurrentOdometer());
        checkIn(staffID, RETAIL_DEPOT, LocalDateTime.now());
        this.closeMovement();

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
