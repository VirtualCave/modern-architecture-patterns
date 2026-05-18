package com.tutorial.hexagonal.infrastructure.adapter.input.rest.mapper;

import com.tutorial.hexagonal.domain.model.Money;
import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderItem;
import com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto.OrderItemRequest;
import com.tutorial.hexagonal.infrastructure.adapter.input.rest.dto.OrderResponse;
import com.tutorial.hexagonal.infrastructure.config.HexagonalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper REST del módulo hexagonal.
 *
 * Estrategia:
 * - Para mappings entre records (OrderItemRequest <-> OrderItem) usamos
 *   autogeneración de MapStruct.
 * - Para Order -> OrderResponse usamos default porque Order es un agregado
 *   (clase normal) con API de dominio, no un record/bean JavaBean.
 */
@Mapper(config = HexagonalMapperConfig.class)
public interface OrderRestMapper {

    List<OrderItem> toDomainItems(List<OrderItemRequest> items);

    // productId, productName y quantity se mapean automáticamente por nombre.
    @Mapping(source = "price", target = "price", qualifiedByName = "toMoney")
    OrderItem toDomainItem(OrderItemRequest item);

    @Named("toMoney")
    default Money toMoney(BigDecimal amount) {
        return new Money(amount, "EUR");
    }

    default OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.id().value(),
                order.customerId(),
                order.status().name(),
                order.totalAmount().amount(),
                order.totalAmount().currency(),
                order.items().stream().map(this::toResponseItem).toList(),
                order.createdAt()
        );
    }

    // Se mantiene default por subtotal() (método de dominio, no componente del record).
    default OrderResponse.OrderItemResponse toResponseItem(OrderItem item) {
        return new OrderResponse.OrderItemResponse(
                item.productId(),
                item.productName(),
                item.price().amount(),
                item.quantity(),
                item.subtotal().amount()
        );
    }
}
