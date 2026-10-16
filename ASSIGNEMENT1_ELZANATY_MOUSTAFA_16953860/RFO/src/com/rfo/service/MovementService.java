package com.rfo.service;

import com.rfo.model.CarTransferMovement;
import com.rfo.model.DrivenMovement;
import com.rfo.model.Driver;
import com.rfo.model.Staff;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.DefleetMovement;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.NewCarMovement;
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
     * Finds a rental that can still be amended: it must be OPEN (not yet
     * checked in). The text can be a movement ID or a plate number. Throws with
     * the reason if not.
     */
    public RentalMovement findRentalForAmend(String idOrPlate) {
        DrivenMovement movement = findDriven(idOrPlate, MovementStatus.OPEN, "amended");
        if (!(movement instanceof RentalMovement)) {
            throw new IllegalArgumentException("Only rentals can be amended (this is a " + kindOf(movement).toLowerCase() + ").");
        }
        if (movement.getStatus() != MovementStatus.OPEN) {
            throw new IllegalArgumentException("This rental has already been checked in or closed, so it cannot be amended.");
        }
        return (RentalMovement) movement;
    }

    /**
     * What an amendment would change, worked out and checked but not saved.
     * Shown on the confirm card.
     */
    public static class AmendPreview {

        public final LocalDateTime newReturn;
        public final Location newLocation;
        public final double oldEstimate;
        public final double newEstimate;

        private AmendPreview(LocalDateTime newReturn, Location newLocation, double oldEstimate, double newEstimate) {
            this.newReturn = newReturn;
            this.newLocation = newLocation;
            this.oldEstimate = oldEstimate;
            this.newEstimate = newEstimate;
        }
    }

    /**
     * Checks a proposed change to an open rental's expected return and works
     * out the new estimate, without saving anything. The date is typed text
     * (the time is optional: without one the rental keeps its current return
     * time). The new return must be in the future and after the pick-up, the
     * location must be active, and something must actually change. Throws with
     * the reason if not.
     */
    public AmendPreview previewAmend(RentalMovement rental, String dateText, String locationID) {
        if (rental.getStatus() != MovementStatus.OPEN) {
            throw new IllegalArgumentException("This rental has already been checked in or closed, so it cannot be amended.");
        }
        DateTimeInput.Result typed = DateTimeInput.parseDateTime(dateText);
        LocalDateTime newReturn = typed.date.atTime(typed.time != null ? typed.time : rental.getExpectedCIDT().toLocalTime());
        validateExpectedReturn(newReturn);
        if (!newReturn.isAfter(rental.getCheckOutDT())) {
            throw new IllegalArgumentException("Return must be after the pick-up (" + rental.getCheckOutDT().toLocalDate() + ").");
        }
        Location newLoc = findActiveLocation(locationID);
        if (newReturn.equals(rental.getExpectedCIDT()) && newLoc.getLocationID().equals(rental.getExpectedCILoc().getLocationID())) {
            throw new IllegalArgumentException("Nothing has changed.");
        }
        return new AmendPreview(newReturn, newLoc, rental.getRentalEstimate(), rental.estimateFor(newReturn));
    }

    /**
     * Applies an amendment after every check in previewAmend has passed, and
     * saves it. Throws with the reason if not.
     */
    public void amendRental(RentalMovement rental, String dateText, String locationID) {
        AmendPreview preview = previewAmend(rental, dateText, locationID);
        rental.amendExpectedReturn(preview.newReturn, preview.newLocation);
        dataStore.saveMovements();
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

    /**
     * The rules for creating a driver, which share this service's data.
     */
    public DriverService drivers() {
        return new DriverService(dataStore);
    }

    /**
     * The invoice rules, which share this service's data (a rental's invoice is
     * made when it closes).
     */
    public InvoiceService invoices() {
        return new InvoiceService(dataStore);
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
        // the invoice is written as soon as the rental closes, so it can be reprinted later
        invoices().issueFor(rental);
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

    // ---- Vehicle movement history (Show Vehicle Movements) ----
    /**
     * One line of a vehicle's history. The check-in values of a movement that
     * has not been checked in yet are the expected ones (expected is true) and
     * the odometer in is empty (null) until it is closed.
     */
    public static class HistoryRow {

        public final String type;
        public final String movementID;
        public final String outLocation;
        public final LocalDateTime outTime;
        public final Integer odoOut;
        public final String inLocation;
        public final LocalDateTime inTime;
        public final Integer odoIn;
        public final boolean expected;
        public final MovementStatus status;

        HistoryRow(String type, String movementID, String outLocation, LocalDateTime outTime, Integer odoOut,
                String inLocation, LocalDateTime inTime, Integer odoIn, boolean expected, MovementStatus status) {
            this.type = type;
            this.movementID = movementID;
            this.outLocation = outLocation;
            this.outTime = outTime;
            this.odoOut = odoOut;
            this.inLocation = inLocation;
            this.inTime = inTime;
            this.odoIn = odoIn;
            this.expected = expected;
            this.status = status;
        }
    }

    /**
     * The short code for a movement: ORA rental, CTM car transfer, WSM
     * workshop, NVM new vehicle, DVM defleet.
     */
    public String typeCode(Movement movement) {
        if (movement instanceof RentalMovement) {
            return "ORA";
        }
        if (movement instanceof CarTransferMovement) {
            return "CTM";
        }
        if (movement instanceof WorkshopMovement) {
            return "WSM";
        }
        if (movement instanceof NewCarMovement) {
            return "NVM";
        }
        if (movement instanceof DefleetMovement) {
            return "DVM";
        }
        return "???";
    }

    /**
     * Every movement of the vehicle with this plate, newest first (so the NVM
     * that brought it into the fleet is last). Throws with the reason if the
     * plate is empty or no vehicle has it.
     */
    public List<HistoryRow> vehicleHistory(String plateText) {
        String plate = plateText == null ? "" : plateText.trim().toUpperCase();
        if (plate.isEmpty()) {
            throw new IllegalArgumentException("Enter a rego.");
        }
        if (dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate)) == null) {
            throw new IllegalArgumentException("No vehicle with this rego.");
        }
        List<Movement> mine = dataStore.search(dataStore.getMovements(),
                m -> m.getVehicle().getPlateNumber().equals(plate));
        // movement IDs are handed out in order, so the biggest ID is the newest movement
        mine.sort(Comparator.comparingInt((Movement m) -> idNumber(m.getMovementID())).reversed());

        List<HistoryRow> rows = new java.util.ArrayList<>();
        for (Movement m : mine) {
            boolean closed = m.getStatus() == MovementStatus.CLOSED;
            boolean checkedIn = m.getCheckInDT() != null;
            boolean expected = !checkedIn && m instanceof DrivenMovement;
            String inLoc = null;
            LocalDateTime inTime = null;
            if (checkedIn) {
                inLoc = m.getCheckInLoc() == null ? null : m.getCheckInLoc().getName();
                inTime = m.getCheckInDT();
            } else if (expected) {
                DrivenMovement dm = (DrivenMovement) m;
                inLoc = dm.getExpectedCILoc() == null ? null : dm.getExpectedCILoc().getName();
                inTime = dm.getExpectedCIDT();
            }
            // a new vehicle or a defleet happens in one go, so the car has not travelled
            Integer odoIn = null;
            if (closed) {
                odoIn = (m instanceof NewCarMovement || m instanceof DefleetMovement) ? m.getOdoOut() : m.getOdoIn();
            }
            rows.add(new HistoryRow(typeCode(m), m.getMovementID(),
                    m.getCheckOutLoc() == null ? null : m.getCheckOutLoc().getName(), m.getCheckOutDT(),
                    m.getOdoOut(), inLoc, inTime, odoIn, expected, m.getStatus()));
        }
        return rows;
    }

    private int idNumber(String movementID) {
        try {
            return Integer.parseInt(movementID);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
