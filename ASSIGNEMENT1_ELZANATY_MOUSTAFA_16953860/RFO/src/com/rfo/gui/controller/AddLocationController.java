package com.rfo.gui.controller;

import com.rfo.gui.view.AddLocationView;
import com.rfo.gui.view.MessageSink;
import com.rfo.model.Address;
import com.rfo.model.Location;
import com.rfo.service.LocationService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Controller for the Add Location form. It builds the address from the form,
 * asks the service to add the location and prints the result in the console.
 */
public class AddLocationController implements ActionListener {

    private final AddLocationView view;
    private final LocationService locationService;
    private final MessageSink console;

    public AddLocationController(AddLocationView view, LocationService locationService, MessageSink console) {
        this.view = view;
        this.locationService = locationService;
        this.console = console;
        view.addController(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Address address = new Address(view.getStreet().trim(), view.getCity().trim(),
                view.getCountry(), view.getPostalCode().trim());

        try {
            Location location = locationService.addLocation(view.getLocationID(), view.getLocationName(), address);
            console.print("Location added successfully: " + location.getLocationID());
            view.clearForm();
        } catch (IllegalArgumentException ex) {
            console.print(ex.getMessage());
        }
    }
}
