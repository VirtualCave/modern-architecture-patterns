# Ejercicios Prácticos

Ejercicios para practicar las arquitecturas modernas presentadas en este tutorial.

---

## Nivel 1: Fundamentos

### Ejercicio 1: Crear un Value Object

**Objetivo**: Implementar un `Email` value object con validación.

**Requisitos**:
- Inmutable
- Validar formato de email
- No permitir nulos
- Implementar equals/hashCode basado en valor
- Método estático de fábrica `of(String)`

**Ubicación**: `shared/domain/Email.java`

**Solución esperada**:
```java
package com.example.hexagonal.shared.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Email {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private final String value;
    
    private Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email no puede ser nulo o vacío");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Formato de email inválido: " + value);
        }
        this.value = value;
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
```

**Test**:
```java
@Test
void shouldCreateValidEmail() {
    Email email = Email.of("test@example.com");
    assertThat(email.getValue()).isEqualTo("test@example.com");
}

@Test
void shouldThrowExceptionForInvalidEmail() {
    assertThatThrownBy(() -> Email.of("invalid"))
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void shouldThrowExceptionForNullEmail() {
    assertThatThrownBy(() -> Email.of(null))
        .isInstanceOf(IllegalArgumentException.class);
}

@Test
void shouldBeEqualWhenSameValue() {
    Email email1 = Email.of("test@example.com");
    Email email2 = Email.of("test@example.com");
    assertThat(email1).isEqualTo(email2);
}
```

---

### Ejercicio 2: Proteger un invariante

**Objetivo**: Añadir validación para que un pedido no pueda tener más de 10 items.

**Requisitos**:
- La validación debe estar en el aggregate `Order`
- Lanzar excepción si se intenta añadir más de 10 items
- Escribir test que verifique el comportamiento
- Añadir constante `MAX_ITEMS`

**Ubicación**: `domain/model/Order.java` en el módulo hexagonal

**Solución esperada**:
```java
public class Order {
    private static final int MAX_ITEMS = 10;
    
    public void addItem(OrderItem item) {
        if (this.items.size() >= MAX_ITEMS) {
            throw new IllegalStateException(
                "Un pedido no puede tener más de " + MAX_ITEMS + " items"
            );
        }
        this.items.add(item);
        this.total = calculateTotal();
    }
}
```

**Test**:
```java
@Test
void shouldNotAllowMoreThanMaxItems() {
    Order order = Order.create(customerId, List.of());
    
    // Añadir 10 items
    for (int i = 0; i < 10; i++) {
        order.addItem(new OrderItem("P" + i, "Product " + i, new Money(10), 1));
    }
    
    // Intentar añadir el 11º
    assertThatThrownBy(() -> order.addItem(new OrderItem("P11", "Product 11", new Money(10), 1)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("no puede tener más de 10 items");
}
```

---

### Ejercicio 3: Implementar un método de dominio

**Objetivo**: Implementar el método `calculateTotal()` en el aggregate `Order`.

**Requisitos**:
- Calcular el total sumando precio * cantidad de cada item
- Usar el Value Object `Money` para el cálculo
- El método debe ser privado
- Llamarse cada vez que se añade o elimina un item

**Ubicación**: `domain/model/Order.java` en el módulo hexagonal

**Solución esperada**:
```java
private Money calculateTotal() {
    return items.stream()
        .map(item -> item.getPrice().multiply(item.getQuantity()))
        .reduce(Money.ZERO, Money::add);
}
```

---

## Nivel 2: Arquitecturas

### Ejercicio 4: Implementar un nuevo Input Port

**Objetivo**: Añadir un caso de uso para cancelar pedidos en el módulo hexagonal.

**Requisitos**:
- Crear interfaz `CancelOrderUseCase` en `domain/port/input/`
- Implementar `CancelOrderService` en `application/`
- Crear endpoint REST en `infrastructure/adapter/input/rest/`
- Validar que solo se pueden cancelar pedidos pendientes
- Publicar evento `OrderCancelledEvent`

**Pasos**:

1. Crear el port:
```java
// domain/port/input/CancelOrderUseCase.java
public interface CancelOrderUseCase {
    Order cancel(OrderId orderId);
}
```

