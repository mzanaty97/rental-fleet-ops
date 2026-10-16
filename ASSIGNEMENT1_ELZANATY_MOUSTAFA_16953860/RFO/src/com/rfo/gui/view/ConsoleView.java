package com.rfo.gui.view;

import com.rfo.gui.theme.Icons;
import com.rfo.gui.theme.Theme;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * The message console at the bottom of a session. At rest it is one slim line
 * showing the newest message, with an icon and a word (OK, ERROR, WARNING,
 * INFO) so the meaning never depends on colour. A new success or warning tints
 * the bar for 4 seconds; an error stays tinted until the next key press or
 * click elsewhere. Click the "N messages" button, or press Ctrl+M, to open the
 * last 100 messages and click Hide (or Ctrl+M) to close it again.
 */
public class ConsoleView extends JPanel implements MessageSink {

    private enum Kind {
        OK("OK", Theme.OK_FG, Theme.OK_BG, Theme.OK_LINE, Theme.OK_DARK, Icons.Type.CHECK_CIRCLE),
        ERROR("ERROR", Theme.ERR_FG, Theme.ERR_BG, Theme.ERR_LINE, Theme.ERR_DARK, Icons.Type.ERROR_CIRCLE),
        WARNING("WARNING", Theme.WARN_FG, Theme.WARN_BG, Theme.WARN_LINE, Theme.WARN_DARK, Icons.Type.WARNING),
        INFO("INFO", Theme.INFO_FG, Theme.INFO_BG, Theme.INFO_LINE, Theme.INFO_DARK, Icons.Type.INFO);

        final String tag;
        final Color fg, bg, line, dark;
        final Icons.Type icon;

        Kind(String tag, Color fg, Color bg, Color line, Color dark, Icons.Type icon) {
            this.tag = tag;
            this.fg = fg;
            this.bg = bg;
            this.line = line;
            this.dark = dark;
            this.icon = icon;
        }
    }

    private static final class Line {

        final Kind kind;
        final String text;
        final String time;

