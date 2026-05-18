package com.tutorial.hybrid.subscription.readmodel;

import com.tutorial.hybrid.subscription.domain.Subscription;
import com.tutorial.hybrid.subscription.domain.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionView(
        String subscriptionId,
        String customerId,
        String planCode,
        BigDecimal monthlyFee,
        String currency,
        String status,
        LocalDateTime createdAt
) {
    public static SubscriptionView fromDomain(Subscription subscription) {
        return new SubscriptionView(
                subscription.id().value(),
                subscription.customerId().value(),
                subscription.planCode().value(),
                subscription.monthlyFee().amount(),
                subscription.monthlyFee().currency(),
                subscription.status().name(),
                subscription.createdAt()
        );
    }
}

