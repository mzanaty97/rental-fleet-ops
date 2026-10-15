package com.rfo.model;

import java.time.LocalDateTime;

/**
 * A saved invoice for one closed rental. Everything is copied in as plain
 * values when the invoice is made (a snapshot), so reprinting it later always
 * shows exactly what the customer was charged, even if vehicle rates or names
 * change afterwards.
 */
public class Invoice {

    private final int invoiceID;
    private final String movementID;
    private final LocalDateTime issuedAt;
    private final String plate;
    private final String vehicle;
    private final String drivers;
    private final String checkOut;
    private final String checkIn;
    private final int odoOut;
    private final int odoIn;
    private final int days;
    private final double base;
    private final double equipment;
    private final String insuranceTier;
    private final double insurance;
    private final double rsa;
    private final double total;
    private final String coStaff;
    private final String ciStaff;

    public Invoice(int invoiceID, String movementID, LocalDateTime issuedAt, String plate, String vehicle,
            String drivers, String checkOut, String checkIn, int odoOut, int odoIn, int days, double base,
            double equipment, String insuranceTier, double insurance, double rsa, double total,
            String coStaff, String ciStaff) {
        this.invoiceID = invoiceID;
        this.movementID = movementID;
        this.issuedAt = issuedAt;
        this.plate = plate;
        this.vehicle = vehicle;
        this.drivers = drivers;
        this.checkOut = checkOut;
        this.checkIn = checkIn;
        this.odoOut = odoOut;
        this.odoIn = odoIn;
        this.days = days;
        this.base = base;
        this.equipment = equipment;
        this.insuranceTier = insuranceTier;
        this.insurance = insurance;
        this.rsa = rsa;
        this.total = total;
        this.coStaff = coStaff;
        this.ciStaff = ciStaff;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public String getMovementID() {
        return movementID;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public String getPlate() {
        return plate;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getDrivers() {
        return drivers;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public int getOdoOut() {
        return odoOut;
    }

    public int getOdoIn() {
        return odoIn;
    }

    public int getDays() {
        return days;
    }

    public double getBase() {
        return base;
    }

    public double getEquipment() {
        return equipment;
    }

    public String getInsuranceTier() {
        return insuranceTier;
    }

    public double getInsurance() {
        return insurance;
    }

    public double getRsa() {
        return rsa;
    }

    public double getTotal() {
        return total;
    }

    public String getCoStaff() {
        return coStaff;
    }

    public String getCiStaff() {
        return ciStaff;
    }

    /**
     * The invoice number as shown to people, for example INV-0007.
     */
    public String getNumber() {
        return String.format("INV-%04d", invoiceID);
    }
}
