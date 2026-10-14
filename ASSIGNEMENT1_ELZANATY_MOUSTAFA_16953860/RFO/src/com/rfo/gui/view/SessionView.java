package com.rfo.gui.view;

import com.rfo.model.Staff;
import com.rfo.service.ModelListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * One logged-in session (one tab). Shows who is logged in and one sub-tab per
 * menu option the staff member's role allows. Listens to the model so the
 * sub-tabs can refresh when data changes.
 */
public class SessionView extends JPanel implements ModelListener {

    private final JButton logoutButton = new JButton("Log out");
    private final JTabbedPane sections = new JTabbedPane();

    public SessionView(Staff staff, List<String> menuOptions) {
        super(new BorderLayout());
        setBackground(Color.WHITE);

        add(buildHeader(staff), BorderLayout.NORTH);

        for (String option : menuOptions) {
            sections.addTab(option, buildPlaceholder(option));
        }
        add(sections, BorderLayout.CENTER);
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

    // temporary content until each section gets its own panel
    private JPanel buildPlaceholder(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(name + " - coming soon", SwingConstants.CENTER);
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    public void addController(ActionListener controller) {
        logoutButton.addActionListener(controller);
    }

    // sections will refresh their tables here as they are built
    @Override
    public void onModelChanged() {
        repaint();
    }
}
