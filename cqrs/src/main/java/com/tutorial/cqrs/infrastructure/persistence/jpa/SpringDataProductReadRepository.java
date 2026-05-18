package com.tutorial.cqrs.infrastructure.persistence.jpa;

import com.tutorial.cqrs.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface SpringDataProductReadRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<ProductEntity> findByActiveTrue();
}

