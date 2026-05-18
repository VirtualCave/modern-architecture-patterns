package com.tutorial.cqrs.product.command.handler;

import com.tutorial.cqrs.product.command.UpdateProductPriceCommand;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.domain.ProductRepository;
import com.tutorial.cqrs.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateProductPriceCommandHandler implements CommandHandler<UpdateProductPriceCommand> {

    private final ProductRepository repository;


    @Override
    public void handle(UpdateProductPriceCommand command) {
        var product = repository.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.productId()));

        product.updatePrice(command.newPrice());
        repository.save(product);
    }

    @Override
    public Class<UpdateProductPriceCommand> commandType() {
        return UpdateProductPriceCommand.class;
    }
}

