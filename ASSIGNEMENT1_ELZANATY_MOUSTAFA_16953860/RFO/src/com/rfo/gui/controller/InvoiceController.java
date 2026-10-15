package com.rfo.gui.controller;

import com.rfo.gui.view.InvoiceView;
import com.rfo.gui.view.MessageSink;
import com.rfo.io.InvoicePDFWriter;
import com.rfo.model.Invoice;
import com.rfo.service.InvoiceService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Print Invoice. Finds the saved invoice with the
 * InvoiceService, gives the view the text to show, and writes the PDF when the
 * user presses Print.
 */
public class InvoiceController implements ActionListener {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final InvoiceView view;
    private final InvoiceService invoiceService;
    private final MessageSink console;
    private Invoice shown;

    public InvoiceController(InvoiceView view, InvoiceService invoiceService, MessageSink console) {
        this.view = view;
        this.invoiceService = invoiceService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (InvoiceView.SAVE_PDF.equals(e.getActionCommand())) {
            savePdf();
        } else {
            find();
        }
    }

    private void find() {
        try {
            Invoice inv = invoiceService.findInvoice(view.getText());
            shown = inv;
            String[][] details = {
                {"Vehicle", inv.getVehicle() + " (" + inv.getPlate() + ")"},
                {"Driver(s)", inv.getDrivers()},
                {"Picked up", inv.getCheckOut()},
                {"Returned", inv.getCheckIn()},
                {"Odometer", String.format("%,d km out, %,d km in (%,d km driven)", inv.getOdoOut(), inv.getOdoIn(),
                    inv.getOdoIn() - inv.getOdoOut())}
            };
            String[][] charges = {
                {"Vehicle hire (" + inv.getDays() + (inv.getDays() == 1 ? " day)" : " days)"), InvoicePDFWriter.money(inv.getBase())},
                {"Insurance (" + inv.getInsuranceTier() + ")", InvoicePDFWriter.money(inv.getInsurance())},
                {"Roadside assistance", InvoicePDFWriter.money(inv.getRsa())},
                {"Equipment", InvoicePDFWriter.money(inv.getEquipment())}
            };
            view.showInvoice(inv.getNumber(), "Rental " + inv.getMovementID() + " · issued " + inv.getIssuedAt().format(DATE),
                    details, charges, InvoicePDFWriter.money(inv.getTotal()));
            console.ok("Showing invoice " + inv.getNumber() + " for rental " + inv.getMovementID() + ", total "
                    + InvoicePDFWriter.money(inv.getTotal()) + ".");
        } catch (IllegalArgumentException ex) {
            shown = null;
            view.showError(ex.getMessage());
            console.error(ex.getMessage());
        }
    }

    private void savePdf() {
        if (shown == null) {
            console.error("Find an invoice first.");
            return;
        }
        File file = view.askSavePath(shown.getNumber() + "-" + shown.getPlate() + ".pdf");
        if (file == null) {
            console.info("Save cancelled.");
            return;
        }
        try {
            invoiceService.writePdf(shown, file);
            console.ok("Invoice " + shown.getNumber() + " saved as " + file.getAbsolutePath());
            view.openFile(file);
        } catch (IllegalArgumentException ex) {
            console.error(ex.getMessage());
        }
    }
}
