package com.tutorial.hybrid.subscription.domain;

public enum SubscriptionStatus {
    DRAFT,
    ACTIVE,
    CANCELLED;

    public boolean canTransitionTo(SubscriptionStatus next) {
        return switch (this) {
            case DRAFT -> next == ACTIVE || next == CANCELLED;
            case ACTIVE -> next == CANCELLED;
            case CANCELLED -> false;
        };
    }
}

