package com.tutorial.hybrid.subscription.infrastructure.adapter.output.memory;

import com.tutorial.hybrid.subscription.domain.event.SubscriptionEvent;
import com.tutorial.hybrid.subscription.domain.port.output.SubscriptionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogSubscriptionEventPublisher implements SubscriptionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LogSubscriptionEventPublisher.class);

    @Override
    public void publish(SubscriptionEvent event) {
        var message = switch (event) {
            case SubscriptionEvent.SubscriptionCreated e ->
                    "Subscription creada: id=%s, customer=%s, plan=%s, fee=%s"
                            .formatted(e.subscriptionId(), e.customerId(), e.planCode(), e.monthlyFee());
            case SubscriptionEvent.SubscriptionActivated e ->
                    "Subscription activada: id=%s".formatted(e.subscriptionId());
            case SubscriptionEvent.SubscriptionCancelled e ->
                    "Subscription cancelada: id=%s, reason=%s".formatted(e.subscriptionId(), e.reason());
        };
        log.info("[SUBSCRIPTION EVENT] {}", message);
    }
}

