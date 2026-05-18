package com.tutorial.hybrid.subscription.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount, String currency) {

    public static final Money ZERO_EUR = new Money(BigDecimal.ZERO, "EUR");

    public Money {
        if (amount == null) throw new IllegalArgumentException("amount no puede ser nulo");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency no puede estar vacía");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money euros(double amount) {
        return new Money(BigDecimal.valueOf(amount), "EUR");
    }
}

