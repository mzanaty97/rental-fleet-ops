package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Location;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The Car Transfer and Workshop screens. They are the same form: which vehicle,
 * who drives it, and when and where it is due back. The Kind says which words
 * to use. It only collects and shows what it is given; the controller checks
 * everything with the service.
 */
public class TransferView extends JPanel implements FullScreen {

    /**
     * The two uses of this form and the words each one needs.
     */
    public enum Kind {
        TRANSFER("Where it goes", "Expected arrival", "Arrival location", "Start transfer", "Transfer started"),
        WORKSHOP("Where it comes back to", "Expected return", "Return location", "Start workshop", "Workshop movement started");

        final String toSection, whenLabel, locationLabel, startText, doneText;

        Kind(String toSection, String whenLabel, String locationLabel, String startText, String doneText) {
            this.toSection = toSection;
            this.whenLabel = whenLabel;
            this.locationLabel = locationLabel;
            this.startText = startText;
            this.doneText = doneText;
        }
    }

    public static final String PLATE = "plate";
    public static final String LIST_AVAILABLE = "listAvailable";
    public static final String ADD_DRIVER = "addDriver";
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String START = "start";
    private static final int W = 700, PAD = 24;

    private final Kind kind;
    private final List<ActionListener> listeners = new ArrayList<>();

    private final JTextField plateBox = new JTextField();
    private final FormField plateField = new FormField("Plate number", true, plateBox, null);
    private final JLabel vehicleStrip = new JLabel();
    private final JTextField fromBox = new JTextField();
    private final JTextField driverBox = new JTextField();
    private final FormField driverField = new FormField("Driver ID", true, driverBox, null);
    private final DriverChips chips = new DriverChips(() -> {
    });
    private final JTextField dateBox = new JTextField();
    private final JTextField timeBox = new JTextField();
    private final FormField dateField;
    private final FormField timeField;
    private final JComboBox<Location> locationBox = new JComboBox<>();
    private final JButton startButton;
    private final JLabel banner = new JLabel();
    private final JPanel bannerRow = new JPanel(new BorderLayout(12, 0));
    private final JPanel form = new JPanel();
    private boolean programmatic = false;
    private boolean dateDirty = false;
    private boolean timeDirty = false;
    private String lastChecked = "";
    private String fromName = "";

    public TransferView(Kind kind) {
        this.kind = kind;
        this.dateField = new FormField(kind.whenLabel + " date", true, dateBox, null);
        this.timeField = new FormField("Time", true, timeBox, null);
        this.startButton = new JButton(kind.startText);
        setOpaque(false);
        setLayout(new BorderLayout());

        FormCard card = new FormCard(W, PAD);

        // success banner, hidden until a movement has been started
        bannerRow.setBackground(Theme.OK_BG);
        bannerRow.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        banner.setFont(Theme.F_BODY_B);
        banner.setForeground(Theme.OK_DARK);
        banner.setIcon(Icons.icon(Icons.Type.CHECK_CIRCLE, 18, Theme.OK_FG));
        banner.setIconTextGap(10);
        JButton another = new JButton("Start another");
        another.putClientProperty("size", "compact");
        another.addActionListener(e -> reset());
        bannerRow.add(banner, BorderLayout.CENTER);
        bannerRow.add(another, BorderLayout.EAST);
        bannerRow.setVisible(false);
        bannerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bannerRow.setMaximumSize(new Dimension(W - 2 * PAD, 60));
        card.add(bannerRow);

        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sectionTitle("Where it is now"));
        form.add(Box.createVerticalStrut(10));
        form.add(fromRow());
        form.add(Box.createVerticalStrut(14));
        form.add(driversBlock());
        form.add(Box.createVerticalStrut(18));
        form.add(sectionTitle(kind.toSection));
        form.add(Box.createVerticalStrut(10));
        form.add(toRow());
        form.add(Box.createVerticalStrut(6));
        JLabel help = new JLabel("Type the date and time together, for example 18102026 1000 or 18.10.26 10:00, then press Enter.");
        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(help);
        card.add(form);

