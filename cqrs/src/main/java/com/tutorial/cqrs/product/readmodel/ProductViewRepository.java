package com.tutorial.cqrs.product.readmodel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio del READ MODEL.
 *
 * Solo tiene métodos de LECTURA (ninguna escritura directa desde aquí).
 * En un sistema con CQRS completo, este modelo se actualiza mediante
 * proyecciones de eventos de dominio, no por llamadas directas.
 *
 * Para este tutorial, simplificamos: write y read comparten la misma BD H2,
 * pero con INTERFACES separadas para ilustrar la separación de responsabilidades.
 */
public interface ProductViewRepository {

    Optional<ProductView> findById(String id);

    List<ProductView> findAll();

    List<ProductView> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductView> findActive();
}

