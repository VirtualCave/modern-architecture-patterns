package com.tutorial.hybrid.subscription.query;

import com.tutorial.hybrid.shared.Query;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;

import java.util.List;

public record GetActiveSubscriptionsQuery() implements Query<List<SubscriptionView>> {}

