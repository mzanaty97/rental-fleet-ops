package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The Defleet screen: a warning, a plate box, the vehicle's details once it is
 * found, and a red "are you sure" panel before anything happens. It only
 * collects and shows what it is given.
 */
public class DefleetView extends JPanel implements PlainScreen {

    public static final String LOOKUP = "lookup";
    public static final String LIST_VEHICLES = "listVehicles";
    public static final String ASK = "ask";
    public static final String CONFIRM = "confirm";
    private static final int W = 640, PAD = 24;

    private final List<ActionListener> listeners = new ArrayList<>();
    private final JTextField plateBox = new JTextField();
    private final FormField plateField = new FormField("Plate number", true, plateBox, "Press Enter to look the vehicle up");
    private final JLabel[] infoValues = {new JLabel(), new JLabel(), new JLabel()};
    private final StatusPill pill = new StatusPill();
    private final JPanel infoRow = new JPanel(new GridLayout(1, 4, 12, 0));
    private final JButton defleetButton = new JButton("Defleet vehicle");
    private final JPanel buttonRow = new JPanel();
    private final JPanel confirmPanel = new JPanel(new BorderLayout(12, 0));
    private final JLabel confirmText = new JLabel();
    private final JLabel doneBanner = new JLabel();
    private String lastChecked = "";

    public DefleetView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard card = new FormCard(W, PAD);

        JLabel warn = new JLabel("<html>Defleeting takes a vehicle out of the fleet for good. It can no longer be rented or moved.</html>",
                Icons.icon(Icons.Type.WARNING, 18, Theme.WARN_FG), JLabel.LEFT);
        warn.setIconTextGap(12);
        warn.setFont(Theme.F_BODY);
        warn.setForeground(Theme.WARN_DARK);
        warn.setOpaque(true);
        warn.setBackground(Theme.WARN_BG);
        warn.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        warn.setAlignmentX(Component.LEFT_ALIGNMENT);
        warn.setMaximumSize(new Dimension(W - 2 * PAD, 60));
        card.add(warn);
        card.add(Box.createVerticalStrut(16));

        doneBanner.setFont(Theme.F_BODY_B);
        doneBanner.setForeground(Theme.OK_DARK);
        doneBanner.setIcon(Icons.icon(Icons.Type.CHECK_CIRCLE, 18, Theme.OK_FG));
        doneBanner.setIconTextGap(10);
        doneBanner.setOpaque(true);
        doneBanner.setBackground(Theme.OK_BG);
        doneBanner.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        doneBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        doneBanner.setMaximumSize(new Dimension(W - 2 * PAD, 48));
        doneBanner.setVisible(false);
        card.add(doneBanner);

        plateBox.setFont(Theme.F_MONO);
        plateField.setFixedWidth(260);
        plateField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(plateField);
        card.add(Box.createVerticalStrut(12));

