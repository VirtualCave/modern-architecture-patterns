package com.tutorial.ddd.customer.domain;

import java.time.LocalDateTime;

public class Customer {

    private final CustomerId id;
    private String name;
    private String email;
    private boolean active;
    private final LocalDateTime createdAt;

    private Customer(CustomerId id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public static Customer create(CustomerId id, String name, String email) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (email == null || email.isBlank() || !email.contains("@")) throw new IllegalArgumentException("valid email is required");
        return new Customer(id, name, email);
    }

    public static Customer reconstitute(CustomerId id, String name, String email, boolean active, LocalDateTime createdAt) {
        var c = new Customer(id, name, email);
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

