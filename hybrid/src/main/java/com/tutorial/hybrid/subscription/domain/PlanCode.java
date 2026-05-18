package com.tutorial.hybrid.subscription.domain;

public record PlanCode(String value) {

    public PlanCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("PlanCode no puede estar vacío");
        }
    }

    public static PlanCode of(String value) {
        return new PlanCode(value);
    }
}

