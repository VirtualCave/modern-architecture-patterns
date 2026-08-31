package com.tutorial.hybrid.subscription.command.handler;

import com.tutorial.hybrid.shared.CommandHandler;
import com.tutorial.hybrid.subscription.application.service.SubscriptionApplicationService;
import com.tutorial.hybrid.subscription.command.CreateSubscriptionCommand;
import com.tutorial.hybrid.subscription.domain.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * COMMAND HANDLER: CreateSubscriptionCommandHandler
 *
 * En CQRS puro (ver módulo cqrs/) el handler habla directamente con el repositorio:
 *   Handler → Repository → BD
 *
 * En este módulo hybrid, el handler delega en el Application Service:
 *   Handler → ApplicationService → Repository + Projection + EventPublisher
 *
 * ¿Cuándo usar cada patrón?
 *   - Handler directo: operaciones simples, sin efectos secundarios
 *   - Handler + AppService: cuando hay lógica de orquestación, proyecciones o eventos
 *
 * El handler aquí es intencionalmente delgado: su única función es conectar el bus
 * con el servicio de aplicación. Toda la lógica real está en SubscriptionApplicationService.
 */
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

