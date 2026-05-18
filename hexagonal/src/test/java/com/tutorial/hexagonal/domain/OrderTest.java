package com.tutorial.hexagonal.domain;

import com.tutorial.hexagonal.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests del agregado Order – DOMINIO PURO.
 *
 * Estos tests son rapidísimos: no usan Spring, ni BD, ni red.
 * Son tests unitarios que verifican las reglas de negocio del dominio.
 * Esta es una de las grandes ventajas de la Arquitectura Hexagonal.
 */
class OrderTest {

    private Order crearPedidoEjemplo() {
        var items = List.of(
                new OrderItem("P1", "Laptop", Money.euros(1200.00), 1),
                new OrderItem("P2", "Mouse", Money.euros(25.00), 2)
        );
        return Order.create(OrderId.generate(), "CUST-1", items);
    }

    @Test
    void create_debeCalcularTotalCorrectamente() {
        var order = crearPedidoEjemplo();

        // 1200 + (25 * 2) = 1250
        assertThat(order.totalAmount().amount().doubleValue()).isEqualTo(1250.00);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void create_debeGenerarEventoOrderCreated() {
        var order = crearPedidoEjemplo();
        var events = order.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.getFirst()).isInstanceOf(
                com.tutorial.hexagonal.domain.event.OrderEvent.OrderCreated.class
        );
    }

    @Test
    void confirm_debeTransicionarDePendingAConfirmed() {
        var order = crearPedidoEjemplo();
        order.pullDomainEvents(); // limpiar evento de creación

        order.confirm();

        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.pullDomainEvents()).hasSize(1);
    }

    @Test
    void confirm_debefallarSiNoEstaPending() {
        var order = crearPedidoEjemplo();
        order.confirm();

        // Intentar confirmar de nuevo el mismo pedido
        assertThatThrownBy(order::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    void flujoCompleto_pendingConfirmedShippedDelivered() {
        var order = crearPedidoEjemplo();

        order.confirm();
        order.ship();
        order.deliver();

        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void cancel_debePoderCancelarPedidoEnviado() {
        var order = crearPedidoEjemplo();
        order.confirm();
        order.ship();

        order.cancel("El cliente cambió de opinión");

        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancel_noPuedesCancelarPedidoEntregado() {
        var order = crearPedidoEjemplo();
        order.confirm();
        order.ship();
        order.deliver();

        assertThatThrownBy(() -> order.cancel("Tarde"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void create_debefallarSinItems() {
        assertThatThrownBy(() -> Order.create(OrderId.generate(), "CUST-1", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un ítem");
    }

    @Test
    void money_debeCalcularCorrectamente() {
        var precio = Money.euros(100.00);
        var doble = precio.multiply(2);

        assertThat(doble.amount().doubleValue()).isEqualTo(200.00);
        assertThatThrownBy(() -> precio.subtract(doble))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

