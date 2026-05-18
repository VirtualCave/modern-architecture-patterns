package com.tutorial.cqrs.infrastructure.rest;

import com.tutorial.cqrs.product.query.GetAllProductsQuery;
import com.tutorial.cqrs.product.query.GetProductByIdQuery;
import com.tutorial.cqrs.product.query.GetProductsByPriceRangeQuery;
import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.shared.QueryBus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/queries/products")
@RequiredArgsConstructor
public class ProductQueryController {

    private final QueryBus queryBus;


    @GetMapping
    public List<ProductView> getAll() {
        return queryBus.dispatch(new GetAllProductsQuery());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductView> getById(@PathVariable String id) {
        return queryBus.dispatch(new GetProductByIdQuery(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/price-range")
    public List<ProductView> getByPriceRange(@RequestParam BigDecimal min,
                                             @RequestParam BigDecimal max) {
        return queryBus.dispatch(new GetProductsByPriceRangeQuery(min, max));
    }
}

