# Errores frecuentes al implementar arquitecturas modernas y cómo solucionarlos.

## Índice de contenidos

- [Poner lógica de negocio en el controlador](#poner-lógica-de-negocio-en-el-controlador)
- [El dominio no se guarda correctamente](#el-dominio-no-se-guarda-correctamente)
- [CQRS: El read model no se actualiza](#cqrs-el-read-model-no-se-actualiza)
- [El dominio depende de Spring](#el-dominio-depende-de-spring)
- [No validar en el constructor](#no-validar-en-el-constructor)
- [Errores de mapeo (MapStruct)](#errores-de-mapeo-mapstruct)
- [Errores de transacción](#errores-de-transacción)
- [Errores de testing](#errores-de-testing)
- [Problemas de configuración y arranque](#problemas-de-configuración-y-arranque)
- [Diagnóstico rápido general](#diagnóstico-rápido-general)
- [Resumen](#resumen)

> **Nota**: Para los antipatrones de diseño (Anemic Domain Model, Leaking Abstraction, Primitive Obsession, etc.) consulta el fichero `ANTI_PATRONES.md` de esta misma carpeta.

## Poner lógica de negocio en el controlador

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

## El dominio no se guarda correctamente

### Síntomas

- Los cambios en la entidad de dominio no persisten
- Tests pasan pero en producción no funciona
- Estado desincronizado

### Error

```java
// ❌ MAL - No guardar la entidad de dominio
public class OrderService {
    public void confirm(OrderId id) {
        Order order = repository.findById(id);
        order.confirm(); // ❌ Cambia el estado pero no lo persiste
        // ❌ Falta repository.save(order)
    }
}
```

### Solución

```java
// ✅ BIEN - Siempre guardar después de modificar
public class OrderService {
    private final OrderRepository repository;
    
    public void confirm(OrderId id) {
        Order order = repository.findById(id);
        order.confirm();
        repository.save(order); // ✅ Guardar el resultado
    }
}
```

> **Nota**: En módulos que usan domain events (como `ddd` o `hybrid`), además del save hay que publicar los eventos acumulados. Ver el módulo `hybrid` para un ejemplo completo.


---

## CQRS: El read model no se actualiza

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

## El dominio depende de Spring

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
    
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        this.status = OrderStatus.CONFIRMED;
        repository.save(this); // ❌ El dominio guarda directamente
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

// El servicio de aplicación orquesta
@Service
public class OrderService {
    private final OrderRepository repository;
    
    public void confirmOrder(OrderId id) {
        Order order = repository.findById(id);
        order.confirm(); // ✅ Lógica en el dominio
        repository.save(order); // ✅ Servicio guarda
    }
}
```

---

## No validar en el constructor

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

## Errores de mapeo (MapStruct)

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

## Errores de transacción

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
    public void createOrder(String customerId, List<OrderItem> items) {
        Order order = Order.create(OrderId.generate(), customerId, items);
        repository.save(order);
        inventoryService.reserveItems(order.items());
    }
}
```

### Solución

```java
// ✅ BIEN - Con transacción
@Service
public class OrderService {
    @Transactional
    public void createOrder(String customerId, List<OrderItem> items) {
        Order order = Order.create(OrderId.generate(), customerId, items);
        repository.save(order);
        inventoryService.reserveItems(order.items());
    }
}

// ✅ BIEN - Rollback explícito si hace falta
@Service
public class OrderService {
    @Transactional(rollbackFor = {InsufficientStockException.class, InvalidOrderException.class})
    public void createOrder(String customerId, List<OrderItem> items) {
        Order order = Order.create(OrderId.generate(), customerId, items);
        repository.save(order);
        inventoryService.reserveItems(order.items());
    }
}
```

### Qué revisar primero

- Que el método sea `public`
- Que la anotación `@Transactional` esté en el caso de uso o servicio orquestador
- Que no estés llamando a un método transaccional desde otro del mismo bean
- Que las excepciones realmente disparen rollback

---

## Errores de testing

### Síntomas

- Tests que fallan solo en la suite completa
- Tests lentos o frágiles
- Dependencia del orden de ejecución
- Diferencias entre tests unitarios e integración

### Error

```java
// ❌ MAL - Test con estado compartido y dependiendo de Spring
@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    private String customerId = "cust-1";
    private List<OrderItem> items = List.of(
        new OrderItem("prod-1", "Laptop", Money.euros(1200.00), 1)
    );

    @Test
    void shouldCreateOrder() {
        orderService.createOrder(customerId, items); // ❌ Tests con BD real, lento y frágil
    }
}
```

### Solución

```java
// ✅ BIEN - Test unitario del dominio, sin Spring ni BD
class OrderTest {

    @Test
    void shouldCreateOrderWithCorrectStatus() {
        var items = List.of(new OrderItem("P1", "Laptop", Money.euros(1200.00), 1));

        var order = Order.create(OrderId.generate(), "CUST-1", items);

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.totalAmount().amount().doubleValue()).isEqualTo(1200.00);
    }

    @Test
    void shouldConfirmOrder() {
        var order = crearPedido();

        order.confirm();

        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
```

### Qué revisar primero

- Que cada test construya su propio estado
- Que no dependas de la base de datos real en unit tests
- Que los mocks estén inicializados correctamente
- Que los tests de integración tengan limpieza automática con `@Transactional` o `@BeforeEach`

---

## Problemas de configuración y arranque

### Puerto ya en uso

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

### Error de compilación por versión de Java

**Síntoma**: Mismatch entre la versión configurada y la instalada.

**Solución**:

```bash
java -version
```

### Maven no encuentra dependencias

**Síntoma**: Fallos al ejecutar `mvn install` o `mvn test`.

**Solución**:

```bash
mvn clean install -U
```

### Consola H2 no accesible

**Síntoma**: No se puede abrir `h2-console`.

**Solución**:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## Diagnóstico rápido general

### Activar logs útiles

```properties
logging.level.com.tutorial.hexagonal.domain=DEBUG
logging.level.com.tutorial.cqrs.product=DEBUG
logging.level.com.tutorial.ddd.account.domain=DEBUG
logging.level.com.tutorial.hybrid.subscription.domain=DEBUG
```

### Verificar domain events (módulo ddd / hybrid)

Los módulos `ddd` y `hybrid` usan domain events. Para verificarlos en tests:

```java
// Ejemplo del módulo ddd
@Test
void shouldGenerateEventWhenAccountOpened() {
    var account = Account.open(AccountId.generate(), "CUST-1", "Alice",
            new Money(BigDecimal.valueOf(500), "EUR"));

    var events = account.pullDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.getFirst()).isInstanceOf(AccountEvent.AccountOpened.class);
}
```

> **Nota**: El módulo `hexagonal` no usa domain events. Ver `hybrid` para un ejemplo de combinación completa.

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
| No guardar la entidad | Cambios no persisten | Siempre guardar tras modificar |
| Read model no actualizado | Datos desactualizados | Proyectar después de command |
| Dominio con Spring | Difícil probar sin Spring | Dominio puro, sin dependencias de framework |
| No validar en constructor | Objetos en estado inválido | Validar en constructor o factory method |
| MapStruct mal configurado | Campos nulos o compilación rota | Configurar `@Mapper` y el procesador de anotaciones |
| Transacciones mal definidas | Datos inconsistentes | Usar `@Transactional` en el caso de uso |
| Tests frágiles | Fallos intermitentes | Tests unitarios de dominio puro, sin Spring |
| Configuración incorrecta | La app no arranca | Revisar puerto, Java, Maven y H2 |

> Para antipatrones de diseño (Anemic Model, Leaking Abstraction, Primitive Obsession, etc.) consulta el fichero `ANTI_PATRONES.md` de esta misma carpeta.




