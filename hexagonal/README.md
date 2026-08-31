# Hexagonal Tutorial

## Índice de contenidos

- [Idea principal (en sencillo)](#idea-principal-en-sencillo)
- [Conceptos clave en este módulo](#conceptos-clave-en-este-módulo)
- [Flujo visual](#flujo-visual)
- [Diagrama de Secuencia: Crear Pedido](#diagrama-de-secuencia-crear-pedido)
- [Diagrama de Secuencia: Cambiar Estado](#diagrama-de-secuencia-cambiar-estado)
- [Diagrama de Estado: Ciclo de Vida de un Pedido](#diagrama-de-estado-ciclo-de-vida-de-un-pedido)

## Idea principal (en sencillo)

La app se construye alrededor del **dominio** (`Order`), no alrededor de Spring ni de la base de datos.

- El dominio define **qué necesita** mediante puertos (`domain/port/input` y `domain/port/output`).
- La infraestructura implementa **cómo se hace** (REST, JPA, logs).
- Si cambias tecnología (por ejemplo, de H2 a PostgreSQL), el dominio no cambia.

## Conceptos clave en este módulo

- **Entidad de dominio**: `domain/model/Order`
  - Protege reglas de negocio: transiciones de estado, cálculo de total, cancelaciones válidas.
- **Tipos de dominio**: `Money`, `OrderId`, `OrderItem`
  - Son inmutables y encapsulan validaciones.
- **Input Ports (casos de uso)**: `CreateOrderUseCase`, `GetOrderUseCase`, `UpdateOrderStatusUseCase`
  - Definen las operaciones que puede pedir el exterior.
- **Application Services**: `CreateOrderService`, `GetOrderService`, `UpdateOrderStatusService`
  - Orquestan el flujo (`load -> domain logic -> save`).
- **Output Ports**: `OrderRepository`
  - Son interfaces del dominio para persistencia.
- **Adapters**:
  - Entrada: `infrastructure/adapter/input/rest/OrderController`
  - Salida: `OrderJpaAdapter` (BD)
- **MapStruct (recomendado en Hexagonal)**
  - Mapper REST: `infrastructure/adapter/input/rest/mapper/OrderRestMapper`
  - Mapper persistencia: `infrastructure/adapter/output/persistence/mapper/OrderPersistenceMapper`
  - Los mapeos viven en infraestructura para no contaminar el dominio.


## Flujo visual

### Crear pedido

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
	A[HTTP POST /api/orders] --> B[OrderController]
	B --> C[CreateOrderUseCase]
	C --> D[CreateOrderService]
	D --> E[Order.create]
	D --> F[OrderRepository.save]
	F --> G[OrderJpaAdapter]
	G --> H[SpringDataOrderRepository]
	D --> I[Order.pullDomainEvents]
	I --> J[OrderEventPublisher.publish]
	J --> K[LogOrderEventPublisher]

  linkStyle default stroke:#94A3B8,stroke-width:2px;

  classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
  classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
  classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
  classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
  classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

	class A,B entry;
	class C,F,J port;
	class D app;
	class E,I domain;
	class G,H,K infra;
```

### Cambiar estado (confirm/ship/deliver/cancel)

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
	A[HTTP PATCH /api/orders/:id/*] --> B[OrderController]
	B --> C[UpdateOrderStatusUseCase]
	C --> D[UpdateOrderStatusService]
	D --> E[OrderRepository.findById]
	D --> F[Order.confirm/ship/deliver/cancel]
	D --> G[OrderRepository.save]
	D --> H[publish domain events]

  linkStyle default stroke:#94A3B8,stroke-width:2px;

  classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
  classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
  classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
  classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;

	class A,B entry;
	class C,E,G port;
	class D app;
	class F,H domain;
```

## Diagrama de Secuencia: Crear Pedido

Este diagrama resume el flujo completo de una petición nueva de pedido:

1. El cliente hace la llamada HTTP.
2. El controlador solo traduce la petición a un comando.
3. El caso de uso y el servicio de aplicación coordinan el proceso.
4. El aggregate `Order` valida sus reglas y calcula su estado.
5. El repositorio guarda el resultado y el publicador emite los eventos.

Piensa en él como una línea muy simple: **recibir -> traducir -> ejecutar -> validar -> guardar -> notificar**.

```mermaid
%%{init: {'themeVariables': {'background':'transparent','lineColor':'#94A3B8'}}}%%
sequenceDiagram
    participant Client
    participant Controller
    participant UseCase
    participant Service
    participant Order
    participant Repository
    participant DB

    Client->>Controller: POST /api/orders
    Controller->>Controller: toCommand(request)
    Controller->>UseCase: create(customerId, items)
    UseCase->>Service: create(customerId, items)
    Service->>Order: Order.create(customerId, items)
    Order->>Order: validate items
    Order->>Order: calculate total
    Order-->>Service: Order
    Service->>Repository: save(order)
    Repository->>DB: INSERT
    DB-->>Repository: Order
    Repository-->>Service: Order
    Service-->>UseCase: Order
    UseCase-->>Controller: Order
    Controller->>Controller: toResponse(order)
    Controller-->>Client: 201 Created + Order
```

**Cómo leerlo rápido:** el controlador no decide reglas de negocio; solo conecta la API con el caso de uso. La parte importante ocurre en `Service` y en `Order`.

## Diagrama de Secuencia: Cambiar Estado

Este segundo diagrama muestra el caso más común después de crear el pedido: cambiar su estado.

1. El cliente pide un cambio de estado.
2. El controlador convierte la petición en comando.
3. El servicio carga el pedido desde el repositorio.
4. El aggregate comprueba si la transición es válida.
5. Si la transición es correcta, se guarda el nuevo estado y se publica el evento.

La idea clave es esta: **el dominio decide si el cambio es válido; la infraestructura solo ejecuta la orden**.

```mermaid
%%{init: {'themeVariables': {'background':'transparent','lineColor':'#94A3B8'}}}%%
sequenceDiagram
    participant Client
    participant Controller
    participant UseCase
    participant Service
    participant Order
    participant Repository

    Client->>Controller: PATCH /api/orders/:id/confirm
    Controller->>UseCase: updateStatus(id, CONFIRM)
    UseCase->>Service: updateStatus(id, CONFIRM)
    Service->>Repository: findById(id)
    Repository-->>Service: Order
    Service->>Order: confirm()
    Order->>Order: validate status transition
    Order-->>Service: success
    Service->>Repository: save(order)
    Repository-->>Service: saved
    Service-->>UseCase: Order
    UseCase-->>Controller: Order
    Controller-->>Client: 200 OK
```

**Resumen mental:** pedir cambio -> cargar pedido -> validar transición -> guardar -> publicar evento -> responder.

## Diagrama de Estado: Ciclo de Vida de un Pedido

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
stateDiagram-v2
    [*] --> PENDING: create()
    PENDING --> CONFIRMED: confirm()
    PENDING --> CANCELLED: cancel()
    CONFIRMED --> SHIPPED: ship()
    SHIPPED --> DELIVERED: deliver()
    DELIVERED --> [*]
    CANCELLED --> [*]
    
    note right of PENDING
        Estado inicial
        Puede cancelarse
    end note
    
    note right of CONFIRMED
        No se puede cancelar
        Puede enviarse
    end note
    
    note right of DELIVERED
        Estado final
        No hay más transiciones
    end note
```




