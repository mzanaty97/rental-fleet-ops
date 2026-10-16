package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import com.rfo.model.FuelType;
import com.rfo.model.VehicleCategory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The New Vehicle form: plate, year, make, model, category, fuel and GPS. The
 * Add button stays off until plate, year, make and model are filled in. It only
 * collects and shows what it is given; the controller checks everything with
 * the VehicleService.
 */
public class NewVehicleView extends JPanel implements PlainScreen {

    public static final String PLATE = "plate";
    public static final String ADD = "add";
    private static final int W = 640, PAD = 24, COL_GAP = 20;

    private final List<ActionListener> listeners = new ArrayList<>();
    private final JTextField plateBox = new JTextField();
    private final JTextField yearBox = new JTextField();
    private final JTextField makeBox = new JTextField();
    private final JTextField modelBox = new JTextField();
    private final FormField plateField = new FormField("Plate number", true, plateBox, "2 to 8 letters or digits");
    private final FormField yearField = new FormField("Year", true, yearBox, null);
    private final FormField makeField = new FormField("Make", true, makeBox, null);
    private final FormField modelField = new FormField("Model", true, modelBox, null);
    private final JComboBox<VehicleCategory> categoryBox = new JComboBox<>(VehicleCategory.values());
    private final JComboBox<FuelType> fuelBox = new JComboBox<>(FuelType.values());
    private final SwitchToggle gpsToggle = new SwitchToggle();
    private final JLabel gpsLabel = new JLabel("No");
    private final JLabel footer = new JLabel();
    private final JButton addButton = new JButton("Add vehicle");
    private String lastChecked = "";

    public NewVehicleView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        plateBox.setFont(Theme.F_MONO);
        yearBox.setFont(Theme.F_MONO);
        categoryBox.setRenderer(new NiceNames());
        fuelBox.setRenderer(new NiceNames());

        FormCard card = new FormCard(W, PAD);
        card.add(row(plateField, yearField));
        card.add(Box.createVerticalStrut(16));
        card.add(row(makeField, modelField));
        card.add(Box.createVerticalStrut(16));
        card.add(row(new FormField("Category", true, categoryBox, "Sets the daily rate"),
                new FormField("Fuel type", true, fuelBox, null)));
        card.add(Box.createVerticalStrut(16));

        JPanel gps = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        gps.setOpaque(false);
        gpsLabel.setFont(Theme.F_BODY_B);
        gpsLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel gpsHelp = new JLabel("Has GPS");
        gpsHelp.setFont(Theme.F_SMALL);
        gpsHelp.setForeground(Theme.TEXT_SECONDARY);
        gps.add(gpsToggle);
        gps.add(gpsLabel);
        gps.add(gpsHelp);
        gpsToggle.addActionListener(e -> gpsLabel.setText(gpsToggle.isSelected() ? "Yes" : "No"));
        gps.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(gps);

        card.add(Box.createVerticalStrut(16));
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_DIVIDER);
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(16));

        footer.setFont(Theme.F_SMALL);
        footer.setForeground(Theme.TEXT_SECONDARY);
        addButton.putClientProperty("variant", "primary");
        addButton.setIcon(Icons.icon(Icons.Type.PLUS, 16, Theme.TEXT_ON_PRIMARY));
        addButton.setEnabled(false);
        addButton.addActionListener(e -> fire(ADD));
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(footer);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(addButton);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(bottom);

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(card);

        plateBox.addActionListener(e -> {
            checkPlate();
            yearBox.requestFocusInWindow();
        });
        plateBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkPlate();
            }
        });
        yearBox.addActionListener(e -> makeBox.requestFocusInWindow());
        makeBox.addActionListener(e -> modelBox.requestFocusInWindow());
        modelBox.addActionListener(e -> {
            if (addButton.isEnabled()) {
                addButton.doClick();
            }
        });
        hook(plateBox, plateField);
        hook(yearBox, yearField);
        hook(makeBox, makeField);
        hook(modelBox, modelField);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                javax.swing.SwingUtilities.invokeLater(() -> plateBox.requestFocusInWindow());
            }
        });
    }

    private void checkPlate() {
        String text = plateBox.getText().trim();
        if (!text.isEmpty() && !text.equalsIgnoreCase(lastChecked)) {
            lastChecked = text;
            fire(PLATE);
        }
    }

    /**
     * Keeps a field at the top of its grid cell, so labels line up even when
     * one field has a help line.
     */
    private static JPanel top(Component field) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setOpaque(false);
        p.add(field, java.awt.BorderLayout.NORTH);
        return p;
    }

    private static JPanel row(Component left, Component right) {
        JPanel row = new JPanel(new GridLayout(1, 2, COL_GAP, 0));
        row.setOpaque(false);
        row.add(top(left));
        row.add(top(right));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(W - 2 * PAD, Integer.MAX_VALUE));
        return row;
    }

    private void hook(javax.swing.text.JTextComponent box, FormField field) {
        box.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            private void changed() {
                field.clearError();
                if (box == plateBox) {
                    lastChecked = "";
                }
                addButton.setEnabled(!plateBox.getText().trim().isEmpty() && !yearBox.getText().trim().isEmpty()
                        && !makeBox.getText().trim().isEmpty() && !modelBox.getText().trim().isEmpty());
            }
        });
    }

    /**
     * Shows enum names as plain words, for example PLUG_IN_HYBRID as "Plug in
     * hybrid".
     */
    private static class NiceNames extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            if (value instanceof Enum) {
                String t = value.toString().toLowerCase().replace('_', ' ');
                setText(Character.toUpperCase(t.charAt(0)) + t.substring(1));
            }
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    // ------------------------------------------------------------------ what the controller uses
    public String getPlate() {
        return plateBox.getText().trim().toUpperCase();
    }

    public String getYearText() {
        return yearBox.getText().trim();
    }

    public String getMake() {
        return makeBox.getText().trim();
    }

    public String getModel() {
        return modelBox.getText().trim();
    }

    public VehicleCategory getCategory() {
        return (VehicleCategory) categoryBox.getSelectedItem();
    }

    public FuelType getFuelType() {
        return (FuelType) fuelBox.getSelectedItem();
    }

    public boolean hasGps() {
        return gpsToggle.isSelected();
    }

    public void setPlateError(String message) {
        plateField.setError(message);
    }

    public void setYearError(String message) {
        yearField.setError(message);
    }

    public void setMakeError(String message) {
        makeField.setError(message);
    }

    public void setModelError(String message) {
        modelField.setError(message);
    }

    /**
     * The line beside the button, for example "Added at Auckland Intake Depot
     * as AVAILABLE".
     */
    public void setFooter(String text) {
        footer.setText(text);
    }

    public void clearForm() {
        plateBox.setText("");
        yearBox.setText("");
        makeBox.setText("");
        modelBox.setText("");
        categoryBox.setSelectedIndex(0);
        fuelBox.setSelectedIndex(0);
        gpsToggle.setSelected(false);
        gpsLabel.setText("No");
        plateField.clearError();
        yearField.clearError();
        makeField.clearError();
        modelField.clearError();
        lastChecked = "";
        plateBox.requestFocusInWindow();
    }

    public void addController(ActionListener controller) {
        listeners.add(controller);
    }

    private void fire(String command) {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command);
        for (ActionListener l : new ArrayList<>(listeners)) {
            l.actionPerformed(event);
        }
    }
}
