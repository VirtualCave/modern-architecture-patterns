package com.tutorial.hexagonal.domain.port.input;

import com.tutorial.hexagonal.domain.model.Order;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE ENTRADA: consultas sobre pedidos
 */
public interface GetOrderUseCase {

    Optional<Order> findById(String orderId);

    List<Order> findByCustomer(String customerId);

    List<Order> findAll();
}

