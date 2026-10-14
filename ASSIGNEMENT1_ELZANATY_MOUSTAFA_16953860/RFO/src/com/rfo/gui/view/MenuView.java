package com.rfo.gui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * One menu screen: a title and one button per option, like a console menu. Each
 * option button carries its label as its action command, and an optional Back
 * button returns to the menu above.
 */
public class MenuView extends JPanel {

    public static final String BACK = "Back";

    private final List<JButton> buttons = new ArrayList<>();

    public MenuView(String title, List<String> options, boolean withBack) {
        super(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(HomeView.NAVY);
        gbc.insets = new Insets(0, 0, 20, 0);
        add(heading, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        for (String option : options) {
            gbc.gridy++;
            add(createButton(option, option), gbc);
        }

        if (withBack) {
            gbc.gridy++;
            gbc.insets = new Insets(20, 0, 0, 0);
            add(createButton("Back", BACK), gbc);
        }
    }

    private JButton createButton(String text, String command) {
        JButton button = new JButton(text);
        button.setActionCommand(command);
        button.setPreferredSize(new Dimension(280, 42));
        buttons.add(button);
        return button;
    }

    public void addController(ActionListener controller) {
        for (JButton button : buttons) {
            button.addActionListener(controller);
        }
    }
}
