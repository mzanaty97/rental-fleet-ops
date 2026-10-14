package com.rfo.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * A small warm yellow box at the bottom of a session where messages are
 * printed, the way the console version prints them. It keeps the most recent
 * lines and scrolls to the newest one.
 */
public class ConsoleView extends JPanel implements MessageSink {

    private static final int MAX_LINES = 100;

    private final JTextArea output = new JTextArea(3, 40);

    public ConsoleView() {
        super(new BorderLayout());
        Color warmYellow = new Color(0xFFF1A8);

        output.setEditable(false);
        output.setBackground(warmYellow);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        output.setMargin(new java.awt.Insets(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD9C25A)));
        scroll.getViewport().setBackground(warmYellow);
        scroll.setPreferredSize(new Dimension(100, 72));

        add(scroll, BorderLayout.CENTER);
    }

    @Override
    public void print(String message) {
        output.append(message + "\n");

        // keep only the latest lines
        int extra = output.getLineCount() - 1 - MAX_LINES;
        if (extra > 0) {
            try {
                output.replaceRange("", 0, output.getLineEndOffset(extra - 1));
            } catch (javax.swing.text.BadLocationException e) {
                output.setText("");
            }
        }
        SwingUtilities.invokeLater(() -> output.setCaretPosition(output.getDocument().getLength()));
    }
}
