package com.rfo.db;

import com.rfo.model.CarTransferMovement;
import com.rfo.model.CountryCode;
import com.rfo.model.DefleetMovement;
import com.rfo.model.Driver;
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
import com.rfo.model.WorkshopMovement;
import com.rfo.service.DataStore;
import com.rfo.service.DriverService;
import com.rfo.service.InvoiceService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A one-off tool that fills the database with sample data for testing and for
 * the demo video. Run this class once (it has its own main method). It adds: -
 * 14 sample drivers, - 25 vehicles spread over the stations, each brought in by
 * a New Vehicle movement (NVM), - 20 of the vehicles get nine months of
 * history: car transfers (CTM), rentals (ORA) and workshop visits (WSM) for
 * scheduled services and for repairs, each with dates, times and odometer
 * readings that follow on from each other, - 8 of those 20 are defleeted (DVM)
 * at the end, and 5 vehicles stay brand new. A few movements are left open so
 * Check In, Close Rental and the reports have something to work on. It builds
 * the movements with the same model classes the screens use, so every vehicle
 * ends up in the state the real screens would have left it in. Running it a
 * second time does nothing.
 */
public class SampleDataLoader {

    // ---------- what to create ----------
    private static final String INTAKE = "AKLC99";
    private static final String RETAIL = "AKLC88";
    private static final String WELLINGTON = "WLGX01";
    private static final String AIRPORT = "AKLT01";

    /**
     * One row per vehicle: plate, make, model, year, category, fuel, GPS, home
     * station, history (false = stays new).
     */
    private static final Object[][] CARS = {
        {"KQR482", "Toyota", "Corolla", 2024, VehicleCategory.COMPACT, FuelType.HYBRID, true, RETAIL, true},
        {"LBN305", "Mazda", "2", 2023, VehicleCategory.COMPACT, FuelType.UNLEADED, false, WELLINGTON, true},
        {"MTX917", "Suzuki", "Swift", 2024, VehicleCategory.COMPACT, FuelType.UNLEADED, false, AIRPORT, true},
        {"NHD264", "Hyundai", "i30", 2023, VehicleCategory.COMPACT, FuelType.UNLEADED, true, INTAKE, true},
        {"PVC738", "Kia", "Picanto", 2024, VehicleCategory.COMPACT, FuelType.UNLEADED, false, INTAKE, false},
        {"QRW150", "Toyota", "Yaris", 2024, VehicleCategory.COMPACT, FuelType.HYBRID, true, WELLINGTON, true},
        {"RFG863", "Toyota", "Camry", 2024, VehicleCategory.INTERMEDIATE, FuelType.HYBRID, true, AIRPORT, true},
        {"SJK429", "Mazda", "3", 2023, VehicleCategory.INTERMEDIATE, FuelType.UNLEADED, false, RETAIL, true},
        {"TDL596", "Hyundai", "Elantra", 2024, VehicleCategory.INTERMEDIATE, FuelType.UNLEADED, true, WELLINGTON, true},
        {"UXM031", "Kia", "Cerato", 2023, VehicleCategory.INTERMEDIATE, FuelType.UNLEADED, false, INTAKE, true},
        {"VBZ672", "Honda", "Civic", 2024, VehicleCategory.INTERMEDIATE, FuelType.UNLEADED, true, INTAKE, false},
        {"WNP348", "Toyota", "RAV4", 2024, VehicleCategory.STANDARD, FuelType.HYBRID, true, AIRPORT, true},
        {"XCH915", "Mazda", "CX-5", 2023, VehicleCategory.STANDARD, FuelType.DIESEL, true, RETAIL, true},
        {"YGT207", "Kia", "Sportage", 2024, VehicleCategory.STANDARD, FuelType.PLUG_IN_HYBRID, true, WELLINGTON, true},
        {"ZLA584", "Subaru", "Outback", 2023, VehicleCategory.STANDARD, FuelType.UNLEADED, true, RETAIL, true},
        {"JDW731", "Ford", "Ranger", 2024, VehicleCategory.STANDARD, FuelType.DIESEL, true, AIRPORT, true},
        {"KMS469", "Mitsubishi", "Outlander", 2024, VehicleCategory.STANDARD, FuelType.PLUG_IN_HYBRID, true, WELLINGTON, true},
        {"LPE826", "BMW", "3 Series", 2024, VehicleCategory.PREMIUM, FuelType.UNLEADED, true, RETAIL, true},
        {"MRB093", "Audi", "A4", 2023, VehicleCategory.PREMIUM, FuelType.UNLEADED, true, AIRPORT, true},
        {"NTY517", "Mercedes-Benz", "C-Class", 2024, VehicleCategory.PREMIUM, FuelType.UNLEADED, true, INTAKE, true},
        {"PZF284", "Tesla", "Model 3", 2024, VehicleCategory.PREMIUM, FuelType.ELECTRIC, true, INTAKE, false},
        {"QHC760", "Volvo", "XC60", 2023, VehicleCategory.PREMIUM, FuelType.PLUG_IN_HYBRID, true, INTAKE, false},
        {"RKV135", "Toyota", "HiAce", 2023, VehicleCategory.SPECIAL, FuelType.DIESEL, false, WELLINGTON, true},
        {"SNB948", "Ford", "Transit", 2024, VehicleCategory.SPECIAL, FuelType.DIESEL, true, RETAIL, true},
        {"TWJ372", "Mercedes-Benz", "Sprinter", 2024, VehicleCategory.SPECIAL, FuelType.DIESEL, true, INTAKE, false},};

