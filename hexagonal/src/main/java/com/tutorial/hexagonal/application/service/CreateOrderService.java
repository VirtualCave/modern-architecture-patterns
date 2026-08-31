package com.tutorial.hexagonal.application.service;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderId;
import com.tutorial.hexagonal.domain.model.OrderItem;
import com.tutorial.hexagonal.domain.port.input.CreateOrderUseCase;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * SERVICIO DE APLICACIÓN: implementa el caso de uso CreateOrder.
 *
 * La capa de application orquesta el flujo entre el dominio y los puertos de salida.
 * No contiene lógica de negocio (esa está en Order). Su responsabilidad es:
 *   1. Generar un nuevo ID
 *   2. Llamar al factory method del modelo de dominio
 *   3. Persistir mediante el puerto de salida
 */
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    public Order createOrder(String customerId, List<OrderItem> items) {
        var orderId = OrderId.generate();
        var order = Order.create(orderId, customerId, items);
        return orderRepository.save(order);
    }
}

