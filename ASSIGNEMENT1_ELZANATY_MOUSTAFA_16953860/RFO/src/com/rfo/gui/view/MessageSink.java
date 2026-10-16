package com.rfo.gui.view;

// Generated with assistance from Claude, at my direction
// (I specified the requirements, layout and behaviour, and I reviewed and tested the code).
/**
 *
 * @author moustafaelzanaty
 */
public interface MessageSink {

    void print(String message);

    default void ok(String message) {
        print(message);
    }

    default void error(String message) {
        print(message);
    }

    default void warn(String message) {
        print(message);
    }

    default void info(String message) {
        print(message);
    }
}
