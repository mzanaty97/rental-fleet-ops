package com.rfo.service;

import com.rfo.model.CarTransferMovement;
import com.rfo.model.DrivenMovement;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleStatus;
import com.rfo.model.WorkshopMovement;
import java.time.LocalDateTime;
import java.util.List;

public class MovementService {

    private DataStore dataStore;

    public MovementService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * The vehicles at a location that are free to be moved (transfer or
     * workshop): there and available.
     */
    public List<Vehicle> getVehiclesForNonRevenue(Location location) {
        if (location == null) {
            return new java.util.ArrayList<>();
        }
        List<Vehicle> found = dataStore.search(dataStore.getVehicles(), v -> v.getCurrentLocation() != null
                && v.getCurrentLocation().getLocationID().equals(location.getLocationID())
                && (v.getStatus() == VehicleStatus.AVAILABLE || v.getStatus() == VehicleStatus.SERVICE_DUE));
        found.sort(java.util.Comparator.comparing(Vehicle::getPlateNumber));
        return found;
    }

    /**
     * The vehicles that could be rented out right now from a location: there,
     * available and not due for service.
     */
    public List<Vehicle> getVehiclesForCheckout(Location location) {
        if (location == null) {
            return new java.util.ArrayList<>();
        }
        List<Vehicle> found = dataStore.search(dataStore.getVehicles(), v -> v.getCurrentLocation() != null
                && v.getCurrentLocation().getLocationID().equals(location.getLocationID())
                && v.isAvailable() && !v.isDueForService());
        found.sort(java.util.Comparator.comparing(Vehicle::getPlateNumber));
        return found;
    }

