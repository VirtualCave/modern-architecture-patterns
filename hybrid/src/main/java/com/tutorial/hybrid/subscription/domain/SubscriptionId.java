package com.tutorial.hybrid.subscription.domain;

public record SubscriptionId(String value) {

    public SubscriptionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SubscriptionId no puede estar vacío");
        }
    }

    public static SubscriptionId of(String value) {
        return new SubscriptionId(value);
    }

    public static SubscriptionId generate() {
        return new SubscriptionId(java.util.UUID.randomUUID().toString());
    }
}

