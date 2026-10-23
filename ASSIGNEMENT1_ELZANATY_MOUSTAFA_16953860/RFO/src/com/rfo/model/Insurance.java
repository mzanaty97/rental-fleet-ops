package com.rfo.model;

public enum Insurance {
    BASIC(0.0),
    MEDIUM(0.8),
    PREMIUM(1.0);

    private final double rate;

    Insurance(double rate) {
        this.rate = rate;
    }

    public double getRate() {
        return rate;
    }
}
