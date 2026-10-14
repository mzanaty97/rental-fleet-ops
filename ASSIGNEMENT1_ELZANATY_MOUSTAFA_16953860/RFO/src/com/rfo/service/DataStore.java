package com.rfo.service;

import com.rfo.io.DriverFileHandler;
import com.rfo.io.LocationFileHandler;
import com.rfo.io.MovementFileHandler;
import com.rfo.io.StaffFileHandler;
import com.rfo.io.VehicleFileHandler;
import com.rfo.model.Driver;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DataStore {

    private List<Driver> drivers = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<Movement> movements = new ArrayList<>();
    private List<Staff> staff = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();

    private final LocationFileHandler locationFileHandler = new LocationFileHandler();
    private final DriverFileHandler driverFileHandler = new DriverFileHandler();
    private final MovementFileHandler movementFileHandler = new MovementFileHandler();
    private final StaffFileHandler staffFileHandler = new StaffFileHandler();
    private final VehicleFileHandler vehicleFileHandler = new VehicleFileHandler();

    public DataStore() {
        // loading in order Locations -> drivers -> vehicles -> staff -> movements

        this.locations = locationFileHandler.loadAll();
        this.drivers = driverFileHandler.loadAll();
        this.vehicles = vehicleFileHandler.loadAll(locations);
        this.staff = staffFileHandler.loadAll(locations);
        this.movements = movementFileHandler.loadAll(vehicles, locations);
    }

    public void saveLocations() {
        locationFileHandler.saveAll(locations);
    }

    public List<Location> getLocations() {
        return this.locations;
    }

    public void saveDrivers() {
        driverFileHandler.saveAll(drivers);
    }

    public List<Driver> getDrivers() {
        return this.drivers;
    }

    public void saveVehicles() {
        vehicleFileHandler.saveAll(vehicles);
    }

    public List<Vehicle> getVehicles() {
        return this.vehicles;
    }

    public void saveStaff() {
        staffFileHandler.saveAll(staff);
    }

    public List<Staff> getStaff() {
        return this.staff;
    }

    public void saveMovements() {
        movementFileHandler.saveAll(movements);
    }

    public List<Movement> getMovements() {
        return this.movements;
    }

    public <T> List<T> search(List<T> list, Predicate<T> condition) {
        List<T> results = new ArrayList<>();
        for (T item : list) {
            if (condition.test(item)) {
                results.add(item);
            }
        }
        return results;
    }

    public <T> T getBy(List<T> list, Predicate<T> condition) {
        for (T item : list) {
            if (condition.test(item)) {
                return item;
            }
        }
        return null;
    }

    public boolean authenticate(String staffId, String passwrod) {
        return staffFileHandler.authenticate(staffId, passwrod);
    }

    public void addNewStaffLine(Staff newStaff, String password) {
        staffFileHandler.addNewStaffLine(newStaff, password);
    }

    public boolean updateStaffPassword(String staffID, String newPassword) {
        return staffFileHandler.updatePassword(staffID, newPassword);
    }
}
