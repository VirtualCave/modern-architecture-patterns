package com.tutorial.hybrid.subscription.domain;

import com.tutorial.hybrid.subscription.domain.event.SubscriptionEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Subscription {

    private final SubscriptionId id;
    private final CustomerId customerId;
    private final PlanCode planCode;
    private final Money monthlyFee;
    private SubscriptionStatus status;
    private final LocalDateTime createdAt;
    private final List<SubscriptionEvent> domainEvents = new ArrayList<>();

    private Subscription(SubscriptionId id, CustomerId customerId, PlanCode planCode, Money monthlyFee) {
        this.id = id;
        this.customerId = customerId;
        this.planCode = planCode;
        this.monthlyFee = monthlyFee;
        this.status = SubscriptionStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public static Subscription create(SubscriptionId id, CustomerId customerId, PlanCode planCode, Money monthlyFee) {
        var subscription = new Subscription(id, customerId, planCode, monthlyFee);
        subscription.domainEvents.add(new SubscriptionEvent.SubscriptionCreated(id.value(), customerId.value(), planCode.value(), monthlyFee));
        return subscription;
    }

    public static Subscription reconstitute(SubscriptionId id, CustomerId customerId, PlanCode planCode, Money monthlyFee, SubscriptionStatus status, LocalDateTime createdAt) {
        return new Subscription(id, customerId, planCode, monthlyFee, status, createdAt);
    }

    private Subscription(SubscriptionId id, CustomerId customerId, PlanCode planCode, Money monthlyFee, SubscriptionStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.planCode = planCode;
        this.monthlyFee = monthlyFee;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void activate() {
        transitionTo(SubscriptionStatus.ACTIVE);
        domainEvents.add(new SubscriptionEvent.SubscriptionActivated(id.value()));
    }

    public void cancel(String reason) {
        transitionTo(SubscriptionStatus.CANCELLED);
        domainEvents.add(new SubscriptionEvent.SubscriptionCancelled(id.value(), reason));
    }

    private void transitionTo(SubscriptionStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException("Transición inválida: %s → %s".formatted(status, next));
        }
        status = next;
    }

    public List<SubscriptionEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public SubscriptionId id() { return id; }
    public CustomerId customerId() { return customerId; }
    public PlanCode planCode() { return planCode; }
    public Money monthlyFee() { return monthlyFee; }
    public SubscriptionStatus status() { return status; }
    public LocalDateTime createdAt() { return createdAt; }
}

