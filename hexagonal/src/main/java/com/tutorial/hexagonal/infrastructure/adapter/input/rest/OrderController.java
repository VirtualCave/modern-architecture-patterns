package com.tutorial.hexagonal.infrastructure.adapter.input.rest;

import com.tutorial.hexagonal.domain.port.input.CreateOrderUseCase;
import com.tutorial.hexagonal.domain.port.input.GetOrderUseCase;
import com.tutorial.hexagonal.domain.port.input.UpdateOrderStatusUseCase;
import com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto.CancelOrderRequest;
import com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto.CreateOrderRequest;
import com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto.OrderResponse;
import com.tutorial.hexagonal.infrastructure.adapter.input.rest.mapper.OrderRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * ADAPTADOR DE ENTRADA REST (Input Adapter)
 *
 * Este controlador NO contiene lógica de negocio.
 * Su única responsabilidad es:
 *   1. Recibir la petición HTTP
 *   2. Convertir el DTO a objetos del dominio
 *   3. Llamar al puerto de entrada (Use Case)
 *   4. Convertir la respuesta del dominio a DTO HTTP
 *
 * Usa ProblemDetail (RFC 7807, Spring Boot 3+) para errores estandarizados.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final OrderRestMapper orderRestMapper;


    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        var items = orderRestMapper.toDomainItems(request.items());

        var order = createOrderUseCase.createOrder(request.customerId(), items);
        var response = orderRestMapper.toResponse(order);

        return ResponseEntity
                .created(URI.create("/api/orders/" + order.id().value()))
                .body(response);
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return getOrderUseCase.findAll().stream()
                .map(orderRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable String id) {
        return getOrderUseCase.findById(id)
                .map(orderRestMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<OrderResponse> getByCustomer(@PathVariable String customerId) {
        return getOrderUseCase.findByCustomer(customerId).stream()
                .map(orderRestMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable String id) {
        return orderRestMapper.toResponse(updateOrderStatusUseCase.confirm(id));
    }

    @PatchMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable String id) {
        return orderRestMapper.toResponse(updateOrderStatusUseCase.ship(id));
    }

    @PatchMapping("/{id}/deliver")
    public OrderResponse deliver(@PathVariable String id) {
        return orderRestMapper.toResponse(updateOrderStatusUseCase.deliver(id));
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable String id,
                                @RequestBody(required = false) CancelOrderRequest request) {
        var reason = request != null ? request.reason() : "Sin motivo especificado";
        return orderRestMapper.toResponse(updateOrderStatusUseCase.cancel(id, reason));
    }

    // -----------------------------------------------------------------
    // Manejo de errores con ProblemDetail (RFC 7807)
    // -----------------------------------------------------------------

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.CONFLICT, ex.getMessage()
        );
        problem.setTitle("Transición de estado inválida");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatus.BAD_REQUEST, ex.getMessage()
        );
        problem.setTitle("Argumento inválido");
        return ResponseEntity.badRequest().body(problem);
    }
}

