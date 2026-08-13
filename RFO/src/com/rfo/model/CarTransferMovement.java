package com.rfo.model;

import java.time.LocalDateTime;
import java.util.List;

public class CarTransferMovement extends DrivenMovement {

    public CarTransferMovement(Vehicle vehicle, String COStaff, Location location, int odoOut, List<Driver> drivers, LocalDateTime expectedCIDT, Location expectedCILoc) {
        super(vehicle, COStaff, location, odoOut, drivers, expectedCIDT, expectedCILoc);
        vehicle.updateStatus(VehicleStatus.IN_USE);
    }
}
