package com.tutorial.cqrs.infrastructure.persistence.mapper;

import com.tutorial.cqrs.infrastructure.config.CqrsMapperConfig;
import com.tutorial.cqrs.infrastructure.persistence.entity.ProductEntity;
import com.tutorial.cqrs.product.domain.Product;
import com.tutorial.cqrs.product.domain.ProductId;
import com.tutorial.cqrs.product.readmodel.ProductView;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper de persistencia para CQRS.
 *
 * Decisión de simplicidad:
 * - Product -> ProductEntity se hace con método default, porque Product
 *   expone acceso estilo dominio (id(), name(), ...) y no getters JavaBean.
 *   Así evitamos expression y mantenemos el código directo.
 * - ProductEntity -> ProductView sí lo genera MapStruct automáticamente,
 *   porque Entity usa getters JavaBean y ProductView es record de lectura.
 */
@Mapper(config = CqrsMapperConfig.class)
public interface ProductPersistenceMapper {

    default ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.id().value(),
                product.name(),
                product.description(),
                product.price(),
                product.stock(),
                product.isActive(),
                product.createdAt()
        );
    }

    default Product toDomain(ProductEntity entity) {
        return Product.reconstitute(
                ProductId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    ProductView toView(ProductEntity entity);

    List<ProductView> toViewList(List<ProductEntity> entities);
}
