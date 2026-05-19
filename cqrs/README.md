# CQRS Tutorial

## Indice de contenidos

- [Idea principal (en sencillo)](#idea-principal-en-sencillo)
- [Conceptos clave en este módulo](#conceptos-clave-en-este-módulo)
- [Flujo visual](#flujo-visual)
- [Diagrama de Secuencia: Command (Crear Producto)](#diagrama-de-secuencia-command-crear-producto)
- [Diagrama de Secuencia: Query (Listar Productos)](#diagrama-de-secuencia-query-listar-productos)

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


## Flujo visual

### 1) Flujo de escritura (Command)

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
    A[HTTP POST/PATCH/DELETE /api/commands/products] --> B[ProductCommandController]
    B --> C[CommandBus.dispatch]
    C --> D[CommandHandler específico]
    D --> E[Product write model]
    D --> F[ProductRepository.save]
    F --> G[ProductWriteRepositoryAdapter]
    G --> H[SpringDataProductWriteRepository]

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef bus fill:#E0E7FF,stroke:#3730A3,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class A,B entry;
    class C bus;
    class D app;
    class E domain;
    class F port;
    class G,H infra;
```

### 2) Flujo de lectura (Query)

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
    A[HTTP GET /api/queries/products] --> B[ProductQueryController]
    B --> C[QueryBus.dispatch]
    C --> D[QueryHandler específico]
    D --> E[ProductViewRepository.find]
    E --> F[ProductReadRepositoryAdapter]
    F --> G[SpringDataProductReadRepository]
    D --> H[ProductView read model]

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef bus fill:#E0E7FF,stroke:#3730A3,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef read fill:#CCFBF1,stroke:#0F766E,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class A,B entry;
    class C bus;
    class D app;
    class E,H read;
    class F,G infra;
```

## Diagrama de Secuencia: Command (Crear Producto)

Este diagrama enseña la parte de escritura de CQRS. Es decir: **crear o modificar datos**.

1. El cliente envía la petición para crear el producto.
2. El controlador solo traduce la petición a un comando.
3. El `CommandBus` entrega ese comando al `CommandHandler` correcto.
4. El handler usa el modelo de escritura para aplicar reglas y guardar.
5. La base de datos recibe el cambio y el cliente obtiene confirmación.

Lo importante aquí es recordar que **no se consulta nada para responder**; solo se escribe.

```mermaid
%%{init: {'themeVariables': {'background':'transparent','lineColor':'#94A3B8'}}}%%
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
```

**Cómo leerlo rápido:** toda la ruta va hacia el lado de escritura. Si ves `Command`, piensa en “cambiar estado” y no en “leer datos”.

## Diagrama de Secuencia: Query (Listar Productos)

Este segundo diagrama muestra la parte de lectura de CQRS. Es decir: **buscar y devolver información**.

1. El cliente pide una lista de productos.
2. El controlador convierte la petición en query.
3. El `QueryBus` la envía al `QueryHandler`.
4. El handler consulta el `ReadModel` o su repositorio de lectura.
5. El resultado se transforma en DTO y vuelve al cliente.

La idea clave es esta: **la consulta no modifica nada; solo lee la información preparada para mostrarla**.

```mermaid
%%{init: {'themeVariables': {'background':'transparent','lineColor':'#94A3B8'}}}%%
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
    DB-->>ReadRepo: ProductView[]
    ReadRepo-->>QueryHandler: ProductView[]
    QueryHandler->>ReadModel: toDto(views)
    ReadModel-->>QueryHandler: ProductDto[]
    QueryHandler-->>QueryBus: ProductDto[]
    QueryBus-->>QueryController: ProductDto[]
    QueryController-->>Client: 200 OK + products
```

**Resumen mental:** pedir datos -> convertir a query -> buscar en lectura -> adaptar a DTO -> responder.


