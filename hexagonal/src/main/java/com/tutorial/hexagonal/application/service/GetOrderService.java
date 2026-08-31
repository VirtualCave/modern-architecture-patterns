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
 * En proyectos con CQRS + Hexagonal se podría ir directamente al read model
 * (ver módulo hybrid para ese ejemplo).
 *
 * En este tutorial los métodos son simples delegaciones al repositorio.
 * En un sistema real, aquí vivirían aspectos transversales como:
 *   - Validación de permisos / autorización ("¿puede este usuario ver este pedido?")
 *   - Enriquecimiento de datos (combinar datos de varios repositorios)
 *   - Caché de consultas frecuentes
 *   - Logging de acceso a datos sensibles
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

