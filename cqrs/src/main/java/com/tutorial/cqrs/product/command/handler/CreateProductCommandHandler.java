package com.tutorial.cqrs.product.command.handler;

import com.tutorial.cqrs.product.command.CreateProductCommand;
import com.tutorial.cqrs.product.domain.Product;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.domain.ProductRepository;
import com.tutorial.cqrs.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand> {

    private final ProductRepository repository;


    @Override
    public void handle(CreateProductCommand command) {
        var product = Product.create(
                ProductId.generate(),
                command.name(),
                command.description(),
                command.price(),
                command.stock()
        );
        repository.save(product);
    }

    @Override
    public Class<CreateProductCommand> commandType() {
        return CreateProductCommand.class;
    }
}

