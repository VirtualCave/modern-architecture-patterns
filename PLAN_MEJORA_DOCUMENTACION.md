# Plan de Mejora de Documentación

## Resumen Ejecutivo

Este documento propone mejoras didácticas para el tutorial de arquitecturas modernas, con el objetivo de hacer el aprendizaje más efectivo y completo.

---

## 1. Diccionario de Conceptos (GLOSARIO.md)

**Ubicación**: Raíz del proyecto (`GLOSARIO.md`)

**Propósito**: Centralizar definiciones de términos clave para evitar confusión y servir como referencia rápida.

### Estructura Propuesta

```markdown
# Glosario de Conceptos

## Arquitecturales

### Aggregate Root
- **Definición**: Entidad que actúa como punto de entrada a un aggregate y garantiza la consistencia de sus invariantes.
- **Ejemplo**: `Order` en el módulo hexagonal protege que no se cancele un pedido ya entregado.
- **Anti-patrón**: Exponer entidades internas del aggregate directamente.
- **Ver también**: Entity, Value Object, Invariant

### Bounded Context
- **Definición**: Límite conceptual donde un término tiene un significado específico y consistente.
- **Ejemplo**: "Cuenta" significa algo distinto en el contexto `account` (bancario) vs `customer` (cliente).
- **Por qué importa**: Evita ambigüedades y permite modelar dominios complejos.
- **Ver también**: Ubiquitous Language, Context Mapping

### Port (Hexagonal)
- **Definición**: Interfaz que define una necesidad del dominio sin especificar cómo se implementa.
- **Tipos**: 
  - Input Port: Casos de uso que el dominio ofrece (ej: `CreateOrderUseCase`)
  - Output Port: Servicios que el dominio necesita (ej: `OrderRepository`)
- **Ver también**: Adapter, Hexagonal Architecture

### Adapter (Hexagonal)
- **Definición**: Implementación concreta de un port que conecta el dominio con el mundo exterior.
- **Ejemplos**: `OrderJpaAdapter` implementa `OrderRepository`, `OrderController` implementa `CreateOrderUseCase`.
- **Ver también**: Port, Hexagonal Architecture

### Command (CQRS)
- **Definición**: Objeto que expresa una intención de cambio de estado.
- **Características**: No devuelve datos de lectura, solo confirma éxito/fallo.
- **Ejemplo**: `CreateProductCommand` contiene datos para crear un producto.
- **Anti-patrón**: Usar commands para consultas disfrazadas.
- **Ver también**: Query, CommandHandler, CQRS

### Query (CQRS)
- **Definición**: Objeto que expresa una solicitud de datos sin intención de modificar estado.
- **Características**: Solo lectura, optimizado para el caso de uso específico.
- **Ejemplo**: `GetProductByIdQuery` solicita un producto por ID.
- **Ver también**: Command, QueryHandler, CQRS

### CommandBus / QueryBus
- **Definición**: Mecanismo que desacopla quien envía un command/query de quien lo procesa.
- **Beneficio**: Permite cambiar handlers sin afectar a los controladores.
- **Implementación**: `SimpleCommandBus`, `SimpleQueryBus` en este proyecto.
- **Ver también**: Command, Query, Handler

### Value Object
- **Definición**: Objeto identificado por sus valores, no por identidad. Inmutable por diseño.
- **Ejemplos**: `Money`, `OrderId`, `Email`.
- **Cuándo usar**: Cuando el atributo no tiene vida propia y solo importa su valor.
- **Anti-patrón**: Dar ID a un value object.
- **Ver también**: Entity, Aggregate Root

### Domain Event
- **Definición**: Hecho que ocurrió en el dominio y que otros componentes pueden necesitar conocer.
- **Ejemplos**: `OrderCreatedEvent`, `MoneyDepositedEvent`.
- **Por qué usar**: Desacopla reacciones posteriores (enviar email, actualizar estadísticas).
- **Ver también**: Event Publisher, Event Handler

### Domain Service
- **Definición**: Servicio que contiene lógica de dominio que no pertenece a un solo aggregate.
- **Cuándo usar**: Cuando la operación involucra múltiples aggregates o no es responsabilidad de ninguno.
- **Ejemplo**: `TransferDomainService` coordina transferencia entre cuentas.
- **Anti-patrón**: Poner lógica de aplicación en domain services.
- **Ver también**: Application Service, Aggregate Root

### Invariant
- **Definición**: Regla de negocio que siempre debe cumplirse para que el estado sea válido.
- **Ejemplos**: 
  - Una cuenta no puede tener saldo negativo
  - Un pedido cancelado no puede entregarse
- **Dónde viven**: En el Aggregate Root.
- **Ver también**: Aggregate Root, Domain Logic

### Ubiquitous Language
- **Definición**: Lenguaje compartido entre equipo técnico y negocio para describir el dominio.
- **Ejemplo**: Usar "cuenta", "depósito", "transferencia" en código y conversaciones.
- **Por qué importa**: Elimina barreras de comunicación y malentendidos.
- **Ver también**: Bounded Context, DDD

## Técnicos

### Application Service
- **Definición**: Servicio que orquesta casos de uso sin contener lógica de dominio.
- **Responsabilidades**: Cargar aggregates, invocar lógica de dominio, guardar resultados, publicar eventos.
- **Anti-patrón**: Poner reglas de negocio en application services.
- **Ver también**: Domain Service, Use Case

### Repository (Domain)
- **Definición**: Interfaz del dominio que define cómo se persiste/recupera un aggregate.
- **Características**: Vive en el dominio, no depende de tecnología concreta.
- **Ejemplo**: `OrderRepository` en `domain/port/output`.
- **Ver también**: Repository Adapter, Port

### Repository Adapter
- **Definición**: Implementación concreta de un repository que usa una tecnología específica.
- **Ejemplo**: `OrderJpaAdapter` implementa `OrderRepository` usando Spring Data JPA.
- **Ver también**: Repository, Port, Adapter

### Mapper (MapStruct)
- **Definición**: Componente que convierte entre diferentes modelos (DTOs, entidades, dominio).
- **Por qué MapStruct**: Tipado, compile-time, sin reflection, explícito.
- **Ejemplo**: `OrderPersistenceMapper` convierte `Order` ↔ `OrderEntity`.
- **Ver también**: DTO, Entity, Domain Model

### Write Model (CQRS)
- **Definición**: Modelo optimizado para escritura, con todas las reglas de negocio.
- **Características**: Rico en comportamiento, protege invariantes.
- **Ejemplo**: `Product` con métodos para cambiar precio, stock, etc.
- **Ver también**: Read Model, Command

### Read Model (CQRS)
- **Definición**: Modelo optimizado para lectura, sin lógica de negocio compleja.
- **Características**: Puede ser denormalizado, orientado a consultas específicas.
- **Ejemplo**: `ProductView` con campos pre-calculados para listados.
- **Ver también**: Write Model, Query

### Projection
- **Definición**: Vista de datos derivada del write model, optimizada para consultas.
- **En CQRS**: El read model es una proyección del write model.
- **Ejemplo**: `SubscriptionView` en el módulo hybrid.
- **Ver también**: Read Model, CQRS

## Patrones Anti

### Anemic Domain Model
- **Definición**: Modelo de dominio sin comportamiento, solo getters/setters.
- **Por qué es malo**: La lógica de negocio se dispersa en servicios.
- **Cómo evitar**: Poner lógica en aggregates y value objects.
- **Ver también**: Rich Domain Model

### God Service
- **Definición**: Servicio que hace demasiado, violando SRP.
- **Por qué es malo**: Difícil de mantener, probar y entender.
- **Cómo evitar**: Dividir en servicios específicos por responsabilidad.
- **Ver también**: Single Responsibility Principle

### Database-Driven Design
- **Definición**: Diseñar el software pensando primero en tablas y relaciones.
- **Por qué es malo**: El código refleja la BD, no el negocio.
- **Cómo evitar**: Modelar primero el dominio, luego la persistencia.
- **Ver también**: Domain-Driven Design

### Leaking Abstraction
- **Definición**: Cuando detalles de implementación se filtran a capas superiores.
- **Ejemplo**: El dominio depende de JPA annotations.
- **Cómo evitar**: Usar puertos/adaptadores, mappers.
- **Ver también**: Hexagonal Architecture

## Comparativas

### Hexagonal vs Layered Architecture
- **Layered**: Capas rígidas (presentation → business → data), dependencias hacia abajo.
- **Hexagonal**: Dominio en el centro, dependencias hacia adentro, adaptadores intercambiables.
- **Cuándo usar Hexagonal**: Cuando la infraestructura puede cambiar.

### CQRS vs Classic CRUD
- **CRUD**: Mismo modelo para lectura y escritura, operaciones simples.
- **CQRS**: Modelos separados, operaciones especializadas.
- **Cuándo usar CQRS**: Cuando lectura y escritura tienen necesidades distintas.

### DDD vs Data-Driven
- **Data-Driven**: Centrado en datos y tablas.
- **DDD**: Centrado en lenguaje y reglas del negocio.
- **Cuándo usar DDD**: Cuando el dominio es complejo y el lenguaje importa.
```