        Line(Kind kind, String text) {
            this.kind = kind;
            this.text = text;
            this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    private static final int MAX_LINES = 100;
    private static final int SETTLE_MS = 4000;

    private final DefaultListModel<Line> lines = new DefaultListModel<>();
    private final CardLayout cards = new CardLayout();
    private final Bar bar = new Bar();
    private final History history = new History();
    private boolean expanded = false;
    private boolean tinted = false;
    private final Timer settle = new Timer(SETTLE_MS, e -> setTinted(false));

    public ConsoleView() {
        super();
        setLayout(cards);
        settle.setRepeats(false);
        add(bar, "bar");
        add(history, "history");
        cards.show(this, "bar");

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "toggleConsole");
        getActionMap().put("toggleConsole", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setExpanded(!expanded);
            }
        });
    }

    // ------------------------------------------------------------------ MessageSink
    @Override
    public void print(String message) {
        addLine(classify(message), message);
    }

    @Override
    public void ok(String message) {
        addLine(Kind.OK, message);
    }

    @Override
    public void error(String message) {
        addLine(Kind.ERROR, message);
    }

    @Override
    public void warn(String message) {
        addLine(Kind.WARNING, message);
    }

    @Override
    public void info(String message) {
        addLine(Kind.INFO, message);
    }

    /**
     * Decides the kind of a plain message from its wording (the messages are
     * the same ones the CUI prints).
     */
    private static Kind classify(String message) {
        String m = message.toLowerCase();
        if (m.startsWith("select ") || m.contains("no change made")) {
            return Kind.WARNING;
        }
        for (String word : new String[]{"must", "not found", "duplicate", "valid", "cancel", "already", "cannot",
            "incorrect", "required", "do not match", "not available", "failed", "locked", "too many"}) {
            if (m.contains(word)) {
                return Kind.ERROR;
            }
        }
        for (String word : new String[]{" is now ", "successfully", "closed:", "added", "checked in", "started"}) {
            if (m.contains(word)) {
                return Kind.OK;
            }
        }
        return Kind.INFO;
    }

    private void addLine(Kind kind, String message) {
        lines.addElement(new Line(kind, message));
        while (lines.size() > MAX_LINES) {
            lines.removeElementAt(0);
        }
        history.scrollToNewest();
        setTinted(kind != Kind.INFO);
        if (kind == Kind.OK || kind == Kind.WARNING) {
            settle.restart();          // errors do not settle by themselves
        } else {
            settle.stop();
        }
        bar.refresh();
        history.refresh();
    }

    private void setTinted(boolean value) {
        tinted = value;
        bar.refresh();
    }

    private Line newest() {
        return lines.isEmpty() ? null : lines.lastElement();
    }

    private void setExpanded(boolean value) {
        expanded = value;
        cards.show(this, value ? "history" : "bar");
        history.scrollToNewest();
        revalidate();
        Component parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, expanded ? Theme.CONSOLE_EXPANDED_H : Theme.CONSOLE_H);
    }

    // an error stays tinted until the user does something else (a key press or a click outside the console)
    private final AWTEventListener anyInput = event -> {
        boolean press = (event instanceof KeyEvent && event.getID() == KeyEvent.KEY_PRESSED)
                || (event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_PRESSED);
        if (press && tinted && newest() != null && newest().kind == Kind.ERROR
                && event.getSource() instanceof Component
                && !SwingUtilities.isDescendingFrom((Component) event.getSource(), this)) {
            setTinted(false);
        }
    };

    @Override
    public void addNotify() {
        super.addNotify();
        Toolkit.getDefaultToolkit().addAWTEventListener(anyInput, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    @Override
    public void removeNotify() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(anyInput);
        settle.stop();
        super.removeNotify();
    }

    // ------------------------------------------------------------------ the slim bar
    private class Bar extends JPanel {

        private final StatusIcon icon = new StatusIcon(16);
        private final JLabel tag = new JLabel();
        private final JLabel message = new JLabel("No messages yet");
        private final JLabel time = new JLabel();
        private final JButton count = new JButton();

        Bar() {
            super(new BorderLayout(10, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 12));

            tag.setFont(Theme.F_MONO.deriveFont(java.awt.Font.BOLD, 12f));
            tag.setPreferredSize(new Dimension(64, 20));
            message.setFont(Theme.F_MONO);
            time.setFont(Theme.F_MONO.deriveFont(12f));
            time.setForeground(Theme.TEXT_SECONDARY);

            count.putClientProperty("variant", "quiet");
            count.putClientProperty("size", "compact");
            count.setFont(Theme.F_LABEL.deriveFont(12f));
            count.setIcon(Icons.icon(Icons.Type.CHEVRON_UP, 12, Theme.TEXT_PRIMARY));
            count.setHorizontalTextPosition(JButton.LEFT);
            count.setIconTextGap(6);
            count.setToolTipText("Show the message history (Ctrl+M)");
            count.addActionListener(e -> setExpanded(true));

            JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            west.setOpaque(false);
            west.add(icon);
            west.add(tag);
            JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            east.setOpaque(false);
            east.add(time);
            east.add(count);

            add(west, BorderLayout.WEST);
            add(message, BorderLayout.CENTER);
            add(east, BorderLayout.EAST);
            refresh();
        }

        void refresh() {
            Line line = newest();
            icon.setKind(line == null ? null : line.kind);
            if (line == null) {
                tag.setText("");
                message.setText("No messages yet");
                message.setForeground(Theme.TEXT_SECONDARY);
                time.setText("");
                count.setVisible(false);
            } else {
                tag.setText(line.kind.tag);
                tag.setForeground(line.kind.fg);
                message.setText(line.text);
                message.setToolTipText(line.text);
                message.setForeground(tinted ? line.kind.dark : Theme.TEXT_SECONDARY);
                time.setText(line.time);
                count.setText(lines.size() == 1 ? "1 message" : lines.size() + " messages");
                count.setVisible(true);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Line line = newest();
            boolean tint = tinted && line != null;
            g.setColor(tint ? line.kind.bg : Theme.BG_SURFACE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(tint ? line.kind.line : Theme.BORDER_CARD);
            g.fillRect(0, 0, getWidth(), 1);
        }
    }

    /**
     * The round icon in front of a message (a grey terminal when there are no
     * messages yet).
     */
    private static class StatusIcon extends JComponent {

        private final int size;
        private Kind kind;

        StatusIcon(int size) {
            this.size = size;
            setOpaque(false);
        }

        void setKind(Kind kind) {
            this.kind = kind;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(size, size);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (kind == null) {
                Icons.paint((Graphics2D) g, Icons.Type.TERMINAL, 0, (getHeight() - size) / 2, size, Theme.TEXT_DISABLED, 2f);
            } else {
                Icons.paint((Graphics2D) g, kind.icon, 0, (getHeight() - size) / 2, size, kind.fg, 2.2f);
            }
        }
    }

    // ------------------------------------------------------------------ the history
    private class History extends JPanel {

        private final JLabel title = new JLabel();
        private final JList<Line> list = new JList<>(lines);
        private final JScrollPane scroll = new JScrollPane(list);

        History() {
            super(new BorderLayout());
            setOpaque(true);
            setBackground(Theme.BG_SURFACE);
            setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_CARD));

            title.setFont(Theme.F_LABEL);
            JButton hide = new JButton("Hide");
            hide.putClientProperty("variant", "quiet");
            hide.putClientProperty("size", "compact");
            hide.setFont(Theme.F_LABEL.deriveFont(12f));
            hide.setIcon(Icons.icon(Icons.Type.CHEVRON_DOWN, 12, Theme.TEXT_PRIMARY));
            hide.setHorizontalTextPosition(JButton.LEFT);
            hide.setIconTextGap(6);
            hide.addActionListener(e -> setExpanded(false));

            JPanel head = new JPanel(new BorderLayout());
            head.setOpaque(false);
            head.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                    BorderFactory.createEmptyBorder(0, 32, 0, 12)));
            head.setPreferredSize(new Dimension(10, 32));
            head.add(title, BorderLayout.CENTER);
            head.add(hide, BorderLayout.EAST);

            list.setCellRenderer(new LineRenderer());
            list.setFixedCellHeight(22);
            list.setFocusable(false);
            list.setSelectionModel(new javax.swing.DefaultListSelectionModel() {
                @Override
                public void setSelectionInterval(int a, int b) {
                }
            });
            scroll.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

            add(head, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            refresh();
        }

        void refresh() {
            title.setText("<html>Messages <span style='color:#4F5D75;font-weight:normal'>· last 100 kept · "
                    + lines.size() + " now</span></html>");
        }

        void scrollToNewest() {
            SwingUtilities.invokeLater(() -> {
                if (!lines.isEmpty()) {
                    list.ensureIndexIsVisible(lines.size() - 1);
                }
            });
        }
    }

    /**
     * One history row: time, icon, word, text. Drawn directly, 22 px tall.
     */
    private static class LineRenderer extends JComponent implements ListCellRenderer<Line> {

        private Line line;

        @Override
        public Component getListCellRendererComponent(JList<? extends Line> list, Line value, int index,
                boolean selected, boolean focus) {
            this.line = value;
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(100, 22);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (line == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            FontMetrics fm;
            int base;
            g2.setFont(Theme.F_MONO.deriveFont(12f));
            fm = g2.getFontMetrics();
            base = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(Theme.TEXT_SECONDARY);
            g2.drawString(line.time, 32, base);

            Icons.paint(g2, line.kind.icon, 32 + 44 + 10, (getHeight() - 14) / 2, 14, line.kind.fg, 2.2f);
            g2.setFont(Theme.F_MONO.deriveFont(java.awt.Font.BOLD, 12f));
            g2.setColor(line.kind.fg);
            g2.drawString(line.kind.tag, 32 + 44 + 10 + 14 + 10, base);

            g2.setFont(Theme.F_MONO);
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(line.text, 32 + 44 + 10 + 14 + 10 + 64 + 10, base);
            g2.dispose();
        }
    }
}
