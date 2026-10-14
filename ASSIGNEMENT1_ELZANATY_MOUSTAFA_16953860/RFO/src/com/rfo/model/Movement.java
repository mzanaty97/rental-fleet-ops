package com.rfo.model;

import java.time.LocalDateTime;

public abstract class Movement {

    private static int LATEST_MOVEMENT_ID;

    protected String movementID;
    protected Vehicle vehicle;
    protected String COStaff;
    protected String CIStaff;
    protected LocalDateTime checkOutDT;
    protected LocalDateTime checkInDT;
    protected Location checkOutLoc;
    protected Location checkInLoc;
    protected int odoOut;
    protected int odoIn;
    protected MovementStatus status;

    // new movement
    protected Movement(Vehicle vehicle, String COStaff, Location COLocation, int odoOut) {
        this.movementID = Integer.toString(LATEST_MOVEMENT_ID++);
        this.vehicle = vehicle;
        this.COStaff = COStaff;
        this.checkOutLoc = COLocation;
        this.checkOutDT = LocalDateTime.now();
        this.odoOut = odoOut;
        this.status = MovementStatus.OPEN;
    }

    // reconstructin movement from file.
    public Movement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status) {
        this.movementID = movementID;
        this.vehicle = vehicle;
        this.COStaff = COStaff;
        this.CIStaff = CIStaff;
        this.checkOutDT = checkOutDT;
        this.checkInDT = checkInDT;
        this.checkOutLoc = checkOutLoc;
        this.checkInLoc = checkInLoc;
        this.odoOut = odoOut;
        this.odoIn = odoIn;
        this.status = status;
    }

    public static void setLatestMovementID(int latest) {
        LATEST_MOVEMENT_ID = latest;
    }

    public void checkIn(String staffID, Location CILocation, LocalDateTime checkInDT) {
        this.CIStaff = staffID;
        this.checkInDT = checkInDT;
        this.checkInLoc = CILocation;
        this.status = MovementStatus.INCOMPLETE;
    }

    public abstract void execute();

    public String getMovementID() {
        return movementID;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getCOStaff() {
        return COStaff;
    }

    public String getCIStaff() {
        return CIStaff;
    }

    public LocalDateTime getCheckOutDT() {
        return checkOutDT;
    }

    public LocalDateTime getCheckInDT() {
        return checkInDT;
    }

    public Location getCheckOutLoc() {
        return checkOutLoc;
    }

    public Location getCheckInLoc() {
        return checkInLoc;
    }

    public int getOdoOut() {
        return odoOut;
    }

    public int getOdoIn() {
        return odoIn;
    }

    public MovementStatus getStatus() {
        return status;
    }

}
