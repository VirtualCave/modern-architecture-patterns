package com.tutorial.hybrid.shared;

public interface CommandHandler<C extends Command> {
    void handle(C command);
    Class<C> commandType();
}

