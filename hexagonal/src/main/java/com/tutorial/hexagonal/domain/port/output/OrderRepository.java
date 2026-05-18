package com.tutorial.hexagonal.domain.port.output;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * PUERTO DE SALIDA (Output Port / Secondary Port)
 *
 * Esta interfaz vive en el DOMINIO pero la implementa la INFRAESTRUCTURA.
 * El dominio no sabe si se guarda en H2, PostgreSQL, MongoDB, etc.
 *
 * Principio de inversión de dependencia (DIP):
 *   - El dominio define QUÉ necesita (la interfaz)
 *   - La infraestructura decide CÓMO hacerlo (la implementación)
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findByCustomerId(String customerId);

    List<Order> findAll();

    void deleteById(OrderId id);
}