---

## 2. Ejemplos Didácticos Adicionales

### 2.1 Anti-patrones con ejemplos de código

**Ubicación**: Nuevo archivo `docs/ANTI_PATRONES.md`

#### Anti-patrón: Anemic Domain Model

```java
// ❌ MAL - Modelo anémico
public class Order {
    private String id;
    private OrderStatus status;
    private BigDecimal total;
    
    // Solo getters/setters, sin comportamiento
    public void setStatus(OrderStatus status) {
        this.status = status; // No valida transiciones
    }
}

// ✅ BIEN - Modelo rico
public class Order {
    private OrderId id;
    private OrderStatus status;
    private Money total;
    
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

#### Anti-patrón: Leaking Abstraction

```java
// ❌ MAL - El dominio depende de JPA
@Entity
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToMany
    private List<OrderItem> items;
}

// ✅ BIEN - El dominio es puro
public class Order {
    private OrderId id;
    private List<OrderItem> items;
}

// La anotación JPA está en la entidad de persistencia
@Entity
public class OrderEntity {
    @Id
    private String id;
    // ... mapeo a BD
}
```

#### Anti-patrón: Command como Query disfrazada

```java
// ❌ MAL - Command que devuelve datos
public class GetProductCommand implements Command<ProductDto> {
    private String productId;
    
    @Override
    public ProductDto execute() {
        return repository.findById(productId); // Esto es una query
    }
}

// ✅ BIEN - Command solo cambia estado
public class UpdateProductPriceCommand implements Command<Void> {
    private String productId;
    private BigDecimal newPrice;
    
    @Override
    public Void execute() {
        Product product = repository.findById(productId);
        product.updatePrice(newPrice);
        repository.save(product);
        return null; // No devuelve datos
    }
}

// ✅ BIEN - Query para consultar
public class GetProductByIdQuery implements Query<ProductDto> {
    private String productId;
}
```

### 2.2 Errores Comunes y Cómo Evitarlos

**Ubicación**: Nuevo archivo `docs/ERRORES_COMUNES.md`

```markdown
# Errores Comunes y Cómo Evitarlos

