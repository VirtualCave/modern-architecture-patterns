package com.tutorial.ddd.shared.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración centralizada de MapStruct para el módulo DDD.
 *
 * Compartida por todos los Bounded Contexts (account y customer).
 * Todos los @Mapper de infraestructura deben declarar: @Mapper(config = DddMapperConfig.class)
 *
 * Opciones configuradas:
 *
 * - componentModel = "spring":
 *     Los mappers son beans de Spring, inyectables en los adapters de persistencia.
 *
 * - unmappedTargetPolicy = ERROR:
 *     En DDD los aggregates tienen Value Objects (Money, AccountId, CustomerId)
 *     que se aplanan en la entidad JPA. ERROR garantiza que ningún campo
 *     de la entidad quede sin cubrir silenciosamente.
 *
 * - nullValueMappingStrategy = RETURN_NULL:
 *     Null entrada → null salida.
 *
 * - nullValuePropertyMappingStrategy = IGNORE:
 *     No sobreescribir campos del destino con null al hacer actualizaciones parciales.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DddMapperConfig {}

