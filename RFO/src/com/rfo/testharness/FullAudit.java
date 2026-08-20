package com.rfo.testharness;

import com.rfo.model.Address;
import com.rfo.model.CarTransferMovement;
import com.rfo.model.CountryCode;
import com.rfo.model.Equipment;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 3 runtime audit harness. Standalone test-only file - does not modify
 * anything in com.rfo.model, com.rfo.io, com.rfo.service or com.rfo.ui.
 * Intended to be run against an EMPTY working directory (so "data/" starts
 * clean) - see the audit report for how this was invoked.
 */
public class FullAudit {

    private static final List<StepResult> RESULTS = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new File("data").mkdirs();

        // ---------------------------------------------------------------
        // Step 1: DataStore construction from a clean data/ directory
        // ---------------------------------------------------------------
        DataStore ds1 = null;
        try {
            ds1 = new DataStore();
            boolean allNonNull = ds1.getLocations() != null && ds1.getDrivers() != null
                    && ds1.getVehicles() != null && ds1.getStaff() != null && ds1.getMovements() != null;
            if (allNonNull) {
                pass(1, "DataStore() from clean data/ dir",
                        "no crash, 5 non-null lists",
                        "locations=" + ds1.getLocations().size() + ", drivers=" + ds1.getDrivers().size()
                                + ", vehicles=" + ds1.getVehicles().size() + ", staff=" + ds1.getStaff().size()
                                + ", movements=" + ds1.getMovements().size());
            } else {
                fail(1, "DataStore() from clean data/ dir", "no crash, 5 non-null lists", "one or more lists were null");
            }
        } catch (Exception e) {
            fail(1, "DataStore() from clean data/ dir", "no crash, 5 non-null lists", "threw " + describe(e));
            printSummary();
            return;
        }

        // ---------------------------------------------------------------
        // Step 2: LocationService.addLocation() x2, persist + reload
        // ---------------------------------------------------------------
        LocationService locationService = new LocationService(ds1);
        // "AKLC99" deliberately matches NewCarMovement.INTAKE_DEPOT's hardcoded ID so that
        // step 7/8 (vehicle intake + startRA at that ID) resolve to a real registered Location.
        Location locA = locationService.addLocation("AKLC99", "Auckland Intake Depot",
                new Address("1 Airport Rd", "Auckland", CountryCode.NZ, "1010"));
        Location locB = locationService.addLocation("WLGX01", "Wellington Branch",
                new Address("2 Lambton Quay", "Wellington", CountryCode.NZ, "6011"));
        try {
            boolean bothNonNull = locA != null && locB != null;
            boolean bothInList = bothNonNull && ds1.getLocations().contains(locA) && ds1.getLocations().contains(locB);

            DataStore ds2 = new DataStore();
            boolean aReloaded = ds2.getBy(ds2.getLocations(), l -> l.getLocationID().equals("AKLC99")) != null;
            boolean bReloaded = ds2.getBy(ds2.getLocations(), l -> l.getLocationID().equals("WLGX01")) != null;

            if (bothNonNull && bothInList && aReloaded && bReloaded) {
                pass(2, "LocationService.addLocation() x2, persist + reload",
                        "both present in-memory and after DataStore reload",
                        "both present pre-reload, both present post-reload");
            } else {
                fail(2, "LocationService.addLocation() x2, persist + reload",
                        "both present in-memory and after DataStore reload",
                        "nonNull=" + bothNonNull + " inList=" + bothInList
                                + " aReloaded=" + aReloaded + " bReloaded=" + bReloaded);
            }
        } catch (Exception e) {
            fail(2, "LocationService.addLocation() x2, persist + reload",
                    "both present in-memory and after DataStore reload", "threw " + describe(e));
        }

        // From here on, use a single reloaded DataStore ("ds") + fresh services bound to it.
        DataStore ds = new DataStore();
        StaffService staffService = new StaffService(ds);
        AuthService authService = new AuthService(ds);
        VehicleService vehicleService = new VehicleService(ds);
        MovementService movementService = new MovementService(ds);
        LocationService locSvc = new LocationService(ds);
        Location aLoc = ds.getBy(ds.getLocations(), l -> l.getLocationID().equals("AKLC99"));
        Location bLoc = ds.getBy(ds.getLocations(), l -> l.getLocationID().equals("WLGX01"));

