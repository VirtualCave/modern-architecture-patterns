package com.tutorial.hexagonal.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object: Money
 *
 * Encapsula la aritmética monetaria y evita errores con doubles.
 * Como record de Java 26, es completamente inmutable.
 * Cada operación devuelve una NUEVA instancia (estilo funcional).
 */
public record Money(BigDecimal amount, String currency) {

    public static final Money ZERO = new Money(BigDecimal.ZERO, "EUR");

    public Money {
        if (amount == null) throw new IllegalArgumentException("El amount no puede ser nulo");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("La moneda no puede estar vacía");
        // Normalizamos a 2 decimales
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money euros(double amount) {
        return new Money(BigDecimal.valueOf(amount), "EUR");
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        var result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El resultado de la resta no puede ser negativo");
        }
        return new Money(result, this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "No se pueden operar monedas distintas: %s vs %s".formatted(this.currency, other.currency)
            );
        }
    }

    @Override
    public String toString() {
        return "%s %s".formatted(amount.toPlainString(), currency);
    }
}


