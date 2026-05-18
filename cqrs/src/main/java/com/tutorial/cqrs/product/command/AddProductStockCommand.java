package com.tutorial.cqrs.product.command;

import com.tutorial.cqrs.shared.Command;

public record AddProductStockCommand(
        String productId,
        int quantity
) implements Command {}

