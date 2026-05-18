package com.tutorial.hybrid.subscription.domain.port.input;

import com.tutorial.hybrid.subscription.domain.Subscription;
import java.math.BigDecimal;

public interface CreateSubscriptionUseCase {
    Subscription create(String customerId, String planCode, BigDecimal monthlyFee);
}

