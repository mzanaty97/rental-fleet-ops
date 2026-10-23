package com.rfo.gui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * One menu screen: a title and one numbered button per option, like a console
 * menu. An option can be chosen by clicking it, or by typing its number and
 * pressing Enter. Backspace deletes a typed digit, or goes back when nothing is
 * typed. Each option button carries its label as its action command, and an
 * optional Back button (numbered last, like the console's exit option) returns
 * to the menu above.
 */
public class MenuView extends JPanel {

    public static final String BACK = "Back";

    private static final int MAX_DIGITS = 2;

    private final List<JButton> optionButtons = new ArrayList<>();
    private final List<JButton> allButtons = new ArrayList<>();
    private final JButton backButton;
    private final JLabel choiceBox = new JLabel("", SwingConstants.CENTER);
    private final StringBuilder typed = new StringBuilder();
    private MessageSink console;

    public MenuView(String title, List<String> options, boolean withBack) {
        super(new GridBagLayout());
        setBackground(Color.WHITE);
        setFocusable(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(HomeView.NAVY);
        gbc.insets = new Insets(0, 0, 20, 0);
        add(heading, gbc);

        gbc.insets = new Insets(6, 0, 6, 0);
        for (int i = 0; i < options.size(); i++) {
            gbc.gridy++;
            JButton button = createButton((i + 1) + ". " + options.get(i), options.get(i));
            optionButtons.add(button);
            add(button, gbc);
        }

        if (withBack) {
            gbc.gridy++;
            gbc.insets = new Insets(20, 0, 0, 0);
            backButton = createButton((options.size() + 1) + ". Back", BACK);
            add(backButton, gbc);
        } else {
            backButton = null;
        }

        // small darker box that shows the number typed so far
        choiceBox.setOpaque(true);
        choiceBox.setBackground(new Color(0xD5D9E2));
        choiceBox.setForeground(HomeView.NAVY);
        choiceBox.setFont(choiceBox.getFont().deriveFont(Font.BOLD, 16f));
        choiceBox.setBorder(BorderFactory.createLineBorder(new Color(0xA9AFBE)));
        choiceBox.setPreferredSize(new Dimension(64, 32));
        gbc.gridy++;
        gbc.insets = new Insets(18, 0, 0, 0);
        add(choiceBox, gbc);

        installKeys();
    }

    private JButton createButton(String text, String command) {
        JButton button = new JButton(text);
        button.setActionCommand(command);
        button.setPreferredSize(new Dimension(280, 42));
        allButtons.add(button);
        return button;
    }

    /**
     * Where "Please enter a valid option." is printed when a typed number
     * matches nothing.
     */
    public void setMessageSink(MessageSink console) {
        this.console = console;
    }

    // keys work whenever this menu is the one showing, wherever the focus is
    private void installKeys() {
        for (char digit = '0'; digit <= '9'; digit++) {
            final char c = digit;
            bind(KeyStroke.getKeyStroke(c), "digit" + c, () -> typeDigit(c));
        }
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter", this::choose);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "back", this::backspace);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clear", this::clearTyped);
    }

    private void bind(KeyStroke key, String name, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void typeDigit(char digit) {
        if (typed.length() < MAX_DIGITS) {
            typed.append(digit);
            updateChoiceBox();
        }
    }

    private void backspace() {
        if (typed.length() > 0) {
            typed.deleteCharAt(typed.length() - 1);
            updateChoiceBox();
        } else if (backButton != null) {
            backButton.doClick();
        }
    }

    private void clearTyped() {
        typed.setLength(0);
        updateChoiceBox();
    }

    private void choose() {
        if (typed.length() == 0) {
            return;
        }
        int number = Integer.parseInt(typed.toString());
        clearTyped();

        if (number >= 1 && number <= optionButtons.size()) {
            optionButtons.get(number - 1).doClick();
        } else if (backButton != null && number == optionButtons.size() + 1) {
            backButton.doClick();
        } else if (console != null) {
            console.print("Please enter a valid option.");
        }
    }

    private void updateChoiceBox() {
        choiceBox.setText(typed.toString());
    }

    public void addController(ActionListener controller) {
        for (JButton button : allButtons) {
            button.addActionListener(controller);
        }
    }
}
