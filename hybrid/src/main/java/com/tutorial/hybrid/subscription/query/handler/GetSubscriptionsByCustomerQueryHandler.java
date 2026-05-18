package com.tutorial.hybrid.subscription.query.handler;

import com.tutorial.hybrid.shared.QueryHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.query.GetSubscriptionsByCustomerQuery;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetSubscriptionsByCustomerQueryHandler implements QueryHandler<GetSubscriptionsByCustomerQuery, List<SubscriptionView>> {

    private final SubscriptionApplicationService service;

    @Override
    public List<SubscriptionView> handle(GetSubscriptionsByCustomerQuery query) {
        return service.findByCustomerId(query.customerId());
    }

    @Override
    public Class<GetSubscriptionsByCustomerQuery> queryType() {
        return GetSubscriptionsByCustomerQuery.class;
    }
}

