package com.tutorial.hybrid.subscription.query.handler;

import com.tutorial.hybrid.shared.QueryHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.query.GetActiveSubscriptionsQuery;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetActiveSubscriptionsQueryHandler implements QueryHandler<GetActiveSubscriptionsQuery, List<SubscriptionView>> {

    private final SubscriptionApplicationService service;

    @Override
    public List<SubscriptionView> handle(GetActiveSubscriptionsQuery query) {
        return service.findActive();
    }

    @Override
    public Class<GetActiveSubscriptionsQuery> queryType() {
        return GetActiveSubscriptionsQuery.class;
    }
}

