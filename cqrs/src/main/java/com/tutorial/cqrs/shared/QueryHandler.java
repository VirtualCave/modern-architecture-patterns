package com.tutorial.cqrs.shared;

/**
 * Interfaz que deben implementar todos los manejadores de Queries.
 *
 * @param <Q> el tipo de Query que maneja
 * @param <R> el tipo de resultado que devuelve
 */
public interface QueryHandler<Q extends Query<R>, R> {

    R handle(Q query);

    Class<Q> queryType();
}

