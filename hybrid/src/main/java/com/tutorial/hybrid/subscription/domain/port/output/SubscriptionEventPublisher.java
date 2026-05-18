package com.tutorial.hybrid.subscription.domain.port.output;

import com.tutorial.hybrid.subscription.domain.event.SubscriptionEvent;

public interface SubscriptionEventPublisher {
    void publish(SubscriptionEvent event);
}

