package com.tutorial.cqrs.infrastructure.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración centralizada de MapStruct para el módulo CQRS.
 *
 * Todos los @Mapper de infraestructura deben declarar: @Mapper(config = CqrsMapperConfig.class)
 *
 * Opciones configuradas:
 *
 * - componentModel = "spring":
 *     Los mappers generados se registran como beans de Spring.
 *
 * - unmappedTargetPolicy = ERROR:
 *     La compilación falla si un campo destino queda sin mapear.
 *     En CQRS es especialmente importante porque write model (Product)
 *     y read model (ProductView) tienen estructuras distintas.
 *     Para saltar un campo: @Mapping(target = "campo", ignore = true)
 *
 * - nullValueMappingStrategy = RETURN_NULL:
 *     Objeto de entrada null → devuelve null.
 *
 * - nullValuePropertyMappingStrategy = IGNORE:
 *     Propiedad null en el origen no machaca el valor existente en el destino.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CqrsMapperConfig {}

