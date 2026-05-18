package com.tutorial.cqrs.product.domain;

/**
 * Value Object: ProductId como record de Java 26.
 */
public record ProductId(String value) {

    public ProductId {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("ProductId no puede estar vacío");
    }

    public static ProductId generate() {
        return new ProductId(java.util.UUID.randomUUID().toString());
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }
}

