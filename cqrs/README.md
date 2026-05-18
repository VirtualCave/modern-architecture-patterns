# CQRS Tutorial

Ejemplo práctico de **CQRS (Command Query Responsibility Segregation)** con catálogo de productos.

## Idea principal (en sencillo)

En CQRS separas claramente:

- **Commands (escritura)**: cambian estado.
- **Queries (lectura)**: solo consultan datos.

Así evitas mezclar reglas de negocio de escritura con necesidades de consulta.

## Conceptos clave en este módulo

- **Command side**
  - API: `infrastructure/rest/ProductCommandController`
  - Bus: `shared/CommandBus` (`SimpleCommandBus`)
  - Handlers: `product/command/handler/*`
  - Write model: `product/domain/Product`
  - Repo escritura: `ProductRepository` -> `ProductWriteRepositoryAdapter`
- **Query side**
  - API: `infrastructure/rest/ProductQueryController`
  - Bus: `shared/QueryBus` (`SimpleQueryBus`)
  - Handlers: `product/query/handler/*`
  - Read model: `product/readmodel/ProductView`
  - Repo lectura: `ProductViewRepository` -> `ProductReadRepositoryAdapter`
- **MapStruct (recomendado en CQRS)**
  - Mapper único de persistencia: `infrastructure/persistence/mapper/ProductPersistenceMapper`
  - Centraliza conversiones entre `Product` (write model), `ProductEntity` y `ProductView` (read model).

## Flujo visual (Mermaid)

### 1) Flujo de escritura (Command)

```mermaid
flowchart TD
    A[HTTP POST/PATCH/DELETE /api/commands/products] --> B[ProductCommandController]
    B --> C[CommandBus.dispatch]
    C --> D[CommandHandler específico]
    D --> E[Product write model]
    D --> F[ProductRepository.save]
    F --> G[ProductWriteRepositoryAdapter]
    G --> H[SpringDataProductWriteRepository]

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef bus fill:#E8EAF6,stroke:#283593,stroke-width:1px,color:#1A237E;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
    classDef port fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1px,color:#4A148C;
    classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

    class A,B entry;
    class C bus;
    class D app;
    class E domain;
    class F port;
    class G,H infra;
```

### 2) Flujo de lectura (Query)

```mermaid
flowchart TD
    A[HTTP GET /api/queries/products] --> B[ProductQueryController]
    B --> C[QueryBus.dispatch]
    C --> D[QueryHandler específico]
    D --> E[ProductViewRepository.find]
    E --> F[ProductReadRepositoryAdapter]
    F --> G[SpringDataProductReadRepository]
    D --> H[ProductView read model]

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef bus fill:#E8EAF6,stroke:#283593,stroke-width:1px,color:#1A237E;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef read fill:#E0F7FA,stroke:#00838F,stroke-width:1px,color:#006064;
    classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

    class A,B entry;
    class C bus;
    class D app;
    class E,H read;
    class F,G infra;
```

## Ejecutar

```bash
mvn spring-boot:run
```

## Diagrama de Secuencia: Command (Crear Producto)

```mermaid
sequenceDiagram
    participant Client
    participant CommandController
    participant CommandBus
    participant CommandHandler
    participant WriteModel
    participant WriteRepo
    participant DB

    Client->>CommandController: POST /api/commands/products
    CommandController->>CommandController: toCommand(request)
    CommandController->>CommandBus: dispatch(CreateProductCommand)
    CommandBus->>CommandHandler: handle(command)
    CommandHandler->>WriteModel: Product.create(name, price, stock)
    WriteModel->>WriteModel: validate data
    WriteModel-->>CommandHandler: Product
    CommandHandler->>WriteRepo: save(product)
    WriteRepo->>WriteRepo: toEntity(product)
    WriteRepo->>DB: INSERT
    DB-->>WriteRepo: saved
    WriteRepo-->>CommandHandler: success
    CommandHandler-->>CommandBus: Void
    CommandBus-->>CommandController: Void
    CommandController-->>Client: 201 Created

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef bus fill:#E8EAF6,stroke:#283593,stroke-width:1px,color:#1A237E;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
    classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

    class Client,CommandController entry;
    class CommandBus bus;
    class CommandHandler app;
    class WriteModel domain;
    class WriteRepo,DB infra;
```

## Diagrama de Secuencia: Query (Listar Productos)

```mermaid
sequenceDiagram
    participant Client
    participant QueryController
    participant QueryBus
    participant QueryHandler
    participant ReadModel
    participant ReadRepo
    participant DB

    Client->>QueryController: GET /api/queries/products
    QueryController->>QueryController: toQuery(request)
    QueryController->>QueryBus: dispatch(GetAllProductsQuery)
    QueryBus->>QueryHandler: handle(query)
    QueryHandler->>ReadRepo: findAll()
    ReadRepo->>DB: SELECT
    DB-->>ReadRepo: List<ProductView>
    ReadRepo-->>QueryHandler: List<ProductView>
    QueryHandler->>ReadModel: toDto(views)
    ReadModel-->>QueryHandler: List<ProductDto>
    QueryHandler-->>QueryBus: List<ProductDto>
    QueryBus-->>QueryController: List<ProductDto>
    QueryController-->>Client: 200 OK + products

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef bus fill:#E8EAF6,stroke:#283593,stroke-width:1px,color:#1A237E;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef read fill:#E0F7FA,stroke:#00838F,stroke-width:1px,color:#006064;
    classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

    class Client,QueryController entry;
    class QueryBus bus;
    class QueryHandler app;
    class ReadModel read;
    class ReadRepo,DB infra;
```

## Tests

```bash
mvn test
```


