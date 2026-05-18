package com.tutorial.hexagonal.domain.port.output;

import com.tutorial.hexagonal.domain.event.OrderEvent;

/**
 * PUERTO DE SALIDA: publicación de eventos de dominio.
 *
 * El dominio no sabe si los eventos van a Kafka, RabbitMQ, logs, etc.
 * Para este tutorial, la implementación simplemente logea.
 * En producción se reemplazaría por un adaptador real sin tocar el dominio.
 */
public interface OrderEventPublisher {

    void publish(OrderEvent event);
}

