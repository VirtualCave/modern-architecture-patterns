package com.tutorial.hybrid.subscription.domain.event;

import com.tutorial.hybrid.subscription.domain.Money;

public sealed interface SubscriptionEvent permits
        SubscriptionEvent.SubscriptionCreated,
        SubscriptionEvent.SubscriptionActivated,
        SubscriptionEvent.SubscriptionCancelled {

    record SubscriptionCreated(String subscriptionId, String customerId, String planCode, Money monthlyFee) implements SubscriptionEvent {}

    record SubscriptionActivated(String subscriptionId) implements SubscriptionEvent {}

    record SubscriptionCancelled(String subscriptionId, String reason) implements SubscriptionEvent {}
}

