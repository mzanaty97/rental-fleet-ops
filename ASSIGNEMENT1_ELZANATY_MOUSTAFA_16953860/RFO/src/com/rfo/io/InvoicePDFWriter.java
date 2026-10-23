package com.rfo.io;

import com.rfo.model.Invoice;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Writes one invoice as a one-page A4 PDF. No library is used: a PDF is a text
 * file made of numbered objects (the catalog, the page, two fonts and the
 * page's drawing commands) followed by a table that says where each object
 * starts (the "xref" table). The built-in Helvetica fonts are used, so nothing
 * has to be embedded. Positions are in points (1/72 inch) measured from the
 * bottom-left of the page.
 */
public class InvoicePDFWriter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final int PAGE_W = 595;
    private static final int PAGE_H = 842;
    private static final int LEFT = 48;
    private static final int RIGHT = PAGE_W - 48;

    // the drawing commands for the page, built up line by line
    private final StringBuilder page = new StringBuilder();

    /**
     * Writes the invoice to the file. Throws IOException if the file cannot be
     * written.
     */
    public void write(Invoice inv, File file) throws IOException {
        Files.write(file.toPath(), build(inv));
    }

    /**
     * The whole PDF file as bytes (kept separate from write so it can be tested
     * without a file).
     */
    public byte[] build(Invoice inv) {
        page.setLength(0);
        draw(inv);

        // object 1 catalog, 2 pages, 3 page, 4 regular font, 5 bold font, 6 page contents
        List<String> objects = new ArrayList<>();
        byte[] content = page.toString().getBytes(StandardCharsets.ISO_8859_1);
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_W + " " + PAGE_H + "] "
                + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        put(out, "%PDF-1.4\n");
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            put(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        offsets.add(out.size());
        put(out, "6 0 obj\n<< /Length " + content.length + " >>\nstream\n");
        out.writeBytes(content);
        put(out, "\nendstream\nendobj\n");

        int xref = out.size();
        put(out, "xref\n0 " + (offsets.size() + 1) + "\n0000000000 65535 f \n");
        for (int off : offsets) {
            put(out, String.format("%010d 00000 n \n", off));
        }
        put(out, "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    // ---- the invoice layout ----
    private void draw(Invoice inv) {
        // header band
        fillRect(0, PAGE_H - 84, PAGE_W, 84, 0.06, 0.13, 0.24);
        text(LEFT, PAGE_H - 50, 22, true, "RentalFleetOps", 1, 1, 1);
        textRight(RIGHT, PAGE_H - 50, 22, true, "INVOICE", 1, 1, 1);

        int y = PAGE_H - 126;
        field(LEFT, y, "Invoice number", inv.getNumber());
        field(230, y, "Date issued", inv.getIssuedAt().format(DATE));
        field(390, y, "Rental ID", inv.getMovementID());

        y -= 56;
        rule(y + 24);
        field(LEFT, y, "Vehicle", inv.getVehicle() + "  (" + inv.getPlate() + ")");
        y -= 44;
        field(LEFT, y, "Driver(s)", inv.getDrivers());
        y -= 44;
        field(LEFT, y, "Picked up", inv.getCheckOut());
        y -= 44;
        field(LEFT, y, "Returned", inv.getCheckIn());
        y -= 44;
        field(LEFT, y, "Odometer", String.format("%,d km out, %,d km in (%,d km driven)",
                inv.getOdoOut(), inv.getOdoIn(), inv.getOdoIn() - inv.getOdoOut()));
        y -= 36;
        rule(y + 10);

        // charges table
        y -= 20;
        text(LEFT, y, 10, true, "Charges", 0.06, 0.13, 0.24);
        textRight(RIGHT, y, 10, true, "Amount", 0.06, 0.13, 0.24);
        y -= 10;
        rule(y);
        y -= 22;
        y = line(y, "Vehicle hire (" + inv.getDays() + (inv.getDays() == 1 ? " day)" : " days)"), inv.getBase());
        y = line(y, "Insurance (" + inv.getInsuranceTier() + ")", inv.getInsurance());
        y = line(y, "Roadside assistance", inv.getRsa());
        y = line(y, "Equipment", inv.getEquipment());
        y -= 4;
        rule(y + 14);
        y -= 12;
        text(LEFT, y, 14, true, "Total", 0.06, 0.13, 0.24);
        textRight(RIGHT, y, 14, true, money(inv.getTotal()), 0.06, 0.13, 0.24);

        // footer
        rule(90);
        text(LEFT, 70, 9, false, "Checked out by " + inv.getCoStaff() + "  |  Checked in by " + inv.getCiStaff(), 0.4, 0.4, 0.4);
        text(LEFT, 56, 9, false, "Thank you for renting with RentalFleetOps.", 0.4, 0.4, 0.4);
    }

    private int line(int y, String label, double amount) {
        text(LEFT, y, 11, false, label, 0.1, 0.1, 0.1);
        textRight(RIGHT, y, 11, false, money(amount), 0.1, 0.1, 0.1);
        return y - 22;
    }

    private void field(int x, int y, String label, String value) {
        text(x, y + 14, 8.5f, false, label.toUpperCase(), 0.45, 0.45, 0.45);
        text(x, y, 11, true, value, 0.1, 0.1, 0.1);
    }

    private void rule(int y) {
        page.append("0.85 0.85 0.85 RG 0.8 w ").append(LEFT).append(' ').append(y).append(" m ")
                .append(RIGHT).append(' ').append(y).append(" l S\n");
    }

    // ---- low level drawing commands ----
    private void fillRect(int x, int y, int w, int h, double r, double g, double b) {
        page.append(r).append(' ').append(g).append(' ').append(b).append(" rg ")
                .append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h).append(" re f\n");
    }

    private void text(int x, int y, float size, boolean bold, String s, double r, double g, double b) {
        page.append("BT ").append(r).append(' ').append(g).append(' ').append(b).append(" rg /")
                .append(bold ? "F2 " : "F1 ").append(size).append(" Tf ").append(x).append(' ').append(y)
                .append(" Td (").append(escape(s)).append(") Tj ET\n");
    }

    // right-aligned text needs the width of the text, worked out from the Helvetica character widths
    private void textRight(int xRight, int y, float size, boolean bold, String s, double r, double g, double b) {
        text(xRight - Math.round(width(s, size, bold)), y, size, bold, s, r, g, b);
    }

    private static float width(String s, float size, boolean bold) {
        float units = 0;
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                units += 556;
            } else if (c == '.' || c == ',' || c == ' ') {
                units += 278;
            } else if (c == '$') {
                units += 556;
            } else if (c == 'I') {
                units += 278;
            } else if (c == 'N' || c == 'V' || c == 'O' || c == 'C' || c == 'E') {
                units += bold ? 722 : 667;
            } else {
                units += bold ? 611 : 556;
            }
        }
        return units * size / 1000f;
    }

    /**
     * Brackets and backslashes have a meaning inside a PDF string, and only
     * plain characters are kept.
     */
    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : (s == null ? "" : s).toCharArray()) {
            if (c == '(' || c == ')' || c == '\\') {
                out.append('\\').append(c);
            } else if (c >= 32 && c < 127) {
                out.append(c);
            } else if (c == '·' || c == '•') {
                out.append('-');
            } else {
                out.append('?');
            }
        }
        return out.toString();
    }

    public static String money(double amount) {
        return String.format("$%,.2f", amount);
    }

    private static void put(ByteArrayOutputStream out, String s) {
        out.writeBytes(s.getBytes(StandardCharsets.ISO_8859_1));
    }
}
