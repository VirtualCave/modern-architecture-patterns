package com.tutorial.cqrs.product.query;

import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.shared.Query;

import java.util.List;

public record GetAllProductsQuery() implements Query<List<ProductView>> {}