    /**
     * The history vehicles that are defleeted at the end (indexes into CARS): 8
     * of the 20.
     */
    private static final int[] DEFLEETED = {1, 3, 5, 8, 12, 15, 17, 22};

    /**
     * The history vehicles left with a movement still open (indexes into CARS).
     */
    private static final int[] OPEN_RENTAL = {0, 6, 13};
    private static final int INCOMPLETE_RENTAL = 19;   // NTY517: checked in, waiting to be closed
    private static final int OPEN_WORKSHOP = 23;           // SNB948: in the workshop now

    private static final String[][] DRIVERS = {
        // first, last, licence, email, phone, street, city, postal
        {"Aroha", "Tane", "NZ482910", "aroha.tane@example.com", "021 555 0101", "12 Karaka Street", "Auckland", "1010"},
        {"Liam", "Walker", "NZ193847", "liam.walker@example.com", "021 555 0102", "8 Ocean View Road", "Wellington", "6011"},
        {"Sophie", "Chen", "NZ556201", "sophie.chen@example.com", "022 555 0103", "45 Queen Street", "Auckland", "1010"},
        {"Hemi", "Ngata", "NZ730145", "hemi.ngata@example.com", "027 555 0104", "3 Rata Place", "Hamilton", "3200"},
        {"Olivia", "Brown", "NZ209384", "olivia.brown@example.com", "021 555 0105", "19 Cuba Street", "Wellington", "6011"},
        {"Noah", "Patel", "NZ874520", "noah.patel@example.com", "022 555 0106", "71 Dominion Road", "Auckland", "1024"},
        {"Mia", "Kereama", "NZ361908", "mia.kereama@example.com", "027 555 0107", "6 Totara Lane", "Tauranga", "3110"},
        {"Jack", "Thompson", "NZ645172", "jack.thompson@example.com", "021 555 0108", "88 Victoria Avenue", "Whanganui", "4500"},
        {"Isla", "Murray", "NZ918263", "isla.murray@example.com", "022 555 0109", "22 Lambton Quay", "Wellington", "6011"},
        {"Ethan", "Singh", "NZ027485", "ethan.singh@example.com", "027 555 0110", "14 Great North Road", "Auckland", "1021"},
        {"Charlotte", "Davies", "NZ583619", "charlotte.davies@example.com", "021 555 0111", "5 Marine Parade", "Napier", "4110"},
        {"Tane", "Rewi", "NZ412096", "tane.rewi@example.com", "022 555 0112", "31 Fenton Street", "Rotorua", "3010"},
        {"Grace", "O'Neill", "NZ790351", "grace.oneill@example.com", "027 555 0113", "9 Princes Street", "Dunedin", "9016"},
        {"Lucas", "Fraser", "NZ256473", "lucas.fraser@example.com", "021 555 0114", "40 Papanui Road", "Christchurch", "8014"},};

