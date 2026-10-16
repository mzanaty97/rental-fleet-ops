package com.rfo.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A workshop movement (WSM): sends a vehicle to the workshop for service or repair. A
 * driven movement that marks the vehicle as WORKSHOP while it is out.
 */
public class WorkshopMovement extends DrivenMovement {

    // new movement
    public WorkshopMovement(Vehicle vehicle,
            String COStaff,
            Location location,
            int odoOut,
            List<Integer> driverIDs,
            LocalDateTime expectedCIDT,
            Location expectedCILoc) {
        super(vehicle, COStaff, location, odoOut, driverIDs, expectedCIDT, expectedCILoc);
        vehicle.updateStatus(VehicleStatus.WORKSHOP);
    }

    // reconstructing workshop movement from file
    public WorkshopMovement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, Location expectedCILoc) {

        super(movementID, vehicle, COStaff, CIStaff, checkOutDT, checkInDT,
                checkOutLoc, checkInLoc, odoOut, odoIn, status,
                driverIDs, expectedCIDT, expectedCILoc);
    }

    @Override
    public void execute() {
        vehicle.setCurrentOdometer(odoIn);
        vehicle.setCurrentLocation(checkInLoc);
        vehicle.markServiced();
    }
}
