package com.tutorial.ddd.customer.application;

import com.tutorial.ddd.customer.application.input.CreateCustomerInput;
import com.tutorial.ddd.customer.domain.Customer;
import com.tutorial.ddd.customer.domain.CustomerId;
import com.tutorial.ddd.customer.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerApplicationService {

    private final CustomerRepository repository;


    public Customer create(CreateCustomerInput input) {
        var customer = Customer.create(CustomerId.generate(), input.name(), input.email());
        return repository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }
}

