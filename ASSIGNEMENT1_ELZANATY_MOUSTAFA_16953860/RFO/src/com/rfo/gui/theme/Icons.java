package com.rfo.gui.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.Icon;

/**
 * Small line icons drawn with Java2D, so the app needs no image files. Every
 * icon is designed on a 20 x 20 grid and scaled to the size you ask for.
 */
public final class Icons {

    private Icons() {
    }

    public enum Type {
        HOME, CLOSE, PIN, CHEVRON_RIGHT, CHEVRON_LEFT, CHEVRON_UP, CHEVRON_DOWN,
        CHECK_CIRCLE, ERROR_CIRCLE, WARNING, INFO, TERMINAL, LOGOUT, DASHED_CIRCLE, CLOCK, PLUS, LOCK
    }

    /**
     * Draws an icon with its top-left corner at (x, y). {@code stroke} is in
     * 20-grid units (2 is normal).
     */
    public static void paint(Graphics2D g, Type type, int x, int y, int size, Color color, float stroke) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.translate(x, y);
        double scale = size / 20.0;
        g2.scale(scale, scale);
        g2.setColor(color);
        if (type == Type.DASHED_CIRCLE) {
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{2.2f, 2.2f}, 0f));
        } else {
            g2.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        for (Shape s : shapes(type)) {
            g2.draw(s);
        }
        g2.dispose();
    }

    /**
     * An Icon object, for JButton.setIcon and similar.
     */
    public static Icon icon(Type type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                paint((Graphics2D) g, type, x, y, size, color, 2f);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static Shape[] shapes(Type type) {
        Shape circle = new Ellipse2D.Float(2, 2, 16, 16);
        switch (type) {
            case HOME:
                return new Shape[]{path("M3 9 L10 3 L17 9 L17 17 L3 17 Z")};
            case CLOSE:
                return new Shape[]{path("M5 5 L15 15 M15 5 L5 15")};
            case CHEVRON_RIGHT:
                return new Shape[]{path("M8 5 L13 10 L8 15")};
            case CHEVRON_LEFT:
                return new Shape[]{path("M12 5 L7 10 L12 15")};
            case CHEVRON_UP:
                return new Shape[]{path("M5 12 L10 7 L15 12")};
            case CHEVRON_DOWN:
                return new Shape[]{path("M5 8 L10 13 L15 8")};
            case CHECK_CIRCLE:
                return new Shape[]{circle, path("M6.5 10.5 L9 13 L13.5 8")};
            case ERROR_CIRCLE:
                return new Shape[]{circle, path("M7 7 L13 13 M13 7 L7 13")};
            case WARNING:
                return new Shape[]{path("M10 3 L18 17 L2 17 Z"), path("M10 8 L10 12 M10 14.5 L10 15")};
            case INFO:
                return new Shape[]{circle, path("M10 9 L10 14 M10 6 L10 6.5")};
            case TERMINAL:
                return new Shape[]{new RoundRectangle2D.Float(2.5f, 4, 15, 12, 4, 4),
                    path("M6 8 L8.5 10 L6 12 M10.5 12 L14 12")};
            case LOGOUT:
                return new Shape[]{path("M8 4 L4 4 L4 16 L8 16 M13 6 L17 10 L13 14 M17 10 L8 10")};
            case DASHED_CIRCLE:
                return new Shape[]{new Ellipse2D.Float(3, 3, 14, 14)};
            case CLOCK:
                return new Shape[]{new Ellipse2D.Float(3, 3, 14, 14), path("M10 6.5 L10 10 L12.5 12")};
            case PLUS:
                return new Shape[]{path("M10 4 L10 16 M4 10 L16 10")};
            case LOCK: {
                Path2D.Float shackle = new Path2D.Float();
                shackle.moveTo(7, 9);
                shackle.lineTo(7, 6);
                shackle.curveTo(7, 1.5, 13, 1.5, 13, 6);
                shackle.lineTo(13, 9);
                return new Shape[]{new RoundRectangle2D.Float(4, 9, 12, 9, 4, 4), shackle};
            }
            case PIN:
                Path2D pin = new Path2D.Float();
                pin.moveTo(10, 18);
                pin.curveTo(10, 18, 16, 12.5, 16, 8);
                pin.append(new Arc2D.Float(4, 2, 12, 12, 0, 180, Arc2D.OPEN), true);
                pin.curveTo(4, 12.5, 10, 18, 10, 18);
                pin.closePath();
                return new Shape[]{pin, new Ellipse2D.Float(8, 6, 4, 4)};
            default:
                return new Shape[0];
        }
    }

    /**
     * Tiny path reader: absolute M, L and Z commands only, which is all these
     * icons need.
     */
    private static Path2D path(String d) {
        Path2D.Float p = new Path2D.Float();
        String[] t = d.replaceAll("([MLZ])", " $1 ").trim().split("\\s+");
        int i = 0;
        while (i < t.length) {
            switch (t[i]) {
                case "M":
                    p.moveTo(Float.parseFloat(t[i + 1]), Float.parseFloat(t[i + 2]));
                    i += 3;
                    break;
                case "L":
                    p.lineTo(Float.parseFloat(t[i + 1]), Float.parseFloat(t[i + 2]));
                    i += 3;
                    break;
                case "Z":
                    p.closePath();
                    i += 1;
                    break;
                default:
                    throw new IllegalArgumentException("Bad icon path: " + d);
            }
        }
        return p;
    }
}
