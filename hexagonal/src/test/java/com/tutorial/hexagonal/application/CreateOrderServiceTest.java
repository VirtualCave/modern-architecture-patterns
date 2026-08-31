package com.tutorial.hexagonal.application;

import com.tutorial.hexagonal.application.service.CreateOrderService;
import com.tutorial.hexagonal.domain.model.*;
import com.tutorial.hexagonal.domain.port.output.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests del servicio de aplicación CreateOrderService usando MOCKS.
 *
 * Esto demuestra una de las ventajas clave de la arquitectura hexagonal:
 * se puede probar la lógica de aplicación sin Spring, sin base de datos, sin red.
 *
 * El repositorio es un mock (Mockito). El test es instantáneo y determinista.
 * Si la lógica del servicio fuera incorrecta, este test lo detectaría.
 *
 * Comparación:
 *   - OrderTest (en domain/) → test de la entidad de dominio pura
 *   - Este test              → test de la orquestación del servicio de aplicación
 */
class CreateOrderServiceTest {

    private OrderRepository orderRepository;
    private CreateOrderService createOrderService;

    @BeforeEach
    void setUp() {
        // Mock del repositorio: no hay BD real, no hay Spring
        orderRepository = mock(OrderRepository.class);
        createOrderService = new CreateOrderService(orderRepository);

        // El mock devuelve el mismo objeto que recibe (simula un save exitoso)
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createOrder_debeCrearElPedidoYGuardarlo() {
        var items = List.of(
                new OrderItem("P1", "Laptop", Money.euros(1200.00), 1)
        );

        var order = createOrderService.createOrder("CUST-1", items);

        // El servicio debe haber llamado al repositorio
        verify(orderRepository).save(any(Order.class));

        // El pedido devuelto debe tener los datos correctos
        assertThat(order.customerId()).isEqualTo("CUST-1");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.totalAmount().amount().doubleValue()).isEqualTo(1200.00);
    }

    @Test
    void createOrder_debefallarSiCustomerIdEsNulo() {
        var items = List.of(
                new OrderItem("P1", "Laptop", Money.euros(1200.00), 1)
        );

        assertThatThrownBy(() -> createOrderService.createOrder(null, items))
                .isInstanceOf(IllegalArgumentException.class);

        // El repositorio NO debe haberse llamado si la validación falla
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_debefallarSiItemsEstaVacio() {
        assertThatThrownBy(() -> createOrderService.createOrder("CUST-1", List.of()))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(orderRepository);
    }
}
