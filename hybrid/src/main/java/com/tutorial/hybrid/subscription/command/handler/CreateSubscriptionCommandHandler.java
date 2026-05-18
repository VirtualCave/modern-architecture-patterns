package com.tutorial.hybrid.subscription.command.handler;

import com.tutorial.hybrid.shared.CommandHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.command.CreateSubscriptionCommand;
import com.tutorial.hybrid.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSubscriptionCommandHandler implements CommandHandler<CreateSubscriptionCommand> {

    private final SubscriptionApplicationService service;

    @Override
    public void handle(CreateSubscriptionCommand command) {
        service.create(command.customerId(), command.planCode(), command.monthlyFee());
    }

    @Override
    public Class<CreateSubscriptionCommand> commandType() {
        return CreateSubscriptionCommand.class;
    }
}

