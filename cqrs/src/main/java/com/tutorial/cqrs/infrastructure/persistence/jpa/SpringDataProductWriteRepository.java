package com.tutorial.cqrs.infrastructure.persistence.jpa;

import com.tutorial.cqrs.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductWriteRepository extends JpaRepository<ProductEntity, String> {}