2. Implementar el servicio:
```java
// application/CancelOrderService.java
@Service
public class CancelOrderService implements CancelOrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;
    
    @Transactional
    @Override
    public Order cancel(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        order.cancel();
        orderRepository.save(order);
        eventPublisher.publish(order.pullDomainEvents());
        return order;
    }
}
```

3. Añadir método en el aggregate:
```java
// domain/model/Order.java
public void cancel() {
    if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
        throw new IllegalStateException("No se pueden cancelar pedidos enviados o entregados");
    }
    this.status = OrderStatus.CANCELLED;
    addDomainEvent(new OrderCancelledEvent(this.id));
}
```

4. Crear el endpoint:
```java
// infrastructure/adapter/input/rest/OrderController.java
@PatchMapping("/orders/{id}/cancel")
public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String id) {
    Order order = cancelOrderUseCase.cancel(new OrderId(id));
    return ResponseEntity.ok(toResponse(order));
}
```

5. Escribir tests:
```java
@Test
void shouldCancelPendingOrder() {
    Order order = Order.create(customerId, items);
    order.cancel();
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
}

@Test
void shouldNotCancelShippedOrder() {
    Order order = Order.create(customerId, items);
    order.confirm();
    order.ship();
    
    assertThatThrownBy(() -> order.cancel())
        .isInstanceOf(IllegalStateException.class);
}
```

---

### Ejercicio 5: Implementar CQRS para un nuevo recurso

**Objetivo**: Añadir CQRS para gestión de categorías de productos en el módulo cqrs.

**Requisitos**:
- Command side: crear, actualizar, eliminar categorías
- Query side: listar categorías, buscar por ID
- Separar write model (`Category`) de read model (`CategoryView`)
- Implementar buses y handlers

**Pasos**:

1. Crear write model:
```java
// product/domain/Category.java
public class Category {
    private CategoryId id;
    private String name;
    private String description;
    
    public static Category create(String name, String description) {
        Category category = new Category();
        category.id = CategoryId.generate();
        category.name = name;
        category.description = description;
        return category;
    }
    
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
```

2. Crear read model:
```java
// product/readmodel/CategoryView.java
public class CategoryView {
    private String id;
    private String name;
    private String description;
    private int productCount; // Pre-calculado
    
    // Constructor, getters, setters
}
```

3. Crear commands:
```java
// product/command/CreateCategoryCommand.java
public record CreateCategoryCommand(String name, String description) implements Command<Void> {}

// product/command/UpdateCategoryCommand.java
public record UpdateCategoryCommand(String categoryId, String name, String description) implements Command<Void> {}

// product/command/DeleteCategoryCommand.java
public record DeleteCategoryCommand(String categoryId) implements Command<Void> {}
```

4. Crear queries:
```java
// product/query/GetAllCategoriesQuery.java
public record GetAllCategoriesQuery() implements Query<List<CategoryView>> {}

// product/query/GetCategoryByIdQuery.java
public record GetCategoryByIdQuery(String categoryId) implements Query<CategoryView> {}
```

5. Implementar handlers:
```java
// product/command/handler/CreateCategoryHandler.java
@Component
public class CreateCategoryHandler implements CommandHandler<CreateCategoryCommand, Void> {
    private final CategoryWriteRepository writeRepository;
    private final CategoryViewRepository viewRepository;
    
    @Override
    public Void handle(CreateCategoryCommand cmd) {
        Category category = Category.create(cmd.name(), cmd.description());
        writeRepository.save(category);
        
        CategoryView view = new CategoryView(
            category.getId().getValue(),
            category.getName(),
            category.getDescription(),
            0
        );
        viewRepository.save(view);
        
        return null;
    }
}

// product/query/handler/GetAllCategoriesHandler.java
@Component
public class GetAllCategoriesHandler implements QueryHandler<GetAllCategoriesQuery, List<CategoryView>> {
    private final CategoryViewRepository viewRepository;
    
    @Override
    public List<CategoryView> handle(GetAllCategoriesQuery query) {
        return viewRepository.findAll();
    }
}
```

