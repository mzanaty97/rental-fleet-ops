package com.rfo.gui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Startup tab: the app name and a button to start a new session.
 */
public class HomeView extends JPanel {

    public static final Color NAVY = new Color(0x1F2A44);

    private final JButton newSessionButton = new JButton("New Session");

    public HomeView() {
        super(new GridBagLayout());
        setBackground(Color.WHITE);

        JLabel title = new JLabel("RentalFleetOps");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 40f));
        title.setForeground(NAVY);

        JLabel subtitle = new JLabel("Rental fleet management system");
        subtitle.setFont(subtitle.getFont().deriveFont(16f));
        subtitle.setForeground(Color.GRAY);

        newSessionButton.setFont(newSessionButton.getFont().deriveFont(Font.BOLD, 16f));
        newSessionButton.setPreferredSize(new Dimension(220, 48));
        newSessionButton.setToolTipText("Open a new login tab");

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        add(title, c);
        c.gridy = 1;
        c.insets = new Insets(4, 0, 32, 0);
        add(subtitle, c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 0);
        add(newSessionButton, c);
    }

    public void addController(ActionListener controller) {
        newSessionButton.addActionListener(controller);
    }
}
