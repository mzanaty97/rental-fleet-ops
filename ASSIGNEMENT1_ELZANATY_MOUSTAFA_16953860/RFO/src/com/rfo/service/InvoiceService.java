package com.rfo.service;

import com.rfo.io.InvoicePDFWriter;
import com.rfo.model.Driver;
import com.rfo.model.Invoice;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.model.Staff;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Business rules for invoices. An invoice is made once for each closed rental
 * and saved in the database; after that it is only read, so a reprint always
 * matches what was charged.
 */
public class InvoiceService {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a");

    private final DataStore dataStore;

    public InvoiceService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Finds the invoice for a closed rental, from a movement ID or a plate
     * number (a plate finds that vehicle's most recent closed rental). If the
     * rental was closed before invoices existed, the invoice is made now.
     * Throws with the reason if there is nothing to invoice.
     */
    public Invoice findInvoice(String idOrPlate) {
        String text = idOrPlate == null ? "" : idOrPlate.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Enter a movement ID or a plate number.");
        }
        Movement byId = dataStore.getBy(dataStore.getMovements(), m -> m.getMovementID().equalsIgnoreCase(text));
        if (byId != null) {
            return invoiceFor(byId);
        }

        // a plate: use the latest closed rental for that vehicle
        RentalMovement latest = null;
        boolean plateKnown = false;
        for (Movement m : dataStore.getMovements()) {
            if (m.getVehicle().getPlateNumber().equalsIgnoreCase(text)) {
                plateKnown = true;
                if (m instanceof RentalMovement && m.getStatus() == MovementStatus.CLOSED
                        && (latest == null || m.getCheckInDT().isAfter(latest.getCheckInDT()))) {
                    latest = (RentalMovement) m;
                }
            }
        }
        if (latest != null) {
            return invoiceFor(latest);
        }
        throw new IllegalArgumentException(plateKnown
                ? "There is no closed rental for " + text.toUpperCase() + " yet."
                : "No movement found for that movement ID or plate number.");
    }

    private Invoice invoiceFor(Movement movement) {
        if (!(movement instanceof RentalMovement)) {
            throw new IllegalArgumentException("Only rentals have invoices (" + movement.getMovementID() + " is not a rental).");
        }
        if (movement.getStatus() != MovementStatus.CLOSED) {
            throw new IllegalArgumentException("Not closed yet (" + movement.getStatus()
                    + "). Close it first");
        }
        return issueFor((RentalMovement) movement);
    }

    /**
     * Returns the saved invoice for a closed rental, making and saving it first
     * if it does not exist yet.
     */
    public Invoice issueFor(RentalMovement rental) {
        return issueFor(rental, LocalDateTime.now());
    }

    /**
     * The same, but the invoice carries the given issue time (used when old
     * rentals are loaded as sample data).
     */
    public Invoice issueFor(RentalMovement rental, LocalDateTime issuedAt) {
        Invoice existing = dataStore.getBy(dataStore.getInvoices(), i -> i.getMovementID().equals(rental.getMovementID()));
        if (existing != null) {
            return existing;
        }

        RentalMovement.Quote charges = rental.getFinalCharges();
        int nextID = 1;
        for (Invoice i : dataStore.getInvoices()) {
            nextID = Math.max(nextID, i.getInvoiceID() + 1);
        }
        Invoice invoice = new Invoice(nextID, rental.getMovementID(), issuedAt,
                rental.getVehicle().getPlateNumber(),
                rental.getVehicle().getMake() + " " + rental.getVehicle().getModel() + " " + rental.getVehicle().getYear(),
                driverNames(rental), place(rental.getCheckOutDT(), rental.getCheckOutLoc() == null ? null : rental.getCheckOutLoc().getName()),
                place(rental.getCheckInDT(), rental.getCheckInLoc() == null ? null : rental.getCheckInLoc().getName()),
                rental.getOdoOut(), rental.getOdoIn(), charges.days, charges.base, charges.equipment,
                rental.getInsuranceTier().name(), charges.insurance, charges.rsa, rental.getPrice(),
                staffName(rental.getCOStaff()), staffName(rental.getCIStaff()));
        dataStore.getInvoices().add(invoice);
        dataStore.saveInvoices();
        return invoice;
    }

    /**
     * The invoice as plain text lines, for the console panel and the
     * command-line menu.
     */
    public String toText(Invoice inv) {
        return inv.getNumber() + " | rental " + inv.getMovementID() + " | " + inv.getVehicle() + " (" + inv.getPlate() + ")\n"
                + "Drivers: " + inv.getDrivers() + "\n"
                + "Out: " + inv.getCheckOut() + "\n"
                + "In:  " + inv.getCheckIn() + "\n"
                + String.format("Hire %s + insurance (%s) %s + RSA %s + equipment %s = TOTAL %s",
                        InvoicePDFWriter.money(inv.getBase()), inv.getInsuranceTier(), InvoicePDFWriter.money(inv.getInsurance()),
                        InvoicePDFWriter.money(inv.getRsa()), InvoicePDFWriter.money(inv.getEquipment()),
                        InvoicePDFWriter.money(inv.getTotal()));
    }

    /**
     * Saves the invoice as a PDF file. Throws with the reason if the file
     * cannot be written.
     */
    public void writePdf(Invoice invoice, File file) {
        try {
            new InvoicePDFWriter().write(invoice, file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not save the PDF: " + e.getMessage());
        }
    }

    private String driverNames(RentalMovement rental) {
        StringBuilder names = new StringBuilder();
        for (int id : rental.getDriverIDs()) {
            Driver d = dataStore.getBy(dataStore.getDrivers(), x -> x.getDriverID() == id);
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(d == null ? "Driver " + id : d.getName() + " (" + id + ")");
        }
        return names.toString();
    }

    private String staffName(String staffID) {
        Staff s = staffID == null ? null : dataStore.getBy(dataStore.getStaff(), x -> x.getStaffID().equals(staffID));
        return s == null ? String.valueOf(staffID) : s.getName();
    }

    private String place(LocalDateTime when, String locationName) {
        return (when == null ? "-" : when.format(WHEN)) + " - " + (locationName == null ? "-" : locationName);
    }
}