6. Crear controllers:
```java
// infrastructure/rest/CategoryCommandController.java
@RestController
@RequestMapping("/api/commands/categories")
public class CategoryCommandController {
    private final CommandBus commandBus;
    
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateCategoryRequest request) {
        CreateCategoryCommand cmd = new CreateCategoryCommand(request.getName(), request.getDescription());
        commandBus.dispatch(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

// infrastructure/rest/CategoryQueryController.java
@RestController
@RequestMapping("/api/queries/categories")
public class CategoryQueryController {
    private final QueryBus queryBus;
    
    @GetMapping
    public ResponseEntity<List<CategoryView>> getAll() {
        List<CategoryView> categories = queryBus.dispatch(new GetAllCategoriesQuery());
        return ResponseEntity.ok(categories);
    }
}
```

---

### Ejercicio 6: Implementar un Bounded Context

**Objetivo**: Crear un bounded context `transaction` en el módulo DDD.

**Requisitos**:
- Modelar transacciones bancarias con su propio lenguaje
- Crear aggregate `Transaction` con value objects
- Implementar domain events para transacciones
- Separar del contexto `account`

**Pasos**:

1. Crear estructura de paquetes:
```
transaction/
├── domain/
│   ├── model/
│   │   ├── Transaction.java
│   │   ├── TransactionId.java
│   │   ├── TransactionAmount.java
│   │   └── TransactionType.java
│   ├── event/
│   │   └── TransactionEvent.java
│   └── repository/
│       └── TransactionRepository.java
├── application/
│   └── TransactionApplicationService.java
└── infrastructure/
    ├── persistence/
    │   ├── TransactionEntity.java
    │   └── TransactionRepositoryAdapter.java
    └── rest/
        └── TransactionController.java
```

2. Crear value objects:
```java
// domain/model/TransactionType.java
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_IN,
    TRANSFER_OUT
}

// domain/model/TransactionAmount.java
public final class TransactionAmount {
    private final Money amount;
    private final Currency currency;
    
    public TransactionAmount(Money amount) {
        if (amount.isNegative()) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        this.amount = amount;
        this.currency = amount.getCurrency();
    }
}
```

3. Crear aggregate:
```java
// domain/model/Transaction.java
public class Transaction {
    private TransactionId id;
    private AccountId accountId;
    private TransactionType type;
    private TransactionAmount amount;
    private LocalDateTime timestamp;
    
    public static Transaction deposit(AccountId accountId, Money amount) {
        Transaction transaction = new Transaction();
        transaction.id = TransactionId.generate();
        transaction.accountId = accountId;
        transaction.type = TransactionType.DEPOSIT;
        transaction.amount = new TransactionAmount(amount);
        transaction.timestamp = LocalDateTime.now();
        transaction.addDomainEvent(new TransactionCreatedEvent(transaction));
        return transaction;
    }
    
    public static Transaction withdrawal(AccountId accountId, Money amount) {
        Transaction transaction = new Transaction();
        transaction.id = TransactionId.generate();
        transaction.accountId = accountId;
        transaction.type = TransactionType.WITHDRAWAL;
        transaction.amount = new TransactionAmount(amount);
        transaction.timestamp = LocalDateTime.now();
        transaction.addDomainEvent(new TransactionCreatedEvent(transaction));
        return transaction;
    }
}
```

4. Crear repository y service:
```java
// domain/repository/TransactionRepository.java
public interface TransactionRepository {
    void save(Transaction transaction);
    Optional<Transaction> findById(TransactionId id);
    List<Transaction> findByAccountId(AccountId accountId);
}

// application/TransactionApplicationService.java
@Service
public class TransactionApplicationService {
    private final TransactionRepository transactionRepository;
    
    @Transactional
    public Transaction recordDeposit(AccountId accountId, Money amount) {
        Transaction transaction = Transaction.deposit(accountId, amount);
        transactionRepository.save(transaction);
        return transaction;
    }
}
```

---

## Nivel 3: Avanzado

### Ejercicio 7: Mezclar Hexagonal + CQRS

**Objetivo**: Refactorizar el módulo hexagonal para usar CQRS en las consultas.

**Requisitos**:
- Mantener la arquitectura hexagonal
- Añadir CQRS solo para el lado de lectura
- Crear read model optimizado para consultas
- Usar puertos para separar concerns

**Pasos**:

1. Crear read model:
```java
// domain/readmodel/OrderView.java
public class OrderView {
    private String id;
    private String customerId;
    private String customerName;
    private OrderStatus status;
    private BigDecimal total;
    private String formattedTotal;
    private int itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor, getters
}
```

