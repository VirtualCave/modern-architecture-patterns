package com.tutorial.hybrid.subscription.command.handler;

import com.tutorial.hybrid.shared.CommandHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.command.ActivateSubscriptionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActivateSubscriptionCommandHandler implements CommandHandler<ActivateSubscriptionCommand> {

    private final SubscriptionApplicationService service;

    @Override
    public void handle(ActivateSubscriptionCommand command) {
        service.activate(command.subscriptionId());
    }

    @Override
    public Class<ActivateSubscriptionCommand> commandType() {
        return ActivateSubscriptionCommand.class;
    }
}

