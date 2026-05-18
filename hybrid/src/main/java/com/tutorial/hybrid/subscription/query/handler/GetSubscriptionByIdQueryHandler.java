package com.tutorial.hybrid.subscription.query.handler;

import com.tutorial.hybrid.shared.QueryHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.query.GetSubscriptionByIdQuery;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetSubscriptionByIdQueryHandler implements QueryHandler<GetSubscriptionByIdQuery, Optional<SubscriptionView>> {

    private final SubscriptionApplicationService service;

    @Override
    public Optional<SubscriptionView> handle(GetSubscriptionByIdQuery query) {
        return service.findById(query.subscriptionId());
    }

    @Override
    public Class<GetSubscriptionByIdQuery> queryType() {
        return GetSubscriptionByIdQuery.class;
    }
}

