package com.tutorial.cqrs.product.query;

import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.shared.Query;

import java.util.Optional;

public record GetProductByIdQuery(String productId) implements Query<Optional<ProductView>> {}

