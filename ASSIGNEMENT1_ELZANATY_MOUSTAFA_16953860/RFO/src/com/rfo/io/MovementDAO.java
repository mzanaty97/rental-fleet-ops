package com.rfo.io;

import com.rfo.db.DBConnection;
import com.rfo.model.CarTransferMovement;
import com.rfo.model.DefleetMovement;
import com.rfo.model.DrivenMovement;
import com.rfo.model.Equipment;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.NewCarMovement;
import com.rfo.model.RentalMovement;
import com.rfo.model.Vehicle;
import com.rfo.model.WorkshopMovement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Generated with assistance from Claude. I designed the structure and logic
// (following my Part 1 design); Claude wrote the repeated methods and boilerplate.
/**
 * DAO replacement for MovementFileHandler. Same public contract as the
 * file-based version, now backed by a single MOVEMENTS table (with a
 * MOVEMENT_TYPE discriminator, matching the type|... prefix already used in
 * movements.txt) plus two junction tables: - MOVEMENT_DRIVERS for
 * DrivenMovement.driverIDs - MOVEMENT_EQUIPMENT for RentalMovement.equipment
 *
 * Columns that only apply to some subtypes (EXPECTED_CI_DT, EXPECTED_CI_LOC_ID,
 * RSA, INSURANCE_TIER, PRICE) are left NULL/default for movement types that
 * don't use them, same idea as the variable tail fields in movements.txt.
 */
public class MovementDAO {

    public List<Movement> loadAll(List<Vehicle> allVehicles, List<Location> allLocations) {
        List<Movement> movements = new ArrayList<>();
        Map<String, Vehicle> vehiclesByPlate = buildVehicleLookup(allVehicles);
        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        String sql = "SELECT * FROM MOVEMENTS";

        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Movement movement = mapRow(rs, vehiclesByPlate, locationsById);
                if (movement != null) {
                    movements.add(movement);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading movements table: " + e.getMessage());
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
        String update = "UPDATE MOVEMENTS SET MOVEMENT_TYPE=?, PLATE_NUMBER=?, CO_STAFF=?, CI_STAFF=?, "
                + "CHECKOUT_DT=?, CHECKIN_DT=?, CHECKOUT_LOC_ID=?, CHECKIN_LOC_ID=?, ODO_OUT=?, ODO_IN=?, STATUS=?, "
                + "EXPECTED_CI_DT=?, EXPECTED_CI_LOC_ID=?, RSA=?, INSURANCE_TIER=?, PRICE=? WHERE MOVEMENT_ID=?";
        String insert = "INSERT INTO MOVEMENTS (MOVEMENT_ID, MOVEMENT_TYPE, PLATE_NUMBER, CO_STAFF, CI_STAFF, "
                + "CHECKOUT_DT, CHECKIN_DT, CHECKOUT_LOC_ID, CHECKIN_LOC_ID, ODO_OUT, ODO_IN, STATUS, "
                + "EXPECTED_CI_DT, EXPECTED_CI_LOC_ID, RSA, INSURANCE_TIER, PRICE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Connection conn = getConnection();
            for (Movement movement : movements) {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    bindFields(ps, movement, false);
                    if (ps.executeUpdate() == 0) {
                        try (PreparedStatement ins = conn.prepareStatement(insert)) {
                            bindFields(ins, movement, true);
                            ins.executeUpdate();
                        }
                    }
                }
                saveDriverIDs(conn, movement);
                saveEquipment(conn, movement);
            }
        } catch (SQLException e) {
            System.out.println("Error writing movements table: " + e.getMessage());
        }
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
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

    private String typeOf(Movement m) {
        if (m instanceof RentalMovement) {
            return "RENTAL";
        }
        if (m instanceof WorkshopMovement) {
            return "WSM";
        }
        if (m instanceof CarTransferMovement) {
            return "CTM";
        }
        if (m instanceof NewCarMovement) {
            return "NCM";
        }
        if (m instanceof DefleetMovement) {
            return "DVM";
        }
        return "UNKNOWN";
    }

    private void bindFields(PreparedStatement ps, Movement m, boolean isInsert) throws SQLException {
        int i = 1;
        if (isInsert) {
            ps.setString(i++, m.getMovementID());
        }
        ps.setString(i++, typeOf(m));
        ps.setString(i++, m.getVehicle().getPlateNumber());
        ps.setString(i++, m.getCOStaff());
        ps.setString(i++, m.getCIStaff());
        ps.setTimestamp(i++, m.getCheckOutDT() != null ? Timestamp.valueOf(m.getCheckOutDT()) : null);
        ps.setTimestamp(i++, m.getCheckInDT() != null ? Timestamp.valueOf(m.getCheckInDT()) : null);
        ps.setString(i++, m.getCheckOutLoc() != null ? m.getCheckOutLoc().getLocationID() : null);
        ps.setString(i++, m.getCheckInLoc() != null ? m.getCheckInLoc().getLocationID() : null);
        ps.setInt(i++, m.getOdoOut());
        ps.setInt(i++, m.getOdoIn());
        ps.setString(i++, m.getStatus().toString());

        LocalDateTime expectedCIDT = null;
        Location expectedCILoc = null;
        boolean rsa = false;
        Insurance insuranceTier = null;
        double price = 0.0;

        if (m instanceof DrivenMovement dm) {
            expectedCIDT = dm.getExpectedCIDT();
            expectedCILoc = dm.getExpectedCILoc();
        }
        if (m instanceof RentalMovement rm) {
            rsa = rm.hasRSA();
            insuranceTier = rm.getInsuranceTier();
            price = rm.getPrice();
        }

        ps.setTimestamp(i++, expectedCIDT != null ? Timestamp.valueOf(expectedCIDT) : null);
        ps.setString(i++, expectedCILoc != null ? expectedCILoc.getLocationID() : null);
        ps.setBoolean(i++, rsa);
        ps.setString(i++, insuranceTier != null ? insuranceTier.toString() : null);
        ps.setDouble(i++, price);

        if (!isInsert) {
            ps.setString(i++, m.getMovementID());
        }
    }