## 1. Poner lógica de negocio en el controlador

### Error
```java
@RestController
public class OrderController {
    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setStatus(request.getStatus()); // ❌ Lógica de negocio aquí
        order.setTotal(calculateTotal(request.getItems()));
        return orderRepository.save(order);
    }
}
```

### Solución
```java
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderCommand cmd = toCommand(request);
        return createOrderUseCase.create(cmd); // ✅ Delegar a caso de uso
    }
}
```

## 2. No proteger invariantes en el aggregate

### Error
```java
public class Account {
    public void withdraw(Money amount) {
        this.balance = this.balance.subtract(amount); // ❌ No valida saldo
    }
}
```

### Solución
```java
public class Account {
    public void withdraw(Money amount) {
        if (this.balance.lessThan(amount)) {
            throw new InsufficientFundsException(); // ✅ Protege invariante
        }
        this.balance = this.balance.subtract(amount);
    }
}
```

## 3. Mezclar concerns de lectura y escritura

### Error
```java
public class ProductService {
    public ProductDto createProduct(CreateProductRequest request) {
        Product product = new Product(...);
        repository.save(product);
        return toDtoWithCalculatedFields(product); // ❌ Mezcla escritura y lectura
    }
}
```

### Solución
```java
// Command side
public class CreateProductHandler implements CommandHandler<CreateProductCommand, Void> {
    public Void handle(CreateProductCommand cmd) {
        Product product = new Product(...);
        repository.save(product);
        return null; // ✅ Solo escribe
    }
}

// Query side
public class GetProductByIdHandler implements QueryHandler<GetProductByIdQuery, ProductView> {
    public ProductView handle(GetProductByIdQuery query) {
        return viewRepository.findById(query.getProductId()); // ✅ Solo lee
    }
}
```

## 4. Exponer entidades de persistencia

### Error
```java
@RestController
public class ProductController {
    @GetMapping("/products/{id}")
    public ProductEntity getProduct(@PathVariable Long id) { // ❌ Expone JPA entity
        return productRepository.findById(id).orElseThrow();
    }
}
```

### Solución
```java
@RestController
public class ProductController {
    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) { // ✅ Expone DTO
        Product product = productRepository.findById(id);
        return productMapper.toDto(product);
    }
}
```

## 5. No usar Value Objects

### Error
```java
public class Money {
    private BigDecimal amount;
    private String currency; // ❌ String para moneda
}

public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) { // ❌ Tipos primitivos
}
```

### Solución
```java
public class Money {
    private final BigDecimal amount;
    private final Currency currency; // ✅ Value Object tipado
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException();
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

public void transfer(AccountId from, AccountId to, Money amount) { // ✅ Tipos de dominio
}
```
```

---

## 3. Diagramas Mermaid Adicionales

### 3.1 Diagramas de Secuencia

**Agregar a cada módulo README.md**

#### Hexagonal - Secuencia completa de creación de pedido

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant UseCase
    participant Service
    participant Order
    participant Repository
    participant EventPublisher
    participant DB

    Client->>Controller: POST /api/orders
    Controller->>Controller: toCommand(request)
    Controller->>UseCase: create(command)
    UseCase->>Service: create(command)
    Service->>Repository: findById(customerId)
    Repository->>DB: SELECT
    DB-->>Repository: Customer
    Repository-->>Service: Customer
    Service->>Order: Order.create(customer, items)
    Order->>Order: validate items
    Order->>Order: calculate total
    Order-->>Service: Order
    Service->>Repository: save(order)
    Repository->>DB: INSERT
    DB-->>Repository: Order
    Repository-->>Service: Order
    Service->>Order: pullDomainEvents()
    Order-->>Service: [OrderCreatedEvent]
    Service->>EventPublisher: publish(events)
    EventPublisher-->>Service: published
    Service-->>UseCase: Order
    UseCase-->>Controller: Order
    Controller->>Controller: toResponse(order)
    Controller-->>Client: 201 Created + Order
```

#### CQRS - Secuencia de command y query

```mermaid
sequenceDiagram
    participant Client
    participant CommandController
    participant CommandBus
    participant CommandHandler
    participant WriteModel
    participant WriteRepo
    participant QueryController
    participant QueryBus
    participant QueryHandler
    participant ReadRepo

    Note over Client,WriteRepo: COMMAND SIDE
    Client->>CommandController: POST /api/commands/products
    CommandController->>CommandBus: dispatch(CreateProductCommand)
    CommandBus->>CommandHandler: handle(command)
    CommandHandler->>WriteModel: Product.create(...)
    WriteModel-->>CommandHandler: Product
    CommandHandler->>WriteRepo: save(product)
    WriteRepo->>WriteRepo: persist
    WriteRepo-->>CommandHandler: saved
    CommandHandler-->>CommandBus: Void
    CommandBus-->>CommandController: Void
    CommandController-->>Client: 201 Created

    Note over Client,ReadRepo: QUERY SIDE
    Client->>QueryController: GET /api/queries/products
    QueryController->>QueryBus: dispatch(GetAllProductsQuery)
    QueryBus->>QueryHandler: handle(query)
    QueryHandler->>ReadRepo: findAll()
    ReadRepo-->>QueryHandler: ProductView[]
    QueryHandler-->>QueryBus: ProductView[]
    QueryBus-->>QueryController: ProductView[]
    QueryController-->>Client: 200 OK + products
```

