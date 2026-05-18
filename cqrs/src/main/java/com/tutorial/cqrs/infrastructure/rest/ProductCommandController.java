package com.tutorial.cqrs.infrastructure.rest;

import com.tutorial.cqrs.product.command.AddProductStockCommand;
import com.tutorial.cqrs.product.command.CreateProductCommand;
import com.tutorial.cqrs.product.command.DeactivateProductCommand;
import com.tutorial.cqrs.product.command.UpdateProductPriceCommand;
import com.tutorial.cqrs.shared.CommandBus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/commands/products")
@RequiredArgsConstructor
public class ProductCommandController {

    private final CommandBus commandBus;


    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateProductRequest request) {
        commandBus.dispatch(new CreateProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        ));
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Void> updatePrice(@PathVariable String id, @Valid @RequestBody UpdatePriceRequest request) {
        commandBus.dispatch(new UpdateProductPriceCommand(id, request.newPrice()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<Void> addStock(@PathVariable String id, @Valid @RequestBody AddStockRequest request) {
        commandBus.dispatch(new AddProductStockCommand(id, request.quantity()));
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        commandBus.dispatch(new DeactivateProductCommand(id));
        return ResponseEntity.accepted().build();
    }

    public record CreateProductRequest(
            @NotBlank String name,
            @NotBlank String description,
            @Positive BigDecimal price,
            @Positive int stock
    ) {}

    public record UpdatePriceRequest(@Positive BigDecimal newPrice) {}

    public record AddStockRequest(@Positive int quantity) {}
}

