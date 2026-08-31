package com.tutorial.hexagonal.domain.model;

/**
 * Línea de un pedido.
 *
 * Record de Java 26: inmutable, compacto.
 * Nota: en Hexagonal Architecture, este record no tiene anotaciones JPA.
 * La conversión al mundo JPA ocurre en la capa de infraestructura.
 */
public record OrderItem(
        String productId,
        String productName,
        Money price,
        int quantity
) {
    public OrderItem {
        if (productId == null || productId.isBlank())
            throw new IllegalArgumentException("productId es obligatorio");
        if (productName == null || productName.isBlank())
            throw new IllegalArgumentException("productName es obligatorio");
        if (price == null)
            throw new IllegalArgumentException("price es obligatorio");
        if (quantity <= 0)
            throw new IllegalArgumentException("quantity debe ser mayor que 0");
    }

    /** Subtotal de esta línea */
    public Money subtotal() {
        return price.multiply(quantity);
    }
}


