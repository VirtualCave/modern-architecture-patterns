package com.tutorial.hexagonal.domain.port.input;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderItem;

import java.util.List;

/**
 * PUERTO DE ENTRADA (Input Port / Use Case)
 *
 * Esta interfaz vive en el DOMINIO. Representa una intención de uso del negocio.
 * - El controlador REST la llama (no importa qué framework usa)
 * - El servicio de aplicación la implementa
 *
 * Al ser una interfaz sin dependencias externas, se puede testear fácilmente con mocks.
 */
public interface CreateOrderUseCase {

    Order createOrder(String customerId, List<OrderItem> items);
}

