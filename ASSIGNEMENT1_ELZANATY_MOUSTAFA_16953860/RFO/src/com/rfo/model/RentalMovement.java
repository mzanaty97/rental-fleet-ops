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

    private static int getDaysBetween(LocalDateTime start, LocalDateTime end) {
        long hours = ChronoUnit.HOURS.between(start, end);
        int days = (int) (hours / 24);
        long remainder = hours % 24;
        if (remainder > 1) {
            days++;
        }
        return Math.max(days, 1);
    }

    private static double getInsuranceBaseRate(int days) {
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

    /**
     * A price worked out before the rental exists: the days, and what each part
     * of the price comes to.
     */
    public static class Quote {

        public final int days;
        public final double base;
        public final double insurance;
        public final double rsa;
        public final double equipment;
        public final double total;

        private Quote(int days, double base, double insurance, double rsa, double equipment) {
            this.days = days;
            this.base = base;
            this.insurance = insurance;
            this.rsa = rsa;
            this.equipment = equipment;
            this.total = base + insurance + rsa + equipment;
        }
    }

    /**
     * Prices a rental that has not started yet (no equipment), using the same
     * rules as the real estimate.
     */
    public static Quote quote(Vehicle vehicle, LocalDateTime start, LocalDateTime end, boolean rsa, Insurance tier) {
        int days = getDaysBetween(start, end);
        double base = vehicle.getRate() * Vehicle.getBaseDailyRate() * days;
        double insurance = getInsuranceBaseRate(days) * tier.getRate() * days;
        double rsaCost = rsa ? RSA_DAILY_RATE * days : 0.0;
        return new Quote(days, base, insurance, rsaCost, 0.0);
    }

    /**
     * What each part of the price came to once the rental was checked in (days,
     * base, insurance, RSA, equipment). The total matches
     * calculateRentalCost(). Used to write the invoice.
     */
    public Quote getFinalCharges() {
        int days = getDaysBetween(checkOutDT, checkInDT);
        double base = vehicle.getRate() * Vehicle.getBaseDailyRate() * days;
        double insurance = getInsuranceBaseRate(days) * insuranceTier.getRate() * days;
        double rsaCost = rsa ? RSA_DAILY_RATE * days : 0.0;
        return new Quote(days, base, insurance, rsaCost, calculateEquipmentCost(days));
    }

    public double getRentalEstimate() {
        return estimateFor(expectedCIDT);
    }

    /**
     * What the rental would cost if it were returned at the given time (used to
     * preview an amendment).
     */
    public double estimateFor(LocalDateTime returnAt) {
        int days = getDaysBetween(checkOutDT, returnAt);
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
