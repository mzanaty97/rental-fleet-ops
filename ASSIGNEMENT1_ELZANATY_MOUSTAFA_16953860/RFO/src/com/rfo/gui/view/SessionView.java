package com.rfo.gui.view;

import com.rfo.model.Staff;
import com.rfo.service.ModelListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * One logged-in session (one tab). A header shows who is logged in, and below
 * it one screen at a time: the main menu, a sub menu or a feature screen. The
 * navigation controller adds the screens and decides which one is showing. A
 * small console box at the bottom shows the messages the screens print. Listens
 * to the model so screens can refresh when data changes.
 */
public class SessionView extends JPanel implements ModelListener {

    private final JButton logoutButton = new JButton("Log out");
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final ConsoleView console = new ConsoleView();
    private final List<ModelListener> screenListeners = new ArrayList<>();

    public SessionView(Staff staff) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        cards.setBackground(Color.WHITE);

        add(buildHeader(staff), BorderLayout.NORTH);
        add(cards, BorderLayout.CENTER);
        add(console, BorderLayout.SOUTH);
    }

    private JPanel buildHeader(Staff staff) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HomeView.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        String where = staff.getCurrentLocation() != null
                ? "  ·  " + staff.getCurrentLocation().getName() : "";
        JLabel who = new JLabel(staff.getName() + "  ·  " + staff.getStaffID()
                + "  ·  " + staff.getRole() + where);
        who.setForeground(Color.WHITE);
        who.setFont(who.getFont().deriveFont(Font.BOLD, 14f));

        header.add(who, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);
        return header;
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
