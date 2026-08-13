package com.rfo.model;

public class Driver implements Person {

    private String name;
    private String licenseNumber;
    private String email;
    private String phoneNumber;
    private Address address;

    public Driver(String name, String licenseNumber, String email, String phoneNumber, Address address) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getContactInfo() {
        return "Phone: " + phoneNumber + "E-mail: " + email;
    }
}
