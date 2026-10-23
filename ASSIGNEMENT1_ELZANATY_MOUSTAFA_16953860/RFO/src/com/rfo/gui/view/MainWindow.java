package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import com.rfo.model.Staff;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The single application window. Home is always the first tab; sessions open as
 * extra tabs.
 */
public class MainWindow extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private final HomeView homeView = new HomeView();

    public MainWindow() {
        super("RFO - RentalFleetOps");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 750);
        setLocationRelativeTo(null);

        tabs.addTab("Home", homeView);
        tabs.setTabComponentAt(0, TabTitle.home());
        add(tabs);
    }

    public HomeView getHomeView() {
        return homeView;
    }

    /**
     * Opens a new login tab.
     */
    public void addTab(String title, Component content, ActionListener closeAction) {
        tabs.addTab(title, content);
        tabs.setTabComponentAt(tabs.indexOfComponent(content), TabTitle.login(closeAction));
        tabs.setSelectedComponent(content);
    }

    /**
     * Swaps a tab's content for a login form (after logging out).
     */
    public void replaceTab(Component oldContent, Component newContent, String title, ActionListener closeAction) {
        int index = tabs.indexOfComponent(oldContent);
        tabs.setComponentAt(index, newContent);
        tabs.setTabComponentAt(index, TabTitle.login(closeAction));
        tabs.setSelectedIndex(index);
    }

    /**
     * Turns a login tab into a session tab: initials in the role colour and
     * "First (ID)". If the same person already has a tab open, this one is
     * called "First (ID) · 2".
     */
    public void replaceTabWithSession(Component oldContent, Component newContent, Staff staff, ActionListener closeAction) {
        int index = tabs.indexOfComponent(oldContent);
        String base = staff.getFName() + " (" + staff.getStaffID() + ")";

        int same = 0;
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component c = tabs.getTabComponentAt(i);
            if (i != index && c instanceof TabTitle && base.equals(((TabTitle) c).getBaseLabel())) {
                same++;
            }
        }
        String text = same == 0 ? base : base + " · " + (same + 1);
        String initials = initial(staff.getFName()) + initial(staff.getLName());

        tabs.setComponentAt(index, newContent);
        tabs.setTabComponentAt(index, TabTitle.session(text, base, initials, Theme.roleSolid(staff.getRole().name()), closeAction));
        tabs.setSelectedIndex(index);
    }

    /**
     * Marks a login tab as locked (too many wrong passwords).
     */
    public void markTabLocked(Component content) {
        int index = tabs.indexOfComponent(content);
        if (index >= 0 && tabs.getTabComponentAt(index) instanceof TabTitle) {
            ((TabTitle) tabs.getTabComponentAt(index)).markLocked();
        }
    }

    private static String initial(String name) {
        return name == null || name.isEmpty() ? "" : name.substring(0, 1).toUpperCase();
    }

    public void removeTab(Component content) {
        tabs.remove(content);
    }
}
