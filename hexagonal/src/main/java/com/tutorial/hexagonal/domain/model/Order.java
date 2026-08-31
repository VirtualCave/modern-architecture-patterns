package com.tutorial.hexagonal.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ENTIDAD DE DOMINIO: Order
 *
 * La clase más importante del módulo hexagonal.
 * Reglas clave en Arquitectura Hexagonal:
 *
 * 1. NO hay imports de Spring, JPA ni ningún framework aquí.
 * 2. Order encapsula TODAS las reglas de negocio relacionadas con un pedido.
 * 3. El estado interno solo cambia a través de los métodos de esta clase.
 *
 * Patrón: Constructor privado + factory method estático para controlar la creación.
 */
public final class Order {

    private final OrderId id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Money totalAmount;
    private final LocalDateTime createdAt;

    // Constructor privado: nadie puede crear un Order sin pasar por create()
    private Order(OrderId id, String customerId, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotal();
    }

    /**
     * Factory method: única forma de crear un pedido nuevo.
     */
    public static Order create(OrderId id, String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank())
            throw new IllegalArgumentException("customerId es obligatorio");
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("El pedido debe tener al menos un ítem");

        return new Order(id, customerId, items);
    }

    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static Order reconstitute(OrderId id, String customerId,
                                     List<OrderItem> items, OrderStatus status,
                                     LocalDateTime createdAt) {
        return new Order(id, customerId, items, status, createdAt);
    }

    private Order(OrderId id, String customerId, List<OrderItem> items,
                  OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.createdAt = createdAt;
        this.totalAmount = calculateTotal();
    }

    // -----------------------------------------------------------------
    // Comportamiento del negocio (transiciones de estado)
    // -----------------------------------------------------------------

    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED);
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel(String reason) {
        transitionTo(OrderStatus.CANCELLED);
    }

    /** Valida la transición usando las reglas del enum */
    private void transitionTo(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Transición inválida: %s → %s no está permitida".formatted(status, newStatus)
            );
        }
        this.status = newStatus;
    }

    // -----------------------------------------------------------------
    // Cálculos
    // -----------------------------------------------------------------

    private Money calculateTotal() {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.ZERO, Money::add);
    }

    // -----------------------------------------------------------------
    // Getters (no hay setters: el estado cambia SOLO por métodos de negocio)
    // -----------------------------------------------------------------

    public OrderId id()            { return id; }
    public String customerId()     { return customerId; }
    public List<OrderItem> items() { return Collections.unmodifiableList(items); }
    public OrderStatus status()    { return status; }
    public Money totalAmount()     { return totalAmount; }
    public LocalDateTime createdAt() { return createdAt; }
}