    // ---------- small helper types ----------
    /**
     * One planned movement. The plan is made first, then turned into real
     * movement objects in time order.
     */
    private static class Plan {

        String type;                // NVM, CTM, WSM, ORA or DVM
        Car car;
        LocalDateTime out;
        LocalDateTime in;           // null while the movement is open
        LocalDateTime expectedIn;
        String outLoc;
        String inLoc;
        String expectedLoc;
        int odoOut;
        int odoIn;
        String outStaff;
        String inStaff;
        List<Integer> drivers = new ArrayList<>();
        boolean rsa;
        Insurance insurance = Insurance.BASIC;
        Map<Equipment, Integer> equipment = new EnumMap<>(Equipment.class);
        MovementStatus status = MovementStatus.CLOSED;
    }

    /**
     * A vehicle and where the planner has got to with it.
     */
    private static class Car {

        Object[] spec;
        boolean history;
        boolean defleet;
        int special;                // 0 none, 1 open rental, 2 checked in rental, 3 open workshop
        LocalDateTime endAt;
        String loc = INTAKE;
        int odo = 30;
        int lastService;
        boolean workshopDone;
        Vehicle vehicle;
        final List<Plan> plans = new ArrayList<>();

        VehicleCategory category() {
            return (VehicleCategory) spec[4];
        }

        int interval() {
            switch (category()) {
                case STANDARD:
                case PREMIUM:
                    return 15000;
                case SPECIAL:
                    return 20000;
                default:
                    return 10000;
            }
        }

        boolean serviceDue() {
            return odo - lastService >= interval();
        }
    }

    // ---------- the loader ----------
    private final DataStore ds;
    private final Random rnd = new Random(2026);
    private final LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
    private List<String> agents, mechanics, fleetOps, managers;
    private List<Integer> driverIDs = new ArrayList<>();

    public SampleDataLoader(DataStore ds) {
        this.ds = ds;
    }

    public static void main(String[] args) {
        new SampleDataLoader(new DataStore()).load();
    }

    /**
     * Creates everything and saves it. Does nothing if the sample vehicles are
     * already there.
     */
    public void load() {
        for (Object[] spec : CARS) {
            if (ds.getBy(ds.getVehicles(), v -> v.getPlateNumber().equals(spec[0])) != null) {
                System.out.println("The sample data is already in the database (" + spec[0] + " exists). Nothing was added.");
                return;
            }
        }
        for (String id : new String[]{INTAKE, RETAIL, WELLINGTON, AIRPORT}) {
            Location loc = location(id);
            if (loc == null || !loc.isActive()) {
                System.out.println("Location " + id + " is missing or closed. Nothing was added.");
                return;
            }
        }
        pickStaff();
        addDrivers();

        // plan every vehicle's history, then build the real movements in the order they happened
        List<Car> cars = new ArrayList<>();
        for (int i = 0; i < CARS.length; i++) {
            Car car = new Car();
            car.spec = CARS[i];
            car.history = (Boolean) CARS[i][8];
            for (int d : DEFLEETED) {
                car.defleet |= d == i;
            }
            if (car.history) {
                for (int o : OPEN_RENTAL) {
                    car.special = o == i ? 1 : car.special;
                }
                car.special = i == INCOMPLETE_RENTAL ? 2 : car.special;
                car.special = i == OPEN_WORKSHOP ? 3 : car.special;
            }
            planCar(car);
            cars.add(car);
        }
        build(cars);
        report(cars);
        verifySaved(cars);
    }

    // ---------- planning ----------
    private void planCar(Car car) {
        // a vehicle with no history is simply brought in during the last two weeks
        LocalDateTime start = now.minusMonths(9).plusDays(rnd.nextInt(45));
        LocalDateTime t = car.history ? at(start.toLocalDate(), 8 + rnd.nextInt(8), 5 * rnd.nextInt(12))
                : at(now.minusDays(2 + rnd.nextInt(12)).toLocalDate(), 9 + rnd.nextInt(7), 5 * rnd.nextInt(12));
        add(car, plan("NVM", car, t, t, INTAKE, INTAKE, 30, 0, pick(fleetOps), pick(fleetOps)));
        if (!car.history) {
            return;
        }

        if (car.defleet) {
            car.endAt = now.minusDays(7 + rnd.nextInt(110));
        } else if (car.special != 0) {
            car.endAt = now.minusDays(3);
        } else {
            car.endAt = now;
        }

        // first move: the car is driven from the intake depot to its home station
        String home = (String) car.spec[7];
        t = t.plusDays(1).withHour(9 + rnd.nextInt(3)).withMinute(5 * rnd.nextInt(12));
        if (!home.equals(INTAKE)) {
            t = transfer(car, t, home);
        }

        while (true) {
            t = workingTime(t.plusHours(idleHours()));
            if (t.isAfter(car.endAt.minusDays(2))) {
                break;
            }
            boolean nearEnd = t.isAfter(car.endAt.minusDays(75));
            LocalDateTime next;
            if (car.serviceDue()) {
                next = workshop(car, t, true);
            } else if (!car.workshopDone && nearEnd) {
                next = workshop(car, t, false);        // every vehicle has been to the workshop at least once
            } else if (rnd.nextInt(100) < 7) {
                next = workshop(car, t, false);        // sent in for a repair
            } else if (rnd.nextInt(100) < 5) {
                next = transfer(car, t, otherStation(car.loc, false));
            } else {
                next = rental(car, t, car.endAt.minusHours(1));
            }
            if (next == null) {
                break;
            }
            t = next;
        }

        // a vehicle that somehow missed the workshop goes in right after its last movement
        if (!car.workshopDone) {
            Plan lastPlan = car.plans.get(car.plans.size() - 1);
            workshop(car, workingTime(lastPlan.in.plusHours(6)), false);
        }

        // the last movement of this vehicle
        if (car.defleet) {
            Plan dvm = plan("DVM", car, car.endAt, car.endAt, car.loc, DefleetMovement.RETAIL_DEPOT.getLocationID(),
                    car.odo, 0, pick(managers), pick(managers));
            add(car, dvm);
        } else if (car.special == 1) {
            LocalDateTime out = now.minusHours(5 + rnd.nextInt(36));
            Plan open = rentalPlan(car, out, 3 + rnd.nextInt(7), false);
            open.status = MovementStatus.OPEN;
            open.in = null;
            open.odoIn = 0;
            add(car, open);
        } else if (car.special == 2) {
            LocalDateTime out = now.minusDays(3).minusHours(rnd.nextInt(10));
            Plan waiting = rentalPlan(car, out, 3, true);
            waiting.in = now.minusHours(1 + rnd.nextInt(4));
            waiting.status = MovementStatus.INCOMPLETE;
            waiting.odoIn = 0;
            add(car, waiting);
        } else if (car.special == 3) {
            Plan open = plan("WSM", car, now.minusHours(22), null, car.loc, null, car.odo, 0, pick(mechanics), null);
            open.expectedIn = now.plusHours(20);
            open.expectedLoc = car.loc;
            open.drivers.add(0);
            open.status = MovementStatus.OPEN;
            add(car, open);
        }
    }

