package com.tutorial.cqrs.product.command;

import com.tutorial.cqrs.shared.Command;

import java.math.BigDecimal;

public record UpdateProductPriceCommand(
        String productId,
        BigDecimal newPrice
) implements Command {}

