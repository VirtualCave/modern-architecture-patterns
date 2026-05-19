# Errores frecuentes al implementar arquitecturas modernas y cómo solucionarlos.

## Indice de contenidos

- [1. Poner lógica de negocio en el controlador](#1-poner-logica-de-negocio-en-el-controlador)
- [2. No proteger invariantes en el aggregate](#2-no-proteger-invariantes-en-el-aggregate)
- [3. Mezclar concerns de lectura y escritura](#3-mezclar-concerns-de-lectura-y-escritura)
- [4. Exponer entidades de persistencia](#4-exponer-entidades-de-persistencia)
- [5. No usar Value Objects](#5-no-usar-value-objects)
- [6. El dominio no se guarda correctamente](#6-el-dominio-no-se-guarda-correctamente)
- [7. CQRS: El read model no se actualiza](#7-cqrs-el-read-model-no-se-actualiza)
- [8. El dominio depende de Spring](#8-el-dominio-depende-de-spring)
- [9. DDD: Bounded contexts mezclados](#9-ddd-bounded-contexts-mezclados)
- [10. No validar en el constructor](#10-no-validar-en-el-constructor)
- [11. Errores de mapeo (MapStruct)](#11-errores-de-mapeo-mapstruct)
- [12. Errores de transacción](#12-errores-de-transaccion)
- [13. Errores de testing](#13-errores-de-testing)
- [14. Problemas de configuración y arranque](#14-problemas-de-configuracion-y-arranque)
- [15. Diagnóstico rápido general](#15-diagnostico-rapido-general)
- [Resumen](#resumen)
- [Referencias](#referencias)

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
    private final OrderRepository orderRepository;

    @PostMapping("/orders")
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        Order order = new Order(request.getCustomerId(), request.getItems());
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

    private OrderItem toOrderItem(CreateOrderItemRequest item) {
        return new OrderItem(item.getProductId(), item.getQuantity(), item.getPrice());
    }
}

// La lógica está en el dominio
public class Order {
    private CustomerId customerId;
    private List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;

    public Order(CustomerId customerId, List<OrderItem> items) {
        this.customerId = customerId;
        this.items = items;
    }

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
public class AccountApplication {
    public void misuse(Account account) {
        account.withdraw(new Money(BigDecimal.valueOf(1000), Currency.getInstance("EUR"))); // Aunque solo tenga 100
        account.setBalance(new Money(BigDecimal.valueOf(-100), Currency.getInstance("EUR"))); // Saldo negativo
    }
}
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
        return toDto(product); // ❌ La misma lógica de lectura se reutiliza aquí
    }
    
    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id);
        // ❌ Usa la misma lógica que createProduct
        return toDto(product);
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setFormattedPrice(formatPrice(product.getPrice())); // ❌ Lógica de lectura
        dto.setAvailable(product.getStock() > 0); // ❌ Lógica de lectura
        return dto;
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
        order.changeStatus(cmd.getNewStatus()); // ❌ Usa el comando pero no persiste el cambio
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
        order.changeStatus(cmd.getNewStatus()); // ✅ El comando alimenta la acción del dominio
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
    private OrderStatus status;

    @Autowired
    private OrderRepository repository; // ❌ Dependencia de Spring
    
    @Autowired
    private OrderEventPublisher publisher; // ❌ Dependencia de Spring
    
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        this.status = OrderStatus.CONFIRMED;
        repository.save(this); // ❌ El dominio guarda directamente
        publisher.publish(new OrderConfirmedEvent(this)); // ❌ El dominio publica directamente
    }
}
```

### Solución

```java
// ✅ BIEN - Dominio puro
public class Order {
    private OrderStatus status;

    // ✅ Sin dependencias de Spring
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        this.status = OrderStatus.CONFIRMED;
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
        publisher.publish(new OrderConfirmedEvent(order.getId())); // ✅ Servicio publica
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

    public void setCustomer(Customer customer) { // ❌ Setter público rompe el límite
        this.customer = customer;
    }
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
    private CustomerId customerId;
    private List<OrderItem> items;
    
    public Order(OrderId id, CustomerId customerId, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = items; // ❌ No valida
    }
    
    public void validate() { // ❌ Validación separada
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException();
        }
    }
}

// Alguien puede crear:
public class OrderCreationDemo {
    public void misuse(OrderId id, CustomerId customerId, List<OrderItem> emptyList) {
        Order order = new Order(id, customerId, emptyList); // ❌ Estado inválido
        order.validate(); // ❌ Hay que recordar llamarlo
    }
}
```

### Solución

```java
// ✅ BIEN - Validar en constructor
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderItem> items;
    
    private Order(OrderId id, CustomerId customerId, List<OrderItem> items) {
        this.id = Objects.requireNonNull(id, "OrderId es requerido");
        this.customerId = Objects.requireNonNull(customerId, "CustomerId es requerido");
        
        if (items == null || items.isEmpty()) { // ✅ Validación en constructor
            throw new InvalidOrderException("El pedido debe tener items");
        }
        
        this.items = List.copyOf(items); // ✅ Inmutable
    }
    
    // ✅ Método de fábrica estático
    public static Order create(CustomerId customerId, List<OrderItem> items) {
        OrderId orderId = OrderId.generate();
        return new Order(orderId, customerId, items);
    }
}

// ✅ No se puede crear en estado inválido y se obliga a usar la fábrica
public class OrderCreationDemo {
    public void misuse(CustomerId customerId, List<OrderItem> emptyList) {
        Order order = Order.create(customerId, emptyList); // ✅ Lanza excepción inmediatamente
    }
}
```

---

## 11. Errores de mapeo (MapStruct)

### Síntomas

- Los DTOs salen con campos nulos o incompletos
- La compilación falla al generar el mapper
- El mapeo funciona en unas rutas y en otras no
- Los objetos complejos llegan mal convertidos

### Error

```java
// ❌ MAL - Mapper sin una configuración clara
@Mapper
public interface OrderMapper {
    OrderDto toDto(Order order);
    Order toDomain(OrderDto dto);
}
```

### Solución

```java
// ✅ BIEN - Mapper simple y explícito
@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "total", source = "total.amount")
    @Mapping(target = "currency", source = "total.currency")
    OrderDto toDto(Order order);

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "total", source = "total")
    Order toDomain(OrderDto dto);
}
```

### Qué revisar primero

- Que el mapper tenga `componentModel = "spring"` si se inyecta como bean
- Que los nombres de los campos coincidan cuando no haya `@Mapping`
- Que `target` y `source` estén bien escritos
- Que el procesador de anotaciones de MapStruct esté activo en el `pom.xml`

---

## 12. Errores de transacción

### Síntomas

- Los cambios no se guardan aunque no hay error
- El rollback no se ejecuta como esperabas
- Hay datos inconsistentes tras una excepción
- Una operación compuesta deja medias escrituras

### Error

```java
// ❌ MAL - Sin transacción
@Service
public class OrderService {
    public void createOrder(CreateOrderCommand cmd) {
        CustomerId customerId = cmd.getCustomerId();
        List<OrderItem> items = cmd.getItems();
        Order order = Order.create(customerId, items);
        repository.save(order);
        inventoryService.reserveItems(order.getItems());
    }
}
```

### Solución

```java
// ✅ BIEN - Con transacción
@Service
public class OrderService {
    @Transactional
    public void createOrder(CreateOrderCommand cmd) {
        CustomerId customerId = cmd.getCustomerId();
        List<OrderItem> items = cmd.getItems();
        Order order = Order.create(customerId, items);
        repository.save(order);
        inventoryService.reserveItems(order.getItems());
    }
}

// ✅ BIEN - Rollback explícito si hace falta
@Service
public class OrderService {
    @Transactional(rollbackFor = {InsufficientStockException.class, InvalidOrderException.class})
    public void createOrder(CreateOrderCommand cmd) {
        CustomerId customerId = cmd.getCustomerId();
        List<OrderItem> items = cmd.getItems();
        Order order = Order.create(customerId, items);
        repository.save(order);
        inventoryService.reserveItems(order.getItems());
    }
}
```

### Qué revisar primero

- Que el método sea `public`
- Que la anotación `@Transactional` esté en el caso de uso o servicio orquestador
- Que no estés llamando a un método transaccional desde otro del mismo bean
- Que las excepciones realmente disparen rollback

---

## 13. Errores de testing

### Síntomas

- Tests que fallan solo en la suite completa
- Tests lentos o frágiles
- Dependencia del orden de ejecución
- Diferencias entre tests unitarios e integración

### Error

```java
// ❌ MAL - Test con estado compartido
@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    private CreateOrderCommand cmd = new CreateOrderCommand(
        CustomerId.of("cust-1"),
        List.of(new OrderItem("prod-1", 2, new Money(BigDecimal.valueOf(25), Currency.getInstance("EUR"))))
    );

    @Test
    void shouldCreateOrder() {
        orderService.createOrder(cmd);
    }
}
```

### Solución

```java
// ✅ BIEN - Test unitario aislado
class OrderServiceTest {
    private OrderRepository orderRepository;
    private OrderEventPublisher eventPublisher;
    private OrderService orderService;
    private CreateOrderCommand cmd;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(OrderEventPublisher.class);
        orderService = new OrderService(orderRepository, eventPublisher);
        cmd = new CreateOrderCommand(
            CustomerId.of("cust-1"),
            List.of(new OrderItem("prod-1", 2, new Money(BigDecimal.valueOf(25), Currency.getInstance("EUR"))))
        );
    }

    @Test
    void shouldCreateOrder() {
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(cmd);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any());
        verify(eventPublisher).publish(any());
    }
}
```

### Qué revisar primero

- Que cada test construya su propio estado
- Que no dependas de la base de datos real en unit tests
- Que los mocks estén inicializados correctamente
- Que los tests de integración tengan limpieza automática con `@Transactional` o `@BeforeEach`

---

## 14. Problemas de configuración y arranque

### 14.1 Puerto ya en uso

**Síntoma**: Error al iniciar la aplicación por puerto ocupado.

**Solución**:

```properties
server.port=8085
```

O localiza el proceso y ciérralo:

```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### 14.2 Error de compilación por versión de Java

**Síntoma**: Mismatch entre la versión configurada y la instalada.

**Solución**:

```bash
java -version
```

### 14.3 Maven no encuentra dependencias

**Síntoma**: Fallos al ejecutar `mvn install` o `mvn test`.

**Solución**:

```bash
mvn clean install -U
```

### 14.4 Consola H2 no accesible

**Síntoma**: No se puede abrir `h2-console`.

**Solución**:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 15. Diagnóstico rápido general

### Activar logs útiles

```properties
logging.level.com.example.hexagonal.domain=DEBUG
logging.level.com.example.cqrs.domain=DEBUG
logging.level.com.example.ddd.domain=DEBUG
logging.level.com.example.hybrid.domain=DEBUG
```

### Verificar eventos de dominio

```java
@Test
void shouldPublishEventWhenOrderCreated() {
    Order order = Order.create(customer, items);

    List<DomainEvent> events = order.pullDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(OrderCreatedEvent.class);
}
```

### Probar aggregates sin Spring

```java
@Test
void shouldNotAllowWithdrawalWhenInsufficientFunds() {
    Account account = new Account(new AccountId("1"), new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")));

    assertThatThrownBy(() -> account.withdraw(new Money(BigDecimal.valueOf(150), Currency.getInstance("EUR"))))
        .isInstanceOf(InsufficientFundsException.class);
}
```

### Verificar mappers

```java
@Test
void shouldMapOrderToEntity() {
    CustomerId customerId = CustomerId.of("cust-1");
    OrderItem item = new OrderItem("prod-1", 2, new Money(BigDecimal.valueOf(25), Currency.getInstance("EUR")));
    Order order = Order.create(customerId, List.of(item));
    OrderEntity entity = mapper.toEntity(order);

    assertThat(entity.getId()).isEqualTo(order.getId().getValue());
    assertThat(entity.getStatus()).isEqualTo(order.getStatus());
}
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
| MapStruct mal configurado | Campos nulos o compilación rota | Configurar `@Mapper` y el procesador |
| Transacciones mal definidas | Datos inconsistentes | Usar `@Transactional` |
| Tests frágiles | Fallos intermitentes | Aislar estado y mocks |
| Configuración incorrecta | La app no arranca | Revisar puerto, Java, Maven y H2 |

---

## Referencias

- Definiciones de conceptos
- Anti-patrones con ejemplos
- Guía histórica de troubleshooting, ahora unificada aquí

