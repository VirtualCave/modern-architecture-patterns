package com.tutorial.hybrid.subscription.domain.port.input;

import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import java.util.Optional;

public interface GetSubscriptionUseCase {
    Optional<SubscriptionView> findById(String subscriptionId);
}