        // ---------------------------------------------------------------
        // Step 3: StaffService.addStaff() MANAGER + persisted password + login
        // ---------------------------------------------------------------
        String staffID = "MGR001";
        String correctPassword = "Passw0rd!";
        Staff mgr = null;
        try {
            mgr = staffService.addStaff(staffID, correctPassword, "Jane", "Doe", StaffRole.MANAGER);

            String persistedPassword = readStaffPassword(staffID);
            boolean passwordPersisted = persistedPassword != null && persistedPassword.equals(correctPassword);

            Staff loginOk = authService.login(staffID, correctPassword);
            Staff loginBad = authService.login(staffID, "wrong-password");

            boolean ok = mgr != null && ds.getStaff().contains(mgr) && passwordPersisted
                    && loginOk != null && loginOk.getStaffID().equals(staffID) && loginBad == null;

            if (ok) {
                pass(3, "StaffService.addStaff(MANAGER) + persisted password + login",
                        "non-null Staff, real password on disk, correct login succeeds, wrong login fails",
                        "staff=" + mgr.getStaffID() + ", disk password=\"" + persistedPassword
                                + "\", correctLogin=" + (loginOk != null) + ", wrongLogin=" + (loginBad != null));
            } else {
                fail(3, "StaffService.addStaff(MANAGER) + persisted password + login",
                        "non-null Staff, real password on disk, correct login succeeds, wrong login fails",
                        "staff=" + (mgr == null ? "null" : mgr.getStaffID()) + ", disk password=\"" + persistedPassword
                                + "\", correctLogin=" + (loginOk != null) + ", wrongLogin=" + (loginBad != null));
            }
        } catch (Exception e) {
            fail(3, "StaffService.addStaff(MANAGER) + persisted password + login",
                    "non-null Staff, real password on disk, correct login succeeds, wrong login fails",
                    "threw " + describe(e));
        }

        // ---------------------------------------------------------------
        // Step 4: StaffService.changePassword()
        // ---------------------------------------------------------------
        String newPassword = "N3wPassword!";
        if (mgr != null) {
            try {
                boolean changed = staffService.changePassword(mgr, correctPassword, newPassword);
                Staff oldStillWorks = authService.login(staffID, correctPassword);
                Staff newWorks = authService.login(staffID, newPassword);

                boolean ok = changed && oldStillWorks == null && newWorks != null;
                if (ok) {
                    pass(4, "StaffService.changePassword(...)",
                            "true, OLD password now fails, NEW password succeeds",
                            "changed=" + changed + ", oldLogin=" + (oldStillWorks != null) + ", newLogin=" + (newWorks != null));
                } else {
                    fail(4, "StaffService.changePassword(...)",
                            "true, OLD password now fails, NEW password succeeds",
                            "changed=" + changed + ", oldLogin=" + (oldStillWorks != null) + ", newLogin=" + (newWorks != null));
                }
            } catch (Exception e) {
                fail(4, "StaffService.changePassword(...)",
                        "true, OLD password now fails, NEW password succeeds", "threw " + describe(e));
            }
        } else {
            skip(4, "StaffService.changePassword(...)", "step 3 did not produce a Staff");
        }

        // ---------------------------------------------------------------
        // Step 5: StaffService.switchLocation() blocked vs allowed
        // ---------------------------------------------------------------
        if (mgr != null && bLoc != null) {
            try {
                staffService.switchLocation(mgr, bLoc);
                boolean blockedAsExpected = mgr.getCurrentLocation() == null;

                staffService.grantLocationAccess(mgr, bLoc);
                staffService.switchLocation(mgr, bLoc);
                boolean succeededAsExpected = mgr.getCurrentLocation() != null
                        && mgr.getCurrentLocation().getLocationID().equals("WLGX01");

                if (blockedAsExpected && succeededAsExpected) {
                    pass(5, "StaffService.switchLocation() blocked/allowed",
                            "blocked with no access, succeeds once granted",
                            "blocked=" + blockedAsExpected + ", succeeded after grant=" + succeededAsExpected);
                } else {
                    fail(5, "StaffService.switchLocation() blocked/allowed",
                            "blocked with no access, succeeds once granted",
                            "blocked=" + blockedAsExpected + ", succeeded after grant=" + succeededAsExpected
                                    + ", currentLocation=" + (mgr.getCurrentLocation() == null ? "null" : mgr.getCurrentLocation().getLocationID()));
                }
            } catch (Exception e) {
                fail(5, "StaffService.switchLocation() blocked/allowed",
                        "blocked with no access, succeeds once granted", "threw " + describe(e));
            }
        } else {
            skip(5, "StaffService.switchLocation() blocked/allowed", "step 3/4 prerequisites missing");
        }

