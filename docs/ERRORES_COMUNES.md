# Errores Comunes y Cómo Evitarlos

Errores frecuentes al implementar arquitecturas modernas y cómo solucionarlos.

---

## 1. Poner lógica de negocio en el controlador

### Síntomas

- El controlador contiene validaciones de negocio
- El controlador decide reglas de negocio
- Difícil de probar la lógica de negocio
- Lógica duplicada en diferentes controladores

### Error

```java
// ❌ MAL - Lógica de negocio en el controlador
@RestController
public class OrderController {
    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(request.getStatus()); // ❌ Lógica de negocio aquí
        
        // ❌ Validación de negocio en el controlador
        if (request.getItems().isEmpty()) {
            throw new InvalidOrderException("El pedido debe tener items");
        }
        
        // ❌ Cálculo de negocio en el controlador
        BigDecimal total = request.getItems().stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);
        
        return orderRepository.save(order);
    }
}
```

### Solución

```java
// ✅ BIEN - Delegar a caso de uso
@RestController
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        CreateOrderCommand cmd = toCommand(request);
        return createOrderUseCase.create(cmd); // ✅ Delegar a caso de uso
    }
    
    private CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
            new CustomerId(request.getCustomerId()),
            request.getItems().stream()
                .map(this::toOrderItem)
                .toList()
        );
    }
}

// La lógica está en el dominio
public class Order {
    public static Order create(CustomerId customerId, List<OrderItem> items) {
        if (items.isEmpty()) { // ✅ Validación en el dominio
            throw new InvalidOrderException("El pedido debe tener items");
        }
        
        Order order = new Order();
        order.customerId = customerId;
        order.items = items;
        order.total = calculateTotal(items); // ✅ Cálculo en el dominio
        order.status = OrderStatus.PENDING;
        return order;
    }
}
```

---

## 2. No proteger invariantes en el aggregate

### Síntomas

- Se crean objetos con estado inválido
- Se modifican objetos rompiendo reglas de negocio
- Validaciones dispersas en diferentes lugares
- Estado inconsistente en la base de datos

### Error

```java
// ❌ MAL - No proteger invariantes
public class Account {
    private Money balance;
    
    public void withdraw(Money amount) {
        this.balance = this.balance.subtract(amount); // ❌ No valida saldo
    }
    
    public void setBalance(Money balance) {
        this.balance = balance; // ❌ Setter público rompe invariantes
    }
}

// Alguien puede hacer:
account.withdraw(new Money(1000)); // Aunque solo tenga 100
account.setBalance(new Money(-100)); // Saldo negativo
```

### Solución

```java
// ✅ BIEN - Proteger invariantes
public class Account {
    private Money balance;
    
    public void withdraw(Money amount) {
        if (this.balance.lessThan(amount)) { // ✅ Protege invariante
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.subtract(amount);
    }
    
    public void deposit(Money amount) {
        if (amount.isNegative()) { // ✅ Protege invariante
            throw new InvalidAmountException();
        }
        this.balance = this.balance.add(amount);
    }
    
    // ✅ Sin setter público
    public Money getBalance() {
        return balance;
    }
}
```

---

## 3. Mezclar concerns de lectura y escritura

### Síntomas

- Los mismos métodos leen y escriben
- Difícil optimizar consultas
- Lógica de escritura mezclada con lógica de lectura
- Violación de CQRS

### Error

```java
// ❌ MAL - Mezclar lectura y escritura
public class ProductService {
    public ProductDto createProduct(CreateProductRequest request) {
        Product product = new Product(request.getName(), request.getPrice());
        productRepository.save(product);
        
        // ❌ Mezcla escritura y lectura
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setFormattedPrice(formatPrice(product.getPrice())); // ❌ Lógica de lectura
        dto.setAvailable(product.getStock() > 0); // ❌ Lógica de lectura
        
        return dto;
    }
    
    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id);
        // ❌ Usa la misma lógica que createProduct
        return toDtoWithCalculatedFields(product);
    }
}
```

### Solución

