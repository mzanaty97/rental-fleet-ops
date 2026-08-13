package com.rfo.model;

import java.time.LocalDateTime;
import java.util.List;

public class WorkshopMovement extends DrivenMovement {

    public WorkshopMovement(Vehicle vehicle, String COStaff, Location location, int odoOut, List<Driver> drivers, LocalDateTime expectedCIDT, Location expectedCILoc) {
        super(vehicle, COStaff, location, odoOut, drivers, expectedCIDT, expectedCILoc);
        vehicle.updateStatus(VehicleStatus.WORKSHOP);
    }

    @Override
    public void execute() {
        vehicle.setCurrentOdometer(odoIn);
        vehicle.setCurrentLocation(checkInLoc);
        vehicle.markServiced();
    }
}