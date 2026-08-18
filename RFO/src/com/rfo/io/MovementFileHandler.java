package com.rfo.io;

import com.rfo.model.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementFileHandler {

    private static final String FILE_PATH = "data/movements.txt";
    // type|movementID|plateNumber|COStaff|CIStaff|checkOutDT|checkInDT|checkOutLocID|checkInLocID|
    //   odoOut|odoIn|status|[type-specific fields]

    public List<Movement> loadAll(List<Vehicle> allVehicles, List<Location> allLocations) {
        List<Movement> movements = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return movements;
        }

        Map<String, Vehicle> vehiclesByPlate = buildVehicleLookup(allVehicles);
        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String ln;
            while ((ln = reader.readLine()) != null) {
                Movement movement = parseLine(ln, vehiclesByPlate, locationsById);
                if (movement != null) {
                    movements.add(movement);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading movements file: " + e.getMessage());
        }

        if (!movements.isEmpty()) {
            int maxID = 0;
            for (Movement m : movements) {
                maxID = Math.max(maxID, Integer.parseInt(m.getMovementID()));
            }
            Movement.setLatestMovementID(maxID + 1);
        }

        return movements;
    }

    public void saveAll(List<Movement> movements) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Movement movement : movements) {
                writer.write(toLine(movement));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing movements file: " + e.getMessage());
        }
    }

    private Map<String, Vehicle> buildVehicleLookup(List<Vehicle> allVehicles) {
        Map<String, Vehicle> map = new HashMap<>();
        for (Vehicle v : allVehicles) {
            map.put(v.getPlateNumber(), v);
        }
        return map;
    }

    private Map<String, Location> buildLocationLookup(List<Location> allLocations) {
        Map<String, Location> map = new HashMap<>();
        for (Location loc : allLocations) {
            map.put(loc.getLocationID(), loc);
        }
        return map;
    }

    private List<Integer> parseDriverIDs(String field) {
        List<Integer> ids = new ArrayList<>();
        if (field != null && !field.isEmpty()) {
            for (String id : field.split(",")) {
                ids.add(Integer.parseInt(id));
            }
        }
        return ids;
    }

    private String joinDriverIDs(List<Integer> driverIDs) {
        List<String> asStrings = new ArrayList<>();
        for (int id : driverIDs) {
            asStrings.add(String.valueOf(id));
        }
        return String.join(",", asStrings);
    }

    private Movement parseLine(String ln, Map<String, Vehicle> vehiclesByPlate, Map<String, Location> locationsById) {
        String[] f = ln.split("\\|");

        String type = f[0];
        String movementID = f[1];
        Vehicle vehicle = vehiclesByPlate.get(f[2]);
        String COStaff = f[3];
        String CIStaff = f[4].isEmpty() ? null : f[4];
        LocalDateTime checkOutDT = LocalDateTime.parse(f[5]);
        LocalDateTime checkInDT = f[6].isEmpty() ? null : LocalDateTime.parse(f[6]);
        Location checkOutLoc = locationsById.get(f[7]);
        Location checkInLoc = f[8].isEmpty() ? null : locationsById.get(f[8]);
        int odoOut = Integer.parseInt(f[9]);
        int odoIn = Integer.parseInt(f[10]);
        MovementStatus status = MovementStatus.valueOf(f[11]);

        switch (type) {
            case "NCM":
                return new NewCarMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status);

            case "DVM":
                return new DefleetMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status);

            case "WSM": {
                List<Integer> driverIDs = parseDriverIDs(f[12]);
                LocalDateTime expectedCIDT = f[13].isEmpty() ? null : LocalDateTime.parse(f[13]);
                Location expectedCILoc = f[14].isEmpty() ? null : locationsById.get(f[14]);
                return new WorkshopMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status,
                        driverIDs, expectedCIDT, expectedCILoc);
            }

            case "CTM": {
                List<Integer> driverIDs = parseDriverIDs(f[12]);
                LocalDateTime expectedCIDT = f[13].isEmpty() ? null : LocalDateTime.parse(f[13]);
                Location expectedCILoc = f[14].isEmpty() ? null : locationsById.get(f[14]);
                return new CarTransferMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status,
                        driverIDs, expectedCIDT, expectedCILoc);
            }

            case "RENTAL": {
                List<Integer> driverIDs = parseDriverIDs(f[12]);
                LocalDateTime expectedCIDT = f[13].isEmpty() ? null : LocalDateTime.parse(f[13]);
                Location expectedCILoc = f[14].isEmpty() ? null : locationsById.get(f[14]);
                boolean rsa = Boolean.parseBoolean(f[15]);
                Insurance insuranceTier = Insurance.valueOf(f[16]);

                Map<Equipment, Integer> equipment = new HashMap<>();
                if (!f[17].isEmpty()) {
                    for (String pair : f[17].split(",")) {
                        String[] kv = pair.split(":");
                        equipment.put(Equipment.valueOf(kv[0]), Integer.parseInt(kv[1]));
                    }
                }

                double price = Double.parseDouble(f[18]);

                return new RentalMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status,
                        driverIDs, expectedCIDT, expectedCILoc,
                        rsa, insuranceTier, equipment, price);
            }

            default:
                System.out.println("Unknown movement type: " + type);
                return null;
        }
    }

    private String toLine(Movement movement) {
        String type;
        StringBuilder tail = new StringBuilder();

        if (movement instanceof RentalMovement rm) {
            type = "RENTAL";
            tail.append(joinDriverIDs(rm.getDriverIDs())).append("|");
            tail.append(rm.getExpectedCIDT() != null ? rm.getExpectedCIDT() : "").append("|");
            tail.append(rm.getExpectedCILoc() != null ? rm.getExpectedCILoc().getLocationID() : "").append("|");
            tail.append(rm.hasRSA()).append("|");
            tail.append(rm.getInsuranceTier()).append("|");

            List<String> equipmentPairs = new ArrayList<>();
            for (Map.Entry<Equipment, Integer> entry : rm.getEquipment().entrySet()) {
                equipmentPairs.add(entry.getKey() + ":" + entry.getValue());
            }
            tail.append(String.join(",", equipmentPairs)).append("|");
            tail.append(rm.getPrice());

        } else if (movement instanceof WorkshopMovement wm) {
            type = "WSM";
            appendDrivenTail(tail, wm.getDriverIDs(), wm.getExpectedCIDT(), wm.getExpectedCILoc());

        } else if (movement instanceof CarTransferMovement cm) {
            type = "CTM";
            appendDrivenTail(tail, cm.getDriverIDs(), cm.getExpectedCIDT(), cm.getExpectedCILoc());

        } else if (movement instanceof NewCarMovement) {
            type = "NCM";

        } else if (movement instanceof DefleetMovement) {
            type = "DVM";

        } else {
            type = "UNKNOWN";
        }

        String base = String.join("|",
                type,
                movement.getMovementID(),
                movement.getVehicle().getPlateNumber(),
                movement.getCOStaff(),
                movement.getCIStaff() != null ? movement.getCIStaff() : "",
                movement.getCheckOutDT().toString(),
                movement.getCheckInDT() != null ? movement.getCheckInDT().toString() : "",
                movement.getCheckOutLoc().getLocationID(),
                movement.getCheckInLoc() != null ? movement.getCheckInLoc().getLocationID() : "",
                String.valueOf(movement.getOdoOut()),
                String.valueOf(movement.getOdoIn()),
                movement.getStatus().toString()
        );

        return tail.length() > 0 ? base + "|" + tail : base;
    }

    private void appendDrivenTail(StringBuilder tail, List<Integer> driverIDs,
            LocalDateTime expectedCIDT, Location expectedCILoc) {
        tail.append(joinDriverIDs(driverIDs)).append("|");
        tail.append(expectedCIDT != null ? expectedCIDT : "").append("|");
        tail.append(expectedCILoc != null ? expectedCILoc.getLocationID() : "");
    }
}
