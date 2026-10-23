package com.rfo.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class VehicleTest {

    private Vehicle car(int odo, VehicleStatus status) {
        return new Vehicle("MOU111", "Lexus", "CT-200", 2025, VehicleCategory.COMPACT, FuelType.HYBRID, false,
                1.0, odo, 0, status, null, false);
    }

    // A vehicle that is AVAILABLE and not overdue can be rented
    @Test
    public void availableVehicleIsRentable() {
        assertTrue(car(1000, VehicleStatus.AVAILABLE).isAvailable());
    }

    // A vehicle in the workshop (not ready) cannot be rented
    @Test
    public void vehicleInWorkshopIsNotAvailable() {
        assertFalse(car(1000, VehicleStatus.WORKSHOP).isAvailable());
    }

    // A vehicle already rented out cannot be rented again
    @Test
    public void vehicleInUseIsNotAvailable() {
        assertFalse(car(1000, VehicleStatus.IN_USE).isAvailable());
    }

    // At 10000 km since the last service a compact is due and no longer available
    @Test
    public void vehicleAtServiceIntervalIsDueAndNotAvailable() {
        Vehicle v = car(10000, VehicleStatus.AVAILABLE);
        v.updateServiceFlag();

        assertTrue(v.isDueForService());
        assertFalse(v.isAvailable());
        assertEquals(VehicleStatus.SERVICE_DUE, v.getStatus());
    }

    // One km under the interval is still fine
    @Test
    public void vehicleJustUnderServiceIntervalIsNotDue() {
        Vehicle v = car(9999, VehicleStatus.AVAILABLE);
        v.updateServiceFlag();

        assertFalse(v.isDueForService());
        assertTrue(v.isAvailable());
    }

    // Overdue km is how far past the interval the vehicle has gone
    @Test
    public void overdueKmIsDistancePastTheInterval() {
        Vehicle v = car(10500, VehicleStatus.AVAILABLE);
        assertEquals(500, v.getOverdueKm());
    }

    // Servicing a vehicle clears the flag and makes it available again
    @Test
    public void markServicedMakesVehicleAvailableAgain() {
        Vehicle v = car(10000, VehicleStatus.AVAILABLE);
        v.updateServiceFlag();
        v.markServiced();

        assertFalse(v.isDueForService());
        assertTrue(v.isAvailable());
    }
}
