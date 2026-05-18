package com.tutorial.cqrs.shared;

/**
 * Marker interface para todos los Commands.
 *
 * Un Command representa una INTENCIÓN DE CAMBIO.
 * Convenios en CQRS:
 *  - Los commands se nombran en imperativo: CreateProduct, UpdatePrice, DeleteProduct
 *  - No devuelven resultado (o devuelven solo el ID generado)
 *  - Se envían al CommandBus para ser despachados
 */
public interface Command {}

