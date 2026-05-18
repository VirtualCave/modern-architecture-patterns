package com.tutorial.hybrid.subscription.query;

import com.tutorial.hybrid.shared.Query;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;

import java.util.Optional;

public record GetSubscriptionByIdQuery(String subscriptionId) implements Query<Optional<SubscriptionView>> {}

