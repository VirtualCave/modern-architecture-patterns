package com.tutorial.ddd.account.domain.service;

import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.shared.domain.Money;

/**
 * DOMAIN SERVICE: TransferDomainService
 *
 * Un Domain Service se usa cuando la lógica de negocio NO pertenece
 * claramente a un solo aggregate.
 *
 * ¿Por qué no está este código dentro de Account?
 *
 * Una transferencia involucra DOS aggregates: la cuenta origen y la cuenta destino.
 * Ninguna de las dos puede orquestar la operación sin conocer a la otra,
 * y los aggregates no deben depender entre sí directamente.
 *
 * Solución: un Domain Service que recibe ambas cuentas y coordina la operación.
 * La lógica de negocio real (withdraw + deposit) sigue viviendo en el aggregate Account.
 *
 * Regla práctica:
 *   - Lógica que involucra UN solo aggregate → método en ese aggregate
 *   - Lógica que involucra VARIOS aggregates → Domain Service
 */
public class TransferDomainService {

    public void transfer(Account from, Account to, Money amount) {
        from.transferTo(to, amount);
    }
}

