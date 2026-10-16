package com.rfo.model;

/**
 * Common interface for the people in the system (staff and drivers).
 */
public interface Person {

    //Returns string with "PERSON" name.
    String getName();

    //Returns string with "PERSON" class-preferred contact info.
    String getContactInfo();
}