        card.add(Box.createVerticalStrut(16));
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_DIVIDER);
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(16));

        startButton.putClientProperty("variant", "primary");
        startButton.addActionListener(e -> fire(START));
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(Box.createHorizontalGlue());
        bottom.add(startButton);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(bottom);

        // the card sits at the top left and the page scrolls if the window is too short for it
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(card, BorderLayout.NORTH);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(holder, BorderLayout.WEST);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(left,
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);

        wire();
        reset();
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing() && !bannerRow.isVisible()) {
                SwingUtilities.invokeLater(() -> plateBox.requestFocusInWindow());
            }
        });
    }

    /**
     * Puts components side by side with a set gap, the first one flush with the
     * left edge.
     */
    private static JPanel hrow(int gap, Component... parts) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                row.add(Box.createHorizontalStrut(gap));
            }
            row.add(parts[i]);
        }
        row.add(Box.createHorizontalGlue());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private static JLabel sectionTitle(String text) {
        JLabel t = new JLabel(text.toUpperCase());
        t.setFont(Theme.F_LABEL);
        t.setForeground(Theme.TEXT_SECONDARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        return t;
    }

    private JComponent fromRow() {
        plateBox.setFont(Theme.F_MONO);
        vehicleStrip.setFont(Theme.F_SMALL);
        vehicleStrip.setForeground(Theme.OK_DARK);
        vehicleStrip.setIcon(Icons.icon(Icons.Type.CHECK_CIRCLE, 16, Theme.OK_FG));
        vehicleStrip.setIconTextGap(10);
        vehicleStrip.setOpaque(true);
        vehicleStrip.setBackground(Theme.OK_BG);
        vehicleStrip.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        vehicleStrip.setVisible(false);
        vehicleStrip.setPreferredSize(new Dimension(260, 40));
        plateField.setFixedWidth(170);

        fromBox.setEditable(false);
        fromBox.setFocusable(false);
        fromBox.setBackground(Theme.BG_SUBTLE);
        fromBox.setForeground(Theme.TEXT_SECONDARY);
        FormField fromField = new FormField("Checkout location", false, fromBox, null);
        fromField.setFixedWidth(220);

        JPanel stripBox = new JPanel(new BorderLayout());
        stripBox.setOpaque(false);
        stripBox.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        stripBox.add(vehicleStrip, BorderLayout.NORTH);

        return hrow(12, plateField, fromField, stripBox);
    }

    private JComponent driversBlock() {
        driverBox.setFont(Theme.F_MONO);
        driverField.setFixedWidth(170);
        JButton add = new JButton("Add driver");
        add.setIcon(Icons.icon(Icons.Type.PLUS, 14, Theme.TEXT_PRIMARY));
        add.addActionListener(e -> fire(ADD_DRIVER));
        JPanel addBox = new JPanel(new BorderLayout());
        addBox.setOpaque(false);
        addBox.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        addBox.add(add, BorderLayout.NORTH);
        addBox.setMaximumSize(new Dimension(add.getPreferredSize().width, Short.MAX_VALUE));
        JPanel row = hrow(12, driverField, addBox);
        JLabel help = new JLabel("At least one driver. Press Enter to add, then Enter again to continue.");
        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(chips);
        body.add(Box.createVerticalStrut(6));
        body.add(row);
        body.add(Box.createVerticalStrut(2));
        body.add(help);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        return body;
    }

    private JComponent toRow() {
        dateBox.setFont(Theme.F_MONO);
        timeBox.setFont(Theme.F_MONO);
        dateField.setFixedWidth(170);
        timeField.setFixedWidth(110);
        locationBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                Component c = super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof Location) {
                    Location l = (Location) value;
                    setText(l.getName() + " (" + l.getLocationID() + ")");
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
        FormField locationField = new FormField(kind.locationLabel, true, locationBox, "Active locations only");
        locationField.setFixedWidth(280);

        List<Component> quickButtons = new ArrayList<>();
        for (int days = 0; days <= 1; days++) {
            final int d = days;
            JButton b = new JButton(days == 0 ? "Today" : "Tomorrow");
            b.putClientProperty("size", "compact");
            b.addActionListener(e -> {
                setDate(LocalDate.now().plusDays(d));
                (timeBox.getText().trim().isEmpty() ? timeBox : locationBox).requestFocusInWindow();
            });
            quickButtons.add(b);
        }
        JPanel quick = hrow(6, quickButtons.toArray(new Component[0]));
        JPanel when = hrow(8, dateField, timeField);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        when.setAlignmentX(Component.LEFT_ALIGNMENT);
        quick.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(when);
        left.add(Box.createVerticalStrut(4));
        left.add(quick);

        JPanel loc = new JPanel(new BorderLayout());
        loc.setOpaque(false);
        loc.add(locationField, BorderLayout.NORTH);
        return hrow(16, left, loc);
    }

    // ------------------------------------------------------------------ keys and typing
    private void wire() {
        plateBox.addActionListener(e -> {
            if (plateBox.getText().trim().isEmpty()) {
                fire(LIST_AVAILABLE);
                return;
            }
            checkPlate();
            if (vehicleStrip.isVisible()) {
                driverBox.requestFocusInWindow();
            }
        });
        plateBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkPlate();
            }
        });
        plateBox.getDocument().addDocumentListener(simple(() -> {
            plateField.clearError();
            vehicleStrip.setVisible(false);
            lastChecked = "";
        }));
        driverBox.addActionListener(e -> {
            if (!driverBox.getText().trim().isEmpty()) {
                fire(ADD_DRIVER);
            } else if (chips.isEmpty()) {
                setDriverError("At least one driver is required.");
            } else {
                dateBox.requestFocusInWindow();
            }
        });
        driverBox.getDocument().addDocumentListener(simple(driverField::clearError));
        dateBox.getDocument().addDocumentListener(simple(() -> {
            dateField.clearError();
            if (!programmatic) {
                dateDirty = true;
            }
        }));
        timeBox.getDocument().addDocumentListener(simple(() -> {
            timeField.clearError();
            if (!programmatic) {
                timeDirty = true;
            }
        }));
        dateBox.addActionListener(e -> commitDate(true));
        timeBox.addActionListener(e -> commitTime(true));
        dateBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitDate(false);
            }
        });
        timeBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitTime(false);
            }
        });
        enter(locationBox, () -> {
            if (locationBox.isPopupVisible()) {
                javax.swing.Action original = locationBox.getActionMap().get("enterPressed");
                if (original != null) {
                    original.actionPerformed(new ActionEvent(locationBox, ActionEvent.ACTION_PERFORMED, "enter"));
                }
                locationBox.hidePopup();
            }
            startButton.requestFocusInWindow();
        });
        enter(startButton, () -> startButton.doClick());
    }

    private void checkPlate() {
        String text = plateBox.getText().trim();
        if (!text.isEmpty() && !text.equalsIgnoreCase(lastChecked)) {
            lastChecked = text;
            fire(PLATE);
        }
    }

    private void commitDate(boolean advance) {
        String text = dateBox.getText().trim();
        if (dateDirty && !text.isEmpty() || (advance && text.isEmpty())) {
            dateDirty = false;
            fire(DATE);
        }
        if (advance && dateBox.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}") && !dateField.hasError()) {
            (timeBox.getText().trim().isEmpty() ? timeBox : locationBox).requestFocusInWindow();
        }
    }

    private void commitTime(boolean advance) {
        String text = timeBox.getText().trim();
        if (timeDirty && !text.isEmpty() || (advance && text.isEmpty())) {
            timeDirty = false;
            fire(TIME);
        }
        if (advance && timeBox.getText().trim().matches("\\d{2}:\\d{2}") && !timeField.hasError()) {
            locationBox.requestFocusInWindow();
        }
    }

    private static void enter(JComponent c, Runnable action) {
        c.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rfoEnter");
        c.getActionMap().put("rfoEnter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    // ------------------------------------------------------------------ what the controller uses
    public String getPlate() {
        return plateBox.getText().trim().toUpperCase();
    }

    public String getDriverText() {
        return driverBox.getText();
    }

    public List<Integer> getDriverIds() {
        return chips.getIds();
    }

    public String getDateText() {
        return dateBox.getText().trim();
    }

    public String getTimeText() {
        return timeBox.getText().trim();
    }

    public Location getChosenLocation() {
        return (Location) locationBox.getSelectedItem();
    }

    public void setFromName(String name) {
        fromName = name == null ? "" : name;
        fromBox.setText(fromName);
    }

    public String getFromName() {
        return fromName;
    }

    /**
     * The active locations to go to. A choice the user already made is kept.
     */
    public void setLocations(List<Location> locations) {
        Location keep = (Location) locationBox.getSelectedItem();
        DefaultComboBoxModel<Location> model = new DefaultComboBoxModel<>();
        for (Location l : locations) {
            model.addElement(l);
        }
        locationBox.setModel(model);
        if (keep != null && locations.contains(keep)) {
            locationBox.setSelectedItem(keep);
        }
    }

    public void setVehicleOk(String text) {
        plateField.clearError();
        vehicleStrip.setText("<html>" + text.replace("<", "&lt;") + "</html>");
        vehicleStrip.setVisible(true);
    }

    public void setVehicleError(String message) {
        vehicleStrip.setVisible(false);
        plateField.setError(message);
    }

    public void setDriverError(String message) {
        driverField.setError(message);
    }

    public void addDriver(int id) {
        chips.add(id);
        driverBox.setText("");
        driverField.clearError();
        driverBox.requestFocusInWindow();
    }

    public void setDateError(String message) {
        dateField.setError(message);
    }

    public void setTimeError(String message) {
        timeField.setError(message);
    }

    public void setDate(LocalDate date) {
        programmatic = true;
        dateBox.setText(date == null ? "" : date.toString());
        programmatic = false;
        dateDirty = false;
        dateField.clearError();
    }

    public void setTime(LocalTime time) {
        programmatic = true;
        timeBox.setText(time == null ? "" : String.format("%02d:%02d", time.getHour(), time.getMinute()));
        programmatic = false;
        timeDirty = false;
        timeField.clearError();
    }

    public void showVehicleChoices(String heading, List<String[]> rows) {
        VehiclePicker.show(plateBox, heading, rows, plate -> {
            plateBox.setText(plate);
            checkPlate();
            if (vehicleStrip.isVisible()) {
                driverBox.requestFocusInWindow();
            }
        });
    }

    /**
     * Shows the green banner with the new movement ID, and dims the form until
     * "Start another".
     */
    public void showDone(String movementId) {
        banner.setText(kind.doneText + ". Movement ID: " + movementId);
        bannerRow.setVisible(true);
        setFormEnabled(form, false);
        startButton.setEnabled(false);
    }

    /**
     * An empty form again, ready for the next one.
     */
    public void reset() {
        bannerRow.setVisible(false);
        setFormEnabled(form, true);
        startButton.setEnabled(true);
        plateBox.setText("");
        driverBox.setText("");
        chips.clear();
        setDate(null);
        setTime(null);
        plateField.clearError();
        driverField.clearError();
        vehicleStrip.setVisible(false);
        lastChecked = "";
        SwingUtilities.invokeLater(() -> plateBox.requestFocusInWindow());
    }

    private static void setFormEnabled(Component c, boolean enabled) {
        if (!(c instanceof JTextField && !((JTextField) c).isEditable())) {
            c.setEnabled(enabled);
        }
        if (c instanceof java.awt.Container) {
            for (Component k : ((java.awt.Container) c).getComponents()) {
                setFormEnabled(k, enabled);
            }
        }
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

    private static DocumentListener simple(Runnable r) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                r.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                r.run();
            }
        };
    }
}
