package com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de una línea de pedido en la request HTTP.
 */
public record OrderItemRequest(

        @NotBlank(message = "productId es obligatorio")
        String productId,

        @NotBlank(message = "productName es obligatorio")
        String productName,

        @Positive(message = "El precio debe ser positivo")
        BigDecimal price,

        @Positive(message = "La cantidad debe ser positiva")
        int quantity
) {}

