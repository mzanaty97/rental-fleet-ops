package com.rfo.gui.view;

import com.rfo.model.CountryCode;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The Add Location form. It only collects what the user types.
 */
public class AddLocationView extends JPanel {

    private final JTextField idField = new JTextField(20);
    private final JTextField nameField = new JTextField(20);
    private final JTextField streetField = new JTextField(20);
    private final JTextField cityField = new JTextField(20);
    private final JComboBox<CountryCode> countryBox = new JComboBox<>(CountryCode.values());
    private final JTextField postalField = new JTextField(20);
    private final JButton addButton = new JButton("Add location");

    public AddLocationView() {
        super(new GridBagLayout());
        setBackground(Color.WHITE);
        countryBox.setSelectedItem(CountryCode.NZ);

        addRow(0, "Location ID", idField);
        addRow(1, "Name", nameField);
        addRow(2, "Street", streetField);
        addRow(3, "City", cityField);
        addRow(4, "Country", countryBox);
        addRow(5, "Postal code", postalField);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(16, 6, 6, 6);
        add(addButton, gbc);
    }

    private void addRow(int row, String label, java.awt.Component field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0;
        add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(field, gbc);
    }

    public String getLocationID() {
        return idField.getText();
    }

    public String getLocationName() {
        return nameField.getText();
    }

    public String getStreet() {
        return streetField.getText();
    }

    public String getCity() {
        return cityField.getText();
    }

    public CountryCode getCountry() {
        return (CountryCode) countryBox.getSelectedItem();
    }

    public String getPostalCode() {
        return postalField.getText();
    }

    public void clearForm() {
        idField.setText("");
        nameField.setText("");
        streetField.setText("");
        cityField.setText("");
        countryBox.setSelectedItem(CountryCode.NZ);
        postalField.setText("");
        idField.requestFocusInWindow();
    }

    public void addController(ActionListener controller) {
        addButton.addActionListener(controller);
    }
}
