package com.rfo.gui.view;

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
