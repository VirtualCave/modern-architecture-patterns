package com.tutorial.hexagonal.infrastructure.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración centralizada de MapStruct para el módulo Hexagonal.
 *
 * Todos los @Mapper de infraestructura deben declarar: @Mapper(config = HexagonalMapperConfig.class)
 * Así heredan estas reglas sin repetirlas en cada mapper.
 *
 * Opciones configuradas:
 *
 * - componentModel = "spring":
 *     Los mappers generados se registran como beans de Spring (@Component).
 *     Puedes inyectarlos con @Autowired o constructor injection.
 *
 * - unmappedTargetPolicy = ERROR:
 *     Si un campo del objeto destino NO está mapeado (ni explícita ni automáticamente),
 *     la compilación FALLA. Esto evita que se queden campos en null sin darnos cuenta.
 *     Para ignorar un campo explícitamente: @Mapping(target = "campo", ignore = true)
 *
 * - nullValueMappingStrategy = RETURN_NULL:
 *     Si el objeto de entrada es null, el método de mapeo devuelve null.
 *     Comportamiento seguro por defecto.
 *
 * - nullValuePropertyMappingStrategy = IGNORE:
 *     Si una propiedad individual del origen es null, NO sobreescribe el campo
 *     del destino. Útil en métodos de actualización parcial (@MappingTarget).
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface HexagonalMapperConfig {}

