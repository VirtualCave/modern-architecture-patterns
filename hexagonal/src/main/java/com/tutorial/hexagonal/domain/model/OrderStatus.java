package com.tutorial.hexagonal.domain.model;

/**
 * Enum de estados del pedido.
 *
 * Define las transiciones válidas como máquina de estados.
 * El Aggregate Order usa este enum para proteger sus invariantes.
 *
 * Transiciones válidas:
 *   PENDING → CONFIRMED → SHIPPED → DELIVERED
 *   PENDING | CONFIRMED | SHIPPED → CANCELLED
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == SHIPPED   || next == CANCELLED;
            case SHIPPED    -> next == DELIVERED || next == CANCELLED;
            case DELIVERED, CANCELLED -> false;  // estados finales
        };
    }
}