2. Crear puerto de lectura:
```java
// domain/port/output/OrderViewRepository.java
public interface OrderViewRepository {
    void save(OrderView view);
    Optional<OrderView> findById(OrderId id);
    List<OrderView> findAll();
    List<OrderView> findByCustomerId(CustomerId customerId);
}
```

3. Crear adapter de lectura:
```java
// infrastructure/adapter/output/persistence/OrderViewRepositoryAdapter.java
@Repository
public class OrderViewRepositoryAdapter implements OrderViewRepository {
    private final SpringDataOrderViewRepository jpaRepository;
    
    @Override
    public void save(OrderView view) {
        OrderViewEntity entity = toEntity(view);
        jpaRepository.save(entity);
    }
    
    @Override
    public Optional<OrderView> findById(OrderId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }
    
    // ... otros métodos
}
```

4. Actualizar el servicio para proyectar:
```java
// application/CreateOrderService.java
@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderViewRepository viewRepository;
    private final OrderEventPublisher eventPublisher;
    
    @Transactional
    @Override
    public Order create(CreateOrderCommand cmd) {
        Customer customer = customerRepository.findById(cmd.getCustomerId());
        Order order = Order.create(customer, cmd.getItems());
        orderRepository.save(order);
        
        // Proyectar al read model
        OrderView view = toView(order, customer);
        viewRepository.save(view);
        
        eventPublisher.publish(order.pullDomainEvents());
        return order;
    }
    
    private OrderView toView(Order order, Customer customer) {
        return new OrderView(
            order.getId().getValue(),
            customer.getId().getValue(),
            customer.getName(),
            order.getStatus(),
            order.getTotal().getAmount(),
            formatMoney(order.getTotal()),
            order.getItems().size(),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
}
```

5. Crear query controller:
```java
// infrastructure/adapter/input/rest/OrderQueryController.java
@RestController
@RequestMapping("/api/queries/orders")
public class OrderQueryController {
    private final OrderViewRepository viewRepository;
    
    @GetMapping
    public ResponseEntity<List<OrderView>> getAllOrders() {
        List<OrderView> orders = viewRepository.findAll();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderView> getOrder(@PathVariable String id) {
        return viewRepository.findById(new OrderId(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

---

### Ejercicio 8: Implementar Event Sourcing

**Objetivo**: Cambiar la persistencia a event sourcing para el módulo DDD.

**Requisitos**:
- Guardar eventos en lugar de estado actual
- Reconstruir estado a partir de eventos
- Implementar snapshot para optimizar

**Pasos**:

1. Crear event store:
```java
// domain/port/output/EventStore.java
public interface EventStore {
    void save(String aggregateId, List<DomainEvent> events, int expectedVersion);
    List<DomainEvent> getEvents(String aggregateId);
}
```

2. Modificar aggregate para emitir eventos:
```java
// domain/model/Account.java
public class Account {
    private List<DomainEvent> newEvents = new ArrayList<>();
    private int version = 0;
    
    public void deposit(Money amount) {
        if (amount.isNegative()) {
            throw new InvalidAmountException();
        }
        this.balance = this.balance.add(amount);
        this.newEvents.add(new MoneyDepositedEvent(this.id, amount));
        this.version++;
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return List.copyOf(newEvents);
    }
    
    public void markEventsAsCommitted() {
        this.newEvents.clear();
    }
    
    // Método para reconstruir desde eventos
    public static Account fromEvents(List<DomainEvent> events) {
        Account account = new Account();
        for (DomainEvent event : events) {
            account.apply(event);
        }
        return account;
    }
    
    private void apply(DomainEvent event) {
        if (event instanceof AccountOpenedEvent) {
            apply((AccountOpenedEvent) event);
        } else if (event instanceof MoneyDepositedEvent) {
            apply((MoneyDepositedEvent) event);
        }
        // ... otros eventos
        this.version++;
    }
}
```

3. Implementar event store:
```java
// infrastructure/persistence/JpaEventStore.java
@Repository
public class JpaEventStore implements EventStore {
    private final SpringDataEventStoreRepository jpaRepository;
    
