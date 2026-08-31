package com.tutorial.hexagonal.domain.model;

import java.math.BigDecimal;

/**
 * Identificador del pedido.
 *
 * Java 26 - Record: inmutable por diseño, equals/hashCode/toString automáticos.
 * Usar un tipo propio para el ID (en lugar de String suelto) evita confusiones
 * y permite validar el formato en un único lugar.
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


