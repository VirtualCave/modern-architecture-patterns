package com.tutorial.cqrs.shared;

/**
 * El Query Bus: punto central de despacho de queries.
 */
public interface QueryBus {

    <R> R dispatch(Query<R> query);
}

