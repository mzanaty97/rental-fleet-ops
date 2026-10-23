package com.rfo.io;

import com.rfo.db.DBConnection;
import com.rfo.model.FuelType;
import com.rfo.model.Location;
import com.rfo.model.Vehicle;
import com.rfo.model.VehicleCategory;
import com.rfo.model.VehicleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO replacement for VehicleFileHandler. Same public contract as the
 * file-based version, now backed by the VEHICLES table.
 *
 * Note: YEAR is a reserved word in Derby, so the column is named YR.
 */
public class VehicleDAO {

    public List<Vehicle> loadAll(List<Location> allLocations) {
        List<Vehicle> vehicles = new ArrayList<>();
        Map<String, Location> locationsById = buildLocationLookup(allLocations);

        String sql = "SELECT PLATE_NUMBER, MAKE, MODEL, YR, CATEGORY, RATE, HAS_GPS, FUEL_TYPE, "
                + "CURRENT_ODOMETER, LAST_SERVICE_ODOMETER, STATUS, CURRENT_LOCATION_ID, SERVICE_FLAG FROM VEHICLES";

        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehicles.add(mapRow(rs, locationsById));
            }
        } catch (SQLException e) {
            System.out.println("Error reading vehicles table: " + e.getMessage());
        }

        return vehicles;
    }

    public void saveAll(List<Vehicle> vehicles) {
        String update = "UPDATE VEHICLES SET MAKE=?, MODEL=?, YR=?, CATEGORY=?, RATE=?, HAS_GPS=?, FUEL_TYPE=?, "
                + "CURRENT_ODOMETER=?, LAST_SERVICE_ODOMETER=?, STATUS=?, CURRENT_LOCATION_ID=?, SERVICE_FLAG=? WHERE PLATE_NUMBER=?";
        String insert = "INSERT INTO VEHICLES (PLATE_NUMBER, MAKE, MODEL, YR, CATEGORY, RATE, HAS_GPS, FUEL_TYPE, "
                + "CURRENT_ODOMETER, LAST_SERVICE_ODOMETER, STATUS, CURRENT_LOCATION_ID, SERVICE_FLAG) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            Connection conn = getConnection();
            for (Vehicle vehicle : vehicles) {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    bindUpdate(ps, vehicle);
                    if (ps.executeUpdate() == 0) {
                        try (PreparedStatement ins = conn.prepareStatement(insert)) {
                            bindInsert(ins, vehicle);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error writing vehicles table: " + e.getMessage());
        }
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    private Map<String, Location> buildLocationLookup(List<Location> allLocations) {
        Map<String, Location> map = new HashMap<>();
        for (Location loc : allLocations) {
            map.put(loc.getLocationID(), loc);
        }
        return map;
    }

    private Vehicle mapRow(ResultSet rs, Map<String, Location> locationsById) throws SQLException {
        Location currentLocation = locationsById.get(rs.getString("CURRENT_LOCATION_ID"));
        return new Vehicle(
                rs.getString("PLATE_NUMBER"),
                rs.getString("MAKE"),
                rs.getString("MODEL"),
                rs.getInt("YR"),
                VehicleCategory.valueOf(rs.getString("CATEGORY")),
                FuelType.valueOf(rs.getString("FUEL_TYPE")),
                rs.getBoolean("HAS_GPS"),
                rs.getDouble("RATE"),
                rs.getInt("CURRENT_ODOMETER"),
                rs.getInt("LAST_SERVICE_ODOMETER"),
                VehicleStatus.valueOf(rs.getString("STATUS")),
                currentLocation,
                rs.getBoolean("SERVICE_FLAG")
        );
    }

    private void bindUpdate(PreparedStatement ps, Vehicle vehicle) throws SQLException {
        ps.setString(1, vehicle.getMake());
        ps.setString(2, vehicle.getModel());
        ps.setInt(3, vehicle.getYear());
        ps.setString(4, vehicle.getCategory().toString());
        ps.setDouble(5, vehicle.getRate());
        ps.setBoolean(6, vehicle.hasGPS());
        ps.setString(7, vehicle.getFuelType().toString());
        ps.setInt(8, vehicle.getCurrentOdometer());
        ps.setInt(9, vehicle.getLastServiceOdometer());
        ps.setString(10, vehicle.getStatus().toString());
        ps.setString(11, vehicle.getCurrentLocation() != null ? vehicle.getCurrentLocation().getLocationID() : null);
        ps.setBoolean(12, vehicle.isDueForService());
        ps.setString(13, vehicle.getPlateNumber());
    }

    private void bindInsert(PreparedStatement ps, Vehicle vehicle) throws SQLException {
        ps.setString(1, vehicle.getPlateNumber());
        ps.setString(2, vehicle.getMake());
        ps.setString(3, vehicle.getModel());
        ps.setInt(4, vehicle.getYear());
        ps.setString(5, vehicle.getCategory().toString());
        ps.setDouble(6, vehicle.getRate());
        ps.setBoolean(7, vehicle.hasGPS());
        ps.setString(8, vehicle.getFuelType().toString());
        ps.setInt(9, vehicle.getCurrentOdometer());
        ps.setInt(10, vehicle.getLastServiceOdometer());
        ps.setString(11, vehicle.getStatus().toString());
        ps.setString(12, vehicle.getCurrentLocation() != null ? vehicle.getCurrentLocation().getLocationID() : null);
        ps.setBoolean(13, vehicle.isDueForService());
    }
}
