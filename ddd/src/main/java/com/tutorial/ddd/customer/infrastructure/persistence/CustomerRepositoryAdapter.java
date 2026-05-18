package com.tutorial.ddd.customer.infrastructure.persistence;

import com.tutorial.ddd.customer.domain.Customer;
import com.tutorial.ddd.customer.domain.CustomerId;
import com.tutorial.ddd.customer.domain.repository.CustomerRepository;
import com.tutorial.ddd.customer.infrastructure.persistence.entity.CustomerEntity;
import com.tutorial.ddd.customer.infrastructure.persistence.jpa.SpringDataCustomerRepository;
import com.tutorial.ddd.customer.infrastructure.persistence.mapper.CustomerPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository repository;
    private final CustomerPersistenceMapper mapper;

    public CustomerRepositoryAdapter(SpringDataCustomerRepository repository,
                                     CustomerPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = mapper.toEntity(customer);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return mapper.toDomainList(repository.findAll());
    }
}

