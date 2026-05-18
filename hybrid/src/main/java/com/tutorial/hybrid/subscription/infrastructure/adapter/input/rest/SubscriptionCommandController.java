package com.tutorial.hybrid.subscription.infrastructure.adapter.input.rest;

import com.tutorial.hybrid.shared.CommandBus;
import com.tutorial.hybrid.subscription.command.ActivateSubscriptionCommand;
import com.tutorial.hybrid.subscription.command.CancelSubscriptionCommand;
import com.tutorial.hybrid.subscription.command.CreateSubscriptionCommand;
import com.tutorial.hybrid.subscription.domain.Subscription;
import com.tutorial.hybrid.subscription.readmodel.SubscriptionView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/commands/subscriptions")
@RequiredArgsConstructor
public class SubscriptionCommandController {

    private final CommandBus commandBus;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        commandBus.dispatch(new CreateSubscriptionCommand(request.customerId(), request.planCode(), request.monthlyFee()));
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable String id) {
        commandBus.dispatch(new ActivateSubscriptionCommand(id));
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id, @RequestBody(required = false) CancelSubscriptionRequest request) {
        commandBus.dispatch(new CancelSubscriptionCommand(id, request != null ? request.reason() : "Sin motivo especificado"));
        return ResponseEntity.accepted().build();
    }

    public record CreateSubscriptionRequest(
            @NotBlank String customerId,
            @NotBlank String planCode,
            @Positive BigDecimal monthlyFee
    ) {}

    public record CancelSubscriptionRequest(String reason) {}
}