```java
// ✅ BIEN - Separar lectura y escritura (CQRS)

// Command side - solo escribe
public class CreateProductHandler implements CommandHandler<CreateProductCommand, Void> {
    private final ProductWriteRepository writeRepository;
    
    public Void handle(CreateProductCommand cmd) {
        Product product = new Product(cmd.getName(), cmd.getPrice());
        writeRepository.save(product);
        return null; // ✅ No devuelve datos
    }
}

// Query side - solo lee
public class GetProductByIdHandler implements QueryHandler<GetProductByIdQuery, ProductView> {
    private final ProductViewRepository viewRepository;
    
    public ProductView handle(GetProductByIdQuery query) {
        return viewRepository.findById(query.getProductId()); // ✅ Solo lee
    }
}

// Read model optimizado para consultas
public class ProductView {
    private String id;
    private String name;
    private BigDecimal price;
    private String formattedPrice; // ✅ Pre-calculado
    private boolean available; // ✅ Pre-calculado
}
```

---

## 4. Exponer entidades de persistencia

### Síntomas

- Los controladores devuelven entidades JPA
- Las APIs reciben entidades JPA
- Difícil cambiar la estructura de la BD
- Posible exposición de datos sensibles

### Error

```java
// ❌ MAL - Exponer entidades JPA
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
    
    @GetMapping("/products")
    public List<ProductEntity> getAllProducts() { // ❌ Expone lista de entidades
        return productRepository.findAll();
    }
}
```

### Solución

```java
// ✅ BIEN - Usar DTOs
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
    
    @GetMapping("/products")
    public List<ProductDto> getAllProducts() { // ✅ Expone lista de DTOs
        List<Product> products = productRepository.findAll();
        return products.stream()
            .map(productMapper::toDto)
            .toList();
    }
}
```

---

## 5. No usar Value Objects

### Síntomas

- Validaciones duplicadas en múltiples lugares
- Código propenso a errores (null, valores inválidos)
- Falta de semántica en el código
- Difícil de entender qué representa un valor

### Error

```java
// ❌ MAL - No usar Value Objects
public class Order {
    private String orderId; // ❌ String para ID
    private BigDecimal total; // ❌ BigDecimal para dinero
    private String currency; // ❌ String para moneda
    private String email; // ❌ String para email
}

public void createOrder(String orderId, BigDecimal total, String currency, String email) {
    // ❌ Validación duplicada en cada lugar
    if (orderId == null || orderId.isBlank()) {
        throw new InvalidOrderIdException();
    }
    if (total == null || total.compareTo(BigDecimal.ZERO) < 0) {
        throw new InvalidAmountException();
    }
    if (currency == null || !isValidCurrency(currency)) {
        throw new InvalidCurrencyException();
    }
    if (email == null || !isValidEmail(email)) {
        throw new InvalidEmailException();
    }
    // ...
}
```

### Solución

```java
// ✅ BIEN - Usar Value Objects
public final class OrderId {
    private final String value;
    
    private OrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderIdException();
        }
        this.value = value;
    }
    
    public static OrderId of(String value) {
        return new OrderId(value);
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
}

public final class Email {
    private final String value;
    
    private Email(String value) {
        if (value == null || !isValidEmail(value)) {
            throw new InvalidEmailException();
        }
        this.value = value;
    }
    
    public static Email of(String value) {
        return new Email(value);
    }
}

public class Order {
    private OrderId orderId; // ✅ Value Object
    private Money total; // ✅ Value Object
    private Email customerEmail; // ✅ Value Object
}

public void createOrder(OrderId orderId, Money total, Email email) {
    // ✅ Los Value Objects ya validan
    // Código más limpio y seguro
}
```

---

## 6. El dominio no se guarda correctamente

### Síntomas

- Los cambios en el aggregate no persisten
- Los eventos de dominio no se publican
- Tests pasan pero en producción no funciona
- Estado desincronizado

### Error

```java
// ❌ MAL - No guardar el aggregate
public class OrderService {
    public void updateOrderStatus(OrderId id, UpdateOrderStatusCommand cmd) {
        Order order = repository.findById(id);
        order.confirm(); // ❌ Modifica el aggregate
        // ❌ Falta guardar
    }
    
    public void addItemToOrder(OrderId id, OrderItem item) {
        Order order = repository.findById(id);
        order.addItem(item);
        // ❌ Falta guardar
        // ❌ Falta publicar eventos
    }
}
```