        // ---------------------------------------------------------------
        // Step 6: grantLocationAccess() persists, revokeAllLocationAccess() clears + persists
        // ---------------------------------------------------------------
        if (mgr != null && aLoc != null) {
            try {
                staffService.grantLocationAccess(mgr, aLoc); // mgr now has WLGX01 (step5) + AKLC99

                DataStore dsAfterGrant = new DataStore();
                Staff mgrAfterGrant = dsAfterGrant.getBy(dsAfterGrant.getStaff(), s -> s.getStaffID().equals(staffID));
                boolean grantPersisted = mgrAfterGrant != null
                        && mgrAfterGrant.getAccessibleLocations().stream().anyMatch(l -> l.getLocationID().equals("AKLC99"))
                        && mgrAfterGrant.getAccessibleLocations().stream().anyMatch(l -> l.getLocationID().equals("WLGX01"));

                StaffService staffServiceAfterGrant = new StaffService(dsAfterGrant);
                boolean revoked = staffServiceAfterGrant.revokeAllLocationAccess(mgrAfterGrant);
                boolean clearedInMemory = revoked && mgrAfterGrant.getAccessibleLocations().isEmpty()
                        && mgrAfterGrant.getCurrentLocation() == null;

                DataStore dsAfterRevoke = new DataStore();
                Staff mgrAfterRevoke = dsAfterRevoke.getBy(dsAfterRevoke.getStaff(), s -> s.getStaffID().equals(staffID));
                boolean revokePersisted = mgrAfterRevoke != null
                        && mgrAfterRevoke.getAccessibleLocations().isEmpty()
                        && mgrAfterRevoke.getCurrentLocation() == null;

                if (grantPersisted && clearedInMemory && revokePersisted) {
                    pass(6, "grantLocationAccess() persists, revokeAllLocationAccess() clears + persists",
                            "access persists across reload; revoke clears accessibleLocations + currentLocation and persists",
                            "grantPersisted=true, clearedInMemory=true, revokePersisted=true");
                } else {
                    fail(6, "grantLocationAccess() persists, revokeAllLocationAccess() clears + persists",
                            "access persists across reload; revoke clears accessibleLocations + currentLocation and persists",
                            "grantPersisted=" + grantPersisted + ", clearedInMemory=" + clearedInMemory
                                    + ", revokePersisted=" + revokePersisted);
                }
            } catch (Exception e) {
                fail(6, "grantLocationAccess() persists, revokeAllLocationAccess() clears + persists",
                        "access persists across reload; revoke clears accessibleLocations + currentLocation and persists",
                        "threw " + describe(e));
            }
        } else {
            skip(6, "grantLocationAccess() persists, revokeAllLocationAccess() clears + persists",
                    "step 3/5 prerequisites missing");
        }