    /**
     * A rental that starts at out, planned from the car's current place and
     * odometer.
     */
    private Plan rentalPlan(Car car, LocalDateTime out, int days, boolean returned) {
        Plan p = plan("ORA", car, out, null, car.loc, null, car.odo, 0, pick(agents), pick(agents));
        p.expectedIn = out.plusDays(days);
        p.expectedLoc = car.loc;
        p.drivers.add(pickDriver());
        p.insurance = rnd.nextInt(100) < 30 ? Insurance.BASIC : rnd.nextInt(100) < 65 ? Insurance.MEDIUM : Insurance.PREMIUM;
        p.rsa = rnd.nextInt(100) < 35;
        if (rnd.nextInt(100) < 15) {
            int second = pickDriver();
            while (second == p.drivers.get(0)) {
                second = pickDriver();      // two different people
            }
            p.drivers.add(second);
            p.equipment.put(Equipment.ADDITIONAL_DRIVER, 1);
        }
        if (rnd.nextInt(100) < 18) {
            p.equipment.put(Equipment.CHILD_SEAT, 1 + rnd.nextInt(2));
        }
        boolean winter = out.getMonthValue() >= 6 && out.getMonthValue() <= 9;
        if (winter && rnd.nextInt(100) < 22) {
            p.equipment.put(Equipment.SNOW_CHAINS, 1);
        }
        if (returned) {
            p.in = p.expectedIn.plusMinutes(5 * (rnd.nextInt(60) - 15));
            p.inLoc = p.expectedLoc;
            p.odoIn = car.odo + days * (90 + rnd.nextInt(170));
        }
        return p;
    }

    /**
     * Plans a finished rental starting at t. Returns when the vehicle is back,
     * or null if there is no time left.
     */
    private LocalDateTime rental(Car car, LocalDateTime t, LocalDateTime limit) {
        int roll = rnd.nextInt(100);
        int days = roll < 25 ? 1 + rnd.nextInt(2) : roll < 60 ? 3 + rnd.nextInt(3) : roll < 85 ? 6 + rnd.nextInt(4) : 10 + rnd.nextInt(5);
        Plan p = rentalPlan(car, t, days, true);
        // where it is returned: usually where it left from, sometimes another station
        boolean sameRegion = true;
        if (rnd.nextInt(100) < 28) {
            boolean cross = days >= 4 && rnd.nextInt(100) < 25;
            String dest = otherStation(car.loc, cross);
            p.expectedLoc = dest;
            p.inLoc = dest;
            sameRegion = region(dest) == region(car.loc);
        }
        int kmPerDay = 130 + rnd.nextInt(200);
        int km = Math.max(25, days * kmPerDay + rnd.nextInt(80) - 40);
        if (!sameRegion) {
            km = Math.max(km, 640 + rnd.nextInt(120));
        }
        p.odoIn = car.odo + km;
        if (p.in.isAfter(limit)) {
            return null;
        }
        car.odo = p.odoIn;
        car.loc = p.inLoc;
        add(car, p);
        return p.in;
    }

    /**
     * A workshop visit: a scheduled service (due is true) or a repair.
     */
    private LocalDateTime workshop(Car car, LocalDateTime t, boolean due) {
        int hours = due ? 20 + rnd.nextInt(50) : 24 + rnd.nextInt(96);
        Plan p = plan("WSM", car, t, null, car.loc, car.loc, car.odo, car.odo + 3 + rnd.nextInt(20),
                pick(mechanics), pick(mechanics));
        p.expectedIn = t.plusHours(hours);
        p.expectedLoc = car.loc;
        p.in = workingTime(p.expectedIn.plusMinutes(5 * (rnd.nextInt(60) - 10)));
        p.drivers.add(0);
        if (p.in.isAfter(car.endAt.minusHours(1))) {
            return null;
        }
        car.odo = p.odoIn;
        car.lastService = car.odo;
        car.workshopDone = true;
        add(car, p);
        return p.in;
    }

    /**
     * A car transfer to another station. Returns when it arrives.
     */
    private LocalDateTime transfer(Car car, LocalDateTime t, String dest) {
        boolean far = region(dest) != region(car.loc);
        Plan p = plan("CTM", car, t, null, car.loc, dest, car.odo, car.odo + distance(car.loc, dest) + rnd.nextInt(7),
                pick(fleetOps), pick(fleetOps));
        p.expectedIn = t.plusHours(far ? 40 : 3);
        p.expectedLoc = dest;
        p.in = workingTime(p.expectedIn.plusMinutes(5 * rnd.nextInt(24)));
        p.drivers.add(0);
        car.odo = p.odoIn;
        car.loc = dest;
        add(car, p);
        return p.in;
    }

