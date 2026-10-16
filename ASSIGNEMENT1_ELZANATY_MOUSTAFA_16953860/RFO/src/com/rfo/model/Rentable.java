package com.rfo.model;

/**
 * Something that can be checked out for rental.
 */
public interface Rentable {

    //returns boolean checking car is available for CO
    boolean isAvailable();
}
