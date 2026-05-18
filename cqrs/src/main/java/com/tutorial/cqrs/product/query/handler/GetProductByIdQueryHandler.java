package com.tutorial.cqrs.product.query.handler;

import com.tutorial.cqrs.product.query.GetProductByIdQuery;
import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.product.readmodel.ProductViewRepository;
import com.tutorial.cqrs.shared.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetProductByIdQueryHandler implements QueryHandler<GetProductByIdQuery, Optional<ProductView>> {

    private final ProductViewRepository repository;


    @Override
    public Optional<ProductView> handle(GetProductByIdQuery query) {
        return repository.findById(query.productId());
    }

    @Override
    public Class<GetProductByIdQuery> queryType() {
        return GetProductByIdQuery.class;
    }
}

