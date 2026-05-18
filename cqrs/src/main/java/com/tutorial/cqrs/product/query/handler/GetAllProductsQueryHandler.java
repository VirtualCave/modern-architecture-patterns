package com.tutorial.cqrs.product.query.handler;

import com.tutorial.cqrs.product.query.GetAllProductsQuery;
import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.product.readmodel.ProductViewRepository;
import com.tutorial.cqrs.shared.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAllProductsQueryHandler implements QueryHandler<GetAllProductsQuery, List<ProductView>> {

    private final ProductViewRepository repository;


    @Override
    public List<ProductView> handle(GetAllProductsQuery query) {
        return repository.findAll();
    }

    @Override
    public Class<GetAllProductsQuery> queryType() {
        return GetAllProductsQuery.class;
    }
}