        // ---------------------------------------------------------------
        // Step 7: VehicleService.addVehicle() rate default, duplicate rejection, paired NCM
        // ---------------------------------------------------------------
        String plateNumber = "TST1001";
        Vehicle vehicle = null;
        try {
            vehicle = vehicleService.addVehicle(plateNumber, "Toyota", "Corolla", 2024,
                    VehicleCategory.STANDARD, FuelType.UNLEADED, true, staffID);

            boolean rateOk = vehicle != null && vehicle.getRate() > 0.0;

            Vehicle dup = vehicleService.addVehicle(plateNumber, "Toyota", "Corolla", 2024,
                    VehicleCategory.STANDARD, FuelType.UNLEADED, true, staffID);
            boolean dupRejected = dup == null;

            final Vehicle v = vehicle;
            Movement ncm = vehicle == null ? null : ds.getBy(ds.getMovements(),
                    m -> m instanceof NewCarMovement && m.getVehicle() == v);
            boolean ncmClosed = ncm != null && ncm.getStatus() == MovementStatus.CLOSED;

            if (rateOk && dupRejected && ncmClosed) {
                pass(7, "VehicleService.addVehicle() rate/duplicate/NCM",
                        "non-zero category rate, duplicate plate rejected, CLOSED NewCarMovement created",
                        "rate=" + vehicle.getRate() + ", dupRejected=" + dupRejected + ", ncmStatus=" + ncm.getStatus());
            } else {
                fail(7, "VehicleService.addVehicle() rate/duplicate/NCM",
                        "non-zero category rate, duplicate plate rejected, CLOSED NewCarMovement created",
                        "rate=" + (vehicle == null ? "n/a" : vehicle.getRate()) + ", dupRejected=" + dupRejected
                                + ", ncm=" + (ncm == null ? "not found" : ncm.getStatus()));
            }
        } catch (Exception e) {
            fail(7, "VehicleService.addVehicle() rate/duplicate/NCM",
                    "non-zero category rate, duplicate plate rejected, CLOSED NewCarMovement created",
                    "threw " + describe(e));
        }

        // ---------------------------------------------------------------
        // Step 8: MovementService.startRA() gating (location, availability, service-due) + success
        // ---------------------------------------------------------------
        RentalMovement rental = null;
        if (vehicle != null) {
            try {
                RentalMovement wrongLoc = movementService.startRA(plateNumber, staffID, "WLGX01",
                        List.of(9001), LocalDateTime.now().plusDays(3), "WLGX01", false, Insurance.BASIC);
                boolean blockedWrongLoc = wrongLoc == null;

                vehicle.updateStatus(VehicleStatus.WORKSHOP);
                RentalMovement unavailable = movementService.startRA(plateNumber, staffID, "AKLC99",
                        List.of(9001), LocalDateTime.now().plusDays(3), "AKLC99", false, Insurance.BASIC);
                boolean blockedUnavailable = unavailable == null;
                vehicle.updateStatus(VehicleStatus.AVAILABLE);

                vehicle.setCurrentOdometer(vehicle.getServiceInterval() + 5000);
                vehicle.updateServiceFlag();
                RentalMovement overdue = movementService.startRA(plateNumber, staffID, "AKLC99",
                        List.of(9001), LocalDateTime.now().plusDays(3), "AKLC99", false, Insurance.BASIC);
                boolean blockedOverdue = overdue == null;
                vehicle.markServiced(); // resets serviceFlag + status=AVAILABLE + lastServiceOdometer
                vehicle.setCurrentOdometer(30);

                rental = movementService.startRA(plateNumber, staffID, "AKLC99",
                        List.of(9001), LocalDateTime.now().plusDays(3), "AKLC99", false, Insurance.BASIC);
                boolean succeeded = rental != null && vehicle.getStatus() == VehicleStatus.IN_USE;

                if (blockedWrongLoc && blockedUnavailable && blockedOverdue && succeeded) {
                    pass(8, "MovementService.startRA() gating + success",
                            "blocked: wrong location, unavailable, overdue; succeeds otherwise with vehicle IN_USE",
                            "all 4 gate checks correct, vehicle status=" + vehicle.getStatus());
                } else {
                    fail(8, "MovementService.startRA() gating + success",
                            "blocked: wrong location, unavailable, overdue; succeeds otherwise with vehicle IN_USE",
                            "blockedWrongLoc=" + blockedWrongLoc + ", blockedUnavailable=" + blockedUnavailable
                                    + ", blockedOverdue=" + blockedOverdue + ", succeeded=" + succeeded);
                }
            } catch (Exception e) {
                fail(8, "MovementService.startRA() gating + success",
                        "blocked: wrong location, unavailable, overdue; succeeds otherwise with vehicle IN_USE",
                        "threw " + describe(e));
            }
        } else {
            skip(8, "MovementService.startRA() gating + success", "step 7 did not produce a Vehicle");
        }

