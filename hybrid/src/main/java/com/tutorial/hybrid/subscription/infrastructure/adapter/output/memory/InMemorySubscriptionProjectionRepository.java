package com.tutorial.hybrid.subscription.infrastructure.adapter.output.memory;

import com.tutorial.hybrid.subscription.domain.port.output.SubscriptionProjectionRepository;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySubscriptionProjectionRepository implements SubscriptionProjectionRepository {

    private final ConcurrentHashMap<String, SubscriptionView> storage = new ConcurrentHashMap<>();

    @Override
    public SubscriptionView save(SubscriptionView view) {
        storage.put(view.subscriptionId(), view);
        return view;
    }

    @Override
    public Optional<SubscriptionView> findById(String subscriptionId) {
        return Optional.ofNullable(storage.get(subscriptionId));
    }

    @Override
    public List<SubscriptionView> findByCustomerId(String customerId) {
        return storage.values().stream()
                .filter(view -> view.customerId().equals(customerId))
                .toList();
    }

    @Override
    public List<SubscriptionView> findActive() {
        return storage.values().stream()
                .filter(view -> "ACTIVE".equals(view.status()))
                .toList();
    }
}

