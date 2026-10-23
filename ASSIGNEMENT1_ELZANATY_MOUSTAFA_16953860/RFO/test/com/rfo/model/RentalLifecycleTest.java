package com.rfo.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

public class RentalLifecycleTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 9, 0);

    private Vehicle car() {
        return new Vehicle("MOU111", "Lexus", "CT-200", 2025, VehicleCategory.COMPACT, FuelType.HYBRID, false,
                1.0, 1000, 0, VehicleStatus.IN_USE, null, false);
    }

    // an open rental booked for 3 days, BASIC insurance, no RSA
    private RentalMovement openRental(Vehicle v) {
        return new RentalMovement("1", v, "STAFF1", null, START, null, null, null,
                1000, 0, MovementStatus.OPEN, List.of(1), START.plusDays(3), null,
                false, Insurance.BASIC, new HashMap<>(), 0);
    }

    // checks in at the given time, then closes with the final price, like the app does
    private void checkInAndClose(RentalMovement r, LocalDateTime returnAt, int odoIn) {
        Location loc = new Location("L1", "Auckland", null);
        r.checkIn("STAFF2", loc, returnAt);
        r.closeMovement(odoIn, r.calculateRentalCost());
    }

    // Check-in alone stops the clock and marks the rental INCOMPLETE; it is not closed yet
    @Test
    public void checkInMarksRentalIncompleteBeforeClosing() {
        RentalMovement r = openRental(car());
        Location loc = new Location("L1", "Auckland", null);

        r.checkIn("STAFF2", loc, START.plusDays(3));

        assertEquals(MovementStatus.INCOMPLETE, r.getStatus());
        assertEquals("STAFF2", r.getCIStaff());
        assertEquals(START.plusDays(3), r.getCheckInDT());
        assertSame(loc, r.getCheckInLoc());
    }

    // On time: closing sets the vehicle's odometer and location and charges the 3 booked days
    @Test
    public void closingOnTimeUpdatesVehicleAndChargesBookedDays() {
        Vehicle v = car();
        RentalMovement r = openRental(v);

        checkInAndClose(r, START.plusDays(3), 1600);

        assertEquals(MovementStatus.CLOSED, r.getStatus());
        assertEquals(1600, r.getOdoIn());
        assertEquals(1600, v.getCurrentOdometer());
        assertSame(r.getCheckInLoc(), v.getCurrentLocation());
        assertEquals(VehicleStatus.AVAILABLE, v.getStatus());
        assertEquals(3, r.getFinalCharges().days);
        assertEquals(150, r.getPrice(), 0.001);
        assertEquals(r.getPrice(), r.getFinalCharges().total, 0.001);
    }

    // Early: returned after 2 of the 3 booked days, so the final price is lower
    @Test
    public void closingEarlyChargesFewerDaysThanBooked() {
        RentalMovement r = openRental(car());
        double estimate = r.getRentalEstimate();

        checkInAndClose(r, START.plusDays(2), 1400);

        assertEquals(150, estimate, 0.001);
        assertEquals(2, r.getFinalCharges().days);
        assertEquals(100, r.getPrice(), 0.001);
        assertTrue(r.getPrice() < estimate);
    }

    // Late: returned after 4 of the 3 booked days, so the final price is higher
    @Test
    public void closingLateChargesExtraDay() {
        RentalMovement r = openRental(car());
        double estimate = r.getRentalEstimate();

        checkInAndClose(r, START.plusDays(4), 1800);

        assertEquals(4, r.getFinalCharges().days);
        assertEquals(200, r.getPrice(), 0.001);
        assertTrue(r.getPrice() > estimate);
    }
}