    /**
     * How long a vehicle sits between movements: at least 2 days, 11 days on
     * average, at most 40.
     */
    private int idleHours() {
        double days = 2 + (-Math.log(1 - rnd.nextDouble()) * 9);
        return (int) (Math.min(days, 40) * 24);
    }

    private Plan plan(String type, Car car, LocalDateTime out, LocalDateTime in, String outLoc, String inLoc,
            int odoOut, int odoIn, String outStaff, String inStaff) {
        Plan p = new Plan();
        p.type = type;
        p.car = car;
        p.out = out;
        p.in = in;
        p.outLoc = outLoc;
        p.inLoc = inLoc;
        p.odoOut = odoOut;
        p.odoIn = odoIn;
        p.outStaff = outStaff;
        p.inStaff = inStaff;
        return p;
    }

    private void add(Car car, Plan p) {
        car.plans.add(p);
    }

    // ---------- building the real movements ----------
    private void build(List<Car> cars) {
        List<Plan> all = new ArrayList<>();
        for (Car car : cars) {
            all.addAll(car.plans);
        }
        // movement IDs go up with time, so the newest movement of a vehicle always has the biggest ID
        all.sort(Comparator.comparing((Plan p) -> p.out).thenComparing(p -> p.car.spec[0].toString()));

        int nextID = 1;
        for (Movement m : ds.getMovements()) {
            try {
                nextID = Math.max(nextID, Integer.parseInt(m.getMovementID()) + 1);
            } catch (NumberFormatException e) {
                // not a number, ignore it
            }
        }

        // an invoice can only be saved once its rental is in the database, so they are made at the end
        List<RentalMovement> closedRentals = new ArrayList<>();
        for (Plan p : all) {
            String id = Integer.toString(nextID++);
            Vehicle v = vehicleOf(p.car);
            Location outLoc = location(p.outLoc);
            Location inLoc = p.inLoc == null ? null : location(p.inLoc);
            Location expLoc = p.expectedLoc == null ? null : location(p.expectedLoc);
            List<Integer> drivers = new ArrayList<>(p.drivers);

            switch (p.type) {
                case "NVM": {
                    NewCarMovement m = new NewCarMovement(id, v, p.outStaff, p.inStaff, p.out, p.in,
                            NewCarMovement.INTAKE_DEPOT, NewCarMovement.INTAKE_DEPOT, 30, 0, MovementStatus.CLOSED);
                    m.execute();
                    ds.getMovements().add(m);
                    break;
                }
                case "DVM": {
                    DefleetMovement m = new DefleetMovement(id, v, p.outStaff, p.inStaff, p.out, p.in,
                            outLoc, DefleetMovement.RETAIL_DEPOT, p.odoOut, 0, MovementStatus.CLOSED);
                    m.execute();
                    ds.getMovements().add(m);
                    break;
                }
                case "CTM": {
                    CarTransferMovement m = new CarTransferMovement(id, v, p.outStaff, null, p.out, null, outLoc, null,
                            p.odoOut, 0, MovementStatus.OPEN, drivers, p.expectedIn, expLoc);
                    v.updateStatus(com.rfo.model.VehicleStatus.IN_USE);
                    finish(m, p, inLoc);
                    ds.getMovements().add(m);
                    break;
                }
                case "WSM": {
                    WorkshopMovement m = new WorkshopMovement(id, v, p.outStaff, null, p.out, null, outLoc, null,
                            p.odoOut, 0, MovementStatus.OPEN, drivers, p.expectedIn, expLoc);
                    v.updateStatus(com.rfo.model.VehicleStatus.WORKSHOP);
                    finish(m, p, inLoc);
                    ds.getMovements().add(m);
                    break;
                }
                default: {   // ORA
                    RentalMovement m = new RentalMovement(id, v, p.outStaff, null, p.out, null, outLoc, null,
                            p.odoOut, 0, MovementStatus.OPEN, drivers, p.expectedIn, expLoc,
                            p.rsa, p.insurance, new EnumMap<>(p.equipment), 0.0);
                    v.updateStatus(com.rfo.model.VehicleStatus.IN_USE);
                    if (p.status != MovementStatus.OPEN) {
                        m.checkIn(p.inStaff, inLoc, p.in);
                    }
                    if (p.status == MovementStatus.CLOSED) {
                        m.closeMovement(p.odoIn, m.calculateRentalCost());
                        closedRentals.add(m);
                    }
                    ds.getMovements().add(m);
                    break;
                }
            }
        }
        Movement.setLatestMovementID(nextID);
        ds.saveVehicles();
        ds.saveMovements();
        InvoiceService invoices = new InvoiceService(ds);
        for (RentalMovement rental : closedRentals) {
            invoices.issueFor(rental, rental.getCheckInDT());
        }
    }

