package com.tutorial.hybrid.shared;

public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
    Class<Q> queryType();
}

