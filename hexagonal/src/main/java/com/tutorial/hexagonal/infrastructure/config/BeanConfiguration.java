package com.tutorial.hexagonal.infrastructure.config;

import com.tutorial.hexagonal.application.service.CreateOrderService;
import com.tutorial.hexagonal.application.service.GetOrderService;
import com.tutorial.hexagonal.application.service.UpdateOrderStatusService;
import com.tutorial.hexagonal.domain.port.input.CreateOrderUseCase;
import com.tutorial.hexagonal.domain.port.input.GetOrderUseCase;
import com.tutorial.hexagonal.domain.port.input.UpdateOrderStatusUseCase;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.OrderJpaAdapter;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.jpa.SpringDataOrderRepository;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.mapper.OrderPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIGURACIÓN DE BEANS: el pegamento de la arquitectura hexagonal.
 *
 * Esta es la única clase que conoce todas las capas.
 * Aquí se conectan los puertos con sus adaptadores:
 *
 *   OrderRepository (puerto) ← OrderJpaAdapter (adaptador de salida)
 *
 * Beneficio: Para cambiar de H2 a PostgreSQL solo se cambia esta clase.
 * El dominio y la application no se tocan.
 *
 * Nota sobre el diseño:
 *   Los servicios de aplicación (CreateOrderService, etc.) NO llevan @Service.
 *   Se instancian aquí explícitamente para que el dominio no dependa de Spring.
 *   Esto es arquitectura hexagonal estricta: la capa de aplicación es agnóstica al framework.
 *   En el módulo hybrid verás que SubscriptionApplicationService SÍ usa @Service,
 *   que es un trade-off válido de simplicidad en proyectos reales.
 */
@Configuration
public class BeanConfiguration {

    // -----------------------------------------------------------------
    // Adaptadores de salida (implementaciones de puertos de dominio)
    // -----------------------------------------------------------------

    @Bean
    public OrderRepository orderRepository(SpringDataOrderRepository jpaRepository,
                                           OrderPersistenceMapper orderPersistenceMapper) {
        return new OrderJpaAdapter(jpaRepository, orderPersistenceMapper);
    }

    // -----------------------------------------------------------------
    // Servicios de aplicación (implementaciones de puertos de entrada)
    // -----------------------------------------------------------------

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository repository) {
        return new CreateOrderService(repository);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepository repository) {
        return new GetOrderService(repository);
    }

    @Bean
    public UpdateOrderStatusUseCase updateOrderStatusUseCase(OrderRepository repository) {
        return new UpdateOrderStatusService(repository);
    }
}

