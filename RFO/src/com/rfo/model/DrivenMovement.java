package com.rfo.model;

import java.time.LocalDateTime;
import java.util.List;

public abstract class DrivenMovement extends Movement {

    protected List<Driver> drivers;
    protected LocalDateTime expectedCIDT;
    protected Location expectedCILoc;

    public DrivenMovement(Vehicle vehicle, String COStaff, Location location, int odoOut,
            List<Driver> drivers, LocalDateTime expectedCIDT, Location expectedCILoc) {

        super(vehicle, COStaff, location, odoOut);

        this.drivers = drivers;
        this.expectedCIDT = expectedCIDT;
        this.expectedCILoc = expectedCILoc;

    }

    public void addDriver(Driver driver) {
        this.drivers.add(driver);
    }

    public List<Driver> getDrivers() {
        return drivers;
    }

    public LocalDateTime getExpectedCIDT() {
        return expectedCIDT;
    }

    public Location getExpectedCILoc() {
        return expectedCILoc;
    }

    @Override
    public void execute() {
        vehicle.setCurrentOdometer(odoIn);
        vehicle.updateServiceFlag();
        vehicle.setCurrentLocation(checkInLoc);
        vehicle.updateStatus(VehicleStatus.TO_BE_CLEANED);
    }

    public void closeMovement() {
        vehicle.updateStatus(VehicleStatus.AVAILABLE);
        this.status = MovementStatus.CLOSED;
    }

}
