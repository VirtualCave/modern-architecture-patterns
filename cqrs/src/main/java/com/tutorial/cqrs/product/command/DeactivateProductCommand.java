package com.tutorial.cqrs.product.command;

import com.tutorial.cqrs.shared.Command;

public record DeactivateProductCommand(String productId) implements Command {}

