package com.rfo.gui.view;

import com.rfo.gui.theme.Avatar;
import com.rfo.gui.theme.BadgeLabel;
import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import com.rfo.model.Staff;
import com.rfo.service.ModelListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * One logged-in session (one tab). A white header shows who is logged in
 * (initials, name, staff ID, role badge), the current location and a Log out
 * button. Below it is one screen at a time: the main menu, a sub menu or a
 * feature screen, which the navigation controller adds and shows. A slim
 * console bar at the bottom shows the messages the screens print. Listens to
 * the model so screens can refresh when data changes.
 */
public class SessionView extends JPanel implements ModelListener {

    private final Staff staff;
    private final JButton logoutButton = new JButton("Log out");
    private final JLabel locationLabel = new JLabel();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final ConsoleView console = new ConsoleView();
    private final List<ModelListener> screenListeners = new ArrayList<>();

    public SessionView(Staff staff) {
        super(new BorderLayout());
        this.staff = staff;
        setBackground(Theme.BG_WINDOW);
        cards.setBackground(Theme.BG_WINDOW);

        add(buildHeader(), BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(console, BorderLayout.SOUTH);

        String where = staff.getCurrentLocation() != null ? " Location: " + staff.getCurrentLocation().getName() + "." : "";
        console.info("Welcome, " + staff.getFName() + "." + where);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(Theme.BG_SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                BorderFactory.createEmptyBorder(0, 32, 0, 32)));
        header.setPreferredSize(new Dimension(10, Theme.SESSION_HEADER_H));

        String role = staff.getRole().name();
        String initials = first(staff.getFName()) + first(staff.getLName());

        JLabel name = new JLabel(staff.getName());
        name.setFont(Theme.F_SECTION);
        JLabel id = new JLabel(staff.getStaffID());
        id.setFont(Theme.F_MONO);
        id.setForeground(Theme.TEXT_SECONDARY);

        JPanel idRow = new JPanel();
        idRow.setOpaque(false);
        idRow.setLayout(new BoxLayout(idRow, BoxLayout.X_AXIS));
        idRow.add(id);
        idRow.add(Box.createHorizontalStrut(8));
        idRow.add(new BadgeLabel(role, false));
        idRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel who = new JPanel();
        who.setOpaque(false);
        who.setLayout(new BoxLayout(who, BoxLayout.Y_AXIS));
        who.add(name);
        who.add(Box.createVerticalStrut(3));
        who.add(idRow);

        logoutButton.putClientProperty("size", "compact");
        logoutButton.setIcon(Icons.icon(Icons.Type.LOGOUT, 15, Theme.TEXT_PRIMARY));
        logoutButton.setIconTextGap(8);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.gridx = 0;
        header.add(new Avatar(initials, Theme.roleSolid(role), 36, 13f), c);
        c.gridx = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 14, 0, 0);
        header.add(who, c);
        c.gridx = 2;
        c.weightx = 0;
        c.anchor = GridBagConstraints.CENTER;
        header.add(new LocationBox(), c);
        c.gridx = 3;
        c.insets = new Insets(0, 14, 0, 0);
        header.add(logoutButton, c);

        refreshHeader();
        return header;
    }

    private static String first(String s) {
        return s == null || s.isEmpty() ? "" : s.substring(0, 1).toUpperCase();
    }

    /**
     * Call after the staff member's current location changes.
     */
    public void refreshHeader() {
        locationLabel.setText(staff.getCurrentLocation() != null ? staff.getCurrentLocation().getName() : "Not set");
    }

    /**
     * The rounded grey box with a pin: "Current location" over the depot name.
     */
    private class LocationBox extends JPanel {

        LocationBox() {
            setOpaque(false);
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 14));

            JLabel caption = new JLabel("Current location");
            caption.setFont(Theme.F_SMALL.deriveFont(11f));
            caption.setForeground(Theme.TEXT_SECONDARY);
            locationLabel.setFont(Theme.F_LABEL);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(caption);
            text.add(locationLabel);

            JComponent pin = new JComponent() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(16, 16);
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Icons.paint((Graphics2D) g, Icons.Type.PIN, 0, (getHeight() - 16) / 2, 16, Theme.PRIMARY, 2f);
                }
            };
            add(pin, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Theme.CONTROL_H;
            return d;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BG_WINDOW);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R_CONTROL * 2, Theme.R_CONTROL * 2);
            g2.dispose();
        }
    }

    /**
     * Where screens print their messages.
     */
    public MessageSink getConsole() {
        return console;
    }

    /**
     * Registers a screen under a key so it can be shown later.
     */
    public void addScreen(String key, JComponent screen) {
        cards.add(screen, key);
    }

    public void showScreen(String key) {
        cardLayout.show(cards, key);
        // put the focus on the screen now showing so the keyboard shortcuts reach it
        for (java.awt.Component screen : cards.getComponents()) {
            if (screen.isVisible()) {
                screen.requestFocusInWindow();
            }
        }
    }

    /**
     * A screen's controller that wants to hear when the model changes.
     */
    public void addModelListener(ModelListener listener) {
        screenListeners.add(listener);
    }

    public void addController(ActionListener controller) {
        logoutButton.addActionListener(controller);
    }

    // pass the news on to every screen that wants to refresh
    @Override
    public void onModelChanged() {
        for (ModelListener listener : screenListeners) {
            listener.onModelChanged();
        }
        repaint();
    }
}