        String[] names = {"Vehicle", "Category", "Odometer", "Status"};
        infoRow.setOpaque(false);
        infoRow.setBorder(BorderFactory.createCompoundBorder(
                new com.rfo.gui.theme.RoundedBorder(8, Theme.BORDER_DIVIDER, new java.awt.Insets(0, 0, 0, 0), false),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        for (int i = 0; i < names.length; i++) {
            JPanel kv = new JPanel();
            kv.setOpaque(false);
            kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
            JLabel k = new JLabel(names[i]);
            k.setFont(Theme.F_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            k.setAlignmentX(Component.LEFT_ALIGNMENT);
            kv.add(k);
            kv.add(Box.createVerticalStrut(2));
            if (i < 3) {
                infoValues[i].setFont(Theme.F_BODY_B);
                infoValues[i].setForeground(Theme.TEXT_PRIMARY);
                infoValues[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                kv.add(infoValues[i]);
            } else {
                pill.setAlignmentX(Component.LEFT_ALIGNMENT);
                kv.add(pill);
            }
            infoRow.add(kv);
        }
        infoRow.setVisible(false);
        infoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoRow.setMaximumSize(new Dimension(W - 2 * PAD, 80));
        card.add(infoRow);

        card.add(Box.createVerticalStrut(16));
        JPanel divider = new JPanel();
        divider.setBackground(Theme.BORDER_DIVIDER);
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(16));

        defleetButton.putClientProperty("variant", "danger");
        defleetButton.setEnabled(false);
        defleetButton.addActionListener(e -> fire(ASK));
        buttonRow.setOpaque(false);
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.add(Box.createHorizontalGlue());
        buttonRow.add(defleetButton);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(buttonRow);

        confirmPanel.setBackground(Theme.ERR_BG);
        confirmPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        confirmText.setFont(Theme.F_BODY_B);
        confirmText.setForeground(Theme.ERR_DARK);
        confirmText.setIcon(Icons.icon(Icons.Type.WARNING, 18, Theme.ERR_FG));
        confirmText.setIconTextGap(10);
        JButton keep = new JButton("Keep vehicle");
        keep.putClientProperty("size", "compact");
        keep.addActionListener(e -> hideConfirm());
        JButton yes = new JButton("Yes, defleet");
        yes.putClientProperty("variant", "danger");
        yes.putClientProperty("size", "compact");
        yes.addActionListener(e -> fire(CONFIRM));
        JPanel two = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        two.setOpaque(false);
        two.add(keep);
        two.add(yes);
        confirmPanel.add(confirmText, BorderLayout.CENTER);
        confirmPanel.add(two, BorderLayout.EAST);
        confirmPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPanel.setMaximumSize(new Dimension(W - 2 * PAD, 60));
        confirmPanel.setVisible(false);
        card.add(confirmPanel);

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(card);

        plateBox.addActionListener(e -> {
            if (plateBox.getText().trim().isEmpty()) {
                fire(LIST_VEHICLES);
            } else {
                lastChecked = "";
                lookup();
                if (defleetButton.isEnabled()) {
                    defleetButton.requestFocusInWindow();
                }
            }
        });
        plateBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookup();
            }
        });
        plateBox.getDocument().addDocumentListener(new DocumentListener() {
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
                plateField.clearError();
                infoRow.setVisible(false);
                defleetButton.setEnabled(false);
                confirmPanel.setVisible(false);
                buttonRow.setVisible(true);
                lastChecked = "";
            }
        });
        defleetButton.getInputMap(JComponent.WHEN_FOCUSED).put(
                javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "rfoEnter");
        defleetButton.getActionMap().put("rfoEnter", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defleetButton.doClick();
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(() -> plateBox.requestFocusInWindow());
            }
        });
    }

    private void lookup() {
        String text = plateBox.getText().trim();
        if (!text.isEmpty() && !text.equalsIgnoreCase(lastChecked)) {
            lastChecked = text;
            fire(LOOKUP);
        }
    }

    // ------------------------------------------------------------------ what the controller uses
    public String getPlate() {
        return plateBox.getText().trim().toUpperCase();
    }

    public void setPlate(String plate) {
        plateBox.setText(plate);
        lastChecked = "";
        lookup();
        if (defleetButton.isEnabled()) {
            defleetButton.requestFocusInWindow();
        }
    }

    public void showVehicle(String name, String category, String odometer, String status) {
        plateField.clearError();
        infoValues[0].setText(name);
        infoValues[1].setText(category);
        infoValues[2].setText(odometer);
        pill.setStatus(status);
        infoRow.setVisible(true);
        defleetButton.setEnabled(true);
    }

    public void showError(String message) {
        infoRow.setVisible(false);
        defleetButton.setEnabled(false);
        plateField.setError(message);
    }

    public void showConfirm(String plate) {
        confirmText.setText("<html>Defleet " + plate + "? This cannot be undone from this screen.</html>");
        buttonRow.setVisible(false);
        confirmPanel.setVisible(true);
    }

    public void hideConfirm() {
        confirmPanel.setVisible(false);
        buttonRow.setVisible(true);
        defleetButton.requestFocusInWindow();
    }

    public void showVehicleChoices(String heading, List<String[]> rows) {
        VehiclePicker.show(plateBox, heading, rows, this::setPlate);
    }

    /**
     * After a defleet: a green banner, and an empty form for the next one.
     */
    public void showDone(String plate) {
        reset();
        doneBanner.setText(plate + " has been defleeted.");
        doneBanner.setVisible(true);
    }

    public void reset() {
        doneBanner.setVisible(false);
        plateBox.setText("");
        infoRow.setVisible(false);
        confirmPanel.setVisible(false);
        buttonRow.setVisible(true);
        defleetButton.setEnabled(false);
        plateField.clearError();
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

    /**
     * A small rounded label for a vehicle status, coloured by what the status
     * means.
     */
    private static class StatusPill extends JComponent {

        private String text = "";

        void setStatus(String status) {
            text = status;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(Theme.F_SMALL.deriveFont(java.awt.Font.BOLD));
            return new Dimension(fm.stringWidth(text) + 20, 24);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color bg, fg;
            if ("AVAILABLE".equals(text)) {
                bg = Theme.OK_BG;
                fg = Theme.OK_DARK;
            } else if ("TO_BE_CLEANED".equals(text) || "SERVICE_DUE".equals(text)) {
                bg = Theme.WARN_BG;
                fg = Theme.WARN_DARK;
            } else {
                bg = Theme.BG_SUBTLE;
                fg = Theme.TEXT_PRIMARY;
            }
            int h = getHeight();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
            g2.setFont(Theme.F_SMALL.deriveFont(java.awt.Font.BOLD));
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, 10, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}
