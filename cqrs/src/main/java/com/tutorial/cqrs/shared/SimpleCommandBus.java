package com.tutorial.cqrs.shared;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del Command Bus usando Spring.
 *
 * En el constructor, Spring inyecta TODOS los beans que implementen CommandHandler.
 * Construimos un mapa: tipo_de_command → handler.
 * Al hacer dispatch(), buscamos el handler correcto y lo ejecutamos.
 *
 * Ventaja: añadir un nuevo command + handler no requiere modificar el bus.
 * Solo se crea el handler con @Component y Spring lo registra automáticamente.
 *
 * Nota sobre @SuppressWarnings:
 *   Java no soporta tipos genéricos covariantes para este patrón.
 *   No podemos declarar Map<Class<?>, CommandHandler<?>> con wildcards seguros.
 *   La seguridad de tipos se mantiene individualmente en cada handler concreto
 *   (cada uno declara su commandType()). El bus acepta este trade-off deliberadamente.
 */
@Component
public class SimpleCommandBus implements CommandBus {

    @SuppressWarnings("rawtypes")
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
            throw new IllegalArgumentException(
                "No hay handler registrado para: " + command.getClass().getSimpleName()
            );
        }
        handler.handle(command);
    }
}

