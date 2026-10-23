package com.rfo.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RentalMovement extends DrivenMovement {

    private static final double RSA_DAILY_RATE = 9.0;

    private Insurance insuranceTier;
    private boolean rsa;
    private Map<Equipment, Integer> equipment;
    private double price;

    // new movement
    public RentalMovement(Vehicle vehicle,
            String COStaff,
            Location checkOutLoc,
            int odoOut,
            List<Integer> driverIDs,
            LocalDateTime expectedCIDT,
            Location expectedCILoc,
            boolean rsa,
            Insurance insuranceTier) {
        super(vehicle, COStaff, checkOutLoc, odoOut, driverIDs, expectedCIDT, expectedCILoc);
        this.rsa = rsa;
        this.insuranceTier = insuranceTier;
        this.equipment = new HashMap<>();
        this.vehicle.updateStatus(VehicleStatus.IN_USE);
    }

    // reconstructing rental movement from file
    public RentalMovement(String movementID, Vehicle vehicle, String COStaff, String CIStaff,
            LocalDateTime checkOutDT, LocalDateTime checkInDT,
            Location checkOutLoc, Location checkInLoc,
            int odoOut, int odoIn, MovementStatus status,
            List<Integer> driverIDs, LocalDateTime expectedCIDT, Location expectedCILoc,
            boolean rsa, Insurance insuranceTier, Map<Equipment, Integer> equipment, double price) {

        super(movementID, vehicle, COStaff, CIStaff, checkOutDT, checkInDT,
                checkOutLoc, checkInLoc, odoOut, odoIn, status,
                driverIDs, expectedCIDT, expectedCILoc);

        this.rsa = rsa;
        this.insuranceTier = insuranceTier;
        this.equipment = equipment;
        this.price = price;
    }

    public void addEquipment(Equipment item, int quantity) {
        this.equipment.merge(item, quantity, Integer::sum);
    }

    public void setInsuranceTier(Insurance insuranceTier) {
        this.insuranceTier = insuranceTier;
    }

    public void setRSA(boolean rsa) {
        this.rsa = rsa;
    }

    private int getDaysBetween(LocalDateTime start, LocalDateTime end) {
        long hours = ChronoUnit.HOURS.between(start, end);
        int days = (int) (hours / 24);
        long remainder = hours % 24;
        if (remainder > 1) {
            days++;
        }
        return Math.max(days, 1);
    }

    private double getInsuranceBaseRate(int days) {
        if (days <= 3) {
            return 60.0;
        } else if (days <= 7) {
            return 50.0;
        } else if (days <= 15) {
            return 40.0;
        } else {
            return 35.0;
        }
    }

    private double calculateEquipmentCost(int days) {
        double total = 0.0;
        for (Map.Entry<Equipment, Integer> entry : equipment.entrySet()) {
            total += entry.getKey().getDailyRate() * entry.getValue() * days;
        }
        return total;
    }

    private double calculateInsuranceCost(int days) {
        double insuranceCost = getInsuranceBaseRate(days) * insuranceTier.getRate() * days;
        double rsaCost = rsa ? RSA_DAILY_RATE * days : 0.0;
        return insuranceCost + rsaCost;
    }

    public double getRentalEstimate() {
        int days = getDaysBetween(checkOutDT, expectedCIDT);
        double base = vehicle.getRate() * Vehicle.getBaseDailyRate() * days;
        return base + calculateEquipmentCost(days) + calculateInsuranceCost(days);
    }

    public double calculateRentalCost() {
        int days = getDaysBetween(checkOutDT, checkInDT);
        double base = vehicle.getRate() * Vehicle.getBaseDailyRate() * days;
        return base + calculateEquipmentCost(days) + calculateInsuranceCost(days);
    }

    public void closeMovement(int odoIn, double finalPrice) {
        this.price = finalPrice;
        super.closeMovement(odoIn);
    }

    public double getPrice() {
        return price;
    }

    public Insurance getInsuranceTier() {
        return insuranceTier;
    }

    public boolean hasRSA() {
        return rsa;
    }

    public Map<Equipment, Integer> getEquipment() {
        return equipment;
    }
}
