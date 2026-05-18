package com.tutorial.cqrs.product.query;

import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.shared.Query;

import java.math.BigDecimal;
import java.util.List;

public record GetProductsByPriceRangeQuery(
        BigDecimal minPrice,
        BigDecimal maxPrice
) implements Query<List<ProductView>> {}

