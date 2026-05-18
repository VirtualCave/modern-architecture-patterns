package com.tutorial.hexagonal.domain.port.input;

import com.tutorial.hexagonal.domain.model.Order;

/**
 * PUERTO DE ENTRADA: cambios de estado del pedido
 */
public interface UpdateOrderStatusUseCase {

    Order confirm(String orderId);

    Order ship(String orderId);

    Order deliver(String orderId);

    Order cancel(String orderId, String reason);
}

