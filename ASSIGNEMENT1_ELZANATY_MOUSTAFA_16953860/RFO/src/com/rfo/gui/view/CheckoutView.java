package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Insurance;
import com.rfo.model.Location;
import com.rfo.model.RentalMovement;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The Checkout screen. One screen with three steps: the form (vehicle, drivers,
 * return, cover) with a live summary beside it, then a confirm card, then a
 * "rental started" card. It only collects and shows what it is given; the
 * controller checks everything with the service.
 */
public class CheckoutView extends JPanel implements FullScreen {

    public static final String PLATE = "plate";
    public static final String ADD_DRIVER = "addDriver";
    public static final String REVIEW = "review";
    public static final String CONFIRM = "confirm";
    public static final String ABANDON = "abandon";
    public static final String QUOTE = "quote";
    public static final String LIST_AVAILABLE = "listAvailable";
    public static final String RETURN_DATE = "returnDate";
    public static final String RETURN_TIME = "returnTime";

    private static final String FORM = "form", CONFIRM_CARD = "confirmCard", DONE = "done";

    private final CardLayout cards = new CardLayout();
    private final List<ActionListener> listeners = new ArrayList<>();

    // step 1
    private final JTextField plateBox = new JTextField();
    private final FormField plateField = new FormField("Plate number", true, plateBox, null);
    private final JLabel vehicleStrip = new JLabel();
    private final StepBadge vehicleBadge = new StepBadge("1");
    private final StepBadge driversBadge = new StepBadge("2");
    private final StepBadge pickupBadge = new StepBadge("3");
    private final StepBadge returnBadge = new StepBadge("4");
    private final StepBadge coverBadge = new StepBadge("5");
    private final JTextField pickupDateBox = new JTextField();
    private final JTextField pickupTimeBox = new JTextField();
    private final JTextField pickupLocBox = new JTextField();
    private boolean programmatic = false;
    private boolean onForm = true;
    private boolean dateDirty = false;
    private boolean timeDirty = false;
    private final JTextField driverBox = new JTextField();
    private final FormField driverField = new FormField("Driver ID", false, driverBox, null);
    private final JPanel driverChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    private final List<Integer> drivers = new ArrayList<>();
    private final JTextField dateBox = new JTextField();
    private final JTextField timeBox = new JTextField();
    private final FormField dateField = new FormField("Return date", true, dateBox, null);
    private final FormField timeField = new FormField("Return time", true, timeBox, null);
    private final JComboBox<Location> locationBox = new JComboBox<>();
    private final SwitchToggle rsaToggle = new SwitchToggle();
    private final JLabel rsaLabel = new JLabel("No");
    private final List<ChoiceCard> insuranceCards = new ArrayList<>();
    private final JLabel[] summaryValues = new JLabel[7];
    private final JLabel totalNote = new JLabel(" ");
    private final JLabel confirmTotal = new JLabel("-");
    private final JLabel confirmBreakdown = new JLabel(" ");
    private java.util.Map<Insurance, RentalMovement.Quote> quotes;
    private final JLabel problems = new JLabel(" ");
    private final JButton reviewButton = new JButton("Review checkout");
    private final JButton cancelButton = new JButton("Cancel checkout");

    // step 2
    private final JLabel confirmTitle = new JLabel();
    private final JLabel[] confirmValues = new JLabel[6];
    private final JPanel abandonStrip = new JPanel(new BorderLayout(12, 0));
    private final JButton confirmButton = new JButton("Confirm checkout");
    private final JButton confirmCancel = new JButton("Cancel");

    // step 3
    private final JLabel doneText = new JLabel();
    private final JLabel doneId = new JLabel();
    private final JLabel doneCost = new JLabel();

    private String vehicleName = "";
    private String fromName = "";
    private String lastChecked = "";

