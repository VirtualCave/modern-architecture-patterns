package com.tutorial.cqrs.product.command.handler;

import com.tutorial.cqrs.product.command.DeactivateProductCommand;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.domain.ProductRepository;
import com.tutorial.cqrs.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateProductCommandHandler implements CommandHandler<DeactivateProductCommand> {

    private final ProductRepository repository;


    @Override
    public void handle(DeactivateProductCommand command) {
        var product = repository.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.productId()));

        product.deactivate();
        repository.save(product);
    }

    @Override
    public Class<DeactivateProductCommand> commandType() {
        return DeactivateProductCommand.class;
    }
}

