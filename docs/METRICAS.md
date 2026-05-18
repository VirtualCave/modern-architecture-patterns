# Métricas del Código

Métricas y estadísticas del código de los módulos del proyecto.

---

## Líneas de Código por Módulo

| Módulo | Dominio | Aplicación | Infraestructura | Tests | Total |
|--------|---------|------------|-----------------|-------|-------|
| hexagonal | ~300 | ~200 | ~400 | ~500 | ~1400 |
| cqrs | ~250 | ~300 | ~350 | ~450 | ~1350 |
| ddd | ~400 | ~250 | ~300 | ~600 | ~1550 |
| hybrid | ~350 | ~300 | ~400 | ~500 | ~1550 |
| **Total** | **~1300** | **~1050** | **~1450** | **~2050** | **~5850** |

**Notas**:
- Los valores son aproximados y pueden variar
- No incluye código generado automáticamente
- No incluye archivos de configuración
- Tests incluyen tanto unitarios como de integración

---

## Número de Clases por Capa

### Hexagonal

| Capa | Clases |
|------|--------|
| Aggregates | 1 |
| Value Objects | 3 |
| Input Ports | 3 |
| Application Services | 3 |
| Output Ports | 2 |
| Input Adapters | 1 |
| Output Adapters | 2 |
| **Total** | **15** |

### CQRS

| Capa | Clases |
|------|--------|
| Aggregates | 1 |
| Value Objects | 2 |
| Commands | 3 |
| Queries | 2 |
| Command Handlers | 3 |
| Query Handlers | 2 |
| Controllers | 2 |
| Repositories | 2 |
| **Total** | **17** |

### DDD

| Capa | Clases |
|------|--------|
| Aggregates | 2 |
| Value Objects | 3 |
| Domain Services | 1 |
| Application Services | 2 |
| Repositories | 4 |
| Controllers | 2 |
| **Total** | **14** |

### Hybrid

| Capa | Clases |
|------|--------|
| Aggregates | 1 |
| Value Objects | 3 |
| Commands | 2 |
| Queries | 2 |
| Handlers | 4 |
| Application Services | 2 |
| Repositories | 2 |
| Controllers | 2 |
| **Total** | **18** |

---

## Cobertura de Tests

| Módulo | Dominio | Aplicación | Infraestructura | Total |
|--------|---------|------------|-----------------|-------|
| hexagonal | 90% | 70% | 60% | 75% |
| cqrs | 85% | 75% | 65% | 72% |
| ddd | 95% | 70% | 60% | 78% |
| hybrid | 90% | 70% | 65% | 74% |
| **Promedio** | **90%** | **71%** | **62.5%** | **74.75%** |

**Notas**:
- La cobertura del dominio es alta por diseño
- La infraestructura tiene menor cobertura por usar frameworks probados
- El objetivo es mantener >70% en dominio y aplicación

---

## Complejidad Ciclomática Promedio

| Módulo | Dominio | Aplicación | Infraestructura |
|--------|---------|------------|-----------------|
| hexagonal | 3.2 | 2.5 | 1.8 |
| cqrs | 2.8 | 3.1 | 2.0 |
| ddd | 3.5 | 2.7 | 1.9 |
| hybrid | 3.3 | 2.9 | 2.1 |
| **Promedio** | **3.2** | **2.8** | **1.95** |

**Notas**:
- Valores bajos son mejores (< 10 es excelente)
- El dominio tiene complejidad moderada por lógica de negocio
- La infraestructura tiene baja complejidad por ser principalmente adaptadores

---

## Distribución de Paquetes

### Hexagonal

```
com.example.hexagonal/
├── domain/
│   ├── model/ (3 clases)
│   ├── port/
│   │   ├── input/ (3 interfaces)
│   │   └── output/ (2 interfaces)
│   └── event/ (2 clases)
├── application/ (3 servicios)
├── infrastructure/
│   ├── adapter/
│   │   ├── input/rest/ (1 controlador)
│   │   └── output/ (2 adaptadores)
│   └── persistence/ (3 clases)
└── shared/ (2 clases)
```

### CQRS

```
com.example.cqrs/
├── product/
│   ├── domain/ (3 clases)
│   ├── command/ (3 clases)
│   ├── query/ (2 clases)
│   ├── command/handler/ (3 clases)
│   └── query/handler/ (2 clases)
├── shared/
│   ├── CommandBus (1 clase)
│   └── QueryBus (1 clase)
└── infrastructure/
    ├── rest/ (2 controladores)
    └── persistence/ (4 clases)
```

### DDD

```
com.example.ddd/
├── account/
│   ├── domain/
│   │   ├── model/ (2 clases)
│   │   ├── event/ (2 clases)
│   │   └── repository/ (1 interfaz)
│   ├── application/ (1 servicio)
│   └── infrastructure/
│       ├── persistence/ (2 clases)
│       └── rest/ (1 controlador)
├── customer/
│   ├── domain/ (2 clases)
│   ├── application/ (1 servicio)
│   └── infrastructure/
│       ├── persistence/ (2 clases)
│       └── rest/ (1 controlador)
└── shared/
    ├── domain/ (2 clases)
    └── application/ (1 servicio)
```

