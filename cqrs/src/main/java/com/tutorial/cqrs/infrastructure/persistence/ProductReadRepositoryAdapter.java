package com.tutorial.cqrs.infrastructure.persistence;

import com.tutorial.cqrs.infrastructure.persistence.jpa.SpringDataProductReadRepository;
import com.tutorial.cqrs.infrastructure.persistence.mapper.ProductPersistenceMapper;
import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.product.readmodel.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductReadRepositoryAdapter implements ProductViewRepository {

    private final SpringDataProductReadRepository repository;
    private final ProductPersistenceMapper mapper;


    @Override
    public Optional<ProductView> findById(String id) {
        return repository.findById(id).map(mapper::toView);
    }

    @Override
    public List<ProductView> findAll() {
        return mapper.toViewList(repository.findAll());
    }

    @Override
    public List<ProductView> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return mapper.toViewList(repository.findByPriceBetween(minPrice, maxPrice));
    }

    @Override
    public List<ProductView> findActive() {
        return mapper.toViewList(repository.findByActiveTrue());
    }
}

