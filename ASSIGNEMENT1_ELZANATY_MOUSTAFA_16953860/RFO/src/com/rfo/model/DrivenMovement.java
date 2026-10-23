package com.rfo.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Base class for movements where a driver takes the vehicle out (rental, transfer,
 * workshop). Adds the driver IDs and the expected check-in time and location to a
 * Movement.
 */
public abstract class DrivenMovement extends Movement {

    protected List<Integer> driverIDs;
    protected LocalDateTime expectedCIDT;
    protected Location expectedCILoc;

    // new movement
    public DrivenMovement(Vehicle vehicle, String COStaff, Location location, int odoOut,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, Location expectedCILoc) {
        super(vehicle, COStaff, location, odoOut);
        this.driverIDs = driverIDs;
        this.expectedCIDT = expectedCIDT;
        this.expectedCILoc = expectedCILoc;
    }

    // reconstructing driven movement from file
    protected DrivenMovement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, Location expectedCILoc) {

        super(movementID, vehicle, COStaff, CIStaff, checkOutDT, checkInDT,
                checkOutLoc, checkInLoc, odoOut, odoIn, status);

        this.driverIDs = driverIDs;
        this.expectedCIDT = expectedCIDT;
        this.expectedCILoc = expectedCILoc;
    }

    public void addDriver(int driverID) {
        this.driverIDs.add(driverID);
    }

    public List<Integer> getDriverIDs() {
        return driverIDs;
    }

    public LocalDateTime getExpectedCIDT() {
        return expectedCIDT;
    }

    public Location getExpectedCILoc() {
        return expectedCILoc;
    }

    // Generated with assistance from Claude: lets an open rental change where and when the vehicle is expected back.
    /**
     * Changes where and when the vehicle is expected back. Used when an open
     * rental is amended.
     */
    public void amendExpectedReturn(LocalDateTime expectedCIDT, Location expectedCILoc) {
        this.expectedCIDT = expectedCIDT;
        this.expectedCILoc = expectedCILoc;
    }

    @Override
    public void execute() {
        vehicle.setCurrentOdometer(odoIn);
        vehicle.updateServiceFlag();
        vehicle.setCurrentLocation(checkInLoc);
        vehicle.updateStatus(VehicleStatus.TO_BE_CLEANED);
    }

    public void closeMovement(int odoIn) {
        this.odoIn = odoIn;
        execute();
        vehicle.updateStatus(VehicleStatus.AVAILABLE);
        this.status = MovementStatus.CLOSED;
    }
}
