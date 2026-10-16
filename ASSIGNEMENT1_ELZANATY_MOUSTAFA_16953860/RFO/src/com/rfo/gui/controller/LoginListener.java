package com.rfo.gui.controller;

import com.rfo.gui.view.LoginView;
import com.rfo.model.Staff;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 *
 * @author moustafaelzanaty
 */
public interface LoginListener {

    void onLoginSucceeded(LoginView view, Staff staff);

    public void onLoginLocked(LoginView view);

}
