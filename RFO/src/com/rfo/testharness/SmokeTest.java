package com.rfo.testharness;

import com.rfo.model.Address;
import com.rfo.model.CountryCode;
import com.rfo.model.DefleetMovement;
import com.rfo.model.FuelType;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.NewCarMovement;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import com.rfo.model.StaffRole;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;
import com.rfo.service.AuthService;
import com.rfo.service.DataStore;
import com.rfo.service.LocationService;
import com.rfo.service.MovementService;
import com.rfo.service.StaffService;
import com.rfo.service.VehicleService;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end smoke test for the RFO service layer.
 *
 * This is a standalone harness only - it does not modify anything in
 * com.rfo.model, com.rfo.io, or com.rfo.service. It builds test data
 * in memory and drives the service layer exactly as a real caller would.
 *
 * Run via the project's Ant build, e.g.:
 *   ant run -Dmain.class=com.rfo.testharness.SmokeTest
 */
public class SmokeTest {

    private static final List<StepResult> RESULTS = new ArrayList<>();

    public static void main(String[] args) {
        // StaffFileHandler/VehicleFileHandler/MovementFileHandler write directly
        // to "data/..." with no mkdirs() of their own, so the directory has to
        // exist before any service call that saves to disk is made.
        new File("data").mkdirs();

        // Unique suffix so re-running the harness doesn't collide with staff/
        // vehicle records left behind on disk by a previous run.
        String runID = Long.toString(System.currentTimeMillis());

        DataStore dataStore = null;
        StaffService staffService;
        AuthService authService;
        VehicleService vehicleService;
        MovementService movementService;
        LocationService locationService;

        // ---- Step 1: create DataStore ----
        try {
            dataStore = new DataStore();
            pass(1, "Create DataStore instance", "non-null DataStore", "DataStore created");
        } catch (Exception e) {
            fail(1, "Create DataStore instance", "non-null DataStore", "threw " + describe(e));
            printSummary();
            return;
        }

        staffService = new StaffService(dataStore);
        authService = new AuthService(dataStore);
        vehicleService = new VehicleService(dataStore);
        movementService = new MovementService(dataStore);
        locationService = new LocationService(dataStore);

        // ---- Step 2: LocationService.addLocation(...) ----
        // ID intentionally matches NewCarMovement.INTAKE_DEPOT ("AKLC99") - that's
        // where addVehicle() actually places a freshly fleeted-in vehicle, so this
        // lets step 8's startRA() find the vehicle "at" a location we control.
        // addLocation() persists to data/locations.txt, so on a rerun against a
        // non-cleared data dir this ID will already exist; fall back to the
        // already-loaded Location in that case rather than treating it as failure.
        Location testLocation = null;
        try {
            testLocation = locationService.addLocation("AKLC99", "Test HQ",
                    new Address("1 Test Street", "Auckland", CountryCode.NZ, "1010"));

            if (testLocation == null) {
                testLocation = dataStore.getBy(dataStore.getLocations(),
                        l -> l.getLocationID().equals("AKLC99"));
            }

            boolean ok = testLocation != null && dataStore.getLocations().contains(testLocation);
            if (ok) {
                pass(2, "LocationService.addLocation(...)",
                        "location present in dataStore.getLocations()", "location present in list");
            } else {
                fail(2, "LocationService.addLocation(...)",
                        "location present in dataStore.getLocations()", "location NOT present in list");
            }
        } catch (Exception e) {
            fail(2, "LocationService.addLocation(...)",
                    "location present in dataStore.getLocations()", "threw " + describe(e));
        }

        // ---- Step 3: StaffService.addStaff(...) with MANAGER role ----
        String staffID = "SM" + runID;
        String correctPassword = "Sm0keTest!";
        Staff staff = null;
        try {
            staff = staffService.addStaff(staffID, correctPassword, "Alex", "Manager", StaffRole.MANAGER);

            boolean nonNull = staff != null;
            boolean inList = nonNull && dataStore.getStaff().contains(staff);
            if (nonNull && inList) {
                pass(3, "StaffService.addStaff(...) with MANAGER role",
                        "non-null Staff, present in dataStore.getStaff()",
                        "Staff " + staff.getStaffID() + " created and present in list");
            } else {
                fail(3, "StaffService.addStaff(...) with MANAGER role",
                        "non-null Staff, present in dataStore.getStaff()",
                        "addStaff returned " + (nonNull ? "non-null but not present in list" : "null"));
            }
        } catch (Exception e) {
            fail(3, "StaffService.addStaff(...) with MANAGER role",
                    "non-null Staff, present in dataStore.getStaff()", "threw " + describe(e));
        }

        // ---- Step 4: AuthService.login with correct password ----
        if (staff != null) {
            try {
                Staff loggedIn = authService.login(staffID, correctPassword);
                if (loggedIn != null && loggedIn.getStaffID().equals(staffID)) {
                    pass(4, "AuthService.login(staffID, correctPassword)",
                            "same Staff object, staffID=" + staffID,
                            "returned Staff staffID=" + loggedIn.getStaffID()
                                    + (loggedIn == staff ? " (same object)" : " (different object!)"));
                } else {
                    fail(4, "AuthService.login(staffID, correctPassword)",
                            "same Staff object, staffID=" + staffID,
                            loggedIn == null ? "null" : "Staff staffID=" + loggedIn.getStaffID());
                }
            } catch (Exception e) {
                fail(4, "AuthService.login(staffID, correctPassword)",
                        "same Staff object, staffID=" + staffID, "threw " + describe(e));
            }
        } else {
            skip(4, "AuthService.login(staffID, correctPassword)", "step 3 did not produce a Staff");
        }

        // ---- Step 5: AuthService.login with wrong password ----
        if (staff != null) {
            try {
                Staff loggedIn = authService.login(staffID, "definitely-wrong-password");
                if (loggedIn == null) {
                    pass(5, "AuthService.login(staffID, wrongPassword)", "null", "null");
                } else {
                    fail(5, "AuthService.login(staffID, wrongPassword)", "null",
                            "Staff staffID=" + loggedIn.getStaffID());
                }
            } catch (Exception e) {
                fail(5, "AuthService.login(staffID, wrongPassword)", "null", "threw " + describe(e));
            }
        } else {
            skip(5, "AuthService.login(staffID, wrongPassword)", "step 3 did not produce a Staff");
        }

        // ---- Step 6: VehicleService.addVehicle(...) ----
        String plateNumber = "SMK" + runID;
        Vehicle vehicle = null;
        if (staff != null) {
            try {
                vehicle = vehicleService.addVehicle(plateNumber, "Toyota", "Corolla", 2024,
                        VehicleCategory.STANDARD, FuelType.UNLEADED, true, staffID);

                boolean nonNull = vehicle != null;
                boolean inList = nonNull && dataStore.getVehicles().contains(vehicle);
                boolean statusOk = nonNull && vehicle.getStatus() == VehicleStatus.AVAILABLE;

                final Vehicle addedVehicle = vehicle;
                Movement ncm = nonNull ? dataStore.getBy(dataStore.getMovements(),
                        m -> m instanceof NewCarMovement && m.getVehicle() == addedVehicle) : null;
                boolean ncmOk = ncm != null && ncm.getStatus() == MovementStatus.CLOSED;

                if (nonNull && inList && statusOk && ncmOk) {
                    pass(6, "VehicleService.addVehicle(...) new plate",
                            "non-null Vehicle, in list, status=AVAILABLE, CLOSED NewCarMovement present",
                            "Vehicle " + vehicle.getPlateNumber() + " status=" + vehicle.getStatus()
                                    + ", NewCarMovement status=" + ncm.getStatus());
                } else {
                    fail(6, "VehicleService.addVehicle(...) new plate",
                            "non-null Vehicle, in list, status=AVAILABLE, CLOSED NewCarMovement present",
                            "vehicle=" + (nonNull ? vehicle.getStatus() : "null")
                                    + ", inList=" + inList
                                    + ", NewCarMovement=" + (ncm == null ? "not found" : ncm.getStatus()));
                }
            } catch (Exception e) {
                fail(6, "VehicleService.addVehicle(...) new plate",
                        "non-null Vehicle, in list, status=AVAILABLE, CLOSED NewCarMovement present",
                        "threw " + describe(e));
            }
        } else {
            skip(6, "VehicleService.addVehicle(...) new plate", "step 3 did not produce a Staff");
        }

        // ---- Step 7: VehicleService.addVehicle(...) duplicate plate ----
        if (vehicle != null) {
            try {
                Vehicle dup = vehicleService.addVehicle(plateNumber, "Toyota", "Corolla", 2024,
                        VehicleCategory.STANDARD, FuelType.UNLEADED, true, staffID);

                int countWithPlate = dataStore.search(dataStore.getVehicles(),
                        v -> v.getPlateNumber().equals(plateNumber)).size();

                if (dup == null && countWithPlate == 1) {
                    pass(7, "VehicleService.addVehicle(...) same plate again",
                            "null, no second vehicle added", "null, still 1 vehicle with plate " + plateNumber);
                } else {
                    fail(7, "VehicleService.addVehicle(...) same plate again",
                            "null, no second vehicle added",
                            (dup == null ? "null" : "non-null Vehicle") + ", " + countWithPlate
                                    + " vehicle(s) with plate " + plateNumber);
                }
            } catch (Exception e) {
                fail(7, "VehicleService.addVehicle(...) same plate again",
                        "null, no second vehicle added", "threw " + describe(e));
            }
        } else {
            skip(7, "VehicleService.addVehicle(...) same plate again", "step 6 did not produce a Vehicle");
        }

        // ---- Step 8: MovementService.startRA(...) ----
        RentalMovement rental = null;
        if (vehicle != null) {
            try {
                rental = movementService.startRA(plateNumber, staffID, "AKLC99",
                        List.of(9001), LocalDateTime.now().plusDays(3), "AKLC99",
                        false, Insurance.BASIC);

                boolean nonNull = rental != null;
                boolean statusOk = vehicle.getStatus() == VehicleStatus.IN_USE;

                if (nonNull && statusOk) {
                    pass(8, "MovementService.startRA(...) on AVAILABLE vehicle",
                            "non-null RentalMovement, vehicle status=IN_USE",
                            "RentalMovement " + rental.getMovementID() + ", vehicle status=" + vehicle.getStatus());
                } else {
                    fail(8, "MovementService.startRA(...) on AVAILABLE vehicle",
                            "non-null RentalMovement, vehicle status=IN_USE",
                            (nonNull ? "RentalMovement returned" : "null") + ", vehicle status=" + vehicle.getStatus());
                }
            } catch (Exception e) {
                fail(8, "MovementService.startRA(...) on AVAILABLE vehicle",
                        "non-null RentalMovement, vehicle status=IN_USE", "threw " + describe(e));
            }
        } else {
            skip(8, "MovementService.startRA(...) on AVAILABLE vehicle", "step 6 did not produce a Vehicle");
        }

        // ---- Step 9: MovementService.startRA(...) on already-IN_USE vehicle ----
        if (rental != null) {
            try {
                RentalMovement second = movementService.startRA(plateNumber, staffID, "AKLC99",
                        List.of(9002), LocalDateTime.now().plusDays(3), "AKLC99",
                        false, Insurance.BASIC);

                if (second == null) {
                    pass(9, "MovementService.startRA(...) again on IN_USE vehicle",
                            "null (vehicle not available)", "null");
                } else {
                    fail(9, "MovementService.startRA(...) again on IN_USE vehicle",
                            "null (vehicle not available)",
                            "RentalMovement " + second.getMovementID() + " was created (should not have been)");
                }
            } catch (Exception e) {
                fail(9, "MovementService.startRA(...) again on IN_USE vehicle",
                        "null (vehicle not available)", "threw " + describe(e));
            }
        } else {
            skip(9, "MovementService.startRA(...) again on IN_USE vehicle", "step 8 did not produce a RentalMovement");
        }

        // ---- Step 10: MovementService.checkIn(rental, ...) ----
        if (rental != null) {
            try {
                movementService.checkIn(rental, staffID, "AKLC99");

                boolean movementOk = rental.getStatus() == MovementStatus.INCOMPLETE;
                boolean vehicleUnchanged = vehicle.getStatus() == VehicleStatus.IN_USE;

                if (movementOk && vehicleUnchanged) {
                    pass(10, "MovementService.checkIn(rental, staffID, locationID)",
                            "movement status=INCOMPLETE, vehicle status still IN_USE",
                            "movement status=" + rental.getStatus() + ", vehicle status=" + vehicle.getStatus());
                } else {
                    fail(10, "MovementService.checkIn(rental, staffID, locationID)",
                            "movement status=INCOMPLETE, vehicle status still IN_USE",
                            "movement status=" + rental.getStatus() + ", vehicle status=" + vehicle.getStatus()
                                    + (vehicleUnchanged ? "" : " (BUG: vehicle status changed during checkIn, before closeRA)"));
                }
            } catch (Exception e) {
                fail(10, "MovementService.checkIn(rental, staffID, locationID)",
                        "movement status=INCOMPLETE, vehicle status still IN_USE", "threw " + describe(e));
            }
        } else {
            skip(10, "MovementService.checkIn(rental, staffID, locationID)", "step 8 did not produce a RentalMovement");
        }

        // ---- Step 11: MovementService.getRentalCost(rental) ----
        Double cost = null;
        if (rental != null) {
            try {
                cost = movementService.getRentalCost(rental);
                System.out.println("[Step 11] getRentalCost() returned: " + cost);

                if (cost > 0) {
                    pass(11, "MovementService.getRentalCost(rental)", "positive double", String.valueOf(cost));
                } else {
                    fail(11, "MovementService.getRentalCost(rental)", "positive double", String.valueOf(cost));
                }
            } catch (Exception e) {
                fail(11, "MovementService.getRentalCost(rental)", "positive double", "threw " + describe(e));
            }
        } else {
            skip(11, "MovementService.getRentalCost(rental)", "step 8 did not produce a RentalMovement");
        }

        // ---- Step 12: MovementService.closeRA(rental, odoIn, cost) ----
        if (rental != null && cost != null) {
            try {
                int odoIn = vehicle.getCurrentOdometer() + 250;
                movementService.closeRA(rental, odoIn, cost);

                boolean movementOk = rental.getStatus() == MovementStatus.CLOSED;
                boolean vehicleStatusOk = vehicle.getStatus() == VehicleStatus.AVAILABLE;
                boolean odoOk = vehicle.getCurrentOdometer() == odoIn;

                if (movementOk && vehicleStatusOk && odoOk) {
                    pass(12, "MovementService.closeRA(rental, odoIn, cost)",
                            "movement status=CLOSED, vehicle status=AVAILABLE, odometer=" + odoIn,
                            "movement status=" + rental.getStatus() + ", vehicle status=" + vehicle.getStatus()
                                    + ", odometer=" + vehicle.getCurrentOdometer());
                } else {
                    fail(12, "MovementService.closeRA(rental, odoIn, cost)",
                            "movement status=CLOSED, vehicle status=AVAILABLE, odometer=" + odoIn,
                            "movement status=" + rental.getStatus() + ", vehicle status=" + vehicle.getStatus()
                                    + ", odometer=" + vehicle.getCurrentOdometer());
                }
            } catch (Exception e) {
                fail(12, "MovementService.closeRA(rental, odoIn, cost)",
                        "movement status=CLOSED, vehicle status=AVAILABLE, odometer set", "threw " + describe(e));
            }
        } else {
            skip(12, "MovementService.closeRA(rental, odoIn, cost)", "step 8 or 11 did not complete");
        }

        // ---- Step 13: VehicleService.defleetVehicle(plateNumber, staffID) ----
        if (vehicle != null) {
            try {
                boolean defleeted = vehicleService.defleetVehicle(plateNumber, staffID);

                boolean statusOk = vehicle.getStatus() == VehicleStatus.DEFLEETED;
                final Vehicle defleetedVehicle = vehicle;
                Movement dvm = dataStore.getBy(dataStore.getMovements(),
                        m -> m instanceof DefleetMovement && m.getVehicle() == defleetedVehicle);
                boolean dvmOk = dvm != null && dvm.getStatus() == MovementStatus.CLOSED;

                if (defleeted && statusOk && dvmOk) {
                    pass(13, "VehicleService.defleetVehicle(plateNumber, staffID)",
                            "true, vehicle status=DEFLEETED, CLOSED DefleetMovement present",
                            "returned " + defleeted + ", vehicle status=" + vehicle.getStatus()
                                    + ", DefleetMovement status=" + dvm.getStatus());
                } else {
                    fail(13, "VehicleService.defleetVehicle(plateNumber, staffID)",
                            "true, vehicle status=DEFLEETED, CLOSED DefleetMovement present",
                            "returned " + defleeted + ", vehicle status=" + vehicle.getStatus()
                                    + ", DefleetMovement=" + (dvm == null ? "not found" : dvm.getStatus()));
                }
            } catch (Exception e) {
                fail(13, "VehicleService.defleetVehicle(plateNumber, staffID)",
                        "true, vehicle status=DEFLEETED, CLOSED DefleetMovement present", "threw " + describe(e));
            }
        } else {
            skip(13, "VehicleService.defleetVehicle(plateNumber, staffID)", "step 6 did not produce a Vehicle");
        }

        printSummary();
    }

