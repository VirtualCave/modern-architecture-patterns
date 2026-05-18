package com.tutorial.hybrid.subscription.infrastructure.adapter.input.rest;

import com.tutorial.hybrid.shared.QueryBus;
import com.tutorial.hybrid.subscription.query.GetActiveSubscriptionsQuery;
import com.tutorial.hybrid.subscription.query.GetSubscriptionByIdQuery;
import com.tutorial.hybrid.subscription.query.GetSubscriptionsByCustomerQuery;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queries/subscriptions")
@RequiredArgsConstructor
public class SubscriptionQueryController {

    private final QueryBus queryBus;

    @GetMapping
    public List<SubscriptionView> getActive() {
        return queryBus.dispatch(new GetActiveSubscriptionsQuery());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionView> getById(@PathVariable String id) {
        return queryBus.dispatch(new GetSubscriptionByIdQuery(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<SubscriptionView> getByCustomer(@PathVariable String customerId) {
        return queryBus.dispatch(new GetSubscriptionsByCustomerQuery(customerId));
    }
}

