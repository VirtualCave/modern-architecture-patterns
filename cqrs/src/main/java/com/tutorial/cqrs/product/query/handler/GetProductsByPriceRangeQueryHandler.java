package com.tutorial.cqrs.product.query.handler;

import com.tutorial.cqrs.product.query.GetProductsByPriceRangeQuery;
import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.product.readmodel.ProductViewRepository;
import com.tutorial.cqrs.shared.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetProductsByPriceRangeQueryHandler implements QueryHandler<GetProductsByPriceRangeQuery, List<ProductView>> {

    private final ProductViewRepository repository;


    @Override
    public List<ProductView> handle(GetProductsByPriceRangeQuery query) {
        return repository.findByPriceRange(query.minPrice(), query.maxPrice());
    }

    @Override
    public Class<GetProductsByPriceRangeQuery> queryType() {
        return GetProductsByPriceRangeQuery.class;
    }
}

