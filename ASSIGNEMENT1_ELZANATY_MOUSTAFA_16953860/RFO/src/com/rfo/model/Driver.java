package com.rfo.model;

public class Driver implements Person {

    private static int LATEST_DRIVER_ID;

    private int driverID;
    private String fName;
    private String lName;
    private String licenseNumber;
    private String email;
    private String phoneNumber;
    private Address address;

    // new driver
    public Driver(String fName, String lName, String licenseNumber, String email, String phoneNumber, Address address) {
        this.driverID = LATEST_DRIVER_ID++;
        this.fName = fName;
        this.lName = lName;
        this.licenseNumber = licenseNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // reconstructing driver from file
    public Driver(int driverID, String fName, String lName, String licenseNumber, String email, String phoneNumber, Address address) {
        this.driverID = driverID;
        this.fName = fName;
        this.lName = lName;
        this.licenseNumber = licenseNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public static void setLatestDriverID(int latest) {
        LATEST_DRIVER_ID = latest;
    }

    public int getDriverID() {
        return driverID;
    }

    @Override
    public String getName() {
        return this.fName + " " + this.lName;
    }

    public String getFName() {
        return this.fName;
    }

    public String getLName() {
        return this.lName;
    }

    @Override
    public String getContactInfo() {
        return email + ", " + phoneNumber + ", " + address.getFullAddress();
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Address getAddress() {
        return address;
    }
}
