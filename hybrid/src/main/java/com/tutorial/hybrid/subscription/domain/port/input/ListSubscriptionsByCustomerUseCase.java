package com.tutorial.hybrid.subscription.domain.port.input;

import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import java.util.List;

public interface ListSubscriptionsByCustomerUseCase {
    List<SubscriptionView> findByCustomerId(String customerId);
}

