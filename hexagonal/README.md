# Hexagonal Tutorial

Ejemplo práctico de **Arquitectura Hexagonal (Ports and Adapters)** con dominio de pedidos.

## Idea principal (en sencillo)

La app se construye alrededor del **dominio** (`Order`), no alrededor de Spring ni de la base de datos.

- El dominio define **qué necesita** mediante puertos (`domain/port/input` y `domain/port/output`).
- La infraestructura implementa **cómo se hace** (REST, JPA, logs).
- Si cambias tecnología (por ejemplo, de H2 a PostgreSQL), el dominio no cambia.

## Conceptos clave en este módulo

- **Aggregate Root**: `domain/model/Order`
  - Protege reglas de negocio: transiciones de estado, cálculo de total, cancelaciones válidas.
- **Value Objects**: `Money`, `OrderId`, `OrderItem`
  - Son inmutables y encapsulan validaciones.
- **Input Ports (casos de uso)**: `CreateOrderUseCase`, `GetOrderUseCase`, `UpdateOrderStatusUseCase`
  - Definen las operaciones que puede pedir el exterior.
- **Application Services**: `CreateOrderService`, `GetOrderService`, `UpdateOrderStatusService`
  - Orquestan el flujo (`load -> domain logic -> save -> publish events`).
- **Output Ports**: `OrderRepository`, `OrderEventPublisher`
  - Son interfaces del dominio para persistencia/publicación de eventos.
- **Adapters**:
  - Entrada: `infrastructure/adapter/input/rest/OrderController`
  - Salida: `OrderJpaAdapter` (BD) y `LogOrderEventPublisher` (eventos)
- **MapStruct (recomendado en Hexagonal)**
  - Mapper REST: `infrastructure/adapter/input/rest/mapper/OrderRestMapper`
  - Mapper persistencia: `infrastructure/adapter/output/persistence/mapper/OrderPersistenceMapper`
  - Los mapeos viven en infraestructura para no contaminar el dominio.

## Flujo visual (Mermaid)

### 1) Crear pedido

```mermaid
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

	classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
	classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
	classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
	classDef port fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1px,color:#4A148C;
	classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

	class A,B entry;
	class C,F,J port;
	class D app;
	class E,I domain;
	class G,H,K infra;
```

### 2) Cambiar estado (confirm/ship/deliver/cancel)

```mermaid
flowchart TD
	A[HTTP PATCH /api/orders/:id/*] --> B[OrderController]
	B --> C[UpdateOrderStatusUseCase]
	C --> D[UpdateOrderStatusService]
	D --> E[OrderRepository.findById]
	D --> F[Order.confirm/ship/deliver/cancel]
	D --> G[OrderRepository.save]
	D --> H[publish domain events]

	classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
	classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
	classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
	classDef port fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1px,color:#4A148C;

	class A,B entry;
	class C,E,G port;
	class D app;
	class F,H domain;
```

## Ejecutar

```bash
mvn spring-boot:run
```

## Tests

```bash
mvn test
```


