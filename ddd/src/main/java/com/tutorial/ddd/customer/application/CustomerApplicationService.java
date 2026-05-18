package com.tutorial.ddd.customer.application;

import com.tutorial.ddd.customer.application.command.CreateCustomerCommand;
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


    public Customer create(CreateCustomerCommand command) {
        var customer = Customer.create(CustomerId.generate(), command.name(), command.email());
        return repository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }
}

