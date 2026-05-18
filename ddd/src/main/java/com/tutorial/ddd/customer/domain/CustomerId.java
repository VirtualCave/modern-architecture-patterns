package com.tutorial.ddd.customer.domain;

public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("customer id is required");
        }
    }

    public static CustomerId generate() {
        return new CustomerId(java.util.UUID.randomUUID().toString());
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }
}

