package com.tutorial.hybrid.subscription.domain.port.input;

import com.tutorial.hybrid.subscription.domain.Subscription;

public interface ActivateSubscriptionUseCase {
    Subscription activate(String subscriptionId);
}

