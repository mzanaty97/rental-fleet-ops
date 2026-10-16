package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The Close Rental screen in two steps: return details (movement ID and the
 * odometer at check in), then payment (the final cost to take from the
 * customer, and a confirm). It only shows what it is given.
 */
public class CloseRentalView extends JPanel implements PlainScreen {

    public static final String LOOKUP = "lookup";
    public static final String CONTINUE = "continue";
    public static final String CONFIRM = "confirm";
    public static final String CANCEL = "cancel";

    private final CardLayout cards = new CardLayout();
    private final JPanel stepCards = new JPanel(cards);
    private final JPanel stepper1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
    private final JTextField idBox = new JTextField();
    private final FormField idField = new FormField("Movement ID or plate number", false, idBox, null);
    private final JLabel idOk = new JLabel(" ");
    private final JTextField odoBox = new JTextField();
    private final FormField odoField = new FormField("Odometer at check in (km)", false, odoBox, null);
    private final JLabel costLabel = new JLabel();
    private final JLabel note = new JLabel();
    private final JButton continueButton = new JButton("Continue to payment");
    private final JLabel[] facts = {new JLabel(), new JLabel(), new JLabel()};
    private final JLabel cost = new JLabel();
    private final JButton confirmButton = new JButton("Confirm payment and close");
    private final JButton cancelButton = new JButton("Cancel");
    private final List<ActionListener> listeners = new ArrayList<>();
    private String lastChecked = "";