    private static String describe(Exception e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    private static void pass(int step, String description, String expected, String actual) {
        StepResult r = new StepResult(step, description, true, expected, actual);
        RESULTS.add(r);
        System.out.println("[Step " + step + "] PASS - " + description);
        System.out.println("          expected: " + expected);
        System.out.println("          actual:   " + actual);
    }

    private static void fail(int step, String description, String expected, String actual) {
        StepResult r = new StepResult(step, description, false, expected, actual);
        RESULTS.add(r);
        System.out.println("[Step " + step + "] FAIL - " + description);
        System.out.println("          expected: " + expected);
        System.out.println("          actual:   " + actual);
    }

    private static void skip(int step, String description, String reason) {
        StepResult r = new StepResult(step, description, false, "n/a", "SKIPPED - " + reason);
        RESULTS.add(r);
        System.out.println("[Step " + step + "] SKIP - " + description + " (" + reason + ")");
    }

    private static void printSummary() {
        System.out.println();
        System.out.println("========================================================================");
        System.out.println("SMOKE TEST SUMMARY");
        System.out.println("========================================================================");

        int passCount = 0;
        for (StepResult r : RESULTS) {
            if (r.pass) {
                passCount++;
            }
        }

        System.out.printf("%-4s %-55s %-6s%n", "Step", "Description", "Result");
        System.out.println("------------------------------------------------------------------------");
        for (StepResult r : RESULTS) {
            System.out.printf("%-4d %-55s %-6s%n", r.step, r.description, r.pass ? "PASS" : "FAIL");
            if (!r.pass) {
                System.out.println("     expected: " + r.expected);
                System.out.println("     actual:   " + r.actual);
            }
        }
        System.out.println("------------------------------------------------------------------------");
        System.out.println(passCount + " / " + RESULTS.size() + " steps passed");
        System.out.println("========================================================================");
    }

    private static class StepResult {
        final int step;
        final String description;
        final boolean pass;
        final String expected;
        final String actual;

        StepResult(int step, String description, boolean pass, String expected, String actual) {
            this.step = step;
            this.description = description;
            this.pass = pass;
            this.expected = expected;
            this.actual = actual;
        }
    }
}
