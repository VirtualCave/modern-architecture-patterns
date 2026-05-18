package com.tutorial.hexagonal.application.service;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderId;
import com.tutorial.hexagonal.domain.model.OrderItem;
import com.tutorial.hexagonal.domain.port.input.CreateOrderUseCase;
import com.tutorial.hexagonal.domain.port.output.OrderEventPublisher;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * SERVICIO DE APLICACIÓN: implementa el caso de uso CreateOrder.
 *
 * La capa de application orquesta el flujo entre el dominio y los puertos de salida.
 * No contiene lógica de negocio (esa está en Order). Su responsabilidad es:
 *   1. Generar un nuevo ID
 *   2. Llamar al factory method del agregado
 *   3. Persistir mediante el puerto de salida
 *   4. Publicar los eventos generados
 *
 * Nota: @UseCase es una anotación semántica definida en infrastructure/config.
 * En realidad es un @Service de Spring, pero el nombre mejora la legibilidad.
 */
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;


    @Override
    public Order createOrder(String customerId, List<OrderItem> items) {
        var orderId = OrderId.generate();
        var order = Order.create(orderId, customerId, items);

        var savedOrder = orderRepository.save(order);

        // Publicar todos los eventos de dominio generados
        savedOrder.pullDomainEvents().forEach(eventPublisher::publish);

        return savedOrder;
    }
}

