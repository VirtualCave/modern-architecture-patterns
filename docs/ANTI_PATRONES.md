# Ejemplos de anti-patrones frecuentes en arquitecturas modernas y cómo evitarlos.

## Índice de contenidos

- [Cómo leer esta guía](#cómo-leer-esta-guía)
- [Anemic Domain Model](#anemic-domain-model)
- [Leaking Abstraction](#leaking-abstraction)
- [Command como Query disfrazada](#command-como-query-disfrazada)
- [God Service](#god-service)
- [Primitive Obsession](#primitive-obsession)
- [Database-Driven Design](#database-driven-design)
- [No proteger invariantes](#no-proteger-invariantes)
- [Exponer entidades de persistencia](#exponer-entidades-de-persistencia)
- [Mezclar bounded contexts](#mezclar-bounded-contexts)
- [No usar Value Objects](#no-usar-value-objects)
- [Resumen](#resumen)
- [Referencias](#referencias)

## Cómo leer esta guía

No todos los anti-patrones aparecen en todas las arquitecturas. Algunos son **transversales** y pueden aparecer en cualquier proyecto moderno; otros son más típicos de una arquitectura concreta, sobre todo **CQRS** o **DDD**.

### Resumen rápido por ámbito

| Ámbito | Anti-patrones que suelen aparecer aquí |
|--------|----------------------------------------|
| **Transversales / varias arquitecturas** | 1. Anemic Domain Model, 2. Leaking Abstraction, 4. God Service, 5. Primitive Obsession, 6. Database-Driven Design, 8. Exponer entidades de persistencia |
| **CQRS** | 3. Command como Query disfrazada |
| **DDD** | 7. No proteger invariantes, 9. Mezclar bounded contexts, 10. No usar Value Objects |

**Idea general:** primero mira el ámbito para saber si el problema es de diseño general o de una arquitectura concreta.

---

## Anemic Domain Model

**Ámbito:** Transversal / DDD

### Descripción

El modelo de dominio no tiene comportamiento, solo getters/setters. La lógica de negocio se dispersa en servicios.

### Por qué es malo

- Pierdes las ventajas de DDD
- El modelo se convierte en un contenedor de datos
- Difícil de mantener y evolucionar
- La lógica de negocio no está donde debe estar

### Ejemplo: ❌ MAL - Modelo anémico

```java
public class Order {
    private OrderId id;
    private OrderStatus status;
    private Money total;
    
    // Solo getters/setters, sin comportamiento
    public void setStatus(OrderStatus status) {
        this.status = status; // No valida transiciones
    }
    
    public void setTotal(Money total) {
        this.total = total;
    }
}

// La lógica está en un servicio
@Service
public class OrderService {
    public void confirmOrder(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        order.setStatus(OrderStatus.CONFIRMED);
    }
}
```

### Ejemplo: ✅ BIEN - Modelo rico

```java
public class Order {
    private OrderId id;
    private OrderStatus status;
    private Money total;
    
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    
    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Solo se pueden enviar pedidos confirmados");
        }
        this.status = OrderStatus.SHIPPED;
    }
    
    public void cancel() {
        if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("No se pueden cancelar pedidos enviados o entregados");
        }
        this.status = OrderStatus.CANCELLED;
    }
}

// El servicio solo orquesta
@Service
public class OrderService {
    public void confirmOrder(OrderId id) {
        Order order = repository.findById(id);
        order.confirm(); // Lógica en el dominio
        repository.save(order);
    }
}
```

### Cómo evitarlo

- Poner lógica de negocio en aggregates y value objects
- Usar métodos que expresan intención (confirm(), ship()) en lugar de setters
- Evitar getters/setters públicos que rompan invariantes
- Hacer que el dominio sea rico en comportamiento

---

## Leaking Abstraction

**Ámbito:** Transversal / Hexagonal

### Descripción

Detalles de implementación se filtran a capas superiores. El dominio depende de tecnología concreta.

### Por qué es malo

- Rompe el aislamiento de capas
- Difícil cambiar la tecnología sin afectar el dominio
- El dominio no es testeable sin la infraestructura
- Violación de Dependency Inversion Principle

### Ejemplo: ❌ MAL - Dominio con JPA

```java
@Entity
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    @Column(nullable = false)
    private String status;
    
    // El dominio depende de JPA
}
```

### Ejemplo: ✅ BIEN - Dominio puro

```java
// Dominio - sin dependencias técnicas
public class Order {
    private OrderId id;
    private List<OrderItem> items;
    private OrderStatus status;
    
    public Order(OrderId id, List<OrderItem> items) {
        this.id = id;
        this.items = items;
        this.status = OrderStatus.PENDING;
    }
}

// Entidad de persistencia - en infraestructura
@Entity
public class OrderEntity {
    @Id
    private String id;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;
    
    @Column(nullable = false)
    private String status;
    
    // Mappers convierten entre dominio y entidad
}

// Mapper en infraestructura
@Mapper
public interface OrderPersistenceMapper {
    OrderEntity toEntity(Order order);
    Order toDomain(OrderEntity entity);
}
```

### Cómo evitarlo

- Mantener el dominio libre de anotaciones de persistencia
- Usar mappers para convertir entre dominio y entidades
- Definir puertos en el dominio, implementar adaptadores en infraestructura
- No exponer entidades de persistencia fuera de la capa de infraestructura

---

## Command como Query disfrazada

**Ámbito:** CQRS

### Descripción

Un command devuelve datos de lectura, violando el principio de CQRS.

### Por qué es malo

- Mezcla responsabilidades de escritura y lectura
- Difícil optimizar cada lado independientemente
- El command handler se vuelve complejo
- No aprovechas los beneficios de CQRS

### Ejemplo: ❌ MAL - Command que devuelve datos

```java
public class GetProductCommand implements Command<ProductDto> {
    private String productId;
    
    @Override
    public ProductDto execute() {
        Product product = repository.findById(productId);
        return toDto(product); // ❌ Esto es una query, no un command
    }
}

// El controlador usa el command como query
@RestController
public class ProductController {
    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable String id) {
        return commandBus.dispatch(new GetProductCommand(id)); // ❌ Mal uso
    }
}
```

### Ejemplo: ✅ BIEN - Command solo cambia estado

```java
public class UpdateProductPriceCommand implements Command<Void> {
    private String productId;
    private BigDecimal newPrice;
    
    @Override
    public Void execute() {
        Product product = repository.findById(productId);
        product.updatePrice(newPrice);
        repository.save(product);
        return null; // ✅ No devuelve datos
    }
}

// Query para consultar
public class GetProductByIdQuery implements Query<ProductDto> {
    private String productId;
}

// Controlador usa command y query correctamente
@RestController
public class ProductController {
    @PostMapping("/products/{id}/price")
    public void updatePrice(@PathVariable String id, @RequestBody UpdatePriceRequest request) {
        commandBus.dispatch(new UpdateProductPriceCommand(id, request.getNewPrice()));
    }
    
    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable String id) {
        return queryBus.dispatch(new GetProductByIdQuery(id)); // ✅ Query para consultar
    }
}
```

### Cómo evitarlo

- Commands deben devolver `Void` o un ID simple
- Queries deben devolver datos
- No usar commands para consultas disfrazadas
- Separar claramente endpoints de commands y queries

---

## God Service

**Ámbito:** Transversal

### Descripción

Un servicio hace demasiado, violando Single Responsibility Principle.

### Por qué es malo

- Difícil de mantener
- Difícil de probar
- Difícil de entender
- Un cambio puede afectar múltiples responsabilidades

### Ejemplo: ❌ MAL - God Service

```java
@Service
public class OrderService {
    public Order createOrder(CreateOrderRequest request) {
        // Validación
        if (request.getItems().isEmpty()) {
            throw new InvalidOrderException();
        }
        
        // Crear orden
        Order order = new Order();
        order.setCustomer(request.getCustomer());
        order.setItems(request.getItems());
        
        // Calcular total
        BigDecimal total = request.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
        
        // Guardar en BD
        orderRepository.save(order);
        
        // Actualizar inventario
        for (OrderItem item : request.getItems()) {
            inventoryService.updateStock(item.getProductId(), -item.getQuantity());
        }
        
        // Enviar email de confirmación
        emailService.sendConfirmation(order);
        
        // Actualizar estadísticas
        statisticsService.recordOrder(order);
        
        // Publicar evento
        eventPublisher.publish(new OrderCreatedEvent(order));
        
        return order;
    }
    
    // ... muchos más métodos que hacen cosas diferentes
}
```

### Ejemplo: ✅ BIEN - Servicios con responsabilidad única

```java
// Application Service - orquesta el caso de uso
@Service
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final OrderEventPublisher eventPublisher;
    
    public Order create(CreateOrderCommand cmd) {
        // Cargar datos necesarios
        Customer customer = customerRepository.findById(cmd.getCustomerId());
        
        // Lógica de dominio
        Order order = Order.create(customer, cmd.getItems());
        
        // Persistir
        orderRepository.save(order);
        
        // Efectos secundarios
        inventoryService.reserveItems(order.getItems());
        eventPublisher.publish(order.pullDomainEvents());
        
        return order;
    }
}

// Domain Service - lógica de dominio compleja
@Service
public class InventoryService {
    public void reserveItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId());
            product.reserveStock(item.getQuantity());
            productRepository.save(product);
        }
    }
}

// Event Handler - reacciona a eventos
@Component
public class OrderEmailHandler {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        emailService.sendConfirmation(event.getOrder());
    }
}
```

### Cómo evitarlo

- Dividir servicios por responsabilidad
- Application Services para orquestación
- Domain Services para lógica de dominio compleja
- Event Handlers para efectos secundarios
- Un servicio = una responsabilidad clara

---

## Primitive Obsession

**Ámbito:** Transversal / DDD

### Descripción

Uso excesivo de tipos primitivos (String, int, long) en lugar de tipos de dominio.

### Por qué es malo

- Pierdes validación
- Pierdes semántica
- Pierdes comportamiento
- El código es más propenso a errores

### Ejemplo: ❌ MAL - Primitive Obsession

```java
public class Account {
    public void withdraw(String accountId, BigDecimal amount, String currency) {
        // ¿Es accountId válido? ¿Es amount positivo? ¿Misma moneda?
        // No hay validación ni semántica
    }
}

public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
    // ¿Son IDs válidos? ¿Es amount positivo?
    // ¿Cuentas existen? ¿Suficiente saldo?
    // Todo es String y BigDecimal, sin significado
}
```

### Ejemplo: ✅ BIEN - Tipos de dominio

```java
// Value Objects con validación y comportamiento
public final class AccountId {
    private final String value;
    
    private AccountId(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAccountIdException();
        }
        this.value = value;
    }
    
    public static AccountId of(String value) {
        return new AccountId(value);
    }
    
    public String getValue() {
        return value;
    }
}

public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyException();
        }
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException();
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException();
        }
        if (this.amount.compareTo(other.amount) < 0) {
            throw new InsufficientFundsException();
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }
}

// Uso en el dominio
public class Account {
    public void withdraw(Money amount) {
        if (this.balance.lessThan(amount)) {
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.subtract(amount);
    }
}

public void transfer(AccountId from, AccountId to, Money amount) {
    // Los tipos ya validan y encapsulan comportamiento
    Account fromAccount = repository.findById(from);
    Account toAccount = repository.findById(to);
    fromAccount.withdraw(amount);
    toAccount.deposit(amount);
}
```

### Cómo evitarlo

- Crear Value Objects para conceptos de dominio
- Encapsular validación en los tipos
- Usar tipos de dominio en lugar de primitivos
- Los tipos deben tener significado y comportamiento

---

## Database-Driven Design

**Ámbito:** Transversal

### Descripción

Diseñar el software pensando primero en tablas y relaciones de base de datos.

### Por qué es malo

- El código refleja la BD, no el negocio
- Difícil de entender para no técnicos
- Cambios en la BD afectan el código
- El modelo no expresa el lenguaje del negocio

### Ejemplo: ❌ MAL - Database-Driven

Primero pensamos en tablas y columnas:
- `orders`: id, customer_id, status, total, created_at
- `order_items`: id, order_id, product_id, quantity, price

Después creamos entidades que copian esa estructura casi tal cual:
- `Order` con campos `Long` y `String`
- `OrderItem` con campos muy parecidos a la tabla
- El lenguaje del negocio queda escondido detrás de detalles técnicos

### Ejemplo: ✅ BIEN - Domain-Driven

```java
// Primero modelamos el dominio
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private OrderStatus status;
    private Money total;
    private List<OrderItem> items;
    
    public static Order create(CustomerId customerId, List<OrderItem> items) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.items = items;
        order.total = calculateTotal(items);
        return order;
    }
    
    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(item -> item.getPrice().multiply(item.getQuantity()))
            .reduce(Money.ZERO, Money::add);
    }
}

// Luego diseñamos la persistencia como un detalle técnico
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "status")
    private String status;
    
    // ... mapeo a BD
}
```

### Cómo evitarlo

- Modelar primero el dominio con DDD
- Diseñar la persistencia como un detalle técnico
- Usar mappers para convertir entre dominio y BD
- El modelo debe reflejar el lenguaje del negocio

---

## No proteger invariantes

**Ámbito:** DDD

### Descripción

Las reglas de negocio no se protegen en el aggregate root.

### Por qué es malo

- El dominio puede estar en estado inválido
- La lógica de negocio se dispersa
- Difícil garantizar consistencia
- Errores sutiles y difíciles de detectar

### Ejemplo: ❌ MAL - Invariantes no protegidos

```java
public class Account {
    private Money balance;
    
    public void withdraw(Money amount) {
        this.balance = this.balance.subtract(amount); // ❌ No valida saldo
    }
    
    public void deposit(Money amount) {
        this.balance = this.balance.add(amount); // ❌ No valida amount positivo
    }
}

// La validación está en el servicio
@Service
public class AccountService {
    public void withdraw(AccountId id, Money amount) {
        Account account = repository.findById(id);
        if (account.getBalance().lessThan(amount)) { // ❌ Validación fuera del dominio
            throw new InsufficientFundsException();
        }
        account.withdraw(amount);
        repository.save(account);
    }
}
```

### Ejemplo: ✅ BIEN - Invariantes protegidos

```java
public class Account {
    private Money balance;
    
    public void withdraw(Money amount) {
        if (this.balance.lessThan(amount)) { // ✅ Validación en el dominio
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.subtract(amount);
    }
    
    public void deposit(Money amount) {
        if (amount.isNegative()) { // ✅ Validación en el dominio
            throw new InvalidAmountException();
        }
        this.balance = this.balance.add(amount);
    }
}

// El servicio solo orquesta
@Service
public class AccountService {
    public void withdraw(AccountId id, Money amount) {
        Account account = repository.findById(id);
        account.withdraw(amount); // ✅ El dominio protege sus invariantes
        repository.save(account);
    }
}
```

### Cómo evitarlo

- Poner validaciones en el aggregate root
- El aggregate es responsable de proteger sus invariantes
- No exponer setters que rompan invariantes
- Usar métodos que expresen intención y validen

---

## Exponer entidades de persistencia

**Ámbito:** Transversal / Hexagonal

### Descripción

Las entidades JPA se exponen directamente en las APIs.

### Por qué es malo

- Acopla la API a la implementación de persistencia
- Difícil cambiar la estructura de la BD
- Puede exponer datos sensibles
- Violación de encapsulamiento

### Ejemplo: ❌ MAL - Exponer entidades JPA

```java
@RestController
public class ProductController {
    @GetMapping("/products/{id}")
    public ProductEntity getProduct(@PathVariable Long id) { // ❌ Expone JPA entity
        return productRepository.findById(id).orElseThrow();
    }
    
    @PostMapping("/products")
    public ProductEntity createProduct(@RequestBody ProductEntity product) { // ❌ Recibe JPA entity
        return productRepository.save(product);
    }
}
```

### Ejemplo: ✅ BIEN - Usar DTOs

```java
@RestController
public class ProductController {
    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) { // ✅ Expone DTO
        Product product = productRepository.findById(id);
        return productMapper.toDto(product);
    }
    
    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody CreateProductRequest request) { // ✅ Recibe DTO
        CreateProductCommand cmd = toCommand(request);
        Product product = createProductHandler.handle(cmd);
        return productMapper.toDto(product);
    }
}
```

### Cómo evitarlo

- Usar DTOs para APIs
- Usar mappers para convertir entre dominio y DTOs
- No exponer entidades de persistencia fuera de la capa de infraestructura
- La API debe ser independiente de la implementación

---

## Mezclar bounded contexts

**Ámbito:** DDD

### Descripción

Diferentes bounded contexts se mezclan sin límites claros.

### Por qué es malo

- Difícil de mantener
- Acoplamiento entre contextos
- Violación de bounded contexts
- El lenguaje se vuelve ambiguo

### Ejemplo: ❌ MAL - Contextos mezclados

```java
// Account context depende directamente de Customer context
public class Account {
    private Customer customer; // ❌ Account depende de Customer
}

// O peor, usa directamente entidades de otro contexto
public class Account {
    private CustomerEntity customerEntity; // ❌ Dependencia de infraestructura de otro contexto
}
```

### Ejemplo: ✅ BIEN - Contextos separados

```java
// Account context - solo IDs
public class Account {
    private AccountId id;
    private CustomerId ownerId; // ✅ Solo ID, no el objeto completo
}

// Si se necesita el customer, se busca por ID
public class AccountService {
    public AccountDto getAccountWithCustomer(AccountId id) {
        Account account = accountRepository.findById(id);
        Customer customer = customerRepository.findById(account.getOwnerId()); // ✅ Búsqueda explícita
        return toDto(account, customer);
    }
}

// O usa un servicio de integración
@Service
public class CustomerIntegrationService {
    public CustomerInfo getCustomerInfo(CustomerId id) {
        // Llama al bounded context de customer
        return customerClient.getCustomer(id);
    }
}
```

### Cómo evitarlo

- Definir bounded contexts claros
- Usar IDs para referencias entre contextos
- No compartir entidades entre contextos
- Usar servicios de integración para comunicación entre contextos

---

## No usar Value Objects

**Ámbito:** DDD

### Descripción

Conceptos importantes del dominio se representan con primitivos en lugar de Value Objects.

### Por qué es malo

- Pierdes validación
- Pierdes semántica
- Código más propenso a errores
- Duplicación de validación

### Ejemplo: ❌ MAL - No usar Value Objects

```java
public class Order {
    private String orderId; // ❌ String para ID
    private BigDecimal total; // ❌ BigDecimal para dinero
    private String currency; // ❌ String para moneda
    private String email; // ❌ String para email
}

public void createOrder(String orderId, BigDecimal total, String currency, String email) {
    // ¿Es orderId válido? ¿Es total positivo? ¿Es currency válida? ¿Es email válido?
    // Validación dispersa y duplicada
}
```

### Ejemplo: ✅ BIEN - Usar Value Objects

```java
public class Order {
    private OrderId orderId; // ✅ Value Object
    private Money total; // ✅ Value Object
    private Email customerEmail; // ✅ Value Object
}

public void createOrder(OrderId orderId, Money total, Email email) {
    // Los Value Objects ya validan
    // Código más limpio y seguro
}
```

### Cómo evitarlo

- Identificar conceptos importantes del dominio
- Crear Value Objects para esos conceptos
- Encapsular validación en los Value Objects
- Usar Value Objects en lugar de primitivos

---

## Resumen

| Anti-patrón | Ámbito | Problema principal | Solución |
|-------------|--------|-------------------|----------|
| Anemic Domain Model | Transversal / DDD | Lógica dispersa en servicios | Poner lógica en el dominio |
| Leaking Abstraction | Transversal / Hexagonal | Dominio depende de tecnología | Usar puertos/adaptadores |
| Command como Query | CQRS | Mezcla lectura/escritura | Separar commands y queries |
| God Service | Transversal | Demasiadas responsabilidades | Dividir por responsabilidad |
| Primitive Obsession | Transversal / DDD | Falta de semántica y validación | Usar Value Objects |
| Database-Driven | Transversal | Código refleja BD | Modelar dominio primero |
| No proteger invariantes | DDD | Estado inválido posible | Validar en aggregates |
| Exponer entidades JPA | Transversal / Hexagonal | API acoplada a persistencia | Usar DTOs |
| Mezclar contexts | DDD | Acoplamiento entre contextos | Usar IDs y servicios de integración |
| No usar Value Objects | DDD | Validación dispersa | Crear Value Objects |

---

## Referencias

- Definiciones de conceptos
- Guía unificada de errores, soluciones y troubleshooting



