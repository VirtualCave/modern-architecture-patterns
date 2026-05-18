package com.tutorial.hexagonal.infrastructure.adapter.output.persistence.jpa;

import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA.
 * Solo existe en la capa de infraestructura.
 */
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, String> {

    List<OrderEntity> findByCustomerId(String customerId);
}

