package com.tutorial.ddd.customer.infrastructure.persistence.jpa;

import com.tutorial.ddd.customer.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, String> {}

