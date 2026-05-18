package com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto;

/**
 * DTO para la petición de cancelar un pedido.
 */
public record CancelOrderRequest(String reason) {}

