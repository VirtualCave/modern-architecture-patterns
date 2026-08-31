package com.tutorial.ddd.customer.domain;

import java.time.LocalDateTime;

public class Customer {

    private final CustomerId id;
    private String name;
    private String email;
    private boolean active;
    private final LocalDateTime createdAt;

    private Customer(CustomerId id, String name, String email, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = true;
        this.createdAt = createdAt;
    }

    /**
     * Factory method para crear un cliente NUEVO.
     * Valida las reglas de negocio antes de construir el objeto.
     * Si la validación falla, lanza excepción: nunca se crea un Customer inválido.
     */
    public static Customer create(CustomerId id, String name, String email) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (email == null || email.isBlank() || !email.contains("@")) throw new IllegalArgumentException("valid email is required");
        return new Customer(id, name, email, LocalDateTime.now());
    }

    /**
     * Factory method para reconstruir un cliente desde persistencia.
     *
     * La diferencia con create() es importante:
     *  - create()       → nuevo objeto, genera fecha actual, valida todas las reglas
     *  - reconstitute() → objeto ya existente, restaura el estado tal como estaba en BD
     *
     * NO se generan eventos de dominio aquí porque no "ocurrió" nada nuevo.
     * NO se recalcula la fecha: se respeta la original de cuando se creó.
     */
    public static Customer reconstitute(CustomerId id, String name, String email, boolean active, LocalDateTime createdAt) {
        var c = new Customer(id, name, email, createdAt);
        c.active = active;
        return c;
    }

    public void deactivate() { this.active = false; }

    public CustomerId id() { return id; }
    public String name() { return name; }
    public String email() { return email; }
    public boolean isActive() { return active; }
    public LocalDateTime createdAt() { return createdAt; }
}

