package com.tutorial.cqrs.product.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WRITE MODEL: Product (dominio de escritura)
 *
 * En CQRS, el write model protege las invariantes del negocio.
 * Es diferente al read model (ProductView) que está optimizado para lectura.
 *
 * Este modelo puede ser complejo, normalizado, y expresar reglas de negocio.
 * El read model puede ser simple, plano, desnormalizado.
 */
public class Product {

    private final ProductId id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private final LocalDateTime createdAt;

    private Product(ProductId id, String name, String description,
                    BigDecimal price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public static Product create(ProductId id, String name, String description,
                                 BigDecimal price, int stock) {
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        if (stock < 0)
            throw new IllegalArgumentException("El stock no puede ser negativo");

        return new Product(id, name, description, price, stock);
    }

    public static Product reconstitute(ProductId id, String name, String description,
                                       BigDecimal price, int stock, boolean active,
                                       LocalDateTime createdAt) {
        var p = new Product(id, name, description, price, stock);
        p.active = active;
        return p;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("El precio debe ser mayor que 0");
        this.price = newPrice;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva");
        this.stock += quantity;
    }

    public void deactivate() {
        this.active = false;
    }

    public ProductId id()          { return id; }
    public String name()           { return name; }
    public String description()    { return description; }
    public BigDecimal price()      { return price; }
    public int stock()             { return stock; }
    public boolean isActive()      { return active; }
    public LocalDateTime createdAt() { return createdAt; }
}