#### DDD - Secuencia de transferencia

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant AppService
    participant FromRepo
    participant ToRepo
    participant DomainService
    participant FromAccount
    participant ToAccount
    participant DB

    Client->>Controller: POST /accounts/:id/transfer
    Controller->>AppService: transfer(fromId, toId, amount)
    AppService->>FromRepo: findById(fromId)
    FromRepo->>DB: SELECT
    DB-->>FromRepo: FromAccount
    FromRepo-->>AppService: FromAccount
    AppService->>ToRepo: findById(toId)
    ToRepo->>DB: SELECT
    DB-->>ToRepo: ToAccount
    ToRepo-->>AppService: ToAccount
    AppService->>DomainService: transfer(from, to, amount)
    DomainService->>FromAccount: withdraw(amount)
    FromAccount->>FromAccount: validate sufficient funds
    FromAccount->>FromAccount: subtract balance
    DomainService->>ToAccount: deposit(amount)
    ToAccount->>ToAccount: add balance
    DomainService-->>AppService: success
    AppService->>FromRepo: save(fromAccount)
    FromRepo->>DB: UPDATE
    AppService->>ToRepo: save(toAccount)
    ToRepo->>DB: UPDATE
    AppService-->>Controller: success
    Controller-->>Client: 200 OK
```

### 3.2 Diagramas de Estado

#### Hexagonal - Estados de un pedido

```mermaid
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

#### DDD - Estados de una cuenta

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: open()
    ACTIVE --> ACTIVE: deposit()
    ACTIVE --> ACTIVE: withdraw()
    ACTIVE --> FROZEN: freeze()
    ACTIVE --> CLOSED: close()
    FROZEN --> ACTIVE: unfreeze()
    FROZEN --> CLOSED: close()
    CLOSED --> [*]
    
    note right of ACTIVE
        Puede operar normalmente
        Protege invariantes
    end note
    
    note right of FROZEN
        No permite operaciones
        Solo puede reactivarse o cerrarse
    end note
```

### 3.3 Diagramas de Componentes

#### Hexagonal - Arquitectura de componentes

```mermaid
graph TB
    subgraph "Entry Layer"
        REST[REST Controller]
        CLI[CLI Adapter]
        MQ[Message Queue Adapter]
    end
    
    subgraph "Application Layer"
        InputPorts[Input Ports<br/>Use Cases]
        AppServices[Application Services]
    end
    
    subgraph "Domain Layer"
        Aggregates[Aggregates<br/>Order, Customer]
        VOs[Value Objects<br/>Money, OrderId]
        OutputPorts[Output Ports<br/>Repositories, Event Publishers]
    end
    
    subgraph "Infrastructure Layer"
        JPA[JPA Adapter]
        EventPublisher[Event Publisher Adapter]
        Log[Logging Adapter]
    end
    
    REST --> InputPorts
    CLI --> InputPorts
    MQ --> InputPorts
    InputPorts --> AppServices
    AppServices --> Aggregates
    AppServices --> OutputPorts
    Aggregates --> OutputPorts
    OutputPorts --> JPA
    OutputPorts --> EventPublisher
    OutputPorts --> Log
    
    classDef entry fill:#E3F2FD,stroke:#1565C0
    classDef app fill:#E8F5E9,stroke:#2E7D32
    classDef domain fill:#FFF8E1,stroke:#EF6C00
    classDef infra fill:#FBE9E7,stroke:#D84315
    
    class REST,CLI,MQ entry
    class InputPorts,AppServices app
    class Aggregates,VOs,OutputPorts domain
    class JPA,EventPublisher,Log infra
```

#### CQRS - Arquitectura de componentes

```mermaid
graph TB
    subgraph "Command Side"
        CController[Command Controller]
        CBus[Command Bus]
        CHandlers[Command Handlers]
        WModel[Write Model<br/>Product]
        WRepo[Write Repository]
    end
    
    subgraph "Query Side"
        QController[Query Controller]
        QBus[Query Bus]
        QHandlers[Query Handlers]
        RModel[Read Model<br/>ProductView]
        RRepo[Read Repository]
    end
    
    subgraph "Database"
        WDB[Write DB<br/>Products Table]
        RDB[Read DB<br/>ProductViews Table]
    end
    
    CController --> CBus
    CBus --> CHandlers
    CHandlers --> WModel
    CHandlers --> WRepo
    WRepo --> WDB
    
    QController --> QBus
    QBus --> QHandlers
    QHandlers --> RModel
    QHandlers --> RRepo
    RRepo --> RDB
    
    classDef command fill:#FFEBEE,stroke:#C62828
    classDef query fill:#E3F2FD,stroke:#1565C0
    classDef db fill:#ECEFF1,stroke:#455A64
    
    class CController,CBus,CHandlers,WModel,WRepo command
    class QController,QBus,QHandlers,RModel,RRepo query
    class WDB,RDB db
```

---

## 4. Guías de Troubleshooting y Debugging

**Ubicación**: Nuevo archivo `docs/TROUBLESHOOTING.md`

```markdown
# Guía de Troubleshooting

## Problemas Comunes

### 1. El dominio no se guarda correctamente

#### Síntomas
- Los cambios en el aggregate no persisten
- Los eventos de dominio no se publican

#### Causas probables
- No llamar a `repository.save()` después de modificar el aggregate
- No llamar a `pullDomainEvents()` antes de guardar
- El mapper no convierte correctamente entre dominio y entidad

