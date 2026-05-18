package com.tutorial.hexagonal.domain.event;

import com.tutorial.hexagonal.domain.model.Money;

/**
 * Eventos de dominio del agregado Order.
 *
 * Java 26 - Sealed Interface + Records:
 * La sealed interface garantiza que solo estos tipos concretos pueden existir.
 * Los records son inmutables, perfectos para eventos (hechos que ya ocurrieron).
 *
 * Beneficio: en el switch del publicador podemos usar pattern matching
 * y el compilador avisa si nos falta un caso.
 */
public sealed interface OrderEvent permits
        OrderEvent.OrderCreated,
        OrderEvent.OrderConfirmed,
        OrderEvent.OrderShipped,
        OrderEvent.OrderDelivered,
        OrderEvent.OrderCancelled {

    /** El pedido fue creado con éxito */
    record OrderCreated(
            String orderId,
            String customerId,
            Money totalAmount
    ) implements OrderEvent {}

    /** El pedido fue confirmado */
    record OrderConfirmed(String orderId) implements OrderEvent {}

    /** El pedido fue enviado */
    record OrderShipped(String orderId) implements OrderEvent {}

    /** El pedido fue entregado */
    record OrderDelivered(String orderId) implements OrderEvent {}

    /** El pedido fue cancelado */
    record OrderCancelled(String orderId, String reason) implements OrderEvent {}
}

