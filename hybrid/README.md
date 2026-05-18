# Hybrid Tutorial

Proyecto combinado que mezcla **Hexagonal Architecture + CQRS + DDD** en un mismo módulo.

## Idea

Este módulo muestra cómo conviven las tres arquitecturas dentro de un caso de uso real de **suscripciones**:

- **DDD** modela el dominio con un `Subscription` aggregate, value objects y eventos de dominio.
- **Hexagonal** protege el núcleo con puertos de entrada y salida.
- **CQRS** separa comandos y consultas con buses y handlers distintos.

## Ejecutar

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Puerto

- `8084`

## Flujo alto nivel

```mermaid
flowchart TD
    A[REST Controller] --> B[CommandBus / QueryBus]
    B --> C[Command/Query Handler]
    C --> D[SubscriptionApplicationService]
    D --> E[Subscription Aggregate Root]
    E --> F[Output Ports]
    F --> G[InMemorySubscriptionRepository]
    F --> H[InMemorySubscriptionProjectionRepository]
    F --> I[LogSubscriptionEventPublisher]

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef bus fill:#E8EAF6,stroke:#283593,stroke-width:1px,color:#1A237E;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
    classDef port fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1px,color:#4A148C;
    classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

    class A entry;
    class B bus;
    class C,D app;
    class E domain;
    class F port;
    class G,H,I infra;
```

