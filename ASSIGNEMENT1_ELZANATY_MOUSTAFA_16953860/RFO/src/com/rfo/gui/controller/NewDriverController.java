package com.rfo.gui.controller;

import com.rfo.gui.view.MessageSink;
import com.rfo.gui.view.NewDriverDialog;
import com.rfo.model.Driver;
import com.rfo.service.DriverService;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.function.IntConsumer;
import javax.swing.SwingUtilities;

/**
 * Controller for the New driver pop-up. It asks the DriverService to check the
 * typed details, shows any problems next to their boxes, and when everything is
 * fine creates the driver and tells the screen that opened the pop-up which
 * driver ID was made (so the screen can add that driver to the rental).
 */
public class NewDriverController implements ActionListener {

    private final NewDriverDialog dialog;
    private final DriverService driverService;
    private final MessageSink console;
    private final IntConsumer onCreated;

    private NewDriverController(NewDriverDialog dialog, DriverService driverService, MessageSink console,
            IntConsumer onCreated) {
        this.dialog = dialog;
        this.driverService = driverService;
        this.console = console;
        this.onCreated = onCreated;
        dialog.addController(this);
    }

    /**
     * Opens the pop-up over the given screen and waits until it is closed.
     */
    public static void open(Component parent, DriverService driverService, MessageSink console, IntConsumer onCreated) {
        NewDriverDialog dialog = new NewDriverDialog(SwingUtilities.getWindowAncestor(parent));
        new NewDriverController(dialog, driverService, console, onCreated);
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (NewDriverDialog.CANCEL.equals(e.getActionCommand())) {
            console.info("New driver cancelled.");
            dialog.dispose();
            return;
        }
        Map<String, String> errors = driverService.check(dialog.getFirst(), dialog.getLast(), dialog.getLicence(),
                dialog.getEmail(), dialog.getPhone(), dialog.getStreet(), dialog.getCity(), dialog.getPostal());
        if (!errors.isEmpty()) {
            dialog.showErrors(errors);
            console.error("Fix the highlighted boxes to create the driver.");
            return;
        }
        try {
            Driver d = driverService.createDriver(dialog.getFirst(), dialog.getLast(), dialog.getLicence(),
                    dialog.getEmail(), dialog.getPhone(), dialog.getStreet(), dialog.getCity(),
                    dialog.getCountry(), dialog.getPostal());
            console.ok("Driver " + d.getDriverID() + " created: " + d.getName() + ".");
            dialog.dispose();
            onCreated.accept(d.getDriverID());
        } catch (IllegalArgumentException ex) {
            console.error(ex.getMessage());
        }
    }
}
