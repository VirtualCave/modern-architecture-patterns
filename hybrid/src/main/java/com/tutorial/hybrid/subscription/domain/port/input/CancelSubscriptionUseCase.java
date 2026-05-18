package com.tutorial.hybrid.subscription.domain.port.input;

import com.tutorial.hybrid.subscription.domain.Subscription;

public interface CancelSubscriptionUseCase {
    Subscription cancel(String subscriptionId, String reason);
}

