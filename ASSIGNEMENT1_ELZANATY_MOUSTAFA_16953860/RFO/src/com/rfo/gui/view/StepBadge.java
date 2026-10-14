package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

/**
 * The small round badge before a section title: a number, a green tick when the
 * section is fine, a red "!" when it is not.
 */
public class StepBadge extends JComponent {

    public enum State {
        NUMBER, OK, BAD
    }

    private final String number;
    private State state = State.NUMBER;

    public StepBadge(String number) {
        this.number = number;
        setOpaque(false);
    }

    public void setState(State state) {
        this.state = state;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(24, 24);
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
        g2.setColor(state == State.OK ? Theme.OK_FG : state == State.BAD ? Theme.ERR_FG : Theme.NAVY_900);
        g2.fillOval(0, 0, 23, 23);
        g2.setColor(Theme.TEXT_ON_PRIMARY);
        if (state == State.OK) {
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(7, 12, 10, 15);
            g2.drawLine(10, 15, 16, 8);
        } else {
            g2.setFont(Theme.F_LABEL);
            FontMetrics fm = g2.getFontMetrics();
            String text = state == State.BAD ? "!" : number;
            g2.drawString(text, (24 - fm.stringWidth(text)) / 2, (24 + fm.getAscent() - fm.getDescent()) / 2);
        }
        g2.dispose();
    }
}
