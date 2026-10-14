package com.rfo.gui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Login form shown in a new session tab. Only displays; the controller decides
 * what happens.
 */
public class LoginView extends JPanel {

    private final JTextField staffIdField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JButton loginButton = new JButton("Log in");
    private final JLabel messageLabel = new JLabel(" ");

    public LoginView() {
        super(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD0D5DD)),
                BorderFactory.createEmptyBorder(28, 36, 28, 36)));

        JLabel heading = new JLabel("Sign in");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(HomeView.NAVY);

        messageLabel.setForeground(new Color(0xB42318));
        messageLabel.setFont(messageLabel.getFont().deriveFont(12f));

        loginButton.setPreferredSize(new Dimension(100, 34));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 4, 0);

        c.gridy = 0;
        c.insets = new Insets(0, 0, 18, 0);
        card.add(heading, c);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = 1;
        card.add(new JLabel("Staff ID"), c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(staffIdField, c);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(new JLabel("Password"), c);
        c.gridy = 4;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(passwordField, c);
        c.gridy = 5;
        c.insets = new Insets(0, 0, 12, 0);
        card.add(messageLabel, c);
        c.gridy = 6;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        card.add(loginButton, c);

        add(card);
    }

    // button click or pressing Enter in either field all count as "log in"
    public void addController(ActionListener controller) {
        loginButton.addActionListener(controller);
        staffIdField.addActionListener(controller);
        passwordField.addActionListener(controller);
    }

    public String getStaffId() {
        return staffIdField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void showError(String message) {
        messageLabel.setText(message);
    }

    public void clearPassword() {
        passwordField.setText("");
        passwordField.requestFocusInWindow();
    }

    public void focusStaffId() {
        staffIdField.requestFocusInWindow();
    }

    // used after too many failed attempts
    public void lockForm(String message) {
        showError(message);
        staffIdField.setEnabled(false);
        passwordField.setEnabled(false);
        loginButton.setEnabled(false);
    }
}
