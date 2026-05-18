package com.tutorial.hybrid.subscription.domain.port.output;

import com.tutorial.hybrid.subscription.domain.Subscription;
import com.tutorial.hybrid.subscription.domain.SubscriptionId;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(SubscriptionId id);
    List<Subscription> findByCustomerId(String customerId);
    List<Subscription> findAll();
}