    /**
     * Checks in and closes a driven movement the way the Check In and Close
     * screens do, unless it is still open.
     */
    private void finish(com.rfo.model.DrivenMovement m, Plan p, Location inLoc) {
        if (p.status == MovementStatus.OPEN) {
            return;
        }
        m.checkIn(p.inStaff, inLoc, p.in);
        if (p.status == MovementStatus.CLOSED) {
            m.closeMovement(p.odoIn);
        }
    }

    private Vehicle vehicleOf(Car car) {
        if (car.vehicle == null) {
            Object[] s = car.spec;
            Vehicle v = new Vehicle((String) s[0], (String) s[1], (String) s[2], (Integer) s[3],
                    (VehicleCategory) s[4], (FuelType) s[5], (Boolean) s[6]);
            v.setRate(rateOf((VehicleCategory) s[4]));
            ds.getVehicles().add(v);
            car.vehicle = v;
        }
        return car.vehicle;
    }

    private double rateOf(VehicleCategory c) {
        switch (c) {
            case COMPACT:
                return 0.75;
            case INTERMEDIATE:
                return 0.90;
            case STANDARD:
                return 1.0;
            case PREMIUM:
                return 1.6;
            default:
                return 1.8;
        }
    }

    // ---------- drivers and staff ----------
    private void addDrivers() {
        DriverService service = new DriverService(ds);
        for (String[] d : DRIVERS) {
            Driver created = service.createDriver(d[0], d[1], d[2], d[3], d[4], d[5], d[6], CountryCode.NZ, d[7]);
            driverIDs.add(created.getDriverID());
        }
    }

    /**
     * Prefers the staff from the original data; falls back to anyone with the
     * right role.
     */
    private void pickStaff() {
        agents = staffOf(StaffRole.AGENT, "AGNT002", "ONZP543", "ONZP777");
        mechanics = staffOf(StaffRole.MECHANIC, "MECH003");
        fleetOps = staffOf(StaffRole.FLEET_OPERATOR, "FLTO004");
        managers = staffOf(StaffRole.MANAGER, "ONZP774");
    }

    private List<String> staffOf(StaffRole role, String... preferred) {
        List<String> found = new ArrayList<>();
        for (String id : preferred) {
            Staff s = ds.getBy(ds.getStaff(), x -> x.getStaffID().equals(id));
            if (s != null && s.getRole() == role) {
                found.add(id);
            }
        }
        if (found.isEmpty()) {
            for (Staff s : ds.getStaff()) {
                if (s.getRole() == role) {
                    found.add(s.getStaffID());
                }
            }
        }
        if (found.isEmpty()) {
            throw new IllegalStateException("No staff member with the role " + role + " in the database.");
        }
        return found;
    }

    private String pick(List<String> list) {
        return list.get(rnd.nextInt(list.size()));
    }

    private int pickDriver() {
        return driverIDs.get(rnd.nextInt(driverIDs.size()));
    }