    public CheckoutView() {
        setOpaque(false);
        setLayout(cards);
        add(buildForm(), FORM);
        add(buildConfirm(), CONFIRM_CARD);
        add(buildDone(), DONE);
        wireEnterKeys();
        reset();
        // whenever the screen appears the plate box is ready for typing
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing() && onForm) {
                javax.swing.SwingUtilities.invokeLater(() -> plateBox.requestFocusInWindow());
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1020, 520);
    }

    // ------------------------------------------------------------------ step 1
    private JComponent buildForm() {
        JPanel page = new JPanel(new BorderLayout(20, 0));
        page.setOpaque(false);

        JPanel sections = new JPanel();
        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.add(section(vehicleBadge, "Vehicle", vehicleBody()));
        sections.add(section(driversBadge, "Drivers", driversBody()));
        sections.add(section(pickupBadge, "Pickup", pickupBody()));
        sections.add(section(returnBadge, "Return", returnBody()));
        sections.add(section(coverBadge, "Cover and extras", coverBody()));

        JScrollPane scroll = new JScrollPane(sections, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        JPanel leftCard = new JPanel(new BorderLayout());
        leftCard.setBackground(Theme.BG_SURFACE);
        leftCard.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(1, 1, 1, 1), false));
        leftCard.add(scroll, BorderLayout.CENTER);
        page.add(leftCard, BorderLayout.CENTER);
        page.add(summaryColumn(), BorderLayout.EAST);
        return page;
    }

    private JComponent section(StepBadge badge, String title, JComponent body) {
        JPanel box = new JPanel(new BorderLayout(0, 12)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        box.setOpaque(false);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        head.setOpaque(false);
        head.add(badge);
        JLabel t = new JLabel(title);
        t.setFont(Theme.F_SECTION);
        t.setForeground(Theme.TEXT_PRIMARY);
        head.add(t);
        box.add(head, BorderLayout.NORTH);
        box.add(body, BorderLayout.CENTER);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    private JComponent vehicleBody() {
        plateBox.setFont(Theme.F_MONO);
        vehicleStrip.setFont(Theme.F_SMALL);
        vehicleStrip.setForeground(Theme.OK_DARK);
        vehicleStrip.setIcon(Icons.icon(Icons.Type.CHECK_CIRCLE, 16, Theme.OK_FG));
        vehicleStrip.setIconTextGap(10);
        vehicleStrip.setOpaque(true);
        vehicleStrip.setBackground(Theme.OK_BG);
        vehicleStrip.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        vehicleStrip.setVisible(false);
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        plateField.setFixedWidth(190);
        vehicleStrip.setPreferredSize(new Dimension(300, 40));
        row.add(plateField, BorderLayout.WEST);
        JPanel stripBox = new JPanel(new BorderLayout());
        stripBox.setOpaque(false);
        stripBox.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        stripBox.add(vehicleStrip, BorderLayout.NORTH);
        row.add(stripBox, BorderLayout.CENTER);
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
            vehicleBadge.setState(StepBadge.State.NUMBER);
            lastChecked = "";
            refreshSummary();
        }));
        return row;
    }

    private void checkPlate() {
        String text = plateBox.getText().trim();
        if (!text.isEmpty() && !text.equalsIgnoreCase(lastChecked)) {
            lastChecked = text;
            fire(PLATE);
        }
    }

    private JComponent driversBody() {
        driverBox.setFont(Theme.F_MONO);
        driverChips.setOpaque(false);
        JPanel add = new JPanel(new BorderLayout(10, 0));
        add.setOpaque(false);
        driverField.setFixedWidth(190);
        JButton addButton = new JButton("Add driver");
        addButton.setIcon(Icons.icon(Icons.Type.PLUS, 14, Theme.TEXT_PRIMARY));
        addButton.setActionCommand(ADD_DRIVER);
        addButton.addActionListener(e -> fire(ADD_DRIVER));
        driverBox.addActionListener(e -> {
            if (!driverBox.getText().trim().isEmpty()) {
                fire(ADD_DRIVER);
            } else if (drivers.isEmpty()) {
                setDriverError("At least one driver is required.");
            } else {
                dateBox.requestFocusInWindow();
            }
        });
        JPanel buttonBox = new JPanel(new BorderLayout());
        buttonBox.setOpaque(false);
        buttonBox.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        buttonBox.add(addButton, BorderLayout.NORTH);
        add.add(driverField, BorderLayout.WEST);
        add.add(buttonBox, BorderLayout.CENTER);
        JLabel help = new JLabel("At least one driver. Press Enter to add, then Enter again to continue.");
        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        driverChips.setAlignmentX(Component.LEFT_ALIGNMENT);
        add.setAlignmentX(Component.LEFT_ALIGNMENT);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(driverChips);
        body.add(Box.createVerticalStrut(8));
        body.add(add);
        body.add(Box.createVerticalStrut(4));
        body.add(help);
        return body;
    }

    private JComponent pickupBody() {
        for (JTextField f : new JTextField[]{pickupDateBox, pickupTimeBox, pickupLocBox}) {
            f.setEditable(false);
            f.setFocusable(false);
            f.setFont(Theme.F_MONO);
            f.setBackground(Theme.BG_SUBTLE);
            f.setForeground(Theme.TEXT_SECONDARY);
        }
        pickupLocBox.setFont(Theme.F_BODY);
        FormField d = new FormField("Pickup date", false, pickupDateBox, null);
        FormField t = new FormField("Pickup time", false, pickupTimeBox, null);
        FormField l = new FormField("Pickup location", false, pickupLocBox, null);
        d.setFixedWidth(170);
        t.setFixedWidth(110);
        l.setFixedWidth(280);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(d);
        row.add(t);
        row.add(l);
        JLabel note = new JLabel("Set automatically and cannot be changed. The rental starts when you confirm.",
                Icons.icon(Icons.Type.LOCK, 14, Theme.TEXT_SECONDARY), JLabel.LEFT);
        note.setIconTextGap(6);
        note.setFont(Theme.F_SMALL);
        note.setForeground(Theme.TEXT_SECONDARY);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(row);
        body.add(Box.createVerticalStrut(2));
        body.add(note);
        return body;
    }

    /**
     * Fills in the read-only pickup boxes with the time right now.
     */
    private void refreshPickup() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        pickupDateBox.setText(now.toLocalDate().toString());
        pickupTimeBox.setText(String.format("%02d:%02d", now.getHour(), now.getMinute()));
        pickupLocBox.setText(fromName);
    }

    private JComponent returnBody() {
        dateBox.setFont(Theme.F_MONO);
        timeBox.setFont(Theme.F_MONO);
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
        FormField locationField = new FormField("Return location", true, locationBox, "Active locations only");

        JPanel when = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        when.setOpaque(false);
        dateField.setFixedWidth(170);
        timeField.setFixedWidth(110);
        when.add(dateField);
        when.add(timeField);
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quick.setOpaque(false);
        for (int days : new int[]{1, 3, 7}) {
            JButton b = new JButton(days == 1 ? "Tomorrow" : "+" + days + " days");
            b.putClientProperty("size", "compact");
            b.addActionListener(e -> {
                setReturnDate(LocalDate.now().plusDays(days));
                (timeBox.getText().trim().isEmpty() ? timeBox : locationBox).requestFocusInWindow();
            });
            quick.add(b);
        }
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        when.setAlignmentX(Component.LEFT_ALIGNMENT);
        quick.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(when);
        left.add(Box.createVerticalStrut(4));
        left.add(quick);

        locationField.setFixedWidth(280);
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);
        row.add(left, BorderLayout.WEST);
        JPanel loc = new JPanel(new BorderLayout());
        loc.setOpaque(false);
        loc.add(locationField, BorderLayout.NORTH);
        row.add(loc, BorderLayout.CENTER);

        JLabel help = new JLabel("Type the date and time together, for example 18102026 1000 or 18.10.26 10:00, then press Enter.");
        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(row);
        body.add(Box.createVerticalStrut(6));
        body.add(help);

        dateBox.getDocument().addDocumentListener(simple(() -> {
            dateField.clearError();
            if (!programmatic) {
                dateDirty = true;
            }
            refreshSummary();
        }));
        timeBox.getDocument().addDocumentListener(simple(() -> {
            timeField.clearError();
            if (!programmatic) {
                timeDirty = true;
            }
            refreshSummary();
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
        locationBox.addActionListener(e -> refreshSummary());
        return body;
    }

    /**
     * Hands a typed date to the controller to turn into yyyy-MM-dd. Enter moves
     * on when the date is good.
     */
    private void commitDate(boolean advance) {
        String text = dateBox.getText().trim();
        if (dateDirty && !text.isEmpty() || (advance && text.isEmpty())) {
            dateDirty = false;
            fire(RETURN_DATE);
        }
        if (advance && dateBox.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}") && !dateField.hasError()) {
            (timeBox.getText().trim().isEmpty() ? timeBox : locationBox).requestFocusInWindow();
        }
    }

    private void commitTime(boolean advance) {
        String text = timeBox.getText().trim();
        if (timeDirty && !text.isEmpty() || (advance && text.isEmpty())) {
            timeDirty = false;
            fire(RETURN_TIME);
        }
        if (advance && timeBox.getText().trim().matches("\\d{2}:\\d{2}") && !timeField.hasError()) {
            locationBox.requestFocusInWindow();
        }
    }

    /**
     * Makes Enter move to the next part of the form.
     */
    private void wireEnterKeys() {
        enter(locationBox, () -> {
            if (locationBox.isPopupVisible()) {
                javax.swing.Action original = locationBox.getActionMap().get("enterPressed");
                if (original != null) {
                    original.actionPerformed(new ActionEvent(locationBox, ActionEvent.ACTION_PERFORMED, "enter"));
                }
                locationBox.hidePopup();
            }
            rsaToggle.requestFocusInWindow();
        });
        enter(rsaToggle, () -> selectedInsuranceCard().requestFocusInWindow());
        for (int i = 0; i < insuranceCards.size(); i++) {
            final int index = i;
            ChoiceCard c = insuranceCards.get(i);
            enter(c, () -> reviewButton.doClick());
            key(c, java.awt.event.KeyEvent.VK_LEFT, () -> chooseCard(index - 1));
            key(c, java.awt.event.KeyEvent.VK_RIGHT, () -> chooseCard(index + 1));
        }
        enter(confirmButton, () -> confirmButton.doClick());

        // keep whatever has the keyboard in view as Enter moves down the long form
        java.util.List<JComponent> stops = new ArrayList<>(insuranceCards);
        stops.add(plateBox);
        stops.add(driverBox);
        stops.add(dateBox);
        stops.add(timeBox);
        stops.add(locationBox);
        stops.add(rsaToggle);
        for (JComponent c : stops) {
            c.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    c.scrollRectToVisible(new java.awt.Rectangle(0, -48, c.getWidth(), c.getHeight() + 96));
                }
            });
        }
    }

    private ChoiceCard selectedInsuranceCard() {
        for (ChoiceCard c : insuranceCards) {
            if (c.isSelected()) {
                return c;
            }
        }
        return insuranceCards.get(0);
    }

    private void chooseCard(int index) {
        if (index >= 0 && index < insuranceCards.size()) {
            ChoiceCard c = insuranceCards.get(index);
            c.setSelected(true);
            c.requestFocusInWindow();
            refreshSummary();
        }
    }

    private static void enter(JComponent c, Runnable action) {
        key(c, java.awt.event.KeyEvent.VK_ENTER, action);
    }

    private static void key(JComponent c, int keyCode, Runnable action) {
        String name = "rfoKey" + keyCode;
        c.getInputMap(JComponent.WHEN_FOCUSED).put(javax.swing.KeyStroke.getKeyStroke(keyCode, 0), name);
        c.getActionMap().put(name, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private JComponent coverBody() {
        JPanel rsa = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rsa.setOpaque(false);
        rsaLabel.setFont(Theme.F_BODY_B);
        rsaLabel.setForeground(Theme.TEXT_PRIMARY);
        JLabel help = new JLabel("Roadside assistance (RSA)");
        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        rsa.add(rsaToggle);
        rsa.add(rsaLabel);
        rsa.add(help);
        rsaToggle.addActionListener(e -> {
            rsaLabel.setText(rsaToggle.isSelected() ? "Yes" : "No");
            refreshSummary();
        });

        JPanel tiers = new JPanel(new GridLayout(1, 3, 8, 0));
        tiers.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (Insurance tier : Insurance.values()) {
            String sub = "";
            ChoiceCard card = new ChoiceCard(tier.toString(), sub);
            card.setActionCommand(tier.toString());
            group.add(card);
            tiers.add(card);
            insuranceCards.add(card);
            card.addActionListener(e -> refreshSummary());
        }
        insuranceCards.get(0).setSelected(true);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        rsa.setAlignmentX(Component.LEFT_ALIGNMENT);
        tiers.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(rsa);
        body.add(Box.createVerticalStrut(12));
        body.add(tiers);
        return body;
    }

    private JComponent summaryColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        FormCard card = new FormCard(300, 18);
        JLabel title = new JLabel("Rental summary");
        title.setFont(Theme.F_SECTION);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        String[] names = {"From", "Vehicle", "Drivers", "Return", "RSA", "Insurance", "Estimated total"};
        for (int i = 0; i < names.length; i++) {
            summaryValues[i] = new JLabel("-");
            card.add(kvRow(names[i], summaryValues[i], i == names.length - 1));
        }
        summaryValues[6].setFont(Theme.F_HEADING);
        totalNote.setFont(Theme.F_SMALL);
        totalNote.setForeground(Theme.TEXT_SECONDARY);
        totalNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(totalNote);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(card);
        col.add(Box.createVerticalStrut(12));

        reviewButton.putClientProperty("variant", "primary");
        reviewButton.setPreferredSize(new Dimension(300, 44));
        reviewButton.setMaximumSize(new Dimension(300, 44));
        reviewButton.setActionCommand(REVIEW);
        reviewButton.addActionListener(e -> fire(REVIEW));
        reviewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(reviewButton);
        problems.setFont(Theme.F_SMALL);
        problems.setForeground(Theme.ERR_FG);
        problems.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(Box.createVerticalStrut(6));
        col.add(problems);
        cancelButton.putClientProperty("variant", "quiet");
        cancelButton.setActionCommand(ABANDON);
        cancelButton.addActionListener(e -> fire(ABANDON));
        cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(Box.createVerticalStrut(4));
        col.add(cancelButton);
        col.setPreferredSize(new Dimension(300, 400));
        return col;
    }

    private static JComponent kvRow(String key, JLabel value, boolean last) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                last ? BorderFactory.createEmptyBorder() : BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)));
        JLabel k = new JLabel(key);
        k.setFont(Theme.F_SMALL.deriveFont(13f));
        k.setForeground(Theme.TEXT_SECONDARY);
        value.setFont(Theme.F_LABEL);
        value.setForeground(Theme.TEXT_PRIMARY);
        value.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        row.add(k, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return row;
    }

    private void refreshSummary() {
        Location to = (Location) locationBox.getSelectedItem();
        summaryValues[0].setText(fromName.isEmpty() ? "-" : fromName);
        summaryValues[1].setText(plateBox.getText().trim().isEmpty() ? "-" : plateBox.getText().trim().toUpperCase());
        summaryValues[2].setText(drivers.isEmpty() ? "-" : joinedDrivers());
        String when = (dateBox.getText().trim() + " " + timeBox.getText().trim()).trim();
        summaryValues[3].setText(when.isEmpty() ? "-" : when);
        summaryValues[4].setText(rsaToggle.isSelected() ? "Yes" : "No");
        summaryValues[5].setText(getInsurance().toString());
        for (JLabel l : summaryValues) {
            l.setForeground(Theme.TEXT_PRIMARY);
        }
        fire(QUOTE);
        if (to != null) {
            summaryValues[3].setToolTipText("Return to " + to.getName());
        }
    }

    private String joinedDrivers() {
        StringBuilder sb = new StringBuilder();
        for (Integer id : drivers) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(id);
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ step 2 and 3
    private JComponent buildConfirm() {
        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        centre.setOpaque(false);
        JPanel card = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(620, super.getPreferredSize().height);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(0, 0, 0, 0), false));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel please = new JLabel("Please check with the customer");
        please.setFont(Theme.F_SMALL);
        please.setForeground(Theme.TEXT_SECONDARY);
        confirmTitle.setFont(Theme.F_HEADING);
        confirmTitle.setForeground(Theme.TEXT_PRIMARY);
        names.add(please);
        names.add(confirmTitle);
        head.add(names, BorderLayout.WEST);
        head.add(new StatusStepper(0, true), BorderLayout.EAST);
        card.add(head);

        JPanel grid = new JPanel(new GridLayout(3, 2, 24, 16));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        String[] names2 = {"Checkout location", "Drivers", "Expected return", "Return location", "Insurance", "Roadside assistance"};
        for (int i = 0; i < names2.length; i++) {
            confirmValues[i] = new JLabel();
            confirmValues[i].setFont(Theme.F_BODY_B);
            confirmValues[i].setForeground(Theme.TEXT_PRIMARY);
            JPanel kv = new JPanel();
            kv.setOpaque(false);
            kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
            JLabel k = new JLabel(names2[i]);
            k.setFont(Theme.F_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            kv.add(k);
            kv.add(confirmValues[i]);
            grid.add(kv);
        }
        card.add(grid);

        JPanel totalRow = new JPanel(new BorderLayout(12, 0));
        totalRow.setBackground(Theme.PRIMARY_TINT);
        totalRow.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JPanel totalText = new JPanel();
        totalText.setOpaque(false);
        totalText.setLayout(new BoxLayout(totalText, BoxLayout.Y_AXIS));
        JLabel totalKey = new JLabel("Estimated total cost");
        totalKey.setFont(Theme.F_SMALL);
        totalKey.setForeground(Theme.PRIMARY_TINT_TEXT);
        confirmBreakdown.setFont(Theme.F_SMALL);
        confirmBreakdown.setForeground(Theme.PRIMARY_TINT_TEXT);
        totalText.add(totalKey);
        totalText.add(confirmBreakdown);
        confirmTotal.setFont(Theme.F_DISPLAY.deriveFont(28f));
        confirmTotal.setForeground(Theme.PRIMARY_TINT_TEXT);
        totalRow.add(totalText, BorderLayout.WEST);
        totalRow.add(confirmTotal, BorderLayout.EAST);
        card.add(totalRow);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        foot.setBackground(Theme.BG_ZEBRA);
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(16, 14, 16, 14)));
        confirmCancel.setActionCommand(ABANDON);
        confirmCancel.setPreferredSize(new Dimension(120, 44));
        confirmButton.putClientProperty("variant", "primary");
        confirmButton.setActionCommand(CONFIRM);
        confirmButton.setPreferredSize(new Dimension(180, 44));
        JButton back = new JButton("Edit details");
        back.putClientProperty("variant", "link");
        back.addActionListener(e -> showForm());
        confirmCancel.addActionListener(e -> abandonStrip.setVisible(true));
        confirmButton.addActionListener(e -> fire(CONFIRM));
        foot.add(back);
        foot.add(confirmCancel);
        foot.add(confirmButton);
        card.add(foot);

        abandonStrip.setBackground(Theme.WARN_BG);
        abandonStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.WARN_LINE),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));
        JLabel warn = new JLabel("Abandon checkout? Nothing will be saved.", Icons.icon(Icons.Type.WARNING, 18, Theme.WARN_FG), JLabel.LEFT);
        warn.setIconTextGap(12);
        warn.setFont(Theme.F_BODY_B);
        warn.setForeground(Theme.WARN_DARK);
        JPanel two = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        two.setOpaque(false);
        JButton keep = new JButton("Keep going");
        keep.putClientProperty("size", "compact");
        keep.addActionListener(e -> abandonStrip.setVisible(false));
        JButton abandon = new JButton("Abandon");
        abandon.putClientProperty("variant", "danger");
        abandon.putClientProperty("size", "compact");
        abandon.addActionListener(e -> fire(ABANDON));
        two.add(keep);
        two.add(abandon);
        abandonStrip.add(warn, BorderLayout.CENTER);
        abandonStrip.add(two, BorderLayout.EAST);
        abandonStrip.setVisible(false);
        card.add(abandonStrip);

        centre.add(card);
        return centre;
    }

    private JComponent buildDone() {
        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 24));
        centre.setOpaque(false);
        FormCard card = new FormCard(480, 32);
        JLabel icon = new JLabel(Icons.icon(Icons.Type.CHECK_CIRCLE, 48, Theme.OK_FG));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel head = new JLabel("Rental started");
        head.setFont(Theme.F_HEADING);
        head.setForeground(Theme.TEXT_PRIMARY);
        head.setAlignmentX(Component.CENTER_ALIGNMENT);
        doneText.setFont(Theme.F_BODY);
        doneText.setForeground(Theme.TEXT_SECONDARY);
        doneText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stats = new JPanel(new GridLayout(1, 2));
        stats.setOpaque(false);
        stats.setBorder(new RoundedBorder(8, Theme.BORDER_DIVIDER, new Insets(14, 14, 14, 14), false));
        stats.add(stat("Movement ID", doneId, true));
        stats.add(stat("Estimated total cost", doneCost, false));
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        StatusStepper stepper = new StatusStepper(0, false);
        stepper.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);
        JButton menu = new JButton("Back to menu");
        menu.setPreferredSize(new Dimension(140, 44));
        menu.addActionListener(e -> {
            reset();
            goToMenu();
        });
        JButton again = new JButton("New checkout");
        again.putClientProperty("variant", "primary");
        again.setPreferredSize(new Dimension(150, 44));
        again.addActionListener(e -> reset());
        buttons.add(menu);
        buttons.add(again);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(14));
        card.add(head);
        card.add(Box.createVerticalStrut(6));
        card.add(doneText);
        card.add(Box.createVerticalStrut(20));
        card.add(stats);
        card.add(Box.createVerticalStrut(20));
        card.add(stepper);
        card.add(Box.createVerticalStrut(20));
        card.add(buttons);
        centre.add(card);
        return centre;
    }

    private static JComponent stat(String label, JLabel value, boolean mono) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel k = new JLabel(label);
        k.setFont(Theme.F_SMALL);
        k.setForeground(Theme.TEXT_SECONDARY);
        value.setFont(mono ? Theme.F_NUMERAL.deriveFont(26f) : Theme.F_TITLE.deriveFont(26f));
        value.setForeground(Theme.TEXT_PRIMARY);
        p.add(k);
        p.add(value);
        return p;
    }

    // ------------------------------------------------------------------ what the controller uses
    public String getPlate() {
        return plateBox.getText().trim().toUpperCase();
    }

    public String getDriverText() {
        return driverBox.getText();
    }

    public List<Integer> getDriverIds() {
        return new ArrayList<>(drivers);
    }

    public String getReturnDateText() {
        return dateBox.getText().trim();
    }

    public String getReturnTimeText() {
        return timeBox.getText().trim();
    }

    public Location getReturnLocation() {
        return (Location) locationBox.getSelectedItem();
    }

    public boolean isRsa() {
        return rsaToggle.isSelected();
    }

    public Insurance getInsurance() {
        for (ChoiceCard c : insuranceCards) {
            if (c.isSelected()) {
                return Insurance.valueOf(c.getTitle());
            }
        }
        return Insurance.BASIC;
    }

    /**
     * The active locations to return to. The given one is chosen unless the
     * user already picked another.
     */
    public void setReturnLocations(List<Location> locations, Location preselect) {
        Location keep = (Location) locationBox.getSelectedItem();
        DefaultComboBoxModel<Location> model = new DefaultComboBoxModel<>();
        for (Location l : locations) {
            model.addElement(l);
        }
        locationBox.setModel(model);
        Location pick = keep != null && locations.contains(keep) ? keep : preselect;
        if (pick != null && locations.contains(pick)) {
            locationBox.setSelectedItem(pick);
        }
        refreshSummary();
    }

    /**
     * Shows the prices. The map has one quote per insurance tier for the
     * current vehicle, return time and RSA choice, or is null when they are not
     * known yet (no valid plate or return time).
     */
    public void setQuotes(java.util.Map<Insurance, RentalMovement.Quote> byTier) {
        quotes = byTier;
        for (ChoiceCard c : insuranceCards) {
            Insurance tier = Insurance.valueOf(c.getTitle());
            if (byTier == null) {
                c.setSubtitle("");
            } else {
                RentalMovement.Quote tq = byTier.get(tier);
                c.setSubtitle(tq.insurance == 0 ? "Free" : "$" + String.format("%,.2f", tq.insurance / tq.days)
                        + "/day · $" + String.format("%,.2f", tq.insurance) + " total");
            }
        }
        RentalMovement.Quote q = byTier == null ? null : byTier.get(getInsurance());
        summaryValues[6].setText(q == null ? "-" : "$" + String.format("%,.2f", q.total));
        totalNote.setText(q == null ? "Enter a valid plate and return time" : "<html>" + q.days
                + (q.days == 1 ? " day" : " days") + ": vehicle $" + String.format("%,.2f", q.base)
                + (q.insurance > 0 ? ", insurance $" + String.format("%,.2f", q.insurance) : "")
                + (q.rsa > 0 ? ", RSA $" + String.format("%,.2f", q.rsa) : "") + "</html>");
    }

    /**
     * Shows the vehicles that can be rented from here in a small list under the
     * plate box. Each row is {plate, make and model, category, odometer}. Enter
     * or a click fills in the plate; Escape closes it.
     */
    public void showAvailableVehicles(List<String[]> rows) {
        // the shared picker also closes itself when the screen is left, so it cannot float over the next screen
        String heading = rows.size() + (rows.size() == 1 ? " vehicle" : " vehicles") + " available at " + fromName + ".";
        VehiclePicker.show(plateBox, heading, rows, plate -> {
            plateBox.setText(plate);
            checkPlate();
            if (vehicleStrip.isVisible()) {
                driverBox.requestFocusInWindow();
            }
        });
    }

    public void setFromName(String name) {
        fromName = name == null ? "" : name;
        pickupLocBox.setText(fromName);
        refreshSummary();
    }

    public void setVehicleOk(String name, String detail) {
        vehicleName = name;
        plateField.clearError();
        vehicleStrip.setText("<html><b>" + name + "</b> · " + detail + "</html>");
        vehicleStrip.setVisible(true);
        vehicleBadge.setState(StepBadge.State.OK);
    }

    public void setVehicleError(String message) {
        vehicleStrip.setVisible(false);
        plateField.setError(message);
        vehicleBadge.setState(StepBadge.State.BAD);
    }

    public void addDriver(int id) {
        drivers.add(id);
        driverBox.setText("");
        driverField.clearError();
        rebuildChips();
        driverBox.requestFocusInWindow();
    }

    public void setDriverError(String message) {
        driverField.setError(message);
        driversBadge.setState(StepBadge.State.BAD);
    }

    /**
     * Shows a date in the box as yyyy-MM-dd.
     */
    public void setReturnDate(LocalDate date) {
        programmatic = true;
        dateBox.setText(date == null ? "" : date.toString());
        programmatic = false;
        dateDirty = false;
        dateField.clearError();
        refreshReturnBadge();
    }

    private void refreshReturnBadge() {
        boolean both = !dateBox.getText().trim().isEmpty() && !timeBox.getText().trim().isEmpty();
        returnBadge.setState(both ? StepBadge.State.OK : StepBadge.State.NUMBER);
    }

    /**
     * Shows a time in the box as HH:mm.
     */
    public void setReturnTime(java.time.LocalTime time) {
        programmatic = true;
        timeBox.setText(time == null ? "" : String.format("%02d:%02d", time.getHour(), time.getMinute()));
        programmatic = false;
        timeDirty = false;
        timeField.clearError();
        refreshReturnBadge();
    }

    public void setTimeError(String message) {
        timeField.setError(message);
        returnBadge.setState(StepBadge.State.BAD);
    }

    public void setReturnError(String message) {
        dateField.setError(message);
        returnBadge.setState(StepBadge.State.BAD);
    }

    public void setReturnOk() {
        returnBadge.setState(StepBadge.State.OK);
    }

    public void setProblemCount(int count) {
        problems.setText(count == 0 ? " " : "Fix " + count + (count == 1 ? " problem" : " problems") + " to continue");
    }

    private void rebuildChips() {
        driverChips.removeAll();
        for (Integer id : drivers) {
            driverChips.add(new DriverChip(id));
        }
        driversBadge.setState(drivers.isEmpty() ? StepBadge.State.NUMBER : StepBadge.State.OK);
        driverChips.revalidate();
        driverChips.repaint();
        refreshSummary();
    }

    /**
     * Moves to the confirm card, built from what is typed in the form.
     */
    public void showConfirm() {
        Location to = getReturnLocation();
        confirmTitle.setText(getPlate() + "  ·  " + vehicleName);
        confirmValues[0].setText(fromName);
        confirmValues[1].setText(joinedDrivers());
        confirmValues[2].setText(getReturnDateText() + " " + getReturnTimeText());
        confirmValues[3].setText(to == null ? "-" : to.getName());
        RentalMovement.Quote chosen = quotes == null ? null : quotes.get(getInsurance());
        confirmValues[4].setText(chosen == null ? getInsurance().toString()
                : getInsurance() + (chosen.insurance == 0 ? " · free" : " · $" + String.format("%,.2f", chosen.insurance)));
        confirmValues[5].setText(rsaToggle.isSelected() ? "Yes" : "No");
        RentalMovement.Quote q = quotes == null ? null : quotes.get(getInsurance());
        confirmTotal.setText(q == null ? "-" : "$" + String.format("%,.2f", q.total));
        confirmBreakdown.setText(q == null ? " " : q.days + (q.days == 1 ? " day" : " days") + " · vehicle $"
                + String.format("%,.2f", q.base) + " · insurance $" + String.format("%,.2f", q.insurance)
                + " · RSA $" + String.format("%,.2f", q.rsa));
        abandonStrip.setVisible(false);
        onForm = false;
        cards.show(this, CONFIRM_CARD);
        confirmButton.requestFocusInWindow();
    }

    public void showDone(String plate, String movementId, String cost) {
        doneText.setText(plate + " is now out on rental.");
        doneId.setText(movementId);
        doneCost.setText(cost);
        onForm = false;
        cards.show(this, DONE);
    }

    public void showForm() {
        onForm = true;
        refreshPickup();
        cards.show(this, FORM);
        plateBox.requestFocusInWindow();
    }

    /**
     * Back to an empty form, ready for the next checkout.
     */
    public void reset() {
        plateBox.setText("");
        driverBox.setText("");
        drivers.clear();
        setReturnDate(null);
        setReturnTime(null);
        rsaToggle.setSelected(false);
        rsaLabel.setText("No");
        insuranceCards.get(0).setSelected(true);
        for (StepBadge b : new StepBadge[]{vehicleBadge, driversBadge, pickupBadge, returnBadge, coverBadge}) {
            b.setState(StepBadge.State.NUMBER);
        }
        coverBadge.setState(StepBadge.State.OK);
        pickupBadge.setState(StepBadge.State.OK);
        refreshPickup();
        plateField.clearError();
        driverField.clearError();
        dateField.clearError();
        timeField.clearError();
        vehicleStrip.setVisible(false);
        vehicleName = "";
        lastChecked = "";
        problems.setText(" ");
        rebuildChips();
        showForm();
    }

    /**
     * Asks the screen frame to go back to the menu, as if Back had been
     * clicked.
     */
    public void goToMenu() {
        Object back = getClientProperty("goBack");
        if (back instanceof Runnable) {
            ((Runnable) back).run();
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

    /**
     * A driver ID as a small pill. Click it to remove the driver.
     */
    private class DriverChip extends JComponent {

        private final int id;

        DriverChip(int id) {
            this.id = id;
            setToolTipText("Click to remove this driver");
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    drivers.remove(Integer.valueOf(DriverChip.this.id));
                    rebuildChips();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.F_KEY);
            return new Dimension(12 + fm.stringWidth(String.valueOf(id)) + 8 + 14 + 10, 30);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(Theme.PRIMARY_TINT);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(Theme.PRIMARY_TINT_BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setFont(Theme.F_KEY);
            g2.setColor(Theme.PRIMARY_TINT_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            String text = String.valueOf(id);
            g2.drawString(text, 12, (h + fm.getAscent() - fm.getDescent()) / 2);
            int x = 12 + fm.stringWidth(text) + 10, y = h / 2;
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x, y - 4, x + 8, y + 4);
            g2.drawLine(x + 8, y - 4, x, y + 4);
            g2.dispose();
        }
    }
}
