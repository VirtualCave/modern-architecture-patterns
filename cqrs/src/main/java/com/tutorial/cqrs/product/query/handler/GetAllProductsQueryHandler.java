package com.tutorial.cqrs.product.query.handler;

import com.tutorial.cqrs.product.query.GetAllProductsQuery;
import com.tutorial.cqrs.product.readmodel.ProductView;
import com.tutorial.cqrs.product.readmodel.ProductViewRepository;
import com.tutorial.cqrs.shared.QueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * QUERY HANDLER: GetAllProductsQueryHandler
 *
 * Este handler es la contraparte de lectura de CQRS.
 * NUNCA modifica estado. Solo lee del READ MODEL (ProductView).
 *
 * Diferencia clave con el command side:
 *  - Los command handlers usan ProductRepository (write model: Product con reglas de negocio)
 *  - Los query handlers usan ProductViewRepository (read model: ProductView plano y optimizado)
 *
 * En este tutorial write y read model comparten la misma BD H2.
 * En producción podrían ser bases de datos separadas (Redis, Elasticsearch, etc.),
 * actualizándose de forma asíncrona cuando se ejecutan comandos (consistencia eventual).
 *
 * Flujo: QueryBus → este handler → read model (ProductView) → respuesta HTTP
 */
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

