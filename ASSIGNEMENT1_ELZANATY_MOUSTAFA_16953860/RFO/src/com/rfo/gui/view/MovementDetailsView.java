package com.rfo.gui.view;

import com.rfo.gui.theme.BadgeLabel;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The Query Movement Details screen: type a movement ID, press Find, and a card
 * shows everything about that movement in titled groups. It stretches across
 * the content area. The controller decides which groups and values to show, so
 * the same card works for rentals, transfers, workshop jobs and the rest.
 */
public class MovementDetailsView extends JPanel implements FullScreen {

    public static final String FIND = "findMovement";

    /**
     * One titled group of label and value pairs, for example "CHECK OUT AND
     * CHECK IN".
     */
    public static class Section {

        private final String title;
        private final List<String[]> items = new ArrayList<>();

        public Section(String title) {
            this.title = title;
        }

        public Section add(String label, String value) {
            items.add(new String[]{label, value});
            return this;
        }
    }

    private final LookupBar lookup = new LookupBar("Movement ID", 200, "Find", FIND, "Type the movement's ID.");
    private final JPanel card = new JPanel();
    private final JPanel sections = new JPanel();
    private final JLabel kindLabel = new JLabel();
    private final JLabel titleLabel = new JLabel();
    private final JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

    public MovementDetailsView() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        FormCard form = new FormCard(640, 24);
        lookup.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lookup);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(form);
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        topRow.add(top);
        add(topRow, BorderLayout.NORTH);

        // the details card: header (kind, title, status) then the groups
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(0, 0, 0, 0), false));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        kindLabel.setFont(Theme.F_SMALL);
        kindLabel.setForeground(Theme.TEXT_SECONDARY);
        titleLabel.setFont(Theme.F_HEADING);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titles.add(kindLabel);
        titles.add(titleLabel);
        statusBox.setOpaque(false);
        header.add(titles, BorderLayout.CENTER);
        header.add(statusBox, BorderLayout.EAST);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(header);

        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sections);
        card.setVisible(false);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(card, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // the ID box is ready for typing as soon as the screen opens
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(lookup::focusBox);
            }
        });
    }

    public String getMovementId() {
        return lookup.getText();
    }

    public void showError(String message) {
        card.setVisible(false);
        lookup.showError(message);
    }

    /**
     * Shows the details card.
     *
     * @param kind small line above the title, for example "Rental"
     * @param title the main line, for example "Movement 12 · ABC123 Toyota
     * Corolla"
     * @param status the movement status, shown as a coloured badge
     * @param groups the titled groups of values
     */
    public void showMovement(String kind, String title, String status, List<Section> groups) {
        lookup.clearError();
        kindLabel.setText(kind);
        titleLabel.setText(title);
        statusBox.removeAll();
        statusBox.add(new BadgeLabel(status, true));

        sections.removeAll();
        for (int s = 0; s < groups.size(); s++) {
            Section group = groups.get(s);
            JPanel box = new JPanel();
            box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBorder(BorderFactory.createCompoundBorder(
                    s < groups.size() - 1 ? BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER)
                    : BorderFactory.createEmptyBorder(),
                    BorderFactory.createEmptyBorder(14, 24, 14, 24)));
            JLabel heading = new JLabel(group.title);
            heading.setFont(Theme.F_BADGE);
            heading.setForeground(Theme.TEXT_SECONDARY);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(heading);
            box.add(javax.swing.Box.createVerticalStrut(10));
            JPanel grid = new JPanel(new GridLayout(0, 4, 12, 10));
            grid.setOpaque(false);
            for (String[] item : group.items) {
                JPanel kv = new JPanel();
                kv.setOpaque(false);
                kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
                JLabel k = new JLabel(item[0]);
                k.setFont(Theme.F_SMALL);
                k.setForeground(Theme.TEXT_SECONDARY);
                JLabel v = new JLabel(item[1]);
                boolean empty = "Not yet".equals(item[1]) || "N/A".equals(item[1]);
                v.setFont(empty ? Theme.F_BODY : Theme.F_BODY_B);
                v.setForeground(empty ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY);
                kv.add(k);
                kv.add(v);
                grid.add(kv);
            }
            grid.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(grid);
            box.setAlignmentX(Component.LEFT_ALIGNMENT);
            sections.add(box);
        }
        card.setVisible(true);
        revalidate();
        repaint();
    }

    public void addController(ActionListener controller) {
        lookup.addController(controller);
    }
}
