package com.tutorial.hybrid.subscription.command;

import com.tutorial.hybrid.shared.Command;

public record ActivateSubscriptionCommand(String subscriptionId) implements Command {}

