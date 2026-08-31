package com.tutorial.cqrs.product.command.handler;

import com.tutorial.cqrs.product.command.CreateProductCommand;
import com.tutorial.cqrs.product.domain.Product;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.domain.ProductRepository;
import com.tutorial.cqrs.shared.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * COMMAND HANDLER: CreateProductCommandHandler
 *
 * En CQRS, cada comando tiene su propio handler con una responsabilidad única.
 * Compara con la arquitectura hexagonal: allí teníamos un Use Case + Application Service.
 * Aquí el handler ES el "caso de uso" de escritura, sin intermediario adicional.
 *
 * ¿Por qué no un servicio genérico ProductService con todos los métodos?
 * Porque cada handler puede evolucionar, testarse y desplegarse de forma independiente.
 * Añadir una nueva operación = añadir un nuevo Command + Handler. El bus no cambia.
 *
 * Flujo: CommandBus → este handler → write model (Product) → repositorio de escritura
 */
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

