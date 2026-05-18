package com.tutorial.hexagonal.infrastructure.config;

import com.tutorial.hexagonal.application.service.CreateOrderService;
import com.tutorial.hexagonal.application.service.GetOrderService;
import com.tutorial.hexagonal.application.service.UpdateOrderStatusService;
import com.tutorial.hexagonal.domain.port.input.CreateOrderUseCase;
import com.tutorial.hexagonal.domain.port.input.GetOrderUseCase;
import com.tutorial.hexagonal.domain.port.input.UpdateOrderStatusUseCase;
import com.tutorial.hexagonal.domain.port.output.OrderEventPublisher;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import com.tutorial.hexagonal.infrastructure.adapter.output.event.LogOrderEventPublisher;
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
 *   OrderEventPublisher (puerto) ← LogOrderEventPublisher (adaptador de salida)
 *
 * Beneficio: Para cambiar de H2 a PostgreSQL solo se cambia esta clase.
 * El dominio y la application no se tocan.
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

    @Bean
    public OrderEventPublisher orderEventPublisher() {
        return new LogOrderEventPublisher();
    }

    // -----------------------------------------------------------------
    // Servicios de aplicación (implementaciones de puertos de entrada)
    // -----------------------------------------------------------------

    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepository repository, OrderEventPublisher publisher) {
        return new CreateOrderService(repository, publisher);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(OrderRepository repository) {
        return new GetOrderService(repository);
    }

    @Bean
    public UpdateOrderStatusUseCase updateOrderStatusUseCase(OrderRepository repository, OrderEventPublisher publisher) {
        return new UpdateOrderStatusService(repository, publisher);
    }
}

