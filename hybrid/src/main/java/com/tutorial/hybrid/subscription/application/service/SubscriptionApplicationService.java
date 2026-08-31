package com.tutorial.hybrid.subscription.application.service;

import com.tutorial.hybrid.subscription.domain.*;
import com.tutorial.hybrid.subscription.domain.event.SubscriptionEvent;
import com.tutorial.hybrid.subscription.domain.port.input.*;
import com.tutorial.hybrid.subscription.domain.port.output.SubscriptionEventPublisher;
import com.tutorial.hybrid.subscription.domain.port.output.SubscriptionProjectionRepository;
import com.tutorial.hybrid.subscription.domain.port.output.SubscriptionRepository;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SERVICIO DE APLICACIÓN: orquesta todos los casos de uso de suscripciones.
 *
 * En este módulo hybrid se usa @Service (Spring gestiona el bean) como trade-off
 * de practicidad. En el módulo hexagonal puro, los servicios de aplicación se
 * instancian manualmente en BeanConfiguration para mantener el dominio agnóstico
 * al framework. Ambas aproximaciones son válidas; este tutorial muestra las dos.
 *
 * Este servicio combina las tres arquitecturas:
 *
 *   HEXAGONAL → implementa Use Cases (puertos de entrada), usa puertos de salida (Repository, EventPublisher)
 *   DDD       → coordina el aggregate Subscription, respeta sus invariantes, recoge domain events
 *   CQRS      → las lecturas van al SubscriptionProjectionRepository (read model),
 *               las escrituras van al SubscriptionRepository (write model)
 *
 * El método clave es persistAndProject(): guarda el aggregate, actualiza el read model
 * y publica los domain events — todo en la secuencia correcta.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionApplicationService implements
        CreateSubscriptionUseCase,
        ActivateSubscriptionUseCase,
        CancelSubscriptionUseCase,
        GetSubscriptionUseCase,
        ListActiveSubscriptionsUseCase,
        ListSubscriptionsByCustomerUseCase {

    private final SubscriptionRepository repository;
    private final SubscriptionProjectionRepository projectionRepository;
    private final SubscriptionEventPublisher eventPublisher;

    @Override
    public Subscription create(String customerId, String planCode, BigDecimal monthlyFee) {
        var subscription = Subscription.create(
                SubscriptionId.generate(),
                CustomerId.of(customerId),
                PlanCode.of(planCode),
                new Money(monthlyFee, "EUR")
        );
        return persistAndProject(subscription);
    }

    @Override
    public Subscription activate(String subscriptionId) {
        var subscription = load(subscriptionId);
        subscription.activate();
        return persistAndProject(subscription);
    }

    @Override
    public Subscription cancel(String subscriptionId, String reason) {
        var subscription = load(subscriptionId);
        subscription.cancel(reason);
        return persistAndProject(subscription);
    }

    @Override
    public Optional<SubscriptionView> findById(String subscriptionId) {
        return projectionRepository.findById(subscriptionId);
    }

    @Override
    public List<SubscriptionView> findActive() {
        return projectionRepository.findActive();
    }

    @Override
    public List<SubscriptionView> findByCustomerId(String customerId) {
        return projectionRepository.findByCustomerId(customerId);
    }

    private Subscription load(String subscriptionId) {
        return repository.findById(SubscriptionId.of(subscriptionId))
                .orElseThrow(() -> new IllegalArgumentException("Subscription no encontrada: " + subscriptionId));
    }

    private Subscription persistAndProject(Subscription subscription) {
        var saved = repository.save(subscription);
        projectionRepository.save(SubscriptionView.fromDomain(saved));
        saved.pullDomainEvents().forEach(eventPublisher::publish);
        return saved;
    }
}