    private void saveDriverIDs(Connection conn, Movement m) throws SQLException {
        if (!(m instanceof DrivenMovement dm)) {
            return;
        }
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM MOVEMENT_DRIVERS WHERE MOVEMENT_ID=?")) {
            del.setString(1, m.getMovementID());
            del.executeUpdate();
        }
        String insert = "INSERT INTO MOVEMENT_DRIVERS (MOVEMENT_ID, DRIVER_ID) VALUES (?,?)";
        try (PreparedStatement ins = conn.prepareStatement(insert)) {
            for (int driverID : dm.getDriverIDs()) {
                ins.setString(1, m.getMovementID());
                ins.setInt(2, driverID);
                ins.executeUpdate();
            }
        }
    }

    private void saveEquipment(Connection conn, Movement m) throws SQLException {
        if (!(m instanceof RentalMovement rm)) {
            return;
        }
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM MOVEMENT_EQUIPMENT WHERE MOVEMENT_ID=?")) {
            del.setString(1, m.getMovementID());
            del.executeUpdate();
        }
        String insert = "INSERT INTO MOVEMENT_EQUIPMENT (MOVEMENT_ID, EQUIPMENT_TYPE, QUANTITY) VALUES (?,?,?)";
        try (PreparedStatement ins = conn.prepareStatement(insert)) {
            for (Map.Entry<Equipment, Integer> entry : rm.getEquipment().entrySet()) {
                ins.setString(1, m.getMovementID());
                ins.setString(2, entry.getKey().toString());
                ins.setInt(3, entry.getValue());
                ins.executeUpdate();
            }
        }
    }

    private List<Integer> loadDriverIDs(String movementID) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DRIVER_ID FROM MOVEMENT_DRIVERS WHERE MOVEMENT_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, movementID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("DRIVER_ID"));
                }
            }
        }
        return ids;
    }

    private Map<Equipment, Integer> loadEquipment(String movementID) throws SQLException {
        Map<Equipment, Integer> equipment = new HashMap<>();
        String sql = "SELECT EQUIPMENT_TYPE, QUANTITY FROM MOVEMENT_EQUIPMENT WHERE MOVEMENT_ID=?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, movementID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    equipment.put(Equipment.valueOf(rs.getString("EQUIPMENT_TYPE")), rs.getInt("QUANTITY"));
                }
            }
        }
        return equipment;
    }

    private Movement mapRow(ResultSet rs, Map<String, Vehicle> vehiclesByPlate, Map<String, Location> locationsById) throws SQLException {
        String type = rs.getString("MOVEMENT_TYPE");
        String movementID = rs.getString("MOVEMENT_ID");
        Vehicle vehicle = vehiclesByPlate.get(rs.getString("PLATE_NUMBER"));
        String COStaff = rs.getString("CO_STAFF");
        String CIStaff = rs.getString("CI_STAFF");
        LocalDateTime checkOutDT = toLocalDateTime(rs.getTimestamp("CHECKOUT_DT"));
        LocalDateTime checkInDT = toLocalDateTime(rs.getTimestamp("CHECKIN_DT"));
        Location checkOutLoc = locationsById.get(rs.getString("CHECKOUT_LOC_ID"));
        Location checkInLoc = locationsById.get(rs.getString("CHECKIN_LOC_ID"));
        int odoOut = rs.getInt("ODO_OUT");
        int odoIn = rs.getInt("ODO_IN");
        MovementStatus status = MovementStatus.valueOf(rs.getString("STATUS"));

        switch (type) {
            case "NCM":
                return new NewCarMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status);

            case "DVM":
                return new DefleetMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status);

            case "WSM": {
                List<Integer> driverIDs = loadDriverIDs(movementID);
                LocalDateTime expectedCIDT = toLocalDateTime(rs.getTimestamp("EXPECTED_CI_DT"));
                Location expectedCILoc = locationsById.get(rs.getString("EXPECTED_CI_LOC_ID"));
                return new WorkshopMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status,
                        driverIDs, expectedCIDT, expectedCILoc);
            }

            case "CTM": {
                List<Integer> driverIDs = loadDriverIDs(movementID);
                LocalDateTime expectedCIDT = toLocalDateTime(rs.getTimestamp("EXPECTED_CI_DT"));
                Location expectedCILoc = locationsById.get(rs.getString("EXPECTED_CI_LOC_ID"));
                return new CarTransferMovement(movementID, vehicle, COStaff, CIStaff,
                        checkOutDT, checkInDT, checkOutLoc, checkInLoc, odoOut, odoIn, status,
                        driverIDs, expectedCIDT, expectedCILoc);
            }

            case "RENTAL": {
                List<Integer> driverIDs = loadDriverIDs(movementID);
                LocalDateTime expectedCIDT = toLocalDateTime(rs.getTimestamp("EXPECTED_CI_DT"));
                Location expectedCILoc = locationsById.get(rs.getString("EXPECTED_CI_LOC_ID"));
                boolean rsa = rs.getBoolean("RSA");
                Insurance insuranceTier = Insurance.valueOf(rs.getString("INSURANCE_TIER"));
                Map<Equipment, Integer> equipment = loadEquipment(movementID);
                double price = rs.getDouble("PRICE");

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

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