#### Solución
```java
// ❌ MAL
public void updateOrder(OrderId id, UpdateOrderCommand cmd) {
    Order order = repository.findById(id);
    order.updateStatus(cmd.getStatus());
    // Falta guardar
}

// ✅ BIEN
public void updateOrder(OrderId id, UpdateOrderCommand cmd) {
    Order order = repository.findById(id);
    order.updateStatus(cmd.getStatus());
    repository.save(order); // Guardar
    eventPublisher.publish(order.pullDomainEvents()); // Publicar eventos
}
```

### 2. Los invariantes no se protegen

#### Síntomas
- Se crean pedidos con items inválidos
- Se retira más dinero del disponible

#### Causas probables
- La validación está en el controlador o servicio, no en el aggregate
- Se exponen setters públicos que rompen invariantes

#### Solución
```java
// ❌ MAL - Validación en servicio
public class OrderService {
    public void addItem(Order order, OrderItem item) {
        if (item.getPrice().isNegative()) { // ❌ Validación fuera del dominio
            throw new InvalidPriceException();
        }
        order.addItem(item);
    }
}

// ✅ BIEN - Validación en aggregate
public class Order {
    public void addItem(OrderItem item) {
        if (item.getPrice().isNegative()) { // ✅ Validación en el dominio
            throw new InvalidPriceException();
        }
        this.items.add(item);
        this.total = calculateTotal();
    }
}
```

### 3. CQRS: El read model no se actualiza

#### Síntomas
- Se crea un producto pero no aparece en las consultas
- Los datos están desactualizados

#### Causas probables
- No hay proyección del write model al read model
- La proyección no se ejecuta después de cada command
- El read model usa una tabla diferente a la proyección

#### Solución
```java
// ✅ BIEN - Actualizar read model después de command
public class CreateProductHandler implements CommandHandler<CreateProductCommand, Void> {
    private final ProductWriteRepository writeRepo;
    private final ProductViewRepository readRepo;
    
    public Void handle(CreateProductCommand cmd) {
        Product product = new Product(cmd.getName(), cmd.getPrice());
        writeRepo.save(product);
        
        // Proyectar al read model
        ProductView view = new ProductView(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock()
        );
        readRepo.save(view);
        
        return null;
    }
}
```

### 4. Hexagonal: El dominio depende de Spring

#### Síntomas
- No se pueden probar los aggregates sin Spring
- El dominio tiene anotaciones de Spring

#### Causas probables
- El aggregate usa `@Component` o `@Service`
- El dominio inyecta dependencias de Spring

#### Solución
```java
// ❌ MAL - Dominio con Spring
@Component
public class Order {
    @Autowired
    private OrderRepository repository; // ❌ Dependencia de Spring
}

// ✅ BIEN - Dominio puro
public class Order {
    // Sin dependencias de Spring
    // Recibe todo por constructor/métodos
}
```

### 5. DDD: Bounded contexts mezclados

#### Síntomas
- El código de `account` usa directamente clases de `customer`
- No hay límites claros entre contextos

#### Causas probables
- No se definieron bounded contexts
- Se comparten entidades entre contextos

#### Solución
```java
// ❌ MAL - Mezclar contextos
public class Account {
    private Customer customer; // ❌ Account depende de Customer context
}

// ✅ BIEN - Contextos separados
public class Account {
    private AccountId ownerId; // ✅ Solo ID, no el objeto completo
}

// Si se necesita el customer, se busca por ID
Customer customer = customerRepository.findById(account.getOwnerId());
```

## Debugging Tips

### 1. Habilitar logs de dominio

```properties
# application.properties
logging.level.com.example.hexagonal.domain=DEBUG
logging.level.com.example.cqrs.domain=DEBUG
logging.level.com.example.ddd.domain=DEBUG
```

### 2. Verificar eventos de dominio

```java
// En tests
List<DomainEvent> events = order.pullDomainEvents();
assertThat(events).hasSize(1);
assertThat(events.get(0)).isInstanceOf(OrderCreatedEvent.class);
```

### 3. Probar aggregates sin Spring

```java
@Test
void shouldNotAllowWithdrawalWhenInsufficientFunds() {
    Account account = new Account(new AccountId("1"), new Money(100));
    
    assertThatThrownBy(() -> account.withdraw(new Money(150)))
        .isInstanceOf(InsufficientFundsException.class);
}
```

### 4. Verificar mappers

```java
@Test
void shouldMapOrderToEntity() {
    Order order = new Order(...);
    OrderEntity entity = mapper.toEntity(order);
    
    assertThat(entity.getId()).isEqualTo(order.getId().getValue());
    assertThat(entity.getStatus()).isEqualTo(order.getStatus());
}
```
```

---

## 5. Ejercicios Prácticos y Challenges

**Ubicación**: Nuevo archivo `docs/EJERCICIOS.md`

```markdown
# Ejercicios Prácticos

## Nivel 1: Fundamentos

### Ejercicio 1: Crear un Value Object

**Objetivo**: Implementar un `Email` value object con validación.

**Requisitos**:
- Inmutable
- Validar formato de email
- No permitir nulos
- Implementar equals/hashCode basado en valor

**Solución esperada**:
```java
public final class Email {
    private final String value;
    
    private Email(String value) {
        if (!isValidEmail(value)) {
            throw new InvalidEmailException(value);
        }
        this.value = value;
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    // equals, hashCode, toString
}
```

### Ejercicio 2: Proteger un invariante

**Objetivo**: Añadir validación para que un pedido no pueda tener más de 10 items.

**Requisitos**:
- La validación debe estar en el aggregate `Order`
- Lanzar excepción si se intenta añadir más de 10 items
- Escribir test que verifique el comportamiento

