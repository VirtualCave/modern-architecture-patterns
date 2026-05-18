package com.tutorial.cqrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Módulo: CQRS (Command Query Responsibility Segregation)
 *
 * La gran idea: SEPARAR las operaciones de ESCRITURA (Commands) de las de LECTURA (Queries).
 *
 * Por qué CQRS:
 *  - Leer y escribir tienen cargas y requisitos muy distintos
 *  - Se puede optimizar cada lado de forma independiente
 *  - Permite modelos de lectura denormalizados (más rápidos para consultar)
 *  - Natural con Event Sourcing (aunque aquí no lo implementamos)
 *
 * Flujo Command (escritura):
 *   POST /api/commands/products
 *     → CommandBus.dispatch(CreateProductCommand)
 *       → CreateProductCommandHandler.handle(cmd)
 *         → escribe en Product (dominio/write model)
 *
 * Flujo Query (lectura):
 *   GET /api/queries/products
 *     → QueryBus.dispatch(GetAllProductsQuery)
 *       → GetAllProductsQueryHandler.handle(query)
 *         → lee del ProductView (read model)
 */
@SpringBootApplication
public class CqrsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CqrsApplication.class, args);
    }
}

