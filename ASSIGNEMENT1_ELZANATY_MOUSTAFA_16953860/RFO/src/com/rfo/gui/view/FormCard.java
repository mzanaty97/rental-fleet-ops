package com.rfo.gui.view;

import com.rfo.gui.theme.RoundedBorder;
import com.rfo.gui.theme.Theme;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * A white rounded card with a fixed width, used for forms and detail panels.
 * Children stack downwards.
 */
public class FormCard extends JPanel {

    private final int width;

    public FormCard(int width, int padding) {
        this.width = width;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG_SURFACE);
        setBorder(new RoundedBorder(Theme.R_CARD, Theme.BORDER_CARD, new Insets(padding, padding, padding, padding), false));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(width, super.getMaximumSize().height);
    }
}