    // ---------- places and times ----------
    private Location location(String id) {
        return ds.getBy(ds.getLocations(), l -> l.getLocationID().equals(id));
    }

    private int region(String locationID) {
        return WELLINGTON.equals(locationID) ? 1 : 0;
    }

    /**
     * Roughly how far apart two stations are, in km.
     */
    private int distance(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }
        if (region(a) != region(b)) {
            return 640 + rnd.nextInt(15);
        }
        return 10 + rnd.nextInt(20);
    }

    /**
     * Another station: in the same region, or on the other island when cross is
     * true.
     */
    private String otherStation(String from, boolean cross) {
        List<String> options = new ArrayList<>();
        for (String id : new String[]{INTAKE, RETAIL, WELLINGTON, AIRPORT}) {
            if (!id.equals(from) && (region(id) != region(from)) == cross) {
                options.add(id);
            }
        }
        if (options.isEmpty()) {
            for (String id : new String[]{INTAKE, RETAIL, WELLINGTON, AIRPORT}) {
                if (!id.equals(from)) {
                    options.add(id);
                }
            }
        }
        return options.get(rnd.nextInt(options.size()));
    }

    private LocalDateTime at(java.time.LocalDate day, int hour, int minute) {
        return day.atTime(hour, minute);
    }

    /**
     * Moves a time into opening hours (8am to 6pm) and to a multiple of 5
     * minutes.
     */
    private LocalDateTime workingTime(LocalDateTime t) {
        LocalDateTime r = t.withMinute(5 * (t.getMinute() / 5)).withSecond(0).withNano(0);
        if (r.getHour() < 8) {
            r = r.withHour(8 + rnd.nextInt(3));
        } else if (r.getHour() >= 18) {
            r = r.plusDays(1).withHour(8 + rnd.nextInt(3));
        }
        return r;
    }

    // ---------- summary ----------
    /**
     * Reads the database again with a fresh DataStore and checks that
     * everything was really saved.
     */
    private void verifySaved(List<Car> cars) {
        DataStore fresh = new DataStore();
        int planned = 0;
        int closedRentals = 0;
        for (Car car : cars) {
            planned += car.plans.size();
            for (Plan p : car.plans) {
                closedRentals += p.type.equals("ORA") && p.status == MovementStatus.CLOSED ? 1 : 0;
            }
        }
        long savedMovements = fresh.getMovements().stream()
                .filter(m -> cars.stream().anyMatch(c -> c.spec[0].equals(m.getVehicle().getPlateNumber()))).count();
        long savedVehicles = fresh.getVehicles().stream()
                .filter(v -> cars.stream().anyMatch(c -> c.spec[0].equals(v.getPlateNumber()))).count();
        boolean ok = savedMovements == planned && savedVehicles == cars.size() && fresh.getInvoices().size() >= closedRentals;
        System.out.println(ok ? "CHECK OK: everything was saved and reads back correctly."
                : "CHECK FAILED: saved " + savedVehicles + " vehicles, " + savedMovements + " of " + planned
                + " movements, " + fresh.getInvoices().size() + " of " + closedRentals + " invoices."
                + " Restore the db folder from your backup and tell me.");
    }

    private void report(List<Car> cars) {
        Map<String, Integer> counts = new HashMap<>();
        for (Car car : cars) {
            for (Plan p : car.plans) {
                counts.merge(p.type, 1, Integer::sum);
            }
        }
        System.out.println("Sample data added: " + driverIDs.size() + " drivers, " + cars.size() + " vehicles.");
        System.out.println("Movements: " + counts);
        System.out.println("Invoices in the database: " + ds.getInvoices().size());
        for (Car car : cars) {
            System.out.println("  " + car.spec[0] + " " + car.spec[1] + " " + car.spec[2] + ": " + car.plans.size()
                    + " movements, now " + car.vehicle.getStatus() + " at "
                    + (car.vehicle.getCurrentLocation() == null ? "-" : car.vehicle.getCurrentLocation().getLocationID())
                    + ", " + car.vehicle.getCurrentOdometer() + " km");
        }
    }
}
