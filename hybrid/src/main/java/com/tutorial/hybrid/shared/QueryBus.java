package com.tutorial.hybrid.shared;

public interface QueryBus {
    <R> R dispatch(Query<R> query);
}

