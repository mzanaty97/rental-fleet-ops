package com.rfo.service;

import com.rfo.model.DefleetMovement;
import com.rfo.model.FuelType;
import com.rfo.model.NewCarMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Generated with assistance from Claude (modified existing class): I wrote this class in Part 1; Claude helped me move more of the business rules into it in Part 2 (see the method notes) so the GUI and the console menus share them.
/**
 * Business rules for vehicles: adding, finding, listing, changing the status of and
 * defleeting vehicles, with checks on the plate and year. Problems are reported with
 * IllegalArgumentException.
 */
public class VehicleService {

    private DataStore dataStore;

    public VehicleService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * The earliest model year accepted for a new vehicle.
     */
    public static final int MIN_YEAR = 1990;

    // Generated with assistance from Claude: checks whether a plate is already in the fleet.
    /**
     * True when a vehicle with this plate is already in the fleet (defleeted
     * ones count too).
     */
    public boolean plateExists(String plateNumber) {
        String plate = plateNumber == null ? "" : plateNumber.trim().toUpperCase();
        return dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate)) != null;
    }

    // Generated with assistance from Claude: checks the plate format and returns it tidied up.
    /**
     * Checks a plate for a new vehicle: letters and digits only, 2 to 8
     * characters, not already used.
     */
    public String validatePlate(String plateNumber) {
        String plate = plateNumber == null ? "" : plateNumber.trim().toUpperCase();
        if (plate.isEmpty()) {
            throw new IllegalArgumentException("Enter a plate number.");
        }
        if (!plate.matches("[A-Z0-9]{2,8}")) {
            throw new IllegalArgumentException("Plate must be 2 to 8 letters or digits, no spaces.");
        }
        if (plateExists(plate)) {
            throw new IllegalArgumentException("This plate is already in the fleet.");
        }
        return plate;
    }

    // Generated with assistance from Claude: turns the typed year into a number and checks it is sensible.
    /**
     * Turns the typed year into a number. Throws with the reason if it is not a
     * sensible model year.
     */
    public int parseYear(String text) {
        String t = text == null ? "" : text.trim();
        if (!t.matches("\\d{4}")) {
            throw new IllegalArgumentException("Enter a 4-digit year, for example 2024.");
        }
        int year = Integer.parseInt(t);
        int max = java.time.Year.now().getValue() + 1;
        if (year < MIN_YEAR || year > max) {
            throw new IllegalArgumentException("Year must be between " + MIN_YEAR + " and " + max + ".");
        }
        return year;
    }

    /**
     * Adds a vehicle at the intake depot. Throws with the reason if anything is
     * wrong.
     */
    public Vehicle addVehicle(String plateNumber, String make, String model, int year,
            VehicleCategory category, FuelType fuelType, boolean hasGPS, String staffID) {

        String plate = validatePlate(plateNumber);
        if (make == null || make.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the make.");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter the model.");
        }
        parseYear(String.valueOf(year));
        if (category == null) {
            throw new IllegalArgumentException("Choose a category.");
        }
        if (fuelType == null) {
            throw new IllegalArgumentException("Choose a fuel type.");
        }

        Vehicle newVehicle = new Vehicle(plate, make.trim(), model.trim(), year, category, fuelType, hasGPS);
        newVehicle.setRate(getDefaultRate(category));

        NewCarMovement ncm = new NewCarMovement(newVehicle, staffID);
        ncm.execute();

        dataStore.getVehicles().add(newVehicle);
        dataStore.getMovements().add(ncm);

        dataStore.saveVehicles();
        dataStore.saveMovements();

        return newVehicle;
    }

    // Generated with assistance from Claude: lists the vehicles that can be defleeted.
    /**
     * The vehicles that could be defleeted: not already defleeted and not out
     * on a movement.
     */
    public List<Vehicle> getVehiclesForDefleet() {
        List<Vehicle> found = dataStore.search(dataStore.getVehicles(),
                v -> v.getStatus() != VehicleStatus.DEFLEETED && v.getStatus() != VehicleStatus.IN_USE
                && v.getStatus() != VehicleStatus.WORKSHOP);
        found.sort(Comparator.comparing(Vehicle::getPlateNumber));
        return found;
    }

    // Generated with assistance from Claude: finds and checks the vehicle chosen to defleet.
    /**
     * Finds a vehicle that can be defleeted: it must exist, not already be
     * defleeted, and not be out.
     */
    public Vehicle findVehicleForDefleet(String plateNumber) {
        String plate = plateNumber == null ? "" : plateNumber.trim().toUpperCase();
        if (plate.isEmpty()) {
            throw new IllegalArgumentException("Enter a plate number.");
        }
        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(), v -> v.getPlateNumber().equals(plate));
        if (vehicle == null) {
            throw new IllegalArgumentException("No vehicle found with that plate number.");
        }
        if (vehicle.getStatus() == VehicleStatus.DEFLEETED) {
            throw new IllegalArgumentException("That vehicle has already been defleeted.");
        }
        if (vehicle.getStatus() == VehicleStatus.IN_USE || vehicle.getStatus() == VehicleStatus.WORKSHOP) {
            throw new IllegalArgumentException("That vehicle is out (" + vehicle.getStatus()
                    + "). Check it in before defleeting.");
        }
        return vehicle;
    }

    // Generated with assistance from Claude: (modified existing method) now returns the vehicle and throws IllegalArgumentException instead of returning a boolean.
    /**
     * Defleets a vehicle. Throws with the reason if it cannot be.
     */
    public Vehicle defleetVehicle(String plateNumber, String staffID) {
        Vehicle vehicle = findVehicleForDefleet(plateNumber);

        DefleetMovement dvm = new DefleetMovement(vehicle, staffID);
        dvm.execute();

        dataStore.getMovements().add(dvm);

        dataStore.saveVehicles();
        dataStore.saveMovements();

        return vehicle;
    }

    // available vehicles, optionally for one category (null = every category)
    // Generated with assistance from Claude: lists the vehicles that can be rented, optionally for one category.
    public List<Vehicle> getAvailableVehicles(VehicleCategory category) {
        List<Vehicle> available = dataStore.search(dataStore.getVehicles(), Vehicle::isAvailable);

        if (category != null) {
            available = dataStore.search(available, v -> v.getCategory() == category);
        }

        Comparator<Vehicle> comparator;
        if (category == null) {
            comparator = Comparator.comparing(Vehicle::getCategory)
                    .thenComparingInt(Vehicle::getCurrentOdometer)
                    .thenComparing(Vehicle::getPlateNumber);
        } else {
            comparator = Comparator.comparingInt(Vehicle::getCurrentOdometer)
                    .thenComparing(Vehicle::getPlateNumber);
        }
        available.sort(comparator);
        return available;
    }

    // vehicles flagged as due for service, optionally for one category
    // Generated with assistance from Claude: lists the vehicles that are due for service.
    public List<Vehicle> getServiceDueVehicles(VehicleCategory category) {
        List<Vehicle> due = dataStore.search(dataStore.getVehicles(), Vehicle::isDueForService);

        if (category != null) {
            due = dataStore.search(due, v -> v.getCategory() == category);
        }
        return due;
    }

    // the whole fleet, optionally for one category
    // Generated with assistance from Claude: lists vehicles, optionally for one category.
    public List<Vehicle> getAllVehicles(VehicleCategory category) {
        List<Vehicle> all = dataStore.search(dataStore.getVehicles(),
                v -> category == null || v.getCategory() == category);

        all.sort(Comparator.comparing(Vehicle::getCategory)
                .thenComparing(Vehicle::getPlateNumber));
        return all;
    }

    // Generated with assistance from Claude: lists the statuses a user may set by hand.
    /**
     * The statuses a person may set by hand. IN_USE is left out because only a
     * rental movement puts a vehicle in use.
     */
    public List<VehicleStatus> getManualStatuses() {
        List<VehicleStatus> statuses = new ArrayList<>();
        for (VehicleStatus status : VehicleStatus.values()) {
            if (status != VehicleStatus.IN_USE) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    // Generated with assistance from Claude: changes a vehicle's status.
    /**
     * Changes the status of the vehicle with the given plate and saves it.
     *
     * @return true if the status changed, false if it was already that status
     * @throws IllegalArgumentException if the plate or status is missing or no
     * such vehicle exists
     */
    public boolean updateVehicleStatus(String plateNumber, VehicleStatus newStatus) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Plate number is required.");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("A new status is required.");
        }

        if (newStatus == VehicleStatus.IN_USE) {
            throw new IllegalArgumentException("IN_USE is set by rental movements and cannot be chosen here.");
        }

        String plate = plateNumber.trim().toUpperCase();
        Vehicle vehicle = dataStore.getBy(dataStore.getVehicles(),
                v -> v.getPlateNumber().equals(plate));

        if (vehicle == null) {
            throw new IllegalArgumentException("No vehicle with plate number " + plate + ".");
        }

        if (vehicle.getStatus() == newStatus) {
            return false;
        }

        vehicle.updateStatus(newStatus);
        dataStore.saveVehicles();
        return true;
    }

    private double getDefaultRate(VehicleCategory category) {
        switch (category) {
            case COMPACT:
                return 0.75;
            case INTERMEDIATE:
                return 0.90;
            case STANDARD:
                return 1.0;
            case PREMIUM:
                return 1.6;
            case SPECIAL:
                return 1.8;
            default:
                return 1.0;
        }
    }

    // Generated with assistance from Claude: lists the vehicles at a location.
    /**
     * Every vehicle currently at this location, lowest odometer first.
     */
    public List<Vehicle> getLocationFleet(String locationID) {
        List<Vehicle> fleet = dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentLocation() != null && v.getCurrentLocation().getLocationID().equals(locationID));
        fleet.sort(Comparator.comparingInt(Vehicle::getCurrentOdometer).thenComparing(Vehicle::getCategory));
        return fleet;
    }

    // Generated with assistance from Claude: lists the vehicles that have not moved yet.
    /**
     * Near new cars: available and 30 km or less on the odometer, grouped by
     * location.
     */
    public List<Vehicle> getNewVehicles() {
        List<Vehicle> cars = dataStore.search(dataStore.getVehicles(),
                v -> v.getCurrentOdometer() <= 30 && v.getStatus().equals(VehicleStatus.AVAILABLE));
        cars.sort(Comparator
                .comparing((Vehicle v) -> v.getCurrentLocation() != null ? v.getCurrentLocation().getLocationID() : "")
                .thenComparingInt(Vehicle::getCurrentOdometer)
                .thenComparing(Vehicle::getPlateNumber));
        return cars;
    }
}
