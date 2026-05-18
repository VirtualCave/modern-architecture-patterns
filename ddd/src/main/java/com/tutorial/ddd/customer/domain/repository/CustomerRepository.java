package com.tutorial.ddd.customer.domain.repository;

import com.tutorial.ddd.customer.domain.Customer;
import com.tutorial.ddd.customer.domain.CustomerId;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(CustomerId id);
    List<Customer> findAll();
}