    @Override
    public void save(String aggregateId, List<DomainEvent> events, int expectedVersion) {
        for (DomainEvent event : events) {
            EventEntity entity = new EventEntity(
                aggregateId,
                event.getClass().getSimpleName(),
                serialize(event),
                expectedVersion + 1
            );
            jpaRepository.save(entity);
        }
    }
    
    @Override
    public List<DomainEvent> getEvents(String aggregateId) {
        return jpaRepository.findByAggregateIdOrderByVersion(aggregateId)
            .stream()
            .map(this::deserialize)
            .toList();
    }
}
```

4. Crear snapshot:
```java
// domain/model/AccountSnapshot.java
public class AccountSnapshot {
    private String accountId;
    private Money balance;
    private int version;
    private LocalDateTime lastUpdated;
    
    // Constructor, getters
}

// infrastructure/persistence/AccountSnapshotRepository.java
@Repository
public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshotEntity, String> {
    Optional<AccountSnapshotEntity> findByAccountId(String accountId);
}
```

---

## Challenges

### Challenge 1: Migración gradual

**Escenario**: Tienes un monolito con arquitectura layered y quieres migrar a hexagonal gradualmente.

**Preguntas**:
1. ¿Por dónde empezar?
2. ¿Cómo conviven ambas arquitecturas?
3. ¿Cómo pruebas que la migración no rompe nada?

**Propuesta de solución**:
1. Identificar un bounded context con lógica de negocio compleja
2. Extraer el dominio a un módulo separado
3. Crear puertos para las dependencias
4. Implementar adaptadores que usen el código existente
5. Migrar gradualmente otros bounded contexts
6. Usar feature flags para cambiar entre arquitecturas
7. Escribir tests de integración para verificar comportamiento

---

### Challenge 2: CQRS con consistencia eventual

**Escenario**: Tu sistema CQRS necesita consistencia eventual entre write y read model.

**Preguntas**:
1. ¿Cómo implementas la sincronización?
2. ¿Cómo manejas errores en la proyección?
3. ¿Cómo comunicas a los usuarios que los datos pueden estar desactualizados?

**Propuesta de solución**:
1. Usar una cola de mensajes para la proyección asíncrona
2. Implementar reintentos automáticos para errores transitorios
3. Usar dead letter queue para errores permanentes
4. Añadir timestamp de última actualización en el read model
5. Mostrar indicador de "datos desactualizados" en la UI
6. Permitir refresco manual por parte del usuario
7. Implementar compensación transactions cuando sea necesario

---

### Challenge 3: DDD en equipo grande

**Escenario**: Tienes 10 desarrolladores trabajando en bounded contexts diferentes.

**Preguntas**:
1. ¿Cómo coordinas los bounded contexts?
2. ¿Cómo manejas dependencias entre contextos?
3. ¿Cómo evitas duplicación de código?

**Propuesta de solución**:
1. Definir context mapping claro entre bounded contexts
2. Usar equipos por bounded context (Conway's Law)
3. Implementar servicios de integración para comunicación
4. Usar contratos (OpenAPI) para integraciones
5. Crear shared kernel para código común
6. Reuniones periódicas de alineación
7. Documentación de bounded contexts y sus relaciones
8. Tests de contrato para integraciones

---

## Evaluación

### Nivel 1 - Fundamentos
- [ ] Ejercicio 1: Value Object Email
- [ ] Ejercicio 2: Proteger invariante
- [ ] Ejercicio 3: Método de dominio

### Nivel 2 - Arquitecturas
- [ ] Ejercicio 4: Input Port
- [ ] Ejercicio 5: CQRS para categorías
- [ ] Ejercicio 6: Bounded Context

### Nivel 3 - Avanzado
- [ ] Ejercicio 7: Hexagonal + CQRS
- [ ] Ejercicio 8: Event Sourcing

### Challenges
- [ ] Challenge 1: Migración gradual
- [ ] Challenge 2: Consistencia eventual
- [ ] Challenge 3: DDD en equipo grande

---

## Referencias

- **GLOSARIO.md** - Definiciones de conceptos
- **docs/ANTI_PATRONES.md** - Anti-patrones a evitar
- **docs/ERRORES_COMUNES.md** - Errores frecuentes
- **docs/TROUBLESHOOTING.md** - Guía de resolución de problemas
- **ROADMAP.md** - Roadmap de aprendizaje
