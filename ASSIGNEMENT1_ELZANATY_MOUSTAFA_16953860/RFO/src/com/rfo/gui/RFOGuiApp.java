package com.rfo.gui;

import com.rfo.db.DBConnection;
import com.rfo.gui.controller.AppController;
import com.rfo.gui.view.MainWindow;
import com.rfo.service.AuthService;
import com.rfo.service.DataStore;
import com.rfo.service.StaffService;
import javax.swing.SwingUtilities;
import com.rfo.gui.theme.Theme;

/**
 * Starts the Swing version of RFO: creates the model, views and controllers and
 * connects them.
 */
public class RFOGuiApp {

    public static void main(String[] args) {
        Theme.installDefaults();

        // close the embedded Derby database cleanly when the window is closed
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DBConnection.getInstance().shutdown()));

        // GUI work belongs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new RFOGuiApp());
    }

    public RFOGuiApp() {
        DataStore dataStore = new DataStore();
        AuthService auth = new AuthService(dataStore);
        StaffService staffService = new StaffService(dataStore);

        MainWindow window = new MainWindow();
        new AppController(window, auth, staffService, dataStore);
        window.setVisible(true);
    }
}
