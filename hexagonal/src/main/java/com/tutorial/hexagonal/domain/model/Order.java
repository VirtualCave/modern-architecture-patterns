package com.tutorial.hexagonal.domain.model;

import com.tutorial.hexagonal.domain.event.OrderEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AGGREGATE ROOT: Order
 *
 * La clase más importante del módulo hexagonal.
 * Reglas clave en Arquitectura Hexagonal:
 *
 * 1. NO hay imports de Spring, JPA ni ningún framework aquí.
 * 2. El Aggregate encapsula TODAS las reglas de negocio relacionadas con un pedido.
 * 3. El estado interno solo cambia a través de los métodos de esta clase.
 * 4. Los eventos de dominio se acumulan para ser despachados por la capa de application.
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

    // Los eventos generados se acumulan aquí y se limpian al ser publicados
    private final List<OrderEvent> domainEvents = new ArrayList<>();

    // -----------------------------------------------------------------
    // Constructor privado: nadie puede crear un Order sin pasar por create()
    // -----------------------------------------------------------------
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
     * Publica el evento OrderCreated automáticamente.
     */
    public static Order create(OrderId id, String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank())
            throw new IllegalArgumentException("customerId es obligatorio");
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("El pedido debe tener al menos un ítem");

        var order = new Order(id, customerId, items);
        // Registramos el evento de dominio
        order.domainEvents.add(new OrderEvent.OrderCreated(id.value(), customerId, order.totalAmount));
        return order;
    }

    /**
     * Factory method para reconstruir desde persistencia (sin generar evento).
     */
    public static Order reconstitute(OrderId id, String customerId,
                                     List<OrderItem> items, OrderStatus status,
                                     LocalDateTime createdAt) {
        var order = new Order(id, customerId, items);
        // No podemos setear status/createdAt directamente (campo final en constructor)
        // así que usamos un constructor alternativo interno
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
        domainEvents.add(new OrderEvent.OrderConfirmed(id.value()));
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
        domainEvents.add(new OrderEvent.OrderShipped(id.value()));
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
        domainEvents.add(new OrderEvent.OrderDelivered(id.value()));
    }

    public void cancel(String reason) {
        transitionTo(OrderStatus.CANCELLED);
        domainEvents.add(new OrderEvent.OrderCancelled(id.value(), reason));
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
    // Eventos de dominio
    // -----------------------------------------------------------------

    /** Devuelve copia inmutable de los eventos pendientes */
    public List<OrderEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
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


