package com.tutorial.cqrs.product.domain;

import java.util.Optional;

/**
 * Repositorio del write model.
 * Solo en el lado de escritura (Commands).
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId id);

    void deleteById(ProductId id);
}

