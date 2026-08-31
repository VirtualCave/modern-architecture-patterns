package com.tutorial.ddd.account.domain.event;

import com.tutorial.ddd.shared.domain.Money;

/**
 * Eventos de dominio del aggregate Account.
 *
 * Java 21+ — Sealed Interface + Records:
 *
 * La sealed interface garantiza que SOLO estos tipos concretos de evento existen.
 * Beneficio práctico: en un switch sobre AccountEvent, el compilador avisa si
 * nos falta cubrir algún caso. No hay forma de que llegue un tipo desconocido.
 *
 * Los records son perfectos para eventos: inmutables, representan hechos ya ocurridos.
 *
 * Patrón "collect-and-dispatch":
 *   Los eventos se acumulan dentro del aggregate durante la operación y se recogen
 *   con pullDomainEvents() DESPUÉS de que la persistencia haya tenido éxito.
 *   Esto garantiza que nunca se publican eventos de una operación que no se guardó.
 */
public sealed interface AccountEvent permits
        AccountEvent.AccountOpened,
        AccountEvent.MoneyDeposited,
        AccountEvent.MoneyWithdrawn,
        AccountEvent.MoneyTransferred {

    record AccountOpened(String accountId, String ownerId, Money initialBalance) implements AccountEvent {}
    record MoneyDeposited(String accountId, Money amount) implements AccountEvent {}
    record MoneyWithdrawn(String accountId, Money amount) implements AccountEvent {}
    record MoneyTransferred(String fromAccountId, String toAccountId, Money amount) implements AccountEvent {}
}

