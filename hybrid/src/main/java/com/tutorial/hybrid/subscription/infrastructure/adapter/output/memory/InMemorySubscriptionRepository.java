package com.tutorial.hybrid.subscription.infrastructure.adapter.output.memory;

import com.tutorial.hybrid.subscription.domain.Subscription;
import com.tutorial.hybrid.subscription.domain.SubscriptionId;
import com.tutorial.hybrid.subscription.domain.port.output.SubscriptionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySubscriptionRepository implements SubscriptionRepository {

    private final ConcurrentHashMap<String, Subscription> storage = new ConcurrentHashMap<>();

    @Override
    public Subscription save(Subscription subscription) {
        storage.put(subscription.id().value(), subscription);
        return subscription;
    }

    @Override
    public Optional<Subscription> findById(SubscriptionId id) {
        return Optional.ofNullable(storage.get(id.value()));
    }

    @Override
    public List<Subscription> findByCustomerId(String customerId) {
        return storage.values().stream()
                .filter(subscription -> subscription.customerId().value().equals(customerId))
                .toList();
    }

    @Override
    public List<Subscription> findAll() {
        return List.copyOf(storage.values());
    }
}