        // ---------------------------------------------------------------
        // Step 9: full rental lifecycle - checkIn (no vehicle change), getRentalCost, closeRA
        // ---------------------------------------------------------------
        if (rental != null) {
            try {
                rental.addEquipment(Equipment.CHILD_SEAT, 1); // exercise equipment persistence for step 13

                movementService.checkIn(rental, staffID, "AKLC99");
                boolean checkInOk = rental.getStatus() == MovementStatus.INCOMPLETE
                        && vehicle.getStatus() == VehicleStatus.IN_USE;

                double cost = movementService.getRentalCost(rental);
                boolean costOk = cost > 0;

                int odoIn = vehicle.getCurrentOdometer() + 250;
                movementService.closeRA(rental, odoIn, cost);
                boolean closeOk = rental.getStatus() == MovementStatus.CLOSED
                        && vehicle.getStatus() == VehicleStatus.AVAILABLE
                        && vehicle.getCurrentOdometer() == odoIn;

                if (checkInOk && costOk && closeOk) {
                    pass(9, "Full rental lifecycle: checkIn / getRentalCost / closeRA",
                            "checkIn leaves vehicle unchanged; cost > 0; close sets AVAILABLE + odometer + CLOSED",
                            "checkIn ok, cost=" + cost + ", final vehicle status=" + vehicle.getStatus()
                                    + ", odometer=" + vehicle.getCurrentOdometer());
                } else {
                    fail(9, "Full rental lifecycle: checkIn / getRentalCost / closeRA",
                            "checkIn leaves vehicle unchanged; cost > 0; close sets AVAILABLE + odometer + CLOSED",
                            "checkInOk=" + checkInOk + ", costOk=" + costOk + ", closeOk=" + closeOk);
                }
            } catch (Exception e) {
                fail(9, "Full rental lifecycle: checkIn / getRentalCost / closeRA",
                        "checkIn leaves vehicle unchanged; cost > 0; close sets AVAILABLE + odometer + CLOSED",
                        "threw " + describe(e));
            }
        } else {
            skip(9, "Full rental lifecycle: checkIn / getRentalCost / closeRA", "step 8 did not produce a RentalMovement");
        }

        // ---------------------------------------------------------------
        // Step 10: VehicleService.defleetVehicle()
        // ---------------------------------------------------------------
        if (vehicle != null) {
            try {
                boolean defleeted = vehicleService.defleetVehicle(plateNumber, staffID);
                boolean statusOk = vehicle.getStatus() == VehicleStatus.DEFLEETED;

                if (defleeted && statusOk) {
                    pass(10, "VehicleService.defleetVehicle(...)",
                            "true, vehicle status=DEFLEETED", "returned " + defleeted + ", status=" + vehicle.getStatus());
                } else {
                    fail(10, "VehicleService.defleetVehicle(...)",
                            "true, vehicle status=DEFLEETED", "returned " + defleeted + ", status=" + vehicle.getStatus());
                }
            } catch (Exception e) {
                fail(10, "VehicleService.defleetVehicle(...)", "true, vehicle status=DEFLEETED", "threw " + describe(e));
            }
        } else {
            skip(10, "VehicleService.defleetVehicle(...)", "step 7 did not produce a Vehicle");
        }