### Solución

```java
// ✅ BIEN - Guardar y publicar eventos
public class OrderService {
    private final OrderRepository repository;
    private final OrderEventPublisher eventPublisher;
    
    public void updateOrderStatus(OrderId id, UpdateOrderStatusCommand cmd) {
        Order order = repository.findById(id);
        order.confirm();
        repository.save(order); // ✅ Guardar
        eventPublisher.publish(order.pullDomainEvents()); // ✅ Publicar eventos
    }
    
    public void addItemToOrder(OrderId id, OrderItem item) {
        Order order = repository.findById(id);
        order.addItem(item);
        repository.save(order); // ✅ Guardar
        eventPublisher.publish(order.pullDomainEvents()); // ✅ Publicar eventos
    }
}
```

---

## 7. CQRS: El read model no se actualiza

### Síntomas

- Se crea un producto pero no aparece en las consultas
- Los datos están desactualizados
- Inconsistencia entre write y read model

### Error

```java
// ❌ MAL - No actualizar read model
public class CreateProductHandler implements CommandHandler<CreateProductCommand, Void> {
    private final ProductWriteRepository writeRepository;
    
    public Void handle(CreateProductCommand cmd) {
        Product product = new Product(cmd.getName(), cmd.getPrice());
        writeRepository.save(product);
        // ❌ No actualiza el read model
        return null;
    }
}

// Las consultas no muestran el producto
public class GetAllProductsHandler implements QueryHandler<GetAllProductsQuery, List<ProductView>> {
    private final ProductViewRepository viewRepository;
    
    public List<ProductView> handle(GetAllProductsQuery query) {
        return viewRepository.findAll(); // ❌ Vacío porque no se actualizó
    }
}
```

### Solución

```java
// ✅ BIEN - Actualizar read model después de command
public class CreateProductHandler implements CommandHandler<CreateProductCommand, Void> {
    private final ProductWriteRepository writeRepository;
    private final ProductViewRepository viewRepository;
    
    public Void handle(CreateProductCommand cmd) {
        Product product = new Product(cmd.getName(), cmd.getPrice());
        writeRepository.save(product);
        
        // ✅ Proyectar al read model
        ProductView view = new ProductView(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getStock(),
            formatPrice(product.getPrice()), // ✅ Pre-calculado
            product.getStock() > 0 // ✅ Pre-calculado
        );
        viewRepository.save(view);
        
        return null;
    }
}
```

---

## 8. El dominio depende de Spring

### Síntomas

- No se pueden probar los aggregates sin Spring
- El dominio tiene anotaciones de Spring
- Difícil de probar en aislamiento
- Acoplamiento a framework

### Error

```java
// ❌ MAL - Dominio con Spring
@Component
public class Order {
    @Autowired
    private OrderRepository repository; // ❌ Dependencia de Spring
    
    @Autowired
    private OrderEventPublisher publisher; // ❌ Dependencia de Spring
    
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        repository.save(this); // ❌ El dominio guarda directamente
        publisher.publish(new OrderConfirmedEvent(this));
    }
}
```

### Solución

```java
// ✅ BIEN - Dominio puro
public class Order {
    // ✅ Sin dependencias de Spring
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        this.status = OrderStatus.CONFIRMED;
        this.addDomainEvent(new OrderConfirmedEvent(this.id));
    }
}

// El servicio orquesta
@Service
public class OrderService {
    private final OrderRepository repository;
    private final OrderEventPublisher publisher;
    
    public void confirmOrder(OrderId id) {
        Order order = repository.findById(id);
        order.confirm(); // ✅ Lógica en el dominio
        repository.save(order); // ✅ Servicio guarda
        publisher.publish(order.pullDomainEvents()); // ✅ Servicio publica
    }
}
```

---

## 9. DDD: Bounded contexts mezclados

### Síntomas

