package com.tutorial.hybrid.subscription.domain.port.output;

import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import java.util.List;
import java.util.Optional;

public interface SubscriptionProjectionRepository {
    SubscriptionView save(SubscriptionView view);
    Optional<SubscriptionView> findById(String subscriptionId);
    List<SubscriptionView> findByCustomerId(String customerId);
    List<SubscriptionView> findActive();
}

