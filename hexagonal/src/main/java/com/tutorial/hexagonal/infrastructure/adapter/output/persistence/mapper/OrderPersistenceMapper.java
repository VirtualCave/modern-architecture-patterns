package com.tutorial.hexagonal.infrastructure.adapter.output.persistence.mapper;

import com.tutorial.hexagonal.domain.model.Money;
import com.tutorial.hexagonal.domain.model.Order;
import com.tutorial.hexagonal.domain.model.OrderId;
import com.tutorial.hexagonal.domain.model.OrderItem;
import com.tutorial.hexagonal.domain.model.OrderStatus;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.entity.OrderEntity;
import com.tutorial.hexagonal.infrastructure.adapter.output.persistence.entity.OrderItemEntity;
import com.tutorial.hexagonal.infrastructure.config.HexagonalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper de persistencia del módulo hexagonal.
 *
 * Estrategia:
 * - En mappings entre records (OrderItem -> OrderItemEntity) priorizamos
 *   autogeneración de MapStruct.
 * - En Order <-> OrderEntity usamos default porque Order es agregado de dominio
 *   (clase normal con factories/invariantes), no record JavaBean clásico.
 */
@Mapper(config = HexagonalMapperConfig.class)
public interface OrderPersistenceMapper {

    default OrderEntity toEntity(Order order) {
        var entity = new OrderEntity(
                order.id().value(),
                order.customerId(),
                order.status().name(),
                order.totalAmount().amount(),
                order.totalAmount().currency(),
                order.createdAt()
        );

        entity.setItems(toItemEntities(order.items()));
        return entity;
    }

    List<OrderItemEntity> toItemEntities(List<OrderItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(source = "price.amount", target = "price")
    @Mapping(source = "price.currency", target = "currency")
    OrderItemEntity toItemEntity(OrderItem item);

    default Order toDomain(OrderEntity entity) {
        var items = entity.getItems().stream().map(this::toDomainItem).toList();
        return Order.reconstitute(
                OrderId.of(entity.getId()),
                entity.getCustomerId(),
                items,
                OrderStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }

    default OrderItem toDomainItem(OrderItemEntity item) {
        return new OrderItem(
                item.getProductId(),
                item.getProductName(),
                new Money(item.getPrice(), item.getCurrency()),
                item.getQuantity()
        );
    }
}
