package com.rfo.model;

import java.time.LocalDateTime;
import static org.junit.Assert.*;
import org.junit.Test;

public class RentalMovementTest {

    private Vehicle car() {
        return new Vehicle("MOU111", "Lexus", "CT-200", 2025, VehicleCategory.COMPACT, FuelType.HYBRID, false,
                1.0, 1000, 0, VehicleStatus.AVAILABLE, null, false);
    }

    //Quote correctly calculated when RSA and PREMIUM insurance is applied
    @Test
    public void quoteForThreeDaysWithPremiumInsAndRSA() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime end = start.plusDays(3);

        RentalMovement.Quote q = RentalMovement.quote(car(), start, end, true, Insurance.PREMIUM);
        assertEquals(3, q.days);
        assertEquals(150, q.base, 0.001);
        assertEquals(180, q.insurance, 0.001);
        assertEquals(27, q.rsa, 0.001);
        assertEquals(357, q.total, 0.001);

    }

    //Quote correctly calculates rate for rental when no insurance is added
    @Test
    public void quoteForTwoDaysWithBasicOnly() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime end = start.plusDays(2);

        RentalMovement.Quote q = RentalMovement.quote(car(), start, end, false, Insurance.BASIC);

        assertEquals(2, q.days);
        assertEquals(100, q.base, 0.001);
        assertEquals(0.0, q.insurance, 0.001);
        assertEquals(0.0, q.rsa, 0.001);
        assertEquals(100, q.total, 0.001);
    }

    //Insurance Rate changes to a cheaper bracket if rental is more than 3 days
    @Test
    public void quoteForFourDaysUsesTheCheaperInsuranceBand() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime end = start.plusDays(4);

        RentalMovement.Quote q = RentalMovement.quote(car(), start, end, false, Insurance.PREMIUM);

        assertEquals(4, q.days);
        assertEquals(200, q.base, 0.001);
        assertEquals(200, q.insurance, 0.001);
        assertEquals(0.0, q.rsa, 0.001);
        assertEquals(400, q.total, 0.001);
    }

    // Charging for 2 days since rental time is more than 24 hours.
    @Test
    public void quoteCountsTwoDaysFor26Hours() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 1, 9, 0);
        LocalDateTime end = start.plusDays(1);
        end = end.plusHours(2);

        RentalMovement.Quote q = RentalMovement.quote(car(), start, end, false, Insurance.BASIC);

        assertEquals(2, q.days);
        assertEquals(100, q.base, 0.001);
        assertEquals(0.0, q.insurance, 0.001);
        assertEquals(0.0, q.rsa, 0.001);
        assertEquals(100, q.total, 0.001);
    }
}
