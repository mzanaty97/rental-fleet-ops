package com.rfo.model;

public enum Equipment {
    CHILD_SEAT(16.5),
    ADDITIONAL_DRIVER(10.0),
    SNOW_CHAINS(7.0);

    private double dailyRate;

    Equipment(double dailyRate) {
        this.dailyRate = dailyRate;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(double dailyRate) {
        this.dailyRate = dailyRate;
    }
}
