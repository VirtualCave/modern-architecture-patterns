package com.tutorial.ddd.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * VALUE OBJECT: Money
 *
 * Encapsula cantidad monetaria y moneda como una unidad indivisible.
 * Inmutable: cada operación devuelve un NUEVO objeto Money.
 *
 * Por qué lanzar excepción en subtract() en lugar de devolver Optional:
 *   En DDD, el dominio PROTEGE sus invariantes activamente.
 *   Si intentas retirar más dinero del disponible, el dominio lo rechaza de inmediato.
 *   Esto hace imposible que el sistema quede en un estado inconsistente.
 *   Es una decisión de diseño consciente: mejor fallar rápido que permitir un saldo negativo.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) throw new IllegalArgumentException("amount is required");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency is required");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("amount cannot be negative");
    }

    public static Money euros(double amount) {
        return new Money(BigDecimal.valueOf(amount), "EUR");
    }

    public Money add(Money other) {
        assertCurrency(other);
        return new Money(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        assertCurrency(other);
        var result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("insufficient amount");
        }
        return new Money(result, currency);
    }

    private void assertCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch");
        }
    }
}