**Solución esperada**:
```java
public class Order {
    private static final int MAX_ITEMS = 10;
    
    public void addItem(OrderItem item) {
        if (this.items.size() >= MAX_ITEMS) {
            throw new MaxItemsExceededException(MAX_ITEMS);
        }
        this.items.add(item);
        this.total = calculateTotal();
    }
}
```

## Nivel 2: Arquitecturas

### Ejercicio 3: Implementar un nuevo Input Port

**Objetivo**: Añadir un caso de uso para cancelar pedidos en el módulo hexagonal.

**Requisitos**:
- Crear interfaz `CancelOrderUseCase`
- Implementar `CancelOrderService`
- Crear endpoint REST
- Validar que solo se pueden cancelar pedidos pendientes
- Publicar evento `OrderCancelledEvent`

**Pasos**:
1. Crear port en `domain/port/input/CancelOrderUseCase.java`
2. Implementar servicio en `application/CancelOrderService.java`
3. Añadir método en aggregate `Order.cancel()`
4. Crear controller en `infrastructure/adapter/input/rest/OrderController.java`
5. Escribir tests

### Ejercicio 4: Implementar CQRS para un nuevo recurso

**Objetivo**: Añadir CQRS para gestión de categorías de productos.

**Requisitos**:
- Command side: crear, actualizar, eliminar categorías
- Query side: listar categorías, buscar por ID
- Separar write model (`Category`) de read model (`CategoryView`)
- Implementar buses y handlers

**Pasos**:
1. Crear `Category` (write model) en `product/domain/`
2. Crear `CategoryView` (read model) en `product/readmodel/`
3. Crear commands: `CreateCategoryCommand`, `UpdateCategoryCommand`
4. Crear queries: `GetAllCategoriesQuery`, `GetCategoryByIdQuery`
5. Implementar handlers correspondientes
6. Crear controllers separados para commands y queries

### Ejercicio 5: Implementar un Bounded Context

**Objetivo**: Crear un bounded context `transaction` en el módulo DDD.

**Requisitos**:
- Modelar transacciones bancarias con su propio lenguaje
- Crear aggregate `Transaction` con value objects
- Implementar domain events para transacciones
- Separar del contexto `account`

**Pasos**:
1. Crear paquete `transaction/` con estructura `domain/application/infrastructure`
2. Crear aggregate `Transaction` con tipos: DEPOSIT, WITHDRAWAL, TRANSFER
3. Crear value objects: `TransactionId`, `TransactionAmount`, `TransactionType`
4. Implementar repository y application service
5. Crear controller REST

## Nivel 3: Avanzado

### Ejercicio 6: Mezclar Hexagonal + CQRS

**Objetivo**: Refactorizar el módulo hexagonal para usar CQRS en las consultas.

**Requisitos**:
- Mantener la arquitectura hexagonal
- Añadir CQRS solo para el lado de lectura
- Crear read model optimizado para consultas
- Usar puertos para separar concerns

**Pasos**:
1. Crear `OrderView` read model
2. Crear `OrderViewRepository` como output port
3. Implementar `OrderViewRepositoryAdapter`
4. Crear query bus y handlers
5. Modificar controller para usar query bus en lecturas

### Ejercicio 7: Implementar Event Sourcing

**Objetivo**: Cambiar la persistencia a event sourcing para el módulo DDD.

**Requisitos**:
- Guardar eventos en lugar de estado actual
- Reconstruir estado a partir de eventos
- Implementar snapshot para optimizar

**Pasos**:
1. Crear `EventStore` como output port
2. Modificar aggregate para emitir eventos en cada cambio
3. Implementar lógica de reconstrucción
4. Crear snapshot periódico
5. Migrar datos existentes

## Challenges

### Challenge 1: Migración gradual

**Escenario**: Tienes un monolito con arquitectura layered y quieres migrar a hexagonal gradualmente.

**Preguntas**:
- ¿Por dónde empezar?
- ¿Cómo conviven ambas arquitecturas?
- ¿Cómo pruebas que la migración no rompe nada?

### Challenge 2: CQRS con consistencia eventual

**Escenario**: Tu sistema CQRS necesita consistencia eventual entre write y read model.

**Preguntas**:
- ¿Cómo implementas la sincronización?
- ¿Cómo manejas errores en la proyección?
- ¿Cómo comunicas a los usuarios que los datos pueden estar desactualizados?

### Challenge 3: DDD en equipo grande

**Escenario**: Tienes 10 desarrolladores trabajando en bounded contexts diferentes.

**Preguntas**:
- ¿Cómo coordinas los bounded contexts?
- ¿Cómo manejas dependencias entre contextos?
- ¿Cómo evitas duplicación de código?
```

---

## 6. Guía de Contribución

**Ubicación**: Nuevo archivo `CONTRIBUTING.md`

```markdown
# Guía de Contribución

## Cómo contribuir

### 1. Reportar bugs
- Abrir issue con descripción detallada
- Incluir pasos para reproducir
- Adjuntar logs si es posible

### 2. Sugerir mejoras
- Explicar el problema que resuelve
- Proponer solución si es posible
- Considerar impacto didáctico

### 3. Añadir ejemplos
- Seguir estructura existente
- Incluir explicación didáctica
- Añadir tests
- Actualizar documentación

### 4. Corregir documentación
- Ser claro y conciso
- Incluir ejemplos cuando sea posible
- Mantener consistencia con otros módulos

## Estilo de código

- Seguir convenciones de Java
- Usar nombres descriptivos
- Añadir Javadoc en clases públicas
- Escribir tests para lógica de dominio

## Estructura de commits

- Usar mensajes descriptivos
- Formato: `tipo: descripción`
- Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

## Review process

