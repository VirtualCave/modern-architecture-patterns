package com.tutorial.hexagonal.domain.model;

import java.math.BigDecimal;

/**
 * Value Object: OrderId
 *
 * Java 26 - Record: inmutable por diseño, equals/hashCode/toString automáticos.
 * En DDD, un Value Object se define por sus atributos, no por identidad.
 *
 * Usamos un record con validación en el constructor compacto.
 */
public record OrderId(String value) {

    // Constructor compacto (Java 16+): se ejecuta dentro del constructor canónico
    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId no puede ser nulo o vacío");
        }
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public static OrderId generate() {
        return new OrderId(java.util.UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}


