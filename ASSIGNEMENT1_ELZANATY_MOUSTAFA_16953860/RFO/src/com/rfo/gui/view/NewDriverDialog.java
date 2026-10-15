package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import com.rfo.model.CountryCode;
import com.rfo.service.DriverService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The New driver pop-up used by Checkout, Car Transfer and Workshop when the
 * customer is not in the system yet. It is a form with the driver's details;
 * the controller checks them and creates the driver. The view only shows the
 * boxes and the errors it is given.
 */
public class NewDriverDialog extends JDialog {

    public static final String CREATE = "createDriver";
    public static final String CANCEL = "cancelDriver";

    private final JTextField first = new JTextField();
    private final JTextField last = new JTextField();
    private final JTextField licence = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField phone = new JTextField();
    private final JTextField street = new JTextField();
    private final JTextField city = new JTextField();
    private final JTextField postal = new JTextField();
    private final JComboBox<CountryCode> country = new JComboBox<>(CountryCode.values());
    private final Map<String, FormField> fields = new HashMap<>();
    private final JButton createButton = new JButton("Create driver");
    private final JButton cancelButton = new JButton("Cancel");

    public NewDriverDialog(Window owner) {
        super(owner, "New driver", ModalityType.APPLICATION_MODAL);
        country.setSelectedItem(CountryCode.NZ);
        licence.setFont(Theme.F_MONO);

        FormField firstField = field("First name", true, first, DriverService.FIRST, 280);
        FormField lastField = field("Last name", true, last, DriverService.LAST, 280);
        FormField licenceField = field("Licence number", true, licence, DriverService.LICENCE, 280);
        FormField emailField = field("Email", true, email, DriverService.EMAIL, 280);
        FormField phoneField = field("Phone", true, phone, DriverService.PHONE, 280);
        FormField streetField = field("Street", true, street, DriverService.STREET, 580);
        FormField cityField = field("City", true, city, DriverService.CITY, 280);
        FormField postalField = field("Postal code", true, postal, DriverService.POSTAL, 280);
        FormField countryField = new FormField("Country", true, country, null);
        countryField.setFixedWidth(280);

        JPanel body = new JPanel();
        body.setBackground(Theme.BG_WINDOW);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        JLabel intro = new JLabel("Add the driver's details. They get the next driver ID and are added to this rental.");
        intro.setFont(Theme.F_SMALL);
        intro.setForeground(Theme.TEXT_SECONDARY);
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(intro);
        body.add(Box.createVerticalStrut(12));
        body.add(row(firstField, lastField));
        body.add(row(licenceField, countryField));
        body.add(row(emailField, phoneField));
        body.add(row(streetField));
        body.add(row(cityField, postalField));

        createButton.putClientProperty("variant", "primary");
        createButton.setActionCommand(CREATE);
        cancelButton.setActionCommand(CANCEL);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.BG_WINDOW);
        buttons.setBorder(BorderFactory.createEmptyBorder(8, 24, 16, 24));
        buttons.add(cancelButton);
        buttons.add(createButton);

        getContentPane().setBackground(Theme.BG_WINDOW);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(body, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(createButton);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private FormField field(String label, boolean required, JTextField box, String key, int width) {
        FormField f = new FormField(label, required, box, null);
        f.setFixedWidth(width);
        fields.put(key, f);
        box.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                f.clearError();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                f.clearError();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                f.clearError();
            }
        });
        return f;
    }

    // fields side by side
    private JPanel row(JComponent... items) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                row.add(Box.createHorizontalStrut(20));
            }
            items[i].setAlignmentY(Component.TOP_ALIGNMENT);
            row.add(items[i]);
        }
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    public String getFirst() {
        return first.getText();
    }

    public String getLast() {
        return last.getText();
    }

    public String getLicence() {
        return licence.getText();
    }

    public String getEmail() {
        return email.getText();
    }

    public String getPhone() {
        return phone.getText();
    }

    public String getStreet() {
        return street.getText();
    }

    public String getCity() {
        return city.getText();
    }

    public String getPostal() {
        return postal.getText();
    }

    public CountryCode getCountry() {
        return (CountryCode) country.getSelectedItem();
    }

    /**
     * Shows each message under its own box (the map key is the field).
     */
    public void showErrors(Map<String, String> errors) {
        for (FormField f : fields.values()) {
            f.clearError();
        }
        for (Map.Entry<String, String> e : errors.entrySet()) {
            FormField f = fields.get(e.getKey());
            if (f != null) {
                f.setError(e.getValue());
            }
        }
        pack();
        // put the cursor in the first box that has a problem
        String[] order = {DriverService.FIRST, DriverService.LAST, DriverService.LICENCE, DriverService.EMAIL,
            DriverService.PHONE, DriverService.STREET, DriverService.CITY, DriverService.POSTAL};
        for (String key : order) {
            if (errors.containsKey(key)) {
                fields.get(key).getInput().requestFocusInWindow();
                break;
            }
        }
    }

    public void addController(ActionListener controller) {
        createButton.addActionListener(controller);
        cancelButton.addActionListener(controller);
    }
}
