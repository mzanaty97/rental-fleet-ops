package com.rfo.service;

import com.rfo.db.DatabaseSeeder;
import com.rfo.io.DriverDAO;
import com.rfo.io.InvoiceDAO;
import com.rfo.io.LocationDAO;
import com.rfo.io.MovementDAO;
import com.rfo.io.StaffDAO;
import com.rfo.io.VehicleDAO;
import com.rfo.model.Driver;
import com.rfo.model.Invoice;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.Staff;
import com.rfo.model.Vehicle;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DataStore {

    private List<Driver> drivers = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<Movement> movements = new ArrayList<>();
    private List<Staff> staff = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Invoice> invoices = new ArrayList<>();

    private final LocationDAO locationDAO = new LocationDAO();
    private final DriverDAO driverDAO = new DriverDAO();
    private final MovementDAO movementDAO = new MovementDAO();
    private final StaffDAO staffDAO = new StaffDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

    public DataStore() {
        // first run only: copy the Part 1 text files into an empty database
        new DatabaseSeeder().seedIfEmpty();

        // loading in order Locations -> drivers -> vehicles -> staff -> movements
        this.locations = locationDAO.loadAll();
        this.drivers = driverDAO.loadAll();
        this.vehicles = vehicleDAO.loadAll(locations);
        this.staff = staffDAO.loadAll(locations);
        this.movements = movementDAO.loadAll(vehicles, locations);
        this.invoices = invoiceDAO.loadAll();
    }

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ModelListener listener : listeners) {
            listener.onModelChanged();
        }
    }

    public void saveLocations() {
        locationDAO.saveAll(locations);
        notifyListeners();
    }

    public List<Location> getLocations() {
        return this.locations;
    }

    public void saveDrivers() {
        driverDAO.saveAll(drivers);
        notifyListeners();
    }

    public List<Driver> getDrivers() {
        return this.drivers;
    }

    public void saveVehicles() {
        vehicleDAO.saveAll(vehicles);
        notifyListeners();
    }

    public List<Vehicle> getVehicles() {
        return this.vehicles;
    }

    public void saveStaff() {
        staffDAO.saveAll(staff);
        notifyListeners();
    }

    public List<Staff> getStaff() {
        return this.staff;
    }

    public void saveMovements() {
        movementDAO.saveAll(movements);
        notifyListeners();
    }

    public List<Movement> getMovements() {
        return this.movements;
    }

    public void saveInvoices() {
        invoiceDAO.saveAll(invoices);
        notifyListeners();
    }

    public List<Invoice> getInvoices() {
        return this.invoices;
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
        return staffDAO.authenticate(staffId, passwrod);
    }

    public void addNewStaffLine(Staff newStaff, String password) {
        staffDAO.addNewStaffLine(newStaff, password);
    }

    public boolean updateStaffPassword(String staffID, String newPassword) {
        return staffDAO.updatePassword(staffID, newPassword);
    }
}
