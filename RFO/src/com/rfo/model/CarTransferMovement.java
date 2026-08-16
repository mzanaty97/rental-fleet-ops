package com.rfo.model;

import java.time.LocalDateTime;
import java.util.List;

public class CarTransferMovement extends DrivenMovement {

    // new movement
    public CarTransferMovement(Vehicle vehicle, String COStaff, Location location, int odoOut,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, Location expectedCILoc) {
        super(vehicle, COStaff, location, odoOut, driverIDs, expectedCIDT, expectedCILoc);
        vehicle.updateStatus(VehicleStatus.IN_USE);
    }

    // reconstructing car transfer movement from file
    public CarTransferMovement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, Location expectedCILoc) {

        super(movementID, vehicle, COStaff, CIStaff, checkOutDT, checkInDT,
                checkOutLoc, checkInLoc, odoOut, odoIn, status,
                driverIDs, expectedCIDT, expectedCILoc);
    }
}
