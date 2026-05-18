package com.tutorial.hexagonal.application.service;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderId;
import com.tutorial.hexagonal.domain.port.input.GetOrderUseCase;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * SERVICIO DE APLICACIÓN: consultas de pedidos.
 *
 * En arquitectura hexagonal pura, las queries también pasan por Use Cases.
 * En proyectos con CQRS + Hexagonal se podría ir directamente al read model.
 */
@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;


    @Override
    public Optional<Order> findById(String orderId) {
        return orderRepository.findById(OrderId.of(orderId));
    }

    @Override
    public List<Order> findByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
}

