package com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto;

import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta. Record de Java 26.
 *
 * Tiene un factory method estático fromDomain() que convierte el agregado
 * dominio → DTO. Así el dominio permanece desacoplado del HTTP.
 */
public record OrderResponse(
        String id,
        String customerId,
        String status,
        BigDecimal totalAmount,
        String currency,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public record OrderItemResponse(
            String productId,
            String productName,
            BigDecimal price,
            int quantity,
            BigDecimal subtotal
    ) {}

    /** Conversión dominio → DTO */
    public static OrderResponse fromDomain(Order order) {
        var items = order.items().stream()
                .map(item -> new OrderItemResponse(
                        item.productId(),
                        item.productName(),
                        item.price().amount(),
                        item.quantity(),
                        item.subtotal().amount()
                ))
                .toList();

        return new OrderResponse(
                order.id().value(),
                order.customerId(),
                order.status().name(),
                order.totalAmount().amount(),
                order.totalAmount().currency(),
                items,
                order.createdAt()
        );
    }
}

