package com.tutorial.ddd.customer.infrastructure.persistence.mapper;

import com.tutorial.ddd.customer.domain.Customer;
import com.tutorial.ddd.customer.domain.CustomerId;
import com.tutorial.ddd.customer.infrastructure.persistence.entity.CustomerEntity;
import com.tutorial.ddd.shared.config.DddMapperConfig;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper de persistencia para el bounded context customer.
 *
 * Implementación minimalista con métodos default para evitar expression
 * y dejar el mapeo explícito y fácil de leer.
 */
@Mapper(config = DddMapperConfig.class)
public interface CustomerPersistenceMapper {

    default CustomerEntity toEntity(Customer customer) {
        return new CustomerEntity(
                customer.id().value(),
                customer.name(),
                customer.email(),
                customer.isActive(),
                customer.createdAt()
        );
    }

    default Customer toDomain(CustomerEntity entity) {
        return Customer.reconstitute(
                CustomerId.of(entity.getId()),
                entity.getName(),
                entity.getEmail(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    List<Customer> toDomainList(List<CustomerEntity> entities);
}
