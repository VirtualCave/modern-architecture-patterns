package com.tutorial.hybrid.shared;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SimpleCommandBus implements CommandBus {

    private final Map<Class<?>, CommandHandler> handlers = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SimpleCommandBus(List<CommandHandler> commandHandlers) {
        for (var handler : commandHandlers) {
            handlers.put(handler.commandType(), handler);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void dispatch(Command command) {
        var handler = (CommandHandler) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No hay handler para: " + command.getClass().getSimpleName());
        }
        handler.handle(command);
    }
}

