package com.tutorial.ddd.account.domain;

/**
 * VALUE OBJECT: AccountId
 *
 * Encapsula el identificador de una cuenta bancaria.
 * Inmutable por diseño (record de Java).
 *
 * ¿Por qué no usar String directamente?
 * - Un String puede ser nulo, vacío o contener cualquier cosa sin que el compilador avise.
 * - AccountId garantiza que si existe, es válido. La validación ocurre una sola vez, aquí.
 * - El código es más expresivo: account.id() devuelve AccountId, no un String ambiguo.
 */
public record AccountId(String value) {

    public AccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("account id is required");
        }
    }

    public static AccountId generate() {
        return new AccountId(java.util.UUID.randomUUID().toString());
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }
}