        // ---------------------------------------------------------------
        // Step 11: LocationService.closeLocation() with a stranded vehicle
        // ---------------------------------------------------------------
        Vehicle strandedVehicle = null;
        int movementCountBeforeSecondClose = -1;
        if (aLoc != null && bLoc != null) {
            try {
                strandedVehicle = vehicleService.addVehicle("TST1002", "Nissan", "Leaf", 2023,
                        VehicleCategory.COMPACT, FuelType.ELECTRIC, true, staffID);
                // freshly-fleeted vehicle lands at INTAKE_DEPOT, ID "AKLC99" == aLoc's ID

                boolean closed = locSvc.closeLocation(aLoc, "WLGX01", staffID);

                final Vehicle strandedVehicleRef = strandedVehicle;
                Movement ctm = ds.getBy(ds.getMovements(), m -> m instanceof CarTransferMovement
                        && m.getVehicle() == strandedVehicleRef);

                boolean vehicleMoved = strandedVehicle.getCurrentLocation() != null
                        && strandedVehicle.getCurrentLocation().getLocationID().equals("WLGX01");
                boolean locationInactive = !aLoc.isActive();
                boolean locationStillListed = ds.getLocations().contains(aLoc);

                movementCountBeforeSecondClose = ds.getMovements().size();

                if (closed && ctm != null && vehicleMoved && locationInactive && locationStillListed) {
                    pass(11, "LocationService.closeLocation() with stranded vehicle",
                            "CTM created with generic driver, vehicle moved to new location, "
                                    + "old location inactive but still listed",
                            "closed=true, CTM present, vehicle now at " + strandedVehicle.getCurrentLocation().getLocationID()
                                    + ", aLoc.isActive()=" + aLoc.isActive());
                } else {
                    fail(11, "LocationService.closeLocation() with stranded vehicle",
                            "CTM created with generic driver, vehicle moved to new location, "
                                    + "old location inactive but still listed",
                            "closed=" + closed
                                    + ", ctmPresent=" + (ctm != null)
                                    + ", ctmDrivers=" + (ctm == null ? "n/a" : ((CarTransferMovement) ctm).getDriverIDs())
                                    + ", ctmStatus=" + (ctm == null ? "n/a" : ctm.getStatus())
                                    + ", vehicleMoved=" + vehicleMoved
                                    + " (actual location=" + (strandedVehicle.getCurrentLocation() == null ? "null"
                                            : strandedVehicle.getCurrentLocation().getLocationID()) + ")"
                                    + ", locationInactive=" + locationInactive
                                    + ", locationStillListed=" + locationStillListed);
                }
            } catch (Exception e) {
                fail(11, "LocationService.closeLocation() with stranded vehicle",
                        "CTM created with generic driver, vehicle moved to new location, "
                                + "old location inactive but still listed", "threw " + describe(e));
            }
        } else {
            skip(11, "LocationService.closeLocation() with stranded vehicle", "step 2 locations not available");
        }

        // ---------------------------------------------------------------
        // Step 12: closeLocation() again on the now-closed location
        // ---------------------------------------------------------------
        if (aLoc != null) {
            try {
                boolean closedAgain = locSvc.closeLocation(aLoc, "WLGX01", staffID);
                int movementCountAfter = ds.getMovements().size();
                boolean noNewMovement = movementCountBeforeSecondClose < 0
                        || movementCountAfter == movementCountBeforeSecondClose;

                if (!closedAgain && noNewMovement) {
                    pass(12, "LocationService.closeLocation() on already-closed location",
                            "false, rejected as already closed, no new movement created",
                            "closedAgain=false, movement count unchanged (" + movementCountAfter + ")");
                } else {
                    fail(12, "LocationService.closeLocation() on already-closed location",
                            "false, rejected as already closed, no new movement created",
                            "closedAgain=" + closedAgain + ", movementCountBefore=" + movementCountBeforeSecondClose
                                    + ", movementCountAfter=" + movementCountAfter);
                }
            } catch (Exception e) {
                fail(12, "LocationService.closeLocation() on already-closed location",
                        "false, rejected as already closed, no new movement created", "threw " + describe(e));
            }
        } else {
            skip(12, "LocationService.closeLocation() on already-closed location", "step 11 prerequisites missing");
        }

