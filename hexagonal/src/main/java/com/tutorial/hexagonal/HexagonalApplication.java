package com.tutorial.hexagonal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Módulo: Arquitectura Hexagonal (Ports & Adapters)
 *
 * La gran idea: el DOMINIO está en el centro y no depende de nada externo.
 * El mundo exterior (HTTP, BD, mensajería) se conecta mediante PUERTOS (interfaces)
 * que están definidos en el dominio pero implementados en la infraestructura.
 *
 * Flujo de una petición:
 *   REST Request
 *     → OrderController (Adaptador de entrada)
 *       → CreateOrderUseCase (Puerto de entrada / interfaz en dominio)
 *         → CreateOrderService (Implementado en capa application)
 *           → OrderRepository (Puerto de salida / interfaz en dominio)
 *             → OrderJpaAdapter (Adaptador de salida / implementado en infra)
 */
@SpringBootApplication
public class HexagonalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HexagonalApplication.class, args);
    }
}