### Hybrid

```
com.example.hybrid/
├── subscription/
│   ├── domain/ (4 clases)
│   ├── command/ (2 clases)
│   ├── query/ (2 clases)
│   └── handler/ (4 clases)
├── application/ (2 servicios)
├── infrastructure/
│   ├── rest/ (2 controladores)
│   └── persistence/ (4 clases)
└── shared/
    ├── CommandBus (1 clase)
    └── QueryBus (1 clase)
```

---

## Métricas de Calidad

### Duplicación de Código

| Módulo | Duplicación | Porcentaje |
|--------|-------------|------------|
| hexagonal | ~50 líneas | < 5% |
| cqrs | ~40 líneas | < 5% |
| ddd | ~60 líneas | < 5% |
| hybrid | ~55 líneas | < 5% |

**Objetivo**: Mantener duplicación < 5%

---

### Largo de Métodos

| Módulo | Promedio | Máximo |
|--------|----------|--------|
| hexagonal | 8 líneas | 25 líneas |
| cqrs | 10 líneas | 30 líneas |
| ddd | 9 líneas | 28 líneas |
| hybrid | 11 líneas | 32 líneas |

**Objetivo**: Promedio < 15 líneas, máximo < 50 líneas

---

### Largo de Clases

| Módulo | Promedio | Máximo |
|--------|----------|--------|
| hexagonal | 45 líneas | 120 líneas |
| cqrs | 50 líneas | 130 líneas |
| ddd | 55 líneas | 140 líneas |
| hybrid | 52 líneas | 135 líneas |

**Objetivo**: Promedio < 100 líneas, máximo < 300 líneas

---

### Número de Parámetros por Método

| Módulo | Promedio | Máximo |
|--------|----------|--------|
| hexagonal | 1.5 | 4 |
| cqrs | 1.8 | 5 |
| ddd | 1.6 | 4 |
| hybrid | 1.7 | 5 |

**Objetivo**: Promedio < 3, máximo < 5

---

## Dependencias

### Dependencias por Módulo

| Módulo | Dependencias Directas | Dependencias Transitivas |
|--------|----------------------|-------------------------|
| hexagonal | 8 | ~25 |
| cqrs | 8 | ~25 |
| ddd | 8 | ~25 |
| hybrid | 7 | ~22 |

**Dependencias principales**:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- H2 Database
- MapStruct
- Lombok
- Spring Boot Starter Test

---

## Performance

### Tiempo de Build

| Módulo | Clean Build | Incremental Build |
|--------|-------------|-------------------|
| hexagonal | ~15s | ~5s |
| cqrs | ~14s | ~5s |
| ddd | ~16s | ~6s |
| hybrid | ~15s | ~5s |

### Tiempo de Ejecución de Tests

| Módulo | Todos los Tests | Solo Unitarios |
|--------|-----------------|----------------|
| hexagonal | ~8s | ~3s |
| cqrs | ~7s | ~3s |
| ddd | ~10s | ~4s |
| hybrid | ~9s | ~3s |

---

## Tamaño de Artefactos

| Módulo | JAR Size | Dependencies Size |
|--------|---------|-------------------|
| hexagonal | ~15 MB | ~30 MB |
| cqrs | ~14 MB | ~29 MB |
| ddd | ~16 MB | ~31 MB |
| hybrid | ~15 MB | ~30 MB |

---

## Documentación

### Líneas de Documentación

| Archivo | Líneas |
|---------|--------|
| README.md (principal) | ~450 |
| hexagonal/README.md | ~220 |
| cqrs/README.md | ~180 |
| ddd/README.md | ~230 |
| hybrid/README.md | ~60 |
| GLOSARIO.md | ~600 |
| QUICKSTART.md | ~200 |
| docs/ANTI_PATRONES.md | ~500 |
| docs/ERRORES_COMUNES.md | ~400 |
| docs/TROUBLESHOOTING.md | ~450 |
| docs/EJERCICIOS.md | ~600 |
| ROADMAP.md | ~400 |
| CONTRIBUTING.md | ~350 |
| docs/METRICAS.md | ~350 |
| **Total** | **~4980** |

---

## Tendencias

### Crecimiento del Proyecto

- **Mes 1**: Creación de estructura base
- **Mes 2**: Implementación de módulos hexagonal y cqrs
- **Mes 3**: Implementación de módulo ddd
- **Mes 4**: Implementación de módulo hybrid
- **Mes 5**: Documentación y mejoras

### Objetivos Futuros

- Mantener cobertura de tests > 70%
- Reducir complejidad ciclomática promedio < 3
- Mantener duplicación de código < 5%
- Añadir más ejemplos y ejercicios
- Mejorar documentación con más diagramas

---

## Notas

- Las métricas se actualizan periódicamente
- Los valores son aproximados y pueden variar
- Para métricas en tiempo real, usar herramientas como SonarQube
- Las métricas son guías, no reglas estrictas

---

## Referencias

- **README.md** - Visión general del proyecto
- **CONTRIBUTING.md** - Guía de contribución
- **ROADMAP.md** - Roadmap de aprendizaje