- El código de `account` usa directamente clases de `customer`
- No hay límites claros entre contextos
- Acoplamiento entre contextos
- Difícil de evolucionar independientemente

### Error

```java
// ❌ MAL - Mezclar bounded contexts
public class Account {
    private Customer customer; // ❌ Account depende de Customer context
}

// O peor
public class Account {
    private CustomerEntity customerEntity; // ❌ Dependencia de infraestructura de otro contexto
}

// El servicio usa directamente entidades de otro contexto
@Service
public class AccountService {
    public void createAccountForCustomer(CustomerEntity customer) { // ❌
        Account account = new Account();
        account.setCustomer(customer);
        accountRepository.save(account);
    }
}
```

### Solución

```java
// ✅ BIEN - Contextos separados
public class Account {
    private AccountId id;
    private CustomerId ownerId; // ✅ Solo ID, no el objeto completo
}

// Si se necesita el customer, se busca por ID
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    
    public AccountDto getAccountWithCustomer(AccountId id) {
        Account account = accountRepository.findById(id);
        Customer customer = customerRepository.findById(account.getOwnerId()); // ✅ Búsqueda explícita
        return toDto(account, customer);
    }
}

// O usa un servicio de integración
@Service
public class CustomerIntegrationService {
    private final CustomerClient customerClient;
    
    public CustomerInfo getCustomerInfo(CustomerId id) {
        // ✅ Llama al bounded context de customer
        return customerClient.getCustomer(id);
    }
}
```

---

## 10. No validar en el constructor

### Síntomas

- Objetos creados en estado inválido
- Validaciones dispersas
- Difícil garantizar invariantes
- Errores en tiempo de ejecución

### Error

```java
// ❌ MAL - No validar en constructor
public class Order {
    private OrderId id;
    private List<OrderItem> items;
    
    public Order(OrderId id, List<OrderItem> items) {
        this.id = id;
        this.items = items; // ❌ No valida
    }
    
    public void validate() { // ❌ Validación separada
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException();
        }
    }
}

// Alguien puede crear:
Order order = new Order(id, emptyList); // ❌ Estado inválido
order.validate(); // ❌ Hay que recordar llamarlo
```

### Solución

```java
// ✅ BIEN - Validar en constructor
public class Order {
    private OrderId id;
    private List<OrderItem> items;
    
    public Order(OrderId id, List<OrderItem> items) {
        this.id = Objects.requireNonNull(id, "OrderId es requerido");
        
        if (items == null || items.isEmpty()) { // ✅ Validación en constructor
            throw new InvalidOrderException("El pedido debe tener items");
        }
        
        this.items = List.copyOf(items); // ✅ Inmutable
    }
    
    // ✅ Método de fábrica estático
    public static Order create(CustomerId customerId, List<OrderItem> items) {
        OrderId orderId = OrderId.generate();
        return new Order(orderId, items);
    }
}

// No se puede crear en estado inválido
Order order = new Order(id, emptyList); // ✅ Lanza excepción inmediatamente
```

---

## Resumen

| Error | Síntoma principal | Solución |
|-------|-------------------|----------|
| Lógica en controlador | Controlador con validaciones | Delegar a casos de uso |
| No proteger invariantes | Estado inválido posible | Validar en aggregates |
| Mezclar lectura/escritura | Métodos que leen y escriben | Separar en CQRS |
| Exponer entidades JPA | API devuelve entidades | Usar DTOs |
| No usar Value Objects | Validación duplicada | Crear Value Objects |
| No guardar aggregate | Cambios no persisten | Guardar y publicar eventos |
| Read model no actualizado | Datos desactualizados | Proyectar después de command |
| Dominio con Spring | Difícil probar sin Spring | Dominio puro |
| Contextos mezclados | Acoplamiento entre contextos | Usar IDs y servicios de integración |
| No validar en constructor | Objetos inválidos | Validar en constructor |

---

## Referencias

- **GLOSARIO.md** - Definiciones de conceptos
- **docs/ANTI_PATRONES.md** - Anti-patrones con ejemplos
- **docs/TROUBLESHOOTING.md** - Guía de resolución de problemas
