package com.rfo.service;

import com.rfo.model.CarTransferMovement;
import com.rfo.model.DrivenMovement;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.RentalMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.WorkshopMovement;
import java.time.LocalDateTime;
import java.util.List;

public class MovementService {

    private DataStore dataStore;

    public MovementService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public RentalMovement startRA(String plateNumber, String staffID, String checkOutLocID,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, String expectedCILocID,
            boolean rsa, Insurance insuranceTier) {

        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plateNumber));

        if (vehicle == null) {
            System.out.println("No vehicles with plate number " + plateNumber);
            return null;
        }

        if (!vehicle.getCurrentLocation().getLocationID().equals(checkOutLocID)) {
            System.out.println("Vehicle does not belong to your current location.");
            return null;
        }

        if (!vehicle.isAvailable()) {
            System.out.println("Vehicle not ready for rent.");
            return null;
        }

        if (vehicle.isDueForService()) {
            System.out.println("Vehicle is " + vehicle.getOverdueKm() + " kms overdue for service.");
            return null;
        }

        Location checkOutLoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(checkOutLocID));
        Location expectedCILoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(expectedCILocID));

        RentalMovement rental = new RentalMovement(vehicle, staffID, checkOutLoc,
                vehicle.getCurrentOdometer(), driverIDs, expectedCIDT, expectedCILoc,
                rsa, insuranceTier);

        dataStore.getMovements().add(rental);
        dataStore.saveVehicles();
        dataStore.saveMovements();

        return rental;
    }

    public WorkshopMovement startWSM(String plateNumber, String staffID, String checkOutLocID,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, String expectedCILocID) {

        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plateNumber));

        if (vehicle == null) {
            System.out.println("No vehicles with plate number " + plateNumber);
            return null;
        }

        if (!vehicle.getCurrentLocation().getLocationID().equals(checkOutLocID)) {
            System.out.println("Vehicle does not belong to your current location.");
            return null;
        }

        Location checkOutLoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(checkOutLocID));
        Location expectedCILoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(expectedCILocID));

        WorkshopMovement wsm = new WorkshopMovement(vehicle, staffID, checkOutLoc,
                vehicle.getCurrentOdometer(), driverIDs, expectedCIDT, expectedCILoc);

        dataStore.getMovements().add(wsm);
        dataStore.saveVehicles();
        dataStore.saveMovements();

        return wsm;
    }

    public CarTransferMovement startCTM(String plateNumber, String staffID, String checkOutLocID,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, String expectedCILocID) {

        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plateNumber));

        if (vehicle == null) {
            System.out.println("No vehicles with plate number " + plateNumber);
            return null;
        }

        if (!vehicle.getCurrentLocation().getLocationID().equals(checkOutLocID)) {
            System.out.println("Vehicle does not belong to your current location.");
            return null;
        }

        Location checkOutLoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(checkOutLocID));
        Location expectedCILoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(expectedCILocID));

        CarTransferMovement ctm = new CarTransferMovement(vehicle, staffID, checkOutLoc,
                vehicle.getCurrentOdometer(), driverIDs, expectedCIDT, expectedCILoc);

        dataStore.getMovements().add(ctm);
        dataStore.saveVehicles();
        dataStore.saveMovements();

        return ctm;
    }

    // Phase 1 of return: stops the billing clock, records who/when/where.
    // Does NOT touch the vehicle or odometer - that only happens at closeX() below.
    public void checkIn(Movement movement, String staffID, String checkInLocID) {
        Location checkInLoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(checkInLocID));

        movement.checkIn(staffID, checkInLoc, LocalDateTime.now());
        dataStore.saveMovements();
    }

    public double getRentalCost(RentalMovement rental) {
        return rental.calculateRentalCost();
    }

    // Phase 2: odometer finally supplied, execute() fires here (inside closeMovement),
    // vehicle state is updated, movement is finalized.
    public void closeRA(RentalMovement rental, int odoIn, double finalPrice) {
        rental.closeMovement(odoIn, finalPrice);
        dataStore.saveVehicles();
        dataStore.saveMovements();
    }

    public void closeDrivenMovement(DrivenMovement movement, int odoIn) {
        movement.closeMovement(odoIn);
        dataStore.saveVehicles();
        dataStore.saveMovements();
    }
}
