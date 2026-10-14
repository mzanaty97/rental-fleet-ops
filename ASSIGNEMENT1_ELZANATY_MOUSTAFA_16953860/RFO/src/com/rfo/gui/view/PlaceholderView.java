package com.rfo.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Temporary screen for a menu option that has not been built yet.
 */
public class PlaceholderView extends JPanel {

    public PlaceholderView(String name) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        JLabel label = new JLabel(name + " - coming soon", SwingConstants.CENTER);
        label.setForeground(Color.GRAY);
        add(label, BorderLayout.CENTER);
    }
}
