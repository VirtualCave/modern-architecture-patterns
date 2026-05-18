package com.tutorial.hybrid.shared;

public interface CommandBus {
    void dispatch(Command command);
}

