package com.tutorial.hybrid.subscription.command.handler;

import com.tutorial.hybrid.shared.CommandHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.command.CancelSubscriptionCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelSubscriptionCommandHandler implements CommandHandler<CancelSubscriptionCommand> {

    private final SubscriptionApplicationService service;

    @Override
    public void handle(CancelSubscriptionCommand command) {
        service.cancel(command.subscriptionId(), command.reason());
    }

    @Override
    public Class<CancelSubscriptionCommand> commandType() {
        return CancelSubscriptionCommand.class;
    }
}

