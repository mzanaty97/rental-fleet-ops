package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Login form shown in a new session tab: a white card with the Staff ID and
 * password, the Log in button, a five-step meter of wrong attempts and a slim
 * message bar at the bottom. After five wrong attempts the card turns into the
 * locked version. This class only displays; the controller decides what
 * happens.
 */
public class LoginView extends JPanel {

    private static final int CARD_WIDTH = 400;

    private final JTextField staffIdField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JButton loginButton = new JButton("Log in");
    private final JLabel idLabel = label("Staff ID");
    private final JLabel passwordLabel = label("Password");
    private final JLabel idHint = new JLabel("For example AGNT002. Upper case is added for you.");
    private final JLabel messageLabel = new JLabel();
    private final JPanel header = column();
    private final JPanel lockedBanner = new JPanel(new BorderLayout(14, 0));
    private final JLabel lockedText = new JLabel();
    private final AttemptMeter meter = new AttemptMeter();
    private final JLabel meterText = new JLabel();
    private final JPanel meterRow = new JPanel(new BorderLayout());
    private final ConsoleView console = new ConsoleView();

    public LoginView() {
        super(new BorderLayout());
        setBackground(Theme.BG_WINDOW);

        JPanel card = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(CARD_WIDTH, super.getPreferredSize().height);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.BG_SURFACE);
        card.setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(32, 32, 32, 32), false));

        // heading, or the red "locked" notice in its place
        JLabel heading = new JLabel("Sign in");
        heading.setFont(Theme.F_TITLE);
        heading.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Start a session with your staff details.");
        sub.setFont(Theme.F_BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        header.add(left(heading));
        header.add(Box.createVerticalStrut(6));
        header.add(left(sub));
        buildLockedBanner();
        lockedBanner.setVisible(false);

        staffIdField.setFont(Theme.F_MONO.deriveFont(14f));
        passwordField.setFont(Theme.F_BODY);
        staffIdField.setPreferredSize(new Dimension(CARD_WIDTH - 64, 40));
        passwordField.setPreferredSize(new Dimension(CARD_WIDTH - 64, 40));
        staffIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        idLabel.setLabelFor(staffIdField);
        passwordLabel.setLabelFor(passwordField);

        idHint.setFont(Theme.F_SMALL);
        idHint.setForeground(Theme.TEXT_SECONDARY);

        messageLabel.setFont(Theme.F_SMALL);
        messageLabel.setForeground(Theme.ERR_FG);
        messageLabel.setIconTextGap(6);
        messageLabel.setIcon(Icons.icon(Icons.Type.ERROR_CIRCLE, 14, Theme.ERR_FG));
        messageLabel.setVisible(false);

        loginButton.putClientProperty("variant", "primary");
        loginButton.setFont(Theme.F_BUTTON.deriveFont(15f));
        loginButton.setPreferredSize(new Dimension(CARD_WIDTH - 64, 44));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        meterText.setFont(Theme.F_SMALL);
        meterText.setForeground(Theme.TEXT_SECONDARY);
        meterRow.setOpaque(false);
        meterRow.add(meter, BorderLayout.WEST);
        meterRow.add(meterText, BorderLayout.EAST);
        meterRow.setVisible(false);

        JLabel keys = new JLabel("<html><b style='color:#1F2A44'>Enter</b> in Staff ID moves to Password<br>"
                + "<b style='color:#1F2A44'>Enter</b> in Password logs in</html>");
        keys.setFont(Theme.F_SMALL);
        keys.setForeground(Theme.TEXT_SECONDARY);

        card.add(left(header));
        card.add(left(lockedBanner));
        card.add(Box.createVerticalStrut(24));
        card.add(left(idLabel));
        card.add(Box.createVerticalStrut(6));
        card.add(left(staffIdField));
        card.add(Box.createVerticalStrut(6));
        card.add(left(idHint));
        card.add(Box.createVerticalStrut(16));
        card.add(left(passwordLabel));
        card.add(Box.createVerticalStrut(6));
        card.add(left(passwordField));
        card.add(Box.createVerticalStrut(6));
        card.add(left(messageLabel));
        card.add(Box.createVerticalStrut(24));
        card.add(left(loginButton));
        card.add(Box.createVerticalStrut(12));
        card.add(left(meterRow));

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        centre.add(card);
        add(centre, BorderLayout.CENTER);
        add(console, BorderLayout.SOUTH);

        // typing again clears the red ring and the message
        DocumentListener clear = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearErrorMarks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearErrorMarks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                clearErrorMarks();
            }
        };
        staffIdField.getDocument().addDocumentListener(clear);
        passwordField.getDocument().addDocumentListener(clear);
    }

    // ------------------------------------------------------------------ helpers
    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.F_LABEL);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void buildLockedBanner() {
        lockedBanner.setBackground(Theme.ERR_BG);
        lockedBanner.setBorder(new RoundedBorder(8, Theme.ERR_BG, new Insets(14, 14, 14, 14), false));
        JComponent disc = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 36, 36);
                Icons.paint(g2, Icons.Type.LOCK, 8, 8, 20, Theme.ERR_FG, 2f);
                g2.dispose();
            }
        };
        lockedText.setFont(Theme.F_BODY);
        lockedText.setForeground(Theme.ERR_DARK);
        lockedText.setText("<html><body style='width:200px'><b style='font-size:14px'>Sign in is locked</b><br>"
                + "There were 5 wrong attempts in this tab. Close this tab with the x above. "
                + "If you have forgotten your password, ask your manager.</body></html>");
        JPanel discHolder = new JPanel(new BorderLayout());
        discHolder.setOpaque(false);
        discHolder.add(disc, BorderLayout.NORTH);
        lockedText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lockedBanner.add(discHolder, BorderLayout.WEST);
        lockedBanner.add(lockedText, BorderLayout.CENTER);
    }

    private void clearErrorMarks() {
        staffIdField.putClientProperty("error", null);
        passwordField.putClientProperty("error", null);
        staffIdField.repaint();
        passwordField.repaint();
        messageLabel.setVisible(false);
    }

    // ------------------------------------------------------------------ what the controller uses
    // the Log in button or Enter in the password field logs in; Enter in the
    // staff ID field just moves on to the password field
    public void addController(ActionListener controller) {
        loginButton.addActionListener(controller);
        passwordField.addActionListener(controller);
        staffIdField.addActionListener(e -> passwordField.requestFocusInWindow());
    }

    public String getStaffId() {
        return staffIdField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /**
     * One or both boxes were left empty: ring the empty ones and say so.
     */
    public void showMissing(boolean idMissing, boolean passwordMissing, String message) {
        clearErrorMarks();
        if (idMissing) {
            staffIdField.putClientProperty("error", Boolean.TRUE);
        }
        if (passwordMissing) {
            passwordField.putClientProperty("error", Boolean.TRUE);
        }
        showMessage(message, message);
        (idMissing ? staffIdField : passwordField).requestFocusInWindow();
    }

    /**
     * Wrong Staff ID or password: ring the password and say so under it.
     */
    public void showWrongLogin(String inline, String consoleMessage) {
        passwordField.putClientProperty("error", Boolean.TRUE);
        passwordField.repaint();
        showMessage(inline, consoleMessage);
    }

    public void showError(String message) {
        showMessage(message, message);
    }

    private void showMessage(String inline, String consoleMessage) {
        messageLabel.setText(inline);
        messageLabel.setVisible(true);
        console.error(consoleMessage);
        revalidate();
        repaint();
    }

    /**
     * Shows how many of the allowed attempts are used up.
     */
    public void showAttempts(int used, int max) {
        meter.set(used, max);
        int left = max - used;
        meterText.setText(left > 0
                ? "<html><b style='color:#1F2A44'>" + left + (left == 1 ? " attempt" : " attempts") + " left</b> before this form locks</html>"
                : "<html><b style='color:#B42318'>0 attempts left</b></html>");
        meterRow.setVisible(true);
        revalidate();
    }

    public void clearPassword() {
        passwordField.setText("");
        passwordField.requestFocusInWindow();
    }

    public void focusStaffId() {
        staffIdField.requestFocusInWindow();
    }

    // used after too many failed attempts
    public void lockForm(String consoleMessage) {
        clearErrorMarks();
        header.setVisible(false);
        lockedBanner.setVisible(true);
        for (JLabel l : new JLabel[]{idLabel, passwordLabel}) {
            l.setForeground(Theme.TEXT_DISABLED);
        }
        idHint.setVisible(false);
        messageLabel.setVisible(false);
        for (JTextField f : new JTextField[]{staffIdField, passwordField}) {
            f.setEnabled(false);
            f.setBackground(Theme.BG_WINDOW);
            f.setBorder(new RoundedBorder(Theme.R_CONTROL, Theme.BORDER_CARD, new Insets(10, 12, 10, 12), false));
        }
        loginButton.setEnabled(false);
        loginButton.setIcon(Icons.icon(Icons.Type.LOCK, 16, Theme.TEXT_DISABLED));
        loginButton.setIconTextGap(8);
        console.error(consoleMessage);
        revalidate();
        repaint();
    }

    /**
     * Five small bars; the used ones are red.
     */
    private static class AttemptMeter extends JComponent {

        private int used;
        private int max = 5;

        void set(int used, int max) {
            this.used = used;
            this.max = max;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(max * 24 - 4, 6);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < max; i++) {
                g2.setColor(i < used ? Theme.ERR_FG : Theme.BORDER_DIVIDER);
                g2.fillRoundRect(i * 24, 0, 20, 6, 6, 6);
            }
            g2.dispose();
        }
    }
}
