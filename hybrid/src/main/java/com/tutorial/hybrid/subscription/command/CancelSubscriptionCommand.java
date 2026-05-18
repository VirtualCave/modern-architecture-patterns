package com.tutorial.hybrid.subscription.command;

import com.tutorial.hybrid.shared.Command;

public record CancelSubscriptionCommand(String subscriptionId, String reason) implements Command {}

