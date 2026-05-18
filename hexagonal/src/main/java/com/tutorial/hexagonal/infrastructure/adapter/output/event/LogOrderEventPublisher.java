package com.tutorial.hexagonal.infrastructure.adapter.output.event;

import com.tutorial.hexagonal.domain.event.OrderEvent;
import com.tutorial.hexagonal.domain.port.output.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ADAPTADOR DE SALIDA: publicador de eventos (implementación de log).
 *
 * En un sistema real, aquí iría Kafka, RabbitMQ, Spring ApplicationEvents, etc.
 * Para el tutorial usamos logs para mostrar que los eventos se generan.
 *
 * Java 26 - Pattern matching en switch:
 * El compilador verifica que cubrimos TODOS los casos del sealed interface.
 * Si añadimos un nuevo evento al sealed interface, aquí aparecería error de compilación.
 */
public class LogOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LogOrderEventPublisher.class);

    @Override
    public void publish(OrderEvent event) {
        // Pattern matching switch (Java 21+): exhaustivo gracias al sealed interface
        var message = switch (event) {
            case OrderEvent.OrderCreated e ->
                "📦 PEDIDO CREADO: id=%s, cliente=%s, total=%s"
                        .formatted(e.orderId(), e.customerId(), e.totalAmount());

            case OrderEvent.OrderConfirmed e ->
                "✅ PEDIDO CONFIRMADO: id=%s".formatted(e.orderId());

            case OrderEvent.OrderShipped e ->
                "🚚 PEDIDO ENVIADO: id=%s".formatted(e.orderId());

            case OrderEvent.OrderDelivered e ->
                "🎉 PEDIDO ENTREGADO: id=%s".formatted(e.orderId());

            case OrderEvent.OrderCancelled e ->
                "❌ PEDIDO CANCELADO: id=%s, motivo=%s".formatted(e.orderId(), e.reason());
        };

        log.info("[ORDER EVENT] {}", message);
    }
}

