package com.tutorial.hexagonal.application.service;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderId;
import com.tutorial.hexagonal.domain.port.input.UpdateOrderStatusUseCase;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;

/**
 * SERVICIO DE APLICACIÓN: cambios de estado del pedido.
 *
 * Patrón: load → modify → save
 * La lógica de validación de transición está en el propio modelo Order.
 */
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;

    @Override
    public Order confirm(String orderId) {
        return executeTransition(orderId, order -> order.confirm());
    }

    @Override
    public Order ship(String orderId) {
        return executeTransition(orderId, order -> order.ship());
    }

    @Override
    public Order deliver(String orderId) {
        return executeTransition(orderId, order -> order.deliver());
    }

    @Override
    public Order cancel(String orderId, String reason) {
        return executeTransition(orderId, order -> order.cancel(reason));
    }

    /**
     * Método helper que encapsula el patrón: find → apply → save
     */
    private Order executeTransition(String orderId, java.util.function.Consumer<Order> transition) {
        var order = orderRepository.findById(OrderId.of(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + orderId));

        transition.accept(order);

        return orderRepository.save(order);
    }
}

