package com.rfo.gui.controller;

import com.rfo.gui.view.SessionView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 * Handles actions inside a logged-in session tab. Sections add their own
 * controllers later.
 */
public class SessionController implements ActionListener {

    private final SessionView view;
    private final SessionListener listener;

    public SessionController(SessionView view, SessionListener listener) {
        this.view = view;
        this.listener = listener;
    }

    // the only button on the session itself is Log out
    @Override
    public void actionPerformed(ActionEvent e) {
        listener.onLogout(view);
    }
}