        // ---------------------------------------------------------------
        // Step 13: full reload after everything above
        // ---------------------------------------------------------------
        try {
            DataStore dsFinal = new DataStore();

            Location aLocFinal = dsFinal.getBy(dsFinal.getLocations(), l -> l.getLocationID().equals("AKLC99"));
            boolean locationReloadOk = aLocFinal != null && !aLocFinal.isActive();

            Movement ctmFinal = dsFinal.getBy(dsFinal.getMovements(), m -> m instanceof CarTransferMovement
                    && m.getVehicle() != null && m.getVehicle().getPlateNumber().equals("TST1002"));
            boolean ctmReloadOk = ctmFinal != null;

            Movement rentalFinal = dsFinal.getBy(dsFinal.getMovements(), m -> m instanceof RentalMovement
                    && m.getVehicle() != null && m.getVehicle().getPlateNumber().equals(plateNumber));
            boolean rentalReloadOk = false;
            String rentalDetail = "not found";
            if (rentalFinal instanceof RentalMovement rmFinal) {
                boolean priceOk = rmFinal.getPrice() > 0;
                boolean insuranceOk = rmFinal.getInsuranceTier() == Insurance.BASIC;
                boolean equipmentOk = rmFinal.getEquipment() != null
                        && rmFinal.getEquipment().getOrDefault(Equipment.CHILD_SEAT, 0) == 1;
                rentalReloadOk = priceOk && insuranceOk && equipmentOk;
                rentalDetail = "price=" + rmFinal.getPrice() + ", insurance=" + rmFinal.getInsuranceTier()
                        + ", equipment=" + rmFinal.getEquipment();
            }

            Staff mgrFinal = dsFinal.getBy(dsFinal.getStaff(), s -> s.getStaffID().equals(staffID));
            boolean staffReloadOk = mgrFinal != null && mgrFinal.getAccessibleLocations().isEmpty()
                    && mgrFinal.getCurrentLocation() == null;

            if (locationReloadOk && ctmReloadOk && rentalReloadOk && staffReloadOk) {
                pass(13, "Full reload after steps 1-12 (simulated app restart)",
                        "closed location stays closed/inactive; CTM + rental reconstruct correctly; revoked staff stays revoked",
                        "location ok, CTM ok, rental(" + rentalDetail + "), staff ok");
            } else {
                fail(13, "Full reload after steps 1-12 (simulated app restart)",
                        "closed location stays closed/inactive; CTM + rental reconstruct correctly; revoked staff stays revoked",
                        "locationReloadOk=" + locationReloadOk + ", ctmReloadOk=" + ctmReloadOk
                                + ", rentalReloadOk=" + rentalReloadOk + " (" + rentalDetail + ")"
                                + ", staffReloadOk=" + staffReloadOk);
            }
        } catch (Exception e) {
            fail(13, "Full reload after steps 1-12 (simulated app restart)",
                    "closed location stays closed/inactive; CTM + rental reconstruct correctly; revoked staff stays revoked",
                    "threw " + describe(e));
        }

        printSummary();
    }

    private static String readStaffPassword(String staffID) throws IOException {
        File file = new File("data/staff.txt");
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                String[] fields = ln.split("\\|", -1);
                if (fields.length > 1 && fields[0].equals(staffID)) {
                    return fields[1];
                }
            }
        }
        return null;
    }

    private static String describe(Exception e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    private static void pass(int step, String description, String expected, String actual) {
        RESULTS.add(new StepResult(step, description, true, expected, actual));
        System.out.println("[Step " + step + "] PASS - " + description);
        System.out.println("          expected: " + expected);
        System.out.println("          actual:   " + actual);
    }

    private static void fail(int step, String description, String expected, String actual) {
        RESULTS.add(new StepResult(step, description, false, expected, actual));
        System.out.println("[Step " + step + "] FAIL - " + description);
        System.out.println("          expected: " + expected);
        System.out.println("          actual:   " + actual);
    }

    private static void skip(int step, String description, String reason) {
        RESULTS.add(new StepResult(step, description, false, "n/a", "SKIPPED - " + reason));
        System.out.println("[Step " + step + "] SKIP - " + description + " (" + reason + ")");
    }

    private static void printSummary() {
        System.out.println();
        System.out.println("========================================================================");
        System.out.println("FULL AUDIT - RUNTIME SMOKE TEST SUMMARY");
        System.out.println("========================================================================");

        int passCount = 0;
        for (StepResult r : RESULTS) {
            if (r.pass) {
                passCount++;
            }
        }

        System.out.printf("%-4s %-65s %-6s%n", "Step", "Description", "Result");
        System.out.println("------------------------------------------------------------------------");
        for (StepResult r : RESULTS) {
            System.out.printf("%-4d %-65s %-6s%n", r.step, r.description, r.pass ? "PASS" : "FAIL");
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