    /**
     * Finds the vehicle for a checkout and checks it can be rented from the
     * given location. Throws with the reason if it cannot.
     */
    public Vehicle findVehicleForCheckout(String plateNumber, Location checkOutLoc) {
        if (checkOutLoc == null) {
            throw new IllegalArgumentException("You have no current location set. Cannot check out a vehicle.");
        }
        String plate = plateNumber == null ? "" : plateNumber.trim().toUpperCase();
        if (plate.isEmpty()) {
            throw new IllegalArgumentException("Enter a plate number.");
        }
        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate));
        if (vehicle == null) {
            throw new IllegalArgumentException("No vehicle found with that plate number.");
        }
        if (vehicle.getCurrentLocation() == null
                || !vehicle.getCurrentLocation().getLocationID().equals(checkOutLoc.getLocationID())) {
            throw new IllegalArgumentException("That vehicle is not at your current location ("
                    + checkOutLoc.getName() + ").");
        }
        if (vehicle.isDueForService()) {
            throw new IllegalArgumentException("That vehicle is due for service (overdue by " + vehicle.getOverdueKm()
                    + " km). Send it to the workshop first.");
        }
        if (!vehicle.isAvailable()) {
            throw new IllegalArgumentException("That vehicle is not available (status: " + vehicle.getStatus() + ").");
        }
        return vehicle;
    }

    /**
     * Turns typed text into a driver ID. Throws with the reason if it is not a
     * positive number.
     */
    public int parseDriverID(String text) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Enter a driver ID.");
        }
        try {
            int id = Integer.parseInt(t);
            if (id <= 0) {
                throw new IllegalArgumentException("Driver ID must be a positive number.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Driver ID must be a number.");
        }
    }

    /**
     * The expected return must be a real date and time in the future.
     */
    public void validateExpectedReturn(LocalDateTime when) {
        if (when == null) {
            throw new IllegalArgumentException("Enter the return date and time.");
        }
        if (!when.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Return must be in the future.");
        }
    }

    /**
     * The price of a rental starting now and returning at the given time.
     * Nothing is saved.
     */
    public RentalMovement.Quote quoteRental(Vehicle vehicle, LocalDateTime expectedReturn, boolean rsa, Insurance tier) {
        return RentalMovement.quote(vehicle, LocalDateTime.now(), expectedReturn, rsa, tier);
    }

    /**
     * Finds an active location by ID. Throws with the reason if there is none.
     */
    public Location findActiveLocation(String locationID) {
        Location loc = locationID == null ? null : dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(locationID.trim().toUpperCase()));
        if (loc == null) {
            throw new IllegalArgumentException("Location not found.");
        }
        if (!loc.isActive()) {
            throw new IllegalArgumentException("That location is closed. Choose an active one.");
        }
        return loc;
    }

    /**
     * Starts a rental. Throws with the reason if anything is wrong.
     */
    public RentalMovement startRA(String plateNumber, String staffID, String checkOutLocID,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, String expectedCILocID,
            boolean rsa, Insurance insuranceTier) {

        Location checkOutLoc = dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(checkOutLocID));
        Vehicle vehicle = findVehicleForCheckout(plateNumber, checkOutLoc);
        if (driverIDs == null || driverIDs.isEmpty()) {
            throw new IllegalArgumentException("At least one driver is required.");
        }
        validateExpectedReturn(expectedCIDT);
        Location expectedCILoc = findActiveLocation(expectedCILocID);
        if (insuranceTier == null) {
            throw new IllegalArgumentException("Choose an insurance tier.");
        }

        RentalMovement rental = new RentalMovement(vehicle, staffID, checkOutLoc,
                vehicle.getCurrentOdometer(), driverIDs, expectedCIDT, expectedCILoc,
                rsa, insuranceTier);

        dataStore.getMovements().add(rental);
        dataStore.saveVehicles();
        dataStore.saveMovements();

        return rental;
    }

    /**
     * Finds a rental that is OPEN and can be checked in. The text can be a
     * movement ID or a plate number; a plate finds that vehicle's open rental.
     * Throws with the reason if not.
     */
    public RentalMovement findRentalForCheckIn(String idOrPlate) {
        RentalMovement rental = findRental(idOrPlate, MovementStatus.OPEN, "checked in");
        if (rental.getStatus() != MovementStatus.OPEN) {
            throw new IllegalArgumentException("This rental has already been checked in or closed.");
        }
        return rental;
    }

    /**
     * Finds a rental that has been checked in and can be closed. The text can
     * be a movement ID or a plate number; a plate finds that vehicle's
     * checked-in rental. Throws with the reason if not.
     */
    public RentalMovement findRentalForClose(String idOrPlate) {
        RentalMovement rental = findRental(idOrPlate, MovementStatus.INCOMPLETE, "closed");
        if (rental.getStatus() != MovementStatus.INCOMPLETE) {
            throw new IllegalArgumentException("This rental must be checked in before it can be closed.");
        }
        return rental;
    }

    /**
     * Looks for a movement with this ID first, then for the rental of a vehicle
     * with this plate in the wanted status.
     */
    private RentalMovement findRental(String idOrPlate, MovementStatus wanted, String action) {
        String text = idOrPlate == null ? "" : idOrPlate.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Enter a movement ID or a plate number.");
        }
        Movement movement = dataStore.getBy(dataStore.getMovements(), m -> m.getMovementID().equals(text));
        if (movement != null) {
            if (!(movement instanceof RentalMovement)) {
                throw new IllegalArgumentException("That movement is not a rental.");
            }
            return (RentalMovement) movement;
        }
        String plate = text.toUpperCase();
        List<Movement> mine = dataStore.search(dataStore.getMovements(), m -> m instanceof RentalMovement
                && m.getVehicle().getPlateNumber().equals(plate) && m.getStatus() == wanted);
        if (!mine.isEmpty()) {
            return (RentalMovement) mine.get(mine.size() - 1);
        }
        boolean knownPlate = dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate)) != null;
        if (knownPlate) {
            throw new IllegalArgumentException("There is no rental for " + plate + " that can be " + action + ".");
        }
        throw new IllegalArgumentException("No rental found for that movement ID or plate number.");
    }

    /**
     * Checks a rental in at the staff member's location. Throws with the reason
     * if it cannot be.
     */
    public RentalMovement checkInRental(String movementID, String staffID, Location checkInLoc) {
        if (checkInLoc == null) {
            throw new IllegalArgumentException("You have no current location set. Cannot check in a vehicle.");
        }
        RentalMovement rental = findRentalForCheckIn(movementID);
        checkIn(rental, staffID, checkInLoc.getLocationID());
        return rental;
    }

    /**
     * Turns the typed odometer into a number. It must be digits only and not
     * lower than at checkout.
     */
    public int parseOdometer(String text, int odoOut) {
        String t = text == null ? "" : text.trim().replace(",", "");
        if (!t.matches("\\d{1,9}")) {
            throw new IllegalArgumentException("Enter digits only, for example 15420.");
        }
        int odo = Integer.parseInt(t);
        if (odo < odoOut) {
            throw new IllegalArgumentException("Odometer cannot be lower than at checkout (" + odoOut + " km).");
        }
        return odo;
    }

    /**
     * Finds the vehicle for a car transfer or a workshop run and checks it is
     * at the given location and free to move. Throws with the reason if it is
     * not.
     */
    public Vehicle findVehicleForNonRevenue(String plateNumber, Location checkOutLoc) {
        if (checkOutLoc == null) {
            throw new IllegalArgumentException("You have no current location set.");
        }
        String plate = plateNumber == null ? "" : plateNumber.trim().toUpperCase();
        if (plate.isEmpty()) {
            throw new IllegalArgumentException("Enter a plate number.");
        }
        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate));
        if (vehicle == null) {
            throw new IllegalArgumentException("No vehicle found with that plate number.");
        }
        if (vehicle.getCurrentLocation() == null
                || !vehicle.getCurrentLocation().getLocationID().equals(checkOutLoc.getLocationID())) {
            throw new IllegalArgumentException("That vehicle is not at your current location ("
                    + checkOutLoc.getName() + ").");
        }
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE && vehicle.getStatus() != VehicleStatus.SERVICE_DUE) {
            throw new IllegalArgumentException("That vehicle is not available (status: " + vehicle.getStatus() + ").");
        }
        return vehicle;
    }

    /**
     * Starts a workshop movement. Throws with the reason if anything is wrong.
     */
    public WorkshopMovement startWSM(String plateNumber, String staffID, String checkOutLocID,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, String expectedCILocID) {

        Location checkOutLoc = findLocationForNonRevenue(checkOutLocID);
        Vehicle vehicle = findVehicleForNonRevenue(plateNumber, checkOutLoc);
        Location expectedCILoc = checkDrivenDetails(driverIDs, expectedCILocID);

        WorkshopMovement wsm = new WorkshopMovement(vehicle, staffID, checkOutLoc,
                vehicle.getCurrentOdometer(), driverIDs, expectedCIDT, expectedCILoc);

        dataStore.getMovements().add(wsm);
        dataStore.saveVehicles();
        dataStore.saveMovements();

        return wsm;
    }

    /**
     * Starts a car transfer. Throws with the reason if anything is wrong.
     */
    public CarTransferMovement startCTM(String plateNumber, String staffID, String checkOutLocID,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, String expectedCILocID) {

        Location checkOutLoc = findLocationForNonRevenue(checkOutLocID);
        Vehicle vehicle = findVehicleForNonRevenue(plateNumber, checkOutLoc);
        Location expectedCILoc = checkDrivenDetails(driverIDs, expectedCILocID);

        CarTransferMovement ctm = new CarTransferMovement(vehicle, staffID, checkOutLoc,
                vehicle.getCurrentOdometer(), driverIDs, expectedCIDT, expectedCILoc);

        dataStore.getMovements().add(ctm);
        dataStore.saveVehicles();
        dataStore.saveMovements();

        return ctm;
    }

    private Location findLocationForNonRevenue(String locationID) {
        Location loc = locationID == null ? null : dataStore.getBy(dataStore.getLocations(),
                l -> l.getLocationID().equals(locationID.trim().toUpperCase()));
        if (loc == null) {
            throw new IllegalArgumentException("You have no current location set.");
        }
        return loc;
    }

    private Location checkDrivenDetails(List<Integer> driverIDs, String expectedCILocID) {
        if (driverIDs == null || driverIDs.isEmpty()) {
            throw new IllegalArgumentException("At least one driver is required.");
        }
        return findActiveLocation(expectedCILocID);
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
        if (rental.getStatus() != MovementStatus.INCOMPLETE) {
            throw new IllegalArgumentException("This rental must be checked in before it can be closed.");
        }
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
