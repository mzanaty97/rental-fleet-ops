package com.rfo.service;

import com.rfo.model.CarTransferMovement;
import com.rfo.model.DrivenMovement;
import com.rfo.model.Driver;
import com.rfo.model.Staff;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleStatus;
import com.rfo.model.WorkshopMovement;
import java.time.LocalDateTime;
import java.util.Comparator;
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
        requireKnownDrivers(driverIDs);
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
     * Finds a movement that is OPEN and can be checked in: a rental, a car
     * transfer or a workshop job. The text can be a movement ID or a plate
     * number; a plate finds that vehicle's open movement. Throws with the
     * reason if not.
     */
    public DrivenMovement findMovementForCheckIn(String idOrPlate) {
        DrivenMovement movement = findDriven(idOrPlate, MovementStatus.OPEN, "checked in");
        if (movement.getStatus() != MovementStatus.OPEN) {
            throw new IllegalArgumentException("This movement has already been checked in or closed.");
        }
        return movement;
    }

    /**
     * Finds a movement that has been checked in and can be closed (rental, car
     * transfer or workshop job). The text can be a movement ID or a plate
     * number. Throws with the reason if not.
     */
    public DrivenMovement findMovementForClose(String idOrPlate) {
        DrivenMovement movement = findDriven(idOrPlate, MovementStatus.INCOMPLETE, "closed");
        if (movement.getStatus() != MovementStatus.INCOMPLETE) {
            throw new IllegalArgumentException("This movement must be checked in before it can be closed.");
        }
        return movement;
    }

    /**
     * A short name for the kind of movement, for screens and messages.
     */
    public String kindOf(Movement movement) {
        if (movement instanceof RentalMovement) {
            return "Rental";
        }
        if (movement instanceof CarTransferMovement) {
            return "Car transfer";
        }
        if (movement instanceof WorkshopMovement) {
            return "Workshop";
        }
        return "Movement";
    }

    /**
     * Looks for a movement with this ID first, then for a movement of the
     * vehicle with this plate in the wanted status.
     */
    private DrivenMovement findDriven(String idOrPlate, MovementStatus wanted, String action) {
        String text = idOrPlate == null ? "" : idOrPlate.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Enter a movement ID or a plate number.");
        }
        Movement movement = dataStore.getBy(dataStore.getMovements(), m -> m.getMovementID().equals(text));
        if (movement != null) {
            if (!(movement instanceof DrivenMovement)) {
                throw new IllegalArgumentException("That movement cannot be checked in or closed.");
            }
            return (DrivenMovement) movement;
        }
        String plate = text.toUpperCase();
        List<Movement> mine = dataStore.search(dataStore.getMovements(), m -> m instanceof DrivenMovement
                && m.getVehicle().getPlateNumber().equals(plate) && m.getStatus() == wanted);
        if (!mine.isEmpty()) {
            return (DrivenMovement) mine.get(mine.size() - 1);
        }
        boolean knownPlate = dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate)) != null;
        if (knownPlate) {
            throw new IllegalArgumentException("There is no movement for " + plate + " that can be " + action + ".");
        }
        throw new IllegalArgumentException("No movement found for that movement ID or plate number.");
    }

    /**
     * Checks a movement in at the staff member's location. Throws with the
     * reason if it cannot be.
     */
    public DrivenMovement checkInMovement(String idOrPlate, String staffID, Location checkInLoc) {
        if (checkInLoc == null) {
            throw new IllegalArgumentException("You have no current location set. Cannot check in a vehicle.");
        }
        DrivenMovement movement = findMovementForCheckIn(idOrPlate);
        checkIn(movement, staffID, checkInLoc.getLocationID());
        return movement;
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
        if (expectedCILoc.getLocationID().equals(checkOutLoc.getLocationID())) {
            throw new IllegalArgumentException("The arrival location must be different from where the car is now.");
        }

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
        requireKnownDrivers(driverIDs);
        return findActiveLocation(expectedCILocID);
    }

    /**
     * Every driver ID on a movement must belong to a real driver.
     */
    private void requireKnownDrivers(List<Integer> driverIDs) {
        for (int id : driverIDs) {
            if (dataStore.getBy(dataStore.getDrivers(), d -> d.getDriverID() == id) == null) {
                throw new IllegalArgumentException("Driver " + id + " was not found.");
            }
        }
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
        if (movement.getStatus() != MovementStatus.INCOMPLETE) {
            throw new IllegalArgumentException("This movement must be checked in before it can be closed.");
        }
        movement.closeMovement(odoIn);
        dataStore.saveVehicles();
        dataStore.saveMovements();
    }

    // ---- Lookups and reports (used by the Movement Details and Reports screens) ----
    /**
     * Finds a movement by its ID. Throws with the reason if the text is empty
     * or nothing matches.
     */
    public Movement findMovement(String movementID) {
        String id = movementID == null ? "" : movementID.trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Enter a movement ID.");
        }
        Movement movement = dataStore.getBy(dataStore.getMovements(), m -> m.getMovementID().equals(id));
        if (movement == null) {
            throw new IllegalArgumentException("Movement not found.");
        }
        return movement;
    }

    /**
     * Finds a driver from typed text. Throws with the reason if it is not a
     * number or no driver has it.
     */
    public Driver findDriver(String text) {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Enter a driver ID.");
        }
        int driverID;
        try {
            driverID = Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a valid number.");
        }
        Driver driver = dataStore.getBy(dataStore.getDrivers(), d -> d.getDriverID() == driverID);
        if (driver == null) {
            throw new IllegalArgumentException("Driver not found.");
        }
        return driver;
    }

    /**
     * A staff member as "ID · Name", or just the ID when that staff member no
     * longer exists.
     */
    public String staffLabel(String staffID) {
        if (staffID == null) {
            return "N/A";
        }
        Staff found = dataStore.getBy(dataStore.getStaff(), s -> s.getStaffID().equals(staffID));
        return found == null ? staffID : staffID + " · " + found.getFName() + " " + found.getLName();
    }

    /**
     * Open rentals that left from this location or are expected back to it,
     * soonest return first. Throws if the staff member has no current location.
     */
    public List<RentalMovement> getOutOnRental(Location here) {
        if (here == null) {
            throw new IllegalArgumentException("You have no current location set.");
        }
        String hereID = here.getLocationID();
        List<RentalMovement> open = new java.util.ArrayList<>();
        for (Movement m : dataStore.getMovements()) {
            if (m instanceof RentalMovement && m.getStatus() == MovementStatus.OPEN) {
                RentalMovement rm = (RentalMovement) m;
                boolean checkedOutHere = m.getCheckOutLoc().getLocationID().equals(hereID);
                boolean returningHere = rm.getExpectedCILoc() != null
                        && rm.getExpectedCILoc().getLocationID().equals(hereID);
                if (checkedOutHere || returningHere) {
                    open.add(rm);
                }
            }
        }
        open.sort(Comparator.comparing(RentalMovement::getExpectedCIDT)
                .thenComparing(m -> m.getVehicle().getCategory()));
        return open;
    }

    /**
     * Every rental this staff member checked out, in movement ID order.
     */
    public List<RentalMovement> getStaffRentals(String staffID) {
        List<RentalMovement> rentals = new java.util.ArrayList<>();
        for (Movement m : dataStore.getMovements()) {
            if (m instanceof RentalMovement && m.getCOStaff().equals(staffID)) {
                rentals.add((RentalMovement) m);
            }
        }
        rentals.sort(Comparator.comparingInt(m -> Integer.parseInt(m.getMovementID())));
        return rentals;
    }
}
