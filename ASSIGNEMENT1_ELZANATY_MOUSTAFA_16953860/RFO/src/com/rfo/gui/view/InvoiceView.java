package com.rfo.gui.view;

import com.rfo.gui.theme.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * The Print Invoice screen: type a movement ID or plate, press Find invoice,
 * and the saved invoice shows as a card. The Print button asks where to put the
 * file; the controller writes it. The view only shows what the controller gives
 * it.
 */
public class InvoiceView extends JPanel implements PlainScreen {

    public static final String FIND = "findInvoice";
    public static final String SAVE_PDF = "savePdf";

    private final LookupBar lookup = new LookupBar("Movement ID or plate number", 290, "Find invoice", FIND,
            "Closed rentals only.");
    private final FormCard card = new FormCard(640, 0);
    private final JLabel numberLabel = new JLabel();
    private final JLabel subLabel = new JLabel();
    private final JPanel body = new JPanel();
    private final JButton pdfButton = new JButton("Print");

    public InvoiceView() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FormCard form = new FormCard(640, 24);
        lookup.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lookup);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(form);
        add(Box.createVerticalStrut(16));

        // header of the invoice card: invoice number on the left, the Print button on the right
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_CARD),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
        JPanel names = new JPanel();
        names.setOpaque(false);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        numberLabel.setFont(Theme.F_HEADING);
        numberLabel.setForeground(Theme.TEXT_PRIMARY);
        subLabel.setFont(Theme.F_SMALL);
        subLabel.setForeground(Theme.TEXT_SECONDARY);
        names.add(numberLabel);
        names.add(subLabel);
        pdfButton.putClientProperty("variant", "primary");
        pdfButton.setActionCommand(SAVE_PDF);
        JPanel buttonBox = new JPanel(new java.awt.GridBagLayout());
        buttonBox.setOpaque(false);
        buttonBox.add(pdfButton);
        head.add(names, BorderLayout.CENTER);
        head.add(buttonBox, BorderLayout.EAST);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(head);

        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(4, 24, 12, 24));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(body);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setVisible(false);
        add(card);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                SwingUtilities.invokeLater(lookup::focusBox);
            }
        });
    }

    public String getText() {
        return lookup.getText();
    }

    public void showError(String message) {
        card.setVisible(false);
        lookup.showError(message);
    }

    /**
     * Shows one invoice. details are label/value pairs, charges are
     * label/amount pairs, and every value is already text.
     */
    public void showInvoice(String number, String subtitle, String[][] details, String[][] charges, String total) {
        lookup.clearError();
        numberLabel.setText(number);
        subLabel.setText(subtitle);
        body.removeAll();
        for (int i = 0; i < details.length; i++) {
            body.add(detailRow(details[i][0], details[i][1], true));
        }
        body.add(Box.createVerticalStrut(10));
        for (String[] c : charges) {
            body.add(chargeRow(c[0], c[1], false));
        }
        body.add(chargeRow("Total", total, true));
        card.setVisible(true);
        revalidate();
        repaint();
    }

    // a label with its value under it, with a divider line
    private JPanel detailRow(String label, String value, boolean divider) {
        JPanel row = fullWidthRow();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DIVIDER),
                BorderFactory.createEmptyBorder(7, 0, 7, 0)));
        JLabel k = new JLabel(label);
        k.setFont(Theme.F_SMALL);
        k.setForeground(Theme.TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(Theme.F_BODY_B);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(k);
        row.add(v);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    // a charge: name on the left, amount on the right
    private JPanel chargeRow(String label, String amount, boolean bold) {
        JPanel row = fullWidthRow();
        row.setLayout(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        JLabel k = new JLabel(label);
        k.setFont(bold ? Theme.F_SECTION : Theme.F_BODY);
        k.setForeground(Theme.TEXT_PRIMARY);
        JLabel v = new JLabel(amount);
        v.setFont(bold ? Theme.F_SECTION : Theme.F_BODY);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    // takes the card's full width and only the height it needs
    private JPanel fullWidthRow() {
        JPanel row = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        row.setOpaque(false);
        return row;
    }

    /**
     * Asks where to save the PDF. Returns null if the user cancels or does not
     * want to replace a file.
     */
    public File askSavePath(String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save invoice as PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), suggestedName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        if (file.exists() && JOptionPane.showConfirmDialog(this, file.getName() + " already exists. Replace it?",
                "Replace file", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return null;
        }
        return file;
    }

    /**
     * Opens the saved PDF in the computer's PDF viewer, if there is one.
     * Quietly does nothing if not.
     */
    public void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {
            // the file is saved either way
        }
    }

    public void addController(ActionListener controller) {
        lookup.addController(controller);
        pdfButton.addActionListener(controller);
    }
}
