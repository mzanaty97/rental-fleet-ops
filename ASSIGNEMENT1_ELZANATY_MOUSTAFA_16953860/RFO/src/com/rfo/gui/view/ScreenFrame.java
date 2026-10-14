package com.rfo.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Wraps a feature screen with a bar on top: a Back button and the screen's
 * title. Every feature screen sits inside one of these so the way back to the
 * menu is always in the same place.
 */
public class ScreenFrame extends JPanel {

    private final JButton backButton = new JButton("< Back");

    public ScreenFrame(String title, JComponent content) {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        backButton.setActionCommand(MenuView.BACK);

        JLabel heading = new JLabel(title);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        heading.setForeground(HomeView.NAVY);

        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 12, 6, 12));
        bar.add(backButton, BorderLayout.WEST);
        bar.add(heading, BorderLayout.CENTER);

        add(bar, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    public void addController(ActionListener controller) {
        backButton.addActionListener(controller);
    }
}
