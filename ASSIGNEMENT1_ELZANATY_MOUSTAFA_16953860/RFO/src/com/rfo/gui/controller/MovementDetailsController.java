package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.MovementDetailsView;
import com.rfo.gui.view.MovementDetailsView.Section;
import com.rfo.model.DrivenMovement;
import com.rfo.model.Equipment;
import com.rfo.model.Movement;
import com.rfo.model.MovementStatus;
import com.rfo.model.RentalMovement;
import com.rfo.service.MovementService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Query Movement Details. Finds the movement with the
 * MovementService and turns it into titled groups of text for the view. A
 * rental, car transfer or workshop job gets the extra groups it has.
 */
public class MovementDetailsController implements ActionListener {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String NOT_YET = "Not yet";

    private final MovementDetailsView view;
    private final MovementService movementService;
    private final MessageSink console;

    public MovementDetailsController(MovementDetailsView view, MovementService movementService, MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Movement m = movementService.findMovement(view.getMovementId());
            view.showMovement(kindOf(m), "Movement " + m.getMovementID() + " · " + m.getVehicle().getPlateNumber()
                    + " " + m.getVehicle().getMake() + " " + m.getVehicle().getModel(),
                    m.getStatus().toString(), sectionsFor(m));
            console.ok("Showing movement " + m.getMovementID() + ".");
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private String kindOf(Movement m) {
        String kind = movementService.kindOf(m);
        if (!kind.equals("Movement")) {
            return kind;
        }
        // the two that are not driven: say what they are in plain words
        return m.getClass().getSimpleName().replace("Movement", "").replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private List<Section> sectionsFor(Movement m) {
        List<Section> sections = new ArrayList<>();
        boolean closed = m.getStatus() == MovementStatus.CLOSED;

        Section io = new Section("CHECK OUT AND CHECK IN");
        io.add("Checked out by", movementService.staffLabel(m.getCOStaff()));
        io.add("Checked out", when(m.getCheckOutDT()));
        io.add("From", m.getCheckOutLoc() == null ? "N/A" : m.getCheckOutLoc().getName());
        io.add("Odometer out", String.format("%,d km", m.getOdoOut()));
        io.add("Checked in by", m.getCIStaff() == null ? NOT_YET : movementService.staffLabel(m.getCIStaff()));
        io.add("Checked in", m.getCheckInDT() == null ? NOT_YET : when(m.getCheckInDT()));
        io.add("At", m.getCheckInLoc() == null ? NOT_YET : m.getCheckInLoc().getName());
        io.add("Odometer in", closed ? String.format("%,d km", m.getOdoIn()) : NOT_YET);
        sections.add(io);

        if (m instanceof DrivenMovement) {
            DrivenMovement dm = (DrivenMovement) m;
            Section drivers = new Section("DRIVERS AND EXPECTED RETURN");
            drivers.add("Driver IDs", dm.getDriverIDs().toString().replace("[", "").replace("]", ""));
            drivers.add("Expected return", when(dm.getExpectedCIDT()));
            drivers.add("Expected location", dm.getExpectedCILoc() == null ? "N/A" : dm.getExpectedCILoc().getName());
            sections.add(drivers);
        }

        if (m instanceof RentalMovement) {
            RentalMovement rm = (RentalMovement) m;
            Section rental = new Section("RENTAL");
            rental.add("Insurance", String.valueOf(rm.getInsuranceTier()));
            rental.add("Roadside assistance", rm.hasRSA() ? "Yes" : "No");
            rental.add("Equipment", equipmentText(rm.getEquipment()));
            // an open rental has no final price yet, so show its estimate
            rental.add(closed ? "Price" : "Estimated price",
                    String.format("$%,.2f", closed ? rm.getPrice() : rm.getRentalEstimate()));
            sections.add(rental);
        }
        return sections;
    }

    private String equipmentText(Map<Equipment, Integer> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return "None";
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Equipment, Integer> entry : equipment.entrySet()) {
            parts.add(entry.getKey() + " × " + entry.getValue());
        }
        return String.join(", ", parts);
    }

    private String when(LocalDateTime time) {
        return time == null ? "N/A" : time.format(WHEN);
    }
}
