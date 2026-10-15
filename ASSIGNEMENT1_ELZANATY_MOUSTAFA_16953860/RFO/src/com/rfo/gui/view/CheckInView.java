package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The Check In screen (rentals, car transfers and workshop jobs): type a
 * movement ID or plate, press Check in, and a result card shows where the
 * rental stands (OPEN to INCOMPLETE) with its times. It checks the rental in at
 * the staff member's current location.
 */
public class CheckInView extends JPanel implements PlainScreen {

    public static final String CHECK_IN = "checkIn";

    private final JTextField idBox = new JTextField();
    private final FormField idField = new FormField("Movement ID or plate number", false, idBox, null);
    private final JButton checkInButton = new JButton("Check in");
    private final FormCard result = new FormCard(640, 0);
    private final JLabel banner = new JLabel();
    private final JLabel title = new JLabel();
    private final JLabel next = new JLabel();
    private final JLabel[] times = {new JLabel(), new JLabel(), new JLabel()};

    public CheckInView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard form = new FormCard(640, 24);
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        idBox.setFont(Theme.F_MONO);
        idField.setFixedWidth(200);
        checkInButton.putClientProperty("variant", "primary");
        checkInButton.setActionCommand(CHECK_IN);
        JPanel buttonBox = new JPanel();
        buttonBox.setOpaque(false);
        buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
        buttonBox.add(Box.createVerticalStrut(22));
        buttonBox.add(checkInButton);
        JLabel help = new JLabel("Checks in at your current location.");
        help.setFont(Theme.F_SMALL);
        help.setForeground(Theme.TEXT_SECONDARY);
        JPanel helpBox = new JPanel();
        helpBox.setOpaque(false);
        helpBox.setLayout(new BoxLayout(helpBox, BoxLayout.Y_AXIS));
        helpBox.add(Box.createVerticalStrut(34));
        helpBox.add(help);
        idField.setAlignmentY(Component.TOP_ALIGNMENT);
        buttonBox.setAlignmentY(Component.TOP_ALIGNMENT);
        helpBox.setAlignmentY(Component.TOP_ALIGNMENT);
        row.add(idField);
        row.add(Box.createHorizontalStrut(12));
        row.add(buttonBox);
        row.add(Box.createHorizontalStrut(12));
        row.add(helpBox);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(row);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(form);
        add(Box.createVerticalStrut(16));

        banner.setFont(Theme.F_BODY_B);
        banner.setForeground(Theme.OK_DARK);
        banner.setIcon(Icons.icon(Icons.Type.CHECK_CIRCLE, 18, Theme.OK_FG));
        banner.setIconTextGap(10);
        banner.setOpaque(true);
        banner.setBackground(Theme.OK_BG);
        banner.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JPanel bannerRow = new JPanel(new java.awt.BorderLayout());
        bannerRow.setBackground(Theme.OK_BG);
        bannerRow.add(banner, java.awt.BorderLayout.CENTER);
        bannerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bannerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        result.add(bannerRow);
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        title.setFont(Theme.F_SECTION.deriveFont(18f));
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(title);
        body.add(Box.createVerticalStrut(14));
        JPanel steps = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        steps.setOpaque(false);
        steps.add(new StatusStepper(1, false));
        next.setText("   Next: Close Rental to take payment");
        next.setFont(Theme.F_SMALL);
        next.setForeground(Theme.TEXT_SECONDARY);
        steps.add(next);
        steps.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(steps);
        body.add(Box.createVerticalStrut(14));
        JPanel grid = new JPanel(new GridLayout(1, 3, 12, 0));
        grid.setOpaque(false);
        String[] names = {"Checked out", "Checked in", "Expected return"};
        for (int i = 0; i < 3; i++) {
            JPanel kv = new JPanel();
            kv.setOpaque(false);
            kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
            JLabel k = new JLabel(names[i]);
            k.setFont(Theme.F_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            times[i].setFont(Theme.F_BODY_B);
            times[i].setForeground(Theme.TEXT_PRIMARY);
            kv.add(k);
            kv.add(times[i]);
            grid.add(kv);
        }
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(grid);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.add(body);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.setVisible(false);
        add(result);

        idBox.addActionListener(e -> checkInButton.doClick());
        idBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                idField.clearError();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                idField.clearError();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                idField.clearError();
            }
        });
    }

    public String getMovementId() {
        return idBox.getText().trim();
    }

    public void showError(String message) {
        result.setVisible(false);
        idField.setError(message);
        idBox.requestFocusInWindow();
    }

    /**
     * Shows the result card. The times are already formatted text.
     */
    public void showResult(String locationName, String kind, String movementId, String plate, String carName,
            String checkedOut, String checkedIn, String expected) {
        idField.clearError();
        banner.setText(kind + " checked in at " + locationName);
        next.setText(kind.equals("Rental") ? "   Next: Close Rental to take payment" : "   Next: Close Rental to finish this " + kind.toLowerCase());
        title.setText("<html>Movement " + movementId + " · <span style='font-family:monospace'>" + plate
                + "</span> " + carName + "</html>");
        times[0].setText(checkedOut);
        times[1].setText(checkedIn);
        times[2].setText(expected);
        result.setVisible(true);
        idBox.setText("");
        idField.clearError();
        revalidate();
        repaint();
    }

    public void addController(ActionListener controller) {
        checkInButton.addActionListener(controller);
    }
}
