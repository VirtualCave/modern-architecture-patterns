package com.tutorial.cqrs.shared;

/**
 * Interfaz genérica para Queries.
 *
 * Una Query representa una PREGUNTA sobre el estado del sistema.
 * Convenios en CQRS:
 *  - Nunca modifican el estado (no tienen efectos secundarios)
 *  - Se nombran como preguntas: GetProductById, GetAllProducts
 *  - Devuelven un resultado tipado (R)
 *
 * @param <R> el tipo del resultado de la query
 */
public interface Query<R> {}

