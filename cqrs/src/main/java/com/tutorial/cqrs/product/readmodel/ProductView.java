package com.tutorial.cqrs.product.readmodel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * READ MODEL: ProductView
 *
 * La diferencia clave entre CQRS y una arquitectura normal:
 * El READ MODEL es diferente al WRITE MODEL.
 *
 * ProductView está diseñado para ser eficiente en lectura:
 *  - Plano (sin joins necesarios)
 *  - Puede estar desnormalizado
 *  - Puede estar en caché
 *  - Puede estar en una BD diferente (eventualmente consistente)
 *
 * Aquí usamos un record de Java 26: inmutable, perfecto para datos de lectura.
 */
public record ProductView(
        String id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        boolean active,
        LocalDateTime createdAt
) {}