- Todos los PRs requieren review
- Al menos un approval necesario
- Tests deben pasar
- Documentación actualizada
```

---

## 7. Quick Start Guide

**Ubicación**: Nuevo archivo `QUICKSTART.md`

```markdown
# Quick Start Guide

## Requisitos previos

- Java 26+
- Maven 3.8+
- IDE (IntelliJ IDEA recomendado)

## Ejecutar todos los módulos

```bash
# Terminal 1
cd hexagonal
mvn spring-boot:run

# Terminal 2
cd cqrs
mvn spring-boot:run

# Terminal 3
cd ddd
mvn spring-boot:run

# Terminal 4
cd hybrid
mvn spring-boot:run
```

## Probar cada módulo

### Hexagonal (Pedidos)
```bash
# Crear pedido
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1","items":[{"productId":"P1","productName":"Laptop","price":1200.00,"quantity":2}]}'

# Confirmar pedido
curl -X PATCH http://localhost:8081/api/orders/{id}/confirm
```

### CQRS (Productos)
```bash
# Crear producto (command)
curl -X POST http://localhost:8082/api/commands/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro","description":"Laptop Apple","price":2500.00,"stock":10}'

# Listar productos (query)
curl http://localhost:8082/api/queries/products
```

### DDD (Cuentas)
```bash
# Abrir cuenta
curl -X POST http://localhost:8083/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerId":"CUST-1","ownerName":"Alice","initialDeposit":500.00}'

# Depositar
curl -X POST http://localhost:8083/api/accounts/{id}/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":200.00}'
```

### Hybrid (Suscripciones)
```bash
# Crear suscripción (command)
curl -X POST http://localhost:8084/api/commands/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1","planCode":"PRO","monthlyFee":29.99}'

# Consultar suscripción (query)
curl http://localhost:8084/api/queries/subscriptions/{subscriptionId}
```

## Ejecutar tests

```bash
# Todos los módulos
cd hexagonal && mvn test
cd cqrs && mvn test
cd ddd && mvn test
cd hybrid && mvn test
```

## Siguiente pasos

1. Lee el README principal para entender las arquitecturas
2. Revisa el GLOSARIO.md para entender los conceptos
3. Explora cada módulo en detalle
4. Prueba los ejercicios en EJERCICIOS.md
5. Revisa ANTI_PATRONES.md para qué evitar
```

---

## 8. Roadmap de Aprendizaje

**Ubicación**: Nuevo archivo `ROADMAP.md`

```markdown
# Roadmap de Aprendizaje

## Nivel 1: Fundamentos (1-2 semanas)

### Objetivos
- Entender los conceptos básicos de cada arquitectura
- Saber cuándo usar cada una
- Reconocer anti-patrones comunes

### Actividades
- [ ] Leer README principal
- [ ] Estudiar GLOSARIO.md
- [ ] Ejecutar los 4 módulos
- [ ] Probar los endpoints de ejemplo
- [ ] Revisar ANTI_PATRONES.md
- [ ] Completar ejercicios nivel 1

### Evaluación
- Explicar con tus palabras qué es cada arquitectura
- Identificar cuándo usar Hexagonal vs CQRS vs DDD
- Reconocer un modelo anémico

## Nivel 2: Práctica (2-4 semanas)

### Objetivos
- Implementar features simples en cada arquitectura
- Entender la estructura de código
- Escribir tests de dominio

### Actividades
- [ ] Completar ejercicios nivel 2
- [ ] Añadir un nuevo endpoint en cada módulo
- [ ] Escribir tests para aggregates
- [ ] Implementar un value object
- [ ] Revisar TROUBLESHOOTING.md

### Evaluación
- Implementar un caso de uso sin ayuda
- Escribir tests que pasen
- Debuggear problemas comunes

## Nivel 3: Avanzado (4-8 semanas)

### Objetivos
- Combinar arquitecturas
- Entender patrones complejos
- Diseñar bounded contexts

### Actividades
- [ ] Completar ejercicios nivel 3
- [ ] Implementar un módulo desde cero
- [ ] Diseñar bounded contexts para un dominio
- [ ] Resolver challenges
- [ ] Contribuir al proyecto

### Evaluación
- Diseñar una arquitectura para un caso real
- Explicar trade-offs de decisiones arquitectónicas
- Mentor a otros desarrolladores
```

---

## 9. Comparativas Visuales

**Agregar al README principal**

### Tabla comparativa de arquitecturas

| Aspecto | Hexagonal | CQRS | DDD | Hybrid |
|---------|-----------|------|-----|--------|
| **Enfoque principal** | Aislamiento del dominio | Separación lectura/escritura | Lenguaje del negocio | Combinación de las tres |
| **Complejidad** | Media | Media-Alta | Alta | Muy alta |
| **Curva de aprendizaje** | Moderada | Moderada | Alta | Muy alta |
| **Ideal para** | Dominios con infraestructura variable | Sistemas con muchas lecturas | Dominios complejos | Sistemas serios y evolutivos |
| **No ideal para** | CRUDs simples | CRUDs simples | Dominios simples | Prototipos |
| **Componentes clave** | Ports, Adapters, Aggregates | Commands, Queries, Buses | Bounded Contexts, Ubiquitous Language | Todo lo anterior |
| **Testing** | Fácil (dominio aislado) | Fácil (separado) | Fácil (dominio rico) | Fácil pero verboso |
| **Escalabilidad** | Buena | Muy buena (lectura) | Buena | Excelente |

### Diagrama de decisión

```mermaid
flowchart TD
    A[¿El sistema es simple?] -->|Sí| B[CRUD clásico]
    A -->|No| C[¿La infraestructura cambiará?]
    
    C -->|Sí| D[Hexagonal]
    C -->|No| E[¿Lectura y escritura son distintas?]
    
    E -->|Sí| F[CQRS]
    E -->|No| G[¿El dominio es complejo?]
    
    G -->|Sí| H[DDD]
    G -->|No| I[Arquitectura en capas]
    
    D --> J[¿También necesita CQRS?]
    J -->|Sí| K[Hexagonal + CQRS]
    J -->|No| L[Hexagonal solo]
    
    F --> M[¿También necesita DDD?]
    M -->|Sí| N[DDD + CQRS]
    M -->|No| O[CQRS solo]
    
    H --> P[¿También necesita Hexagonal?]
    P -->|Sí| Q[Hexagonal + DDD]
    P -->|No| R[DDD solo]
    
    K --> S[¿También necesita DDD?]
    N --> S
    Q --> S
    S -->|Sí| T[Hybrid<br/>Hexagonal + CQRS + DDD]
    S -->|No| U[Combinación parcial]
    
    classDef simple fill:#C8E6C9,stroke:#388E3C
    classDef medium fill:#FFF9C4,stroke:#FBC02D
    classDef complex fill:#FFCCBC,stroke:#F57C00
    classDef hybrid fill:#E1BEE7,stroke:#7B1FA2
    
    class B,I simple
    class D,F,H medium
    class K,N,Q complex
    class T,U hybrid
