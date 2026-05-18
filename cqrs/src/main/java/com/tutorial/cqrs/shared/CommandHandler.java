package com.tutorial.cqrs.shared;

/**
 * Interfaz que deben implementar todos los manejadores de Commands.
 *
 * Cada handler:
 *  - Maneja exactamente UN tipo de command
 *  - Declara su tipo mediante commandType() para que el bus lo registre
 *  - Implementa la lógica de negocio en handle()
 *
 * @param <C> el tipo del Command que maneja
 */
public interface CommandHandler<C extends Command> {

    void handle(C command);

    Class<C> commandType();
}