    public CloseRentalView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new java.awt.Insets(0, 0, 0, 0), false));
        card.add(stepHeader(), BorderLayout.NORTH);
        stepCards.setOpaque(false);
        stepCards.add(detailsStep(), "details");
        stepCards.add(paymentStep(), "payment");
        card.add(stepCards, BorderLayout.CENTER);
        JPanel sized = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(560, super.getPreferredSize().height);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        sized.setOpaque(false);
        sized.add(card);
        sized.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(sized);
    }

    private JComponent stepHeader() {
        stepper1.setBackground(Theme.BG_ZEBRA);
        stepper1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(10, 24, 10, 0)));
        return stepper1;
    }

    private void paintHeader(boolean paymentStep) {
        stepper1.removeAll();
        stepper1.add(stepLabel(paymentStep ? StepBadge.State.OK : StepBadge.State.NUMBER, "1", "Return details"));
        JPanel line = new JPanel();
        line.setBackground(paymentStep ? Theme.OK_FG : Theme.BORDER_KEY);
        line.setPreferredSize(new Dimension(40, 2));
        stepper1.add(line);
        stepper1.add(stepLabel(StepBadge.State.NUMBER, "2", "Payment"));
        stepper1.revalidate();
        stepper1.repaint();
    }

    private static JComponent stepLabel(StepBadge.State state, String number, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        StepBadge badge = new StepBadge(number);
        badge.setState(state);
        JLabel l = new JLabel(text);
        l.setFont(Theme.F_LABEL);
        l.setForeground(Theme.TEXT_PRIMARY);
        p.add(badge);
        p.add(l);
        return p;
    }

    private JComponent detailsStep() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        idBox.setFont(Theme.F_MONO);
        odoBox.setFont(Theme.F_MONO);
        idField.setFixedWidth(220);
        odoField.setFixedWidth(220);
        idField.setAlignmentX(Component.LEFT_ALIGNMENT);
        odoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        idOk.setFont(Theme.F_SMALL);
        idOk.setForeground(Theme.OK_FG);
        idOk.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(idField);
        body.add(idOk);
        body.add(Box.createVerticalStrut(12));
        body.add(odoField);
        body.add(Box.createVerticalStrut(12));
        continueButton.putClientProperty("variant", "primary");
        continueButton.setIcon(Icons.icon(Icons.Type.CHEVRON_RIGHT, 16, Theme.TEXT_ON_PRIMARY));
        continueButton.setHorizontalTextPosition(JButton.LEFT);
        continueButton.setActionCommand(CONTINUE);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttons.setOpaque(false);
        buttons.add(continueButton);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(buttons);

        idBox.addActionListener(e -> {
            lookup();
            odoBox.requestFocusInWindow();
        });
        idBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                lookup();
            }
        });
        idBox.getDocument().addDocumentListener(simple(() -> {
            idField.clearError();
            idOk.setText(" ");
            lastChecked = "";
        }));
        odoBox.getDocument().addDocumentListener(simple(odoField::clearError));
        odoBox.addActionListener(e -> continueButton.doClick());
        continueButton.addActionListener(e -> fire(CONTINUE));
        paintHeader(false);
        return body;
    }

    private void lookup() {
        String text = idBox.getText().trim();
        if (!text.isEmpty() && !text.equals(lastChecked)) {
            lastChecked = text;
            fire(LOOKUP);
        }
    }

    private JComponent paymentStep() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel grid = new JPanel(new GridLayout(1, 3, 12, 0));
        grid.setOpaque(false);
        String[] names = {"Movement", "Odometer", "Distance"};
        for (int i = 0; i < 3; i++) {
            JPanel kv = new JPanel();
            kv.setOpaque(false);
            kv.setLayout(new BoxLayout(kv, BoxLayout.Y_AXIS));
            JLabel k = new JLabel(names[i]);
            k.setFont(Theme.F_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            facts[i].setFont(Theme.F_BODY_B);
            facts[i].setForeground(Theme.TEXT_PRIMARY);
            kv.add(k);
            kv.add(facts[i]);
            grid.add(kv);
        }
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(grid);
        body.add(Box.createVerticalStrut(16));

        JPanel costBox = new JPanel(new BorderLayout());
        costBox.setBackground(Theme.BG_WINDOW);
        costBox.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        costLabel.setText("Final rental cost");
        costLabel.setFont(Theme.F_OPTION);
        costLabel.setForeground(Theme.TEXT_PRIMARY);
        cost.setFont(Theme.F_DISPLAY);
        cost.setForeground(Theme.TEXT_PRIMARY);
        costBox.add(costLabel, BorderLayout.WEST);
        costBox.add(cost, BorderLayout.EAST);
        costBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        costBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        body.add(costBox);
        body.add(Box.createVerticalStrut(10));

        note.setText("<html>Take payment from the customer, then confirm. Closing sets the vehicle back to AVAILABLE.</html>");
        note.setFont(Theme.F_SMALL.deriveFont(13f));
        note.setForeground(Theme.TEXT_SECONDARY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(note);
        body.add(Box.createVerticalStrut(14));

        cancelButton.setActionCommand(CANCEL);
        cancelButton.addActionListener(e -> fire(CANCEL));
        confirmButton.putClientProperty("variant", "primary");
        confirmButton.setIcon(Icons.icon(Icons.Type.CHECK_CIRCLE, 18, Theme.TEXT_ON_PRIMARY));
        confirmButton.setActionCommand(CONFIRM);
        confirmButton.addActionListener(e -> fire(CONFIRM));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancelButton);
        buttons.add(confirmButton);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(buttons);
        return body;
    }

    public String getMovementId() {
        return idBox.getText().trim();
    }

    public String getOdometerText() {
        return odoBox.getText();
    }

    public void showMovementOk(String text) {
        idField.clearError();
        idOk.setText("✓ " + text);
    }

    public void showMovementError(String message) {
        idOk.setText(" ");
        idField.setError(message);
    }

    public void showOdometerError(String message) {
        odoField.setError(message);
        odoBox.requestFocusInWindow();
    }

    public void showPayment(String movement, int odoOut, int odoIn, String finalCost, boolean isRental) {
        costLabel.setText(isRental ? "Final rental cost" : "Charge");
        note.setText(isRental
                ? "<html>Take payment from the customer, then confirm. Closing sets the vehicle back to AVAILABLE.</html>"
                : "<html>Nothing to pay for this movement. Closing updates the vehicle and sets it back to AVAILABLE.</html>");
        confirmButton.setText(isRental ? "Confirm payment and close" : "Confirm and close");
        facts[0].setText(movement);
        facts[1].setText(String.format("%,d → %,d", odoOut, odoIn));
        facts[2].setText(String.format("%,d km", odoIn - odoOut));
        cost.setText(finalCost);
        paintHeader(true);
        cards.show(stepCards, "payment");
        confirmButton.requestFocusInWindow();
    }

    public void showDetails() {
        paintHeader(false);
        cards.show(stepCards, "details");
        idBox.requestFocusInWindow();
    }

    /**
     * Empties the form and returns to step 1.
     */
    public void reset() {
        idBox.setText("");
        odoBox.setText("");
        idOk.setText(" ");
        idField.clearError();
        odoField.clearError();
        lastChecked = "";
        showDetails();
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
