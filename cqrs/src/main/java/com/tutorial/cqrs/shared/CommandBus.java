package com.tutorial.cqrs.shared;

/**
 * El Command Bus: punto central de despacho de commands.
 *
 * Los controladores solo conocen el bus, no los handlers específicos.
 * Esto desacopla completamente el HTTP de la lógica de negocio.
 */
public interface CommandBus {

    void dispatch(Command command);
}

