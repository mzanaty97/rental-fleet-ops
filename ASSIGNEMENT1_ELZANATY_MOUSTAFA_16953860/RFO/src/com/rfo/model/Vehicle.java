package com.rfo.model;

public class Vehicle implements Rentable, Serviceable, Comparable<Vehicle> {

    private static double baseDailyRate = 50.0;
    private String plateNumber;
    private String make;
    private String model;
    private int year;
    private VehicleCategory category;
    private double rate;
    private boolean hasGPS;
    private FuelType fuelType;
    private int currentOdometer;
    private int lastServiceOdometer;
    private VehicleStatus status;
    private Location currentLocation;
    private boolean serviceFlag;

    // new vehicle
    public Vehicle(String plateNumber, String make, String model, int year,
            VehicleCategory category, FuelType fuelType, boolean hasGPS) {

        this(plateNumber, make, model, year, category, fuelType, hasGPS,
                0.0, 0, 0, VehicleStatus.AVAILABLE, null, false);
    }

    // reconstructing vehicle from file
    public Vehicle(String plateNumber, String make, String model, int year,
            VehicleCategory category, FuelType fuelType, boolean hasGPS,
            double rate, int currentOdometer, int lastServiceOdometer,
            VehicleStatus status, Location currentLocation, boolean service) {

        this.plateNumber = plateNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.category = category;
        this.fuelType = fuelType;
        this.hasGPS = hasGPS;
        this.rate = rate;
        this.currentOdometer = currentOdometer;
        this.lastServiceOdometer = lastServiceOdometer;
        this.status = status;
        this.currentLocation = currentLocation;
        this.serviceFlag = service;
    }

    public static double getBaseDailyRate() {
        return baseDailyRate;
    }

    public static void setBaseDailyRate(double newRate) {
        baseDailyRate = newRate;
    }

    public void updateStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean hasGPS() {
        return hasGPS;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public int getCurrentOdometer() {
        return currentOdometer;
    }

    public void setCurrentOdometer(int currentOdometer) {
        this.currentOdometer = currentOdometer;
    }

    public int getLastServiceOdometer() {
        return lastServiceOdometer;
    }

    public void setLastServiceOdometer(int lastServiceOdometer) {
        this.lastServiceOdometer = lastServiceOdometer;
    }

    /**
     * The status shown to the user: an available vehicle that is overdue for
     * service shows as SERVICE_DUE.
     */
    public VehicleStatus getStatus() {
        if (status == VehicleStatus.AVAILABLE && serviceFlag) {
            return VehicleStatus.SERVICE_DUE;
        }
        return status;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public int getYear() {
        return this.year;
    }

    public int getServiceInterval() {
        switch (category) {
            case COMPACT:
                return 10000;
            case INTERMEDIATE:
                return 10000;
            case STANDARD:
                return 15000;
            case PREMIUM:
                return 15000;
            case SPECIAL:
                return 20000;
            default:
                return 10000;
        }
    }

    @Override
    public boolean isAvailable() {
        // a vehicle that is overdue for service cannot be rented, so it is not available
        return this.status == VehicleStatus.AVAILABLE && !serviceFlag;
    }

    @Override
    public boolean isDueForService() {
        return serviceFlag;
    }

    public void updateServiceFlag() {
        if ((currentOdometer - lastServiceOdometer) >= getServiceInterval()) {
            serviceFlag = true;
        }
    }

    public int getOverdueKm() {
        return (currentOdometer - lastServiceOdometer) - getServiceInterval();
    }

    @Override
    public void markServiced() {
        lastServiceOdometer = currentOdometer;
        this.serviceFlag = false;
        this.status = VehicleStatus.AVAILABLE;
    }

    @Override
    public int compareTo(Vehicle o) {
        return Double.compare(this.rate, o.rate);
    }
}
