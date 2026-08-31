package com.tutorial.hexagonal.infrastructure.adapter.output.persistence;

import com.tutorial.hexagonal.domain.model.*;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.jpa.SpringDataOrderRepository;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.mapper.OrderPersistenceMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * ADAPTADOR DE SALIDA: convierte entre dominio y JPA.
 *
 * Implementa OrderRepository (puerto de salida del dominio).
 * El dominio no sabe que esto existe: solo ve la interfaz OrderRepository.
 *
 * Esta clase es la "capa anticorrupción" de la infraestructura:
 *   - Convierte Order (dominio) → OrderEntity (JPA) para guardar
 *   - Convierte OrderEntity (JPA) → Order (dominio) al leer
 *
 * Si mañana migramos de JPA a MongoDB, solo se reescribe esta clase.
 * El dominio (Order, OrderRepository) no cambia nada.
 * Esa es la promesa central de la arquitectura hexagonal.
 */
@RequiredArgsConstructor
public class OrderJpaAdapter implements OrderRepository {

    private final SpringDataOrderRepository jpaRepository;
    private final OrderPersistenceMapper mapper;


    @Override
    public Order save(Order order) {
        var entity = mapper.toEntity(order);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

}

