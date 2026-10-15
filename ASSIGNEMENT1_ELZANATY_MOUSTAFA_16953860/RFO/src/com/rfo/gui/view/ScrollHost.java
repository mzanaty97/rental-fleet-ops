package com.rfo.gui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/**
 * Makes any screen or menu scrollable when the window is too short for it. The
 * content keeps filling the window as before; only when the window is shorter
 * than the content needs do a scroll bar appear and the mouse wheel start
 * working. Every screen and menu goes through this one class, so scrolling
 * behaves the same everywhere.
 */
public final class ScrollHost {

    private ScrollHost() {
    }

    /**
     * Wraps the content in a scroll pane.
     *
     * @param useMinimumHeight true for a screen that can shrink (for example
     * one with its own scrolling table or list): it only scrolls when even its
     * smallest size does not fit. False for a screen that needs its full
     * preferred height: it scrolls whenever the window is shorter than that.
     */
    public static Pane wrap(JComponent content, boolean useMinimumHeight) {
        return new Pane(content, useMinimumHeight);
    }

    /**
     * The scroll pane. Asking it for the keyboard focus gives the focus to the
     * content inside.
     */
    public static final class Pane extends JScrollPane {

        private final JComponent content;

        Pane(JComponent content, boolean useMinimumHeight) {
            super(new Host(content, useMinimumHeight), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
            this.content = content;
            setBorder(null);
            setOpaque(false);
            getViewport().setOpaque(false);
            getVerticalScrollBar().setUnitIncrement(24);
        }

        public JComponent getContent() {
            return content;
        }

        @Override
        public boolean requestFocusInWindow() {
            return content.requestFocusInWindow();
        }
    }

    private static final class Host extends JPanel implements Scrollable {

        private final JComponent content;
        private final boolean useMinimumHeight;

        Host(JComponent content, boolean useMinimumHeight) {
            super(new BorderLayout());
            setOpaque(false);
            this.content = content;
            this.useMinimumHeight = useMinimumHeight;
            add(content, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return 24;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(24, visible.height - 48);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        // true = the content is stretched to the window; false = it keeps its own height and the pane scrolls
        @Override
        public boolean getScrollableTracksViewportHeight() {
            if (!(getParent() instanceof JViewport)) {
                return true;
            }
            int needed = useMinimumHeight ? content.getMinimumSize().height : getPreferredSize().height;
            return getParent().getHeight() >= needed;
        }
    }
}
