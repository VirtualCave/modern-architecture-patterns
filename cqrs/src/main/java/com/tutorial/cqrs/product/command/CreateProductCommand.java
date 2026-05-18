package com.tutorial.cqrs.product.command;

import com.tutorial.cqrs.shared.Command;

import java.math.BigDecimal;

public record CreateProductCommand(
        String name,
        String description,
        BigDecimal price,
        int stock
) implements Command {}

