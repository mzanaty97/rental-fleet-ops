package com.rfo.model;

/**
 * The life cycle of a movement: OPEN until it is checked in, INCOMPLETE after check-in,
 * CLOSED once it is finished.
 */
public enum MovementStatus {
    OPEN,
    INCOMPLETE,
    CLOSED
}
