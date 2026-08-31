package com.tutorial.cqrs.shared;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del Query Bus.
 * Mismo patrón que SimpleCommandBus pero para queries.
 *
 * Nota sobre @SuppressWarnings:
 *   Mismo trade-off que SimpleCommandBus. La seguridad de tipos se garantiza
 *   en cada QueryHandler concreto a través de su método queryType().
 */
@Component
public class SimpleQueryBus implements QueryBus {

    @SuppressWarnings("rawtypes")
    private final Map<Class<?>, QueryHandler> handlers = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SimpleQueryBus(List<QueryHandler> queryHandlers) {
        for (var handler : queryHandlers) {
            handlers.put(handler.queryType(), handler);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <R> R dispatch(Query<R> query) {
        var handler = (QueryHandler) handlers.get(query.getClass());
        if (handler == null) {
            throw new IllegalArgumentException(
                "No hay handler registrado para: " + query.getClass().getSimpleName()
            );
        }
        return (R) handler.handle(query);
    }
}

