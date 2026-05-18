package com.tutorial.cqrs.infrastructure.persistence;

import com.tutorial.cqrs.infrastructure.persistence.entity.ProductEntity;
import com.tutorial.cqrs.infrastructure.persistence.jpa.SpringDataProductWriteRepository;
import com.tutorial.cqrs.infrastructure.persistence.mapper.ProductPersistenceMapper;
import com.tutorial.cqrs.product.domain.Product;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductWriteRepositoryAdapter implements ProductRepository {

    private final SpringDataProductWriteRepository repository;
    private final ProductPersistenceMapper mapper;


    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public void deleteById(ProductId id) {
        repository.deleteById(id.value());
    }
}

