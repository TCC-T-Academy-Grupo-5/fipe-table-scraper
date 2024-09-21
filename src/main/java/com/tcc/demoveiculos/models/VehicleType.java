package com.tcc.demoveiculos.models;

public enum VehicleType {
    CAR("cars"),
    MOTORCYCLE("motorcycles"),
    TRUCK("trucks");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
