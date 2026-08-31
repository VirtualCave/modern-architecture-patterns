package com.tutorial.hybrid.subscription.domain.event;

import com.tutorial.hybrid.subscription.domain.Money;

/**
 * Eventos de dominio del aggregate Subscription.
 *
 * Sealed interface + records (Java 21+):
 * La sealed interface garantiza que el compilador conozca TODOS los tipos posibles.
 * En el LogSubscriptionEventPublisher se usa un switch exhaustivo:
 * si añades un nuevo tipo de evento aquí, el compilador obliga a añadir el caso.
 *
 * Los records son inmutables por diseño: un evento es un hecho ocurrido en el pasado,
 * no debe poder modificarse después de crearse.
 *
 * Estos eventos son publicados por SubscriptionApplicationService DESPUÉS de guardar,
 * garantizando que nunca se publican eventos de operaciones que fallaron.
 */
public sealed interface SubscriptionEvent permits
        SubscriptionEvent.SubscriptionCreated,
        SubscriptionEvent.SubscriptionActivated,
        SubscriptionEvent.SubscriptionCancelled {

    record SubscriptionCreated(String subscriptionId, String customerId, String planCode, Money monthlyFee) implements SubscriptionEvent {}

    record SubscriptionActivated(String subscriptionId) implements SubscriptionEvent {}

    record SubscriptionCancelled(String subscriptionId, String reason) implements SubscriptionEvent {}
}

