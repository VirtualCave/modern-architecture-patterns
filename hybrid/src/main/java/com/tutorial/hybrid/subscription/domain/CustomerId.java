package com.tutorial.hybrid.subscription.domain;

public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId no puede estar vacío");
        }
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }
}

