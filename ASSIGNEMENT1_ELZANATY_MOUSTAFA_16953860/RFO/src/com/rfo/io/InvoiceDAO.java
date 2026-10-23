package com.rfo.io;

import com.rfo.db.DBConnection;
import com.rfo.model.Invoice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * DAO for the INVOICES table. An invoice is a saved snapshot and is never
 * changed once written, so saveAll only inserts invoices that are not in the
 * table yet.
 */
public class InvoiceDAO {

    private static final String COLUMNS = "INVOICE_ID, MOVEMENT_ID, ISSUED_AT, PLATE, VEHICLE, DRIVERS, CHECKOUT_TEXT, "
            + "CHECKIN_TEXT, ODO_OUT, ODO_IN, DAYS, BASE, EQUIPMENT, INSURANCE_TIER, INSURANCE, RSA, TOTAL, "
            + "CO_STAFF, CI_STAFF";

    public List<Invoice> loadAll() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM INVOICES ORDER BY INVOICE_ID ASC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                invoices.add(new Invoice(rs.getInt("INVOICE_ID"), rs.getString("MOVEMENT_ID"),
                        rs.getTimestamp("ISSUED_AT").toLocalDateTime(), rs.getString("PLATE"),
                        rs.getString("VEHICLE"), rs.getString("DRIVERS"), rs.getString("CHECKOUT_TEXT"),
                        rs.getString("CHECKIN_TEXT"), rs.getInt("ODO_OUT"), rs.getInt("ODO_IN"),
                        rs.getInt("DAYS"), rs.getDouble("BASE"), rs.getDouble("EQUIPMENT"),
                        rs.getString("INSURANCE_TIER"), rs.getDouble("INSURANCE"), rs.getDouble("RSA"),
                        rs.getDouble("TOTAL"), rs.getString("CO_STAFF"), rs.getString("CI_STAFF")));
            }
        } catch (SQLException e) {
            System.out.println("Error reading invoices table: " + e.getMessage());
        }
        return invoices;
    }

    public void saveAll(List<Invoice> invoices) {
        String exists = "SELECT 1 FROM INVOICES WHERE INVOICE_ID = ?";
        String insert = "INSERT INTO INVOICES (" + COLUMNS + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            Connection conn = getConnection();
            for (Invoice inv : invoices) {
                try (PreparedStatement check = conn.prepareStatement(exists)) {
                    check.setInt(1, inv.getInvoiceID());
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            continue; // already saved, invoices never change
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    int i = 1;
                    ps.setInt(i++, inv.getInvoiceID());
                    ps.setString(i++, inv.getMovementID());
                    ps.setTimestamp(i++, Timestamp.valueOf(inv.getIssuedAt()));
                    ps.setString(i++, inv.getPlate());
                    ps.setString(i++, inv.getVehicle());
                    ps.setString(i++, inv.getDrivers());
                    ps.setString(i++, inv.getCheckOut());
                    ps.setString(i++, inv.getCheckIn());
                    ps.setInt(i++, inv.getOdoOut());
                    ps.setInt(i++, inv.getOdoIn());
                    ps.setInt(i++, inv.getDays());
                    ps.setDouble(i++, inv.getBase());
                    ps.setDouble(i++, inv.getEquipment());
                    ps.setString(i++, inv.getInsuranceTier());
                    ps.setDouble(i++, inv.getInsurance());
                    ps.setDouble(i++, inv.getRsa());
                    ps.setDouble(i++, inv.getTotal());
                    ps.setString(i++, inv.getCoStaff());
                    ps.setString(i++, inv.getCiStaff());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error writing invoices table: " + e.getMessage());
        }
    }

    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }
}