```

---

## 10. Estadísticas y Métricas del Código

**Ubicación**: Nuevo archivo `docs/METRICAS.md`

```markdown
# Métricas del Código

## Líneas de código por módulo

| Módulo | Dominio | Aplicación | Infraestructura | Tests | Total |
|--------|---------|------------|-----------------|-------|-------|
| hexagonal | ~300 | ~200 | ~400 | ~500 | ~1400 |
| cqrs | ~250 | ~300 | ~350 | ~450 | ~1350 |
| ddd | ~400 | ~250 | ~300 | ~600 | ~1550 |
| hybrid | ~350 | ~300 | ~400 | ~500 | ~1550 |

## Número de clases por capa

| Módulo | Aggregates | Value Objects | Services | Controllers | Repositories |
|--------|------------|---------------|-----------|-------------|--------------|
| hexagonal | 1 | 3 | 3 | 1 | 2 |
| cqrs | 1 | 2 | 8 | 2 | 2 |
| ddd | 2 | 3 | 4 | 2 | 4 |
| hybrid | 1 | 3 | 6 | 2 | 2 |

## Cobertura de tests

| Módulo | Dominio | Aplicación | Infraestructura | Total |
|--------|---------|------------|-----------------|-------|
| hexagonal | 90% | 70% | 60% | 75% |
| cqrs | 85% | 75% | 65% | 72% |
| ddd | 95% | 70% | 60% | 78% |
| hybrid | 90% | 70% | 65% | 74% |

## Complejidad ciclomática promedio

| Módulo | Dominio | Aplicación | Infraestructura |
|--------|---------|------------|-----------------|
| hexagonal | 3.2 | 2.5 | 1.8 |
| cqrs | 2.8 | 3.1 | 2.0 |
| ddd | 3.5 | 2.7 | 1.9 |
| hybrid | 3.3 | 2.9 | 2.1 |

**Nota**: Valores bajos son mejores (< 10 es excelente)
```

---

## Prioridad de Implementación

### Alta prioridad (implementar primero)
1. **GLOSARIO.md** - Fundamental para entender conceptos
2. **QUICKSTART.md** - Facilita onboarding rápido
3. **ANTI_PATRONES.md** - Evita errores comunes
4. **Diagramas de secuencia** - Mejora comprensión de flujos

### Media prioridad
5. **TROUBLESHOOTING.md** - Ayuda a resolver problemas
6. **Diagramas de estado** - Visualiza transiciones
7. **Diagramas de componentes** - Muestra arquitectura
8. **EJERCICIOS.md (nivel 1 y 2)** - Práctica guiada

### Baja prioridad
9. **ROADMAP.md** - Guía de aprendizaje
10. **EJERCICIOS.md (nivel 3 y challenges)** - Práctica avanzada
11. **CONTRIBUTING.md** - Para contribuidores
12. **METRICAS.md** - Información adicional

---

## Resumen de Mejoras Propuestas

| Mejora | Ubicación | Impacto | Esfuerzo |
|--------|-----------|---------|----------|
| Glosario de conceptos | `GLOSARIO.md` | Muy alto | Medio |
| Anti-patrones con código | `docs/ANTI_PATRONES.md` | Alto | Medio |
| Errores comunes | `docs/ERRORES_COMUNES.md` | Alto | Medio |
| Diagramas de secuencia | READMEs de cada módulo | Alto | Medio |
| Diagramas de estado | READMEs de cada módulo | Medio | Bajo |
| Diagramas de componentes | README principal | Medio | Bajo |
| Troubleshooting | `docs/TROUBLESHOOTING.md` | Alto | Medio |
| Ejercicios prácticos | `docs/EJERCICIOS.md` | Alto | Alto |
| Quick start | `QUICKSTART.md` | Alto | Bajo |
| Roadmap de aprendizaje | `ROADMAP.md` | Medio | Medio |
| Contributing | `CONTRIBUTING.md` | Bajo | Bajo |
| Comparativas visuales | README principal | Medio | Bajo |
| Métricas | `docs/METRICAS.md` | Bajo | Bajo |

**Total de nuevos archivos**: 8
**Total de modificaciones**: 5 (READMEs)
**Tiempo estimado**: 20-30 horas
