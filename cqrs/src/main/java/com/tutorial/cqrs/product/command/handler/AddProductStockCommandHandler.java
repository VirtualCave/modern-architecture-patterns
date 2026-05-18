package com.tutorial.cqrs.product.command.handler;

import com.tutorial.cqrs.product.command.AddProductStockCommand;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.domain.ProductRepository;
import com.tutorial.cqrs.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddProductStockCommandHandler implements CommandHandler<AddProductStockCommand> {

    private final ProductRepository repository;


    @Override
    public void handle(AddProductStockCommand command) {
        var product = repository.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + command.productId()));

        product.addStock(command.quantity());
        repository.save(product);
    }

    @Override
    public Class<AddProductStockCommand> commandType() {
        return AddProductStockCommand.class;
    }
}

