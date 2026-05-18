package com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO de entrada para crear un pedido.
 *
 * Record de Java 26: inmutable, compacto, con toString/equals/hashCode automáticos.
 * Las anotaciones de validación de Jakarta Bean Validation se aplican directamente.
 *
 * Nota: este DTO vive en infrastructure, NUNCA en el dominio.
 * El controlador convierte este DTO en objetos del dominio.
 */
public record CreateOrderRequest(

        @NotBlank(message = "customerId es obligatorio")
        String customerId,

        @NotEmpty(message = "Debe incluir al menos un ítem")
        @Valid
        List<OrderItemRequest> items
) {}

