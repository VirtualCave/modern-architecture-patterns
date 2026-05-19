# Hybrid Tutorial

Proyecto combinado que mezcla **Hexagonal Architecture + CQRS + DDD** en un mismo módulo.

## Idea

Este módulo muestra cómo conviven las tres arquitecturas dentro de un caso de uso real de **suscripciones**:

- **DDD** modela el dominio con un `Subscription` aggregate, value objects y eventos de dominio.
- **Hexagonal** protege el núcleo con puertos de entrada y salida.
- **CQRS** separa comandos y consultas con buses y handlers distintos.


## Puerto

- `8084`

## Leyenda de colores

- `entry`: entrada HTTP/controladores (`fill: #DBEAFE`, `stroke: #1D4ED8`)
- `bus`: buses de CQRS (`fill: #E0E7FF`, `stroke: #3730A3`)
- `app`: handlers y servicios de aplicación (`fill: #DCFCE7`, `stroke: #15803D`)
- `domain`: aggregate y reglas de dominio (`fill: #FEF3C7`, `stroke: #D97706`)
- `port`: puertos de salida (`fill: #F3E8FF`, `stroke: #7E22CE`)
- `infra`: adaptadores e infraestructura (`fill: #FFEDD5`, `stroke: #C2410C`)

## Flujo alto nivel

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
    A[REST Controller] --> B[CommandBus / QueryBus]
    B --> C[Command/Query Handler]
    C --> D[SubscriptionApplicationService]
    D --> E[Subscription Aggregate Root]
    E --> F[Output Ports]
    F --> G[InMemorySubscriptionRepository]
    F --> H[InMemorySubscriptionProjectionRepository]
    F --> I[LogSubscriptionEventPublisher]

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef bus fill:#E0E7FF,stroke:#3730A3,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class A entry;
    class B bus;
    class C,D app;
    class E domain;
    class F port;
    class G,H,I infra;
```

