package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.VehicleMovementsView;
import com.rfo.service.MovementService;
import com.rfo.service.MovementService.HistoryRow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Controller for Show Vehicle Movements. Asks the MovementService for the
 * vehicle's history and turns each movement into one row of text for the table.
 * A check-in that has not happened yet shows the expected location and time
 * with a star.
 */
public class VehicleMovementsController implements ActionListener {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VehicleMovementsView view;
    private final MovementService movementService;
    private final MessageSink console;

    public VehicleMovementsController(VehicleMovementsView view, MovementService movementService,
            MessageSink console) {
        this.view = view;
        this.movementService = movementService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean reopened = VehicleMovementsView.OPENED.equals(e.getActionCommand());
        String rego = view.getRego();
        if (reopened && rego.isEmpty()) {
            return;     // nothing was searched yet
        }
        try {
            List<HistoryRow> history = movementService.vehicleHistory(rego);
            List<Object[]> rows = new ArrayList<>();
            boolean anyExpected = false;
            for (HistoryRow r : history) {
                String star = r.expected ? " *" : "";
                anyExpected |= r.expected;
                rows.add(new Object[]{r.type, r.movementID, text(r.outLocation), when(r.outTime), number(r.odoOut),
                    text(r.inLocation) + (r.inLocation == null ? "" : star),
                    r.inTime == null ? "" : when(r.inTime) + star, number(r.odoIn), statusOf(r)});
            }
            String plate = rego.toUpperCase();
            view.showRows(plate + " · " + history.size() + (history.size() == 1 ? " movement" : " movements")
                    + ", newest first", rows,
                    anyExpected ? "* Expected check-in. The vehicle has not been checked in yet." : "");
            if (!reopened) {
                console.ok("Showing " + history.size() + " movements for " + plate + ".");
            }
        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
            if (!reopened) {
                console.error(ex.getMessage());
            }
        }
    }

    /**
     * The word shown in the Status column. A new vehicle is "YARDED" and a
     * defleet is "DEFLEETED".
     */
    private String statusOf(HistoryRow r) {
        if (r.type.equals("NVM")) {
            return "YARDED";
        }
        if (r.type.equals("DVM")) {
            return "DEFLEETED";
        }
        return r.status.toString();
    }

    private String text(String value) {
        return value == null ? "" : value;
    }

    private String when(LocalDateTime time) {
        return time == null ? "" : time.format(WHEN);
    }

    /**
     * A whole number with thousands separators, or empty when there is no
     * reading yet.
     */
    private String number(Integer value) {
        return value == null ? "" : String.format("%,d", value);
    }
}
