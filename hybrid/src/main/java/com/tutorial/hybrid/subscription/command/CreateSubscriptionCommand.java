package com.tutorial.hybrid.subscription.command;

import com.tutorial.hybrid.shared.Command;

import java.math.BigDecimal;

public record CreateSubscriptionCommand(String customerId, String planCode, BigDecimal monthlyFee) implements Command {}

