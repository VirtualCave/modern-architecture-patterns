package com.tutorial.hybrid.subscription.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    @Test
    void createActivateAndCancelSubscription() {
        var subscription = Subscription.create(
                SubscriptionId.generate(),
                CustomerId.of("CUST-1"),
                PlanCode.of("PRO"),
                new Money(BigDecimal.valueOf(29.99), "EUR")
        );

        assertThat(subscription.status()).isEqualTo(SubscriptionStatus.DRAFT);
        assertThat(subscription.pullDomainEvents()).hasSize(1);

        subscription.activate();
        assertThat(subscription.status()).isEqualTo(SubscriptionStatus.ACTIVE);

        subscription.cancel("Cliente solicitó baja");
        assertThat(subscription.status()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void cannotActivateCancelledSubscription() {
        var subscription = Subscription.create(
                SubscriptionId.generate(),
                CustomerId.of("CUST-1"),
                PlanCode.of("PRO"),
                Money.euros(29.99)
        );
        subscription.cancel("Stop");

        assertThatThrownBy(subscription::activate)
                .isInstanceOf(IllegalStateException.class);
    }
}

