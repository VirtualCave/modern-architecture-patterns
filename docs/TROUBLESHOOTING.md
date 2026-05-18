# Guía de Troubleshooting

Guía de resolución de problemas comunes al trabajar con las arquitecturas modernas de este tutorial.

---

## Problemas Comunes

### 1. El dominio no se guarda correctamente

#### Síntomas
- Los cambios en el aggregate no persisten
- Los eventos de dominio no se publican
- Los tests pasan pero en producción no funciona
- Estado desincronizado

#### Causas probables
- No llamar a `repository.save()` después de modificar el aggregate
- No llamar a `pullDomainEvents()` antes de guardar
- El mapper no convierte correctamente entre dominio y entidad
- Falta transacción en el servicio

#### Solución

```java
// ❌ MAL - No guardar el aggregate
public class OrderService {
    public void updateOrderStatus(OrderId id, UpdateOrderStatusCommand cmd) {
        Order order = repository.findById(id);
        order.confirm(); // ❌ Modifica el aggregate
        // ❌ Falta guardar
    }
}

// ✅ BIEN - Guardar y publicar eventos
public class OrderService {
    private final OrderRepository repository;
    private final OrderEventPublisher eventPublisher;
    
    @Transactional
    public void updateOrderStatus(OrderId id, UpdateOrderStatusCommand cmd) {
        Order order = repository.findById(id);
        order.confirm();
        repository.save(order); // ✅ Guardar
        eventPublisher.publish(order.pullDomainEvents()); // ✅ Publicar eventos
    }
}
```

#### Debugging Tips
- Añadir logs antes y después de `repository.save()`
- Verificar que el mapper convierte correctamente
- Comprobar que la transacción está activa
- Revisar si hay excepciones silenciosas

---

### 2. Los invariantes no se protegen

#### Síntomas
- Se crean objetos con estado inválido
- Se modifican objetos rompiendo reglas de negocio
- Validaciones dispersas en diferentes lugares
- Estado inconsistente en la base de datos

#### Causas probables
- La validación está en el controlador o servicio, no en el aggregate
- Se exponen setters públicos que rompen invariantes
- No se valida en el constructor
- Se usan setters genéricos en lugar de métodos con intención

#### Solución

```java
// ❌ MAL - Validación fuera del dominio
public class Account {
    public void withdraw(Money amount) {
        this.balance = this.balance.subtract(amount); // ❌ No valida saldo
    }
}

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

// ✅ BIEN - Validación en el dominio
public class Account {
    public void withdraw(Money amount) {
        if (this.balance.lessThan(amount)) { // ✅ Protege invariante
            throw new InsufficientFundsException();
        }
        this.balance = this.balance.subtract(amount);
    }
}

@Service
public class AccountService {
    public void withdraw(AccountId id, Money amount) {
        Account account = repository.findById(id);
        account.withdraw(amount); // ✅ El dominio protege sus invariantes
        repository.save(account);
    }
}
```

#### Debugging Tips
- Revisar todos los setters públicos en el aggregate
- Verificar que las validaciones están en el constructor
- Escribir tests que intenten romper invariantes
- Usar logs para rastrear dónde se rompe el invariante

---

### 3. CQRS: El read model no se actualiza

#### Síntomas
- Se crea un producto pero no aparece en las consultas
- Los datos están desactualizados
- Inconsistencia entre write y read model
- Las consultas devuelven datos vacíos

#### Causas probables
- No hay proyección del write model al read model
- La proyección no se ejecuta después de cada command
- El read model usa una tabla diferente a la proyección
- Error en el mapper entre write y read model

#### Solución

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

// ✅ BIEN - Actualizar read model después de command
public class CreateProductHandler implements CommandHandler<CreateProductCommand, Void> {
    private final ProductWriteRepository writeRepository;
    private final ProductViewRepository viewRepository;
    
    @Transactional
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

#### Debugging Tips
- Verificar que el command handler actualiza el read model
- Comprobar que el mapper entre write y read model funciona
- Revisar si hay errores en la proyección
- Usar logs para rastrear la actualización del read model

---

### 4. Hexagonal: El dominio depende de Spring

#### Síntomas
- No se pueden probar los aggregates sin Spring
- El dominio tiene anotaciones de Spring
- Difícil de probar en aislamiento
- Tests lentos por necesidad de contexto Spring

#### Causas probables
- El aggregate usa `@Component` o `@Service`
- El dominio inyecta dependencias de Spring
- El dominio usa anotaciones JPA
- El dominio depende de beans de Spring

#### Solución

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
    
    @Transactional
    public void confirmOrder(OrderId id) {
        Order order = repository.findById(id);
        order.confirm(); // ✅ Lógica en el dominio
        repository.save(order); // ✅ Servicio guarda
        publisher.publish(order.pullDomainEvents()); // ✅ Servicio publica
    }
}
```

#### Debugging Tips
- Buscar anotaciones de Spring en el dominio
- Verificar que los aggregates no tienen dependencias externas
- Escribir tests del dominio sin Spring
- Revisar si hay inyección de dependencias en el dominio

---

### 5. DDD: Bounded contexts mezclados

#### Síntomas
- El código de `account` usa directamente clases de `customer`
- No hay límites claros entre contextos
- Acoplamiento entre contextos
- Difícil de evolucionar independientemente

#### Causas probables
- No se definieron bounded contexts
- Se comparten entidades entre contextos
- Se usan directamente objetos de otros contextos
- Falta de separación clara

#### Solución

```java
// ❌ MAL - Mezclar bounded contexts
public class Account {
    private Customer customer; // ❌ Account depende de Customer context
}

// O peor
public class Account {
    private CustomerEntity customerEntity; // ❌ Dependencia de infraestructura de otro contexto
}

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

#### Debugging Tips
- Revisar las importaciones entre bounded contexts
- Verificar que solo se usan IDs para referencias cruzadas
- Buscar dependencias directas entre contextos
- Definir claramente los límites de cada contexto

---

### 6. Errores de mapeo (MapStruct)

#### Síntomas
- Los datos no se convierten correctamente
- Campos nulos después del mapeo
- Excepciones al convertir objetos
- Datos incorrectos en la base de datos

#### Causas probables
- El mapper no está configurado correctamente
- Los nombres de campos no coinciden
- Falta mapeo para campos complejos
- El mapper no se genera correctamente

#### Solución

```java
// ❌ MAL - Mapper sin configuración
@Mapper
public interface OrderMapper {
    OrderDto toDto(Order order);
    // ❌ No especifica cómo mapear campos complejos
}

// ✅ BIEN - Mapper con configuración
@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "total", source = "total.amount")
    @Mapping(target = "currency", source = "total.currency")
    OrderDto toDto(Order order);
    
    @Mapping(target = "customerId", expression = "java(new CustomerId(dto.getCustomerId()))")
    @Mapping(target = "total", expression = "java(new Money(dto.getTotal(), Currency.getInstance(dto.getCurrency())))")
    Order toDomain(OrderDto dto);
    
    @Mapping(target = "id", source = "id.value")
    OrderEntity toEntity(Order order);
    
    @Mapping(target = "id", expression = "java(new OrderId(entity.getId()))")
    Order toEntity(OrderEntity entity);
}
```

#### Debugging Tips
- Verificar que el mapper se genera correctamente
- Revisar los logs de compilación de MapStruct
- Escribir tests para el mapper
- Comprobar que los nombres de campos coinciden

---

### 7. Errores de transacción

#### Síntomas
- Los cambios no se guardan aunque no hay error
- Datos inconsistentes después de errores
- Rollback no funciona correctamente
- Transacciones anidadas no se comportan como esperado

#### Causas probables
- Falta anotación `@Transactional`
- La transacción no se propaga correctamente
- Excepciones no marcadas como rollback
- Transacciones en métodos privados

#### Solución

```java
// ❌ MAL - Sin transacción
@Service
public class OrderService {
    public void createOrder(CreateOrderCommand cmd) {
        Order order = Order.create(...);
        repository.save(order);
        // ❌ Si falla aquí, el order ya se guardó
        inventoryService.reserveItems(order.getItems());
    }
}

// ✅ BIEN - Con transacción
@Service
public class OrderService {
    @Transactional
    public void createOrder(CreateOrderCommand cmd) {
        Order order = Order.create(...);
        repository.save(order);
        inventoryService.reserveItems(order.getItems()); // ✅ Si falla, hace rollback
    }
}

// ✅ BIEN - Configurar rollback para excepciones específicas
@Service
public class OrderService {
    @Transactional(rollbackFor = {InsufficientStockException.class, InvalidOrderException.class})
    public void createOrder(CreateOrderCommand cmd) {
        Order order = Order.create(...);
        repository.save(order);
        inventoryService.reserveItems(order.getItems());
    }
}
```

#### Debugging Tips
- Verificar que el método es público (las transacciones no funcionan en métodos privados)
- Comprobar que la excepción causa rollback
- Revisar la configuración de transacciones
- Usar logs para rastrear el inicio y fin de transacciones

---

### 8. Errores de testing

#### Síntomas
- Tests fallan inexplicablemente
- Tests pasan individualmente pero fallan en suite
- Tests lentos
- Tests dependientes del orden de ejecución

#### Causas probables
- Tests dependientes de estado compartido
- No se limpia el estado entre tests
- Tests dependientes de la base de datos real
- Mocks mal configurados

#### Solución

```java
// ❌ MAL - Test dependiente de estado compartido
@SpringBootTest
class OrderServiceTest {
    @Autowired
    private OrderService orderService;
    
    @Test
    void shouldCreateOrder() {
        Order order = orderService.createOrder(cmd);
        // ❌ No limpia el estado, afecta otros tests
    }
}

// ✅ BIEN - Test aislado
class OrderServiceTest {
    private OrderRepository orderRepository;
    private OrderEventPublisher eventPublisher;
    private OrderService orderService;
    
    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        eventPublisher = mock(OrderEventPublisher.class);
        orderService = new OrderService(orderRepository, eventPublisher);
    }
    
    @Test
    void shouldCreateOrder() {
        // Arrange
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Order order = orderService.createOrder(cmd);
        
        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any());
        verify(eventPublisher).publish(any());
    }
}

// ✅ BIEN - Test de integración con limpieza
@SpringBootTest
@Transactional
class OrderIntegrationTest {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    void shouldCreateOrder() {
        // Act
        Order order = orderService.createOrder(cmd);
        
        // Assert
        Order saved = orderRepository.findById(order.getId());
        assertThat(saved).isNotNull();
    }
    
    // ✅ @Transactional hace rollback después de cada test
}
```

#### Debugging Tips
- Usar `@BeforeEach` y `@AfterEach` para limpiar estado
- Evitar dependencias entre tests
- Usar mocks para tests unitarios
- Usar `@Transactional` para tests de integración

---

## Debugging Tips Generales

### Habilitar logs de dominio

```properties
# application.properties
logging.level.com.example.hexagonal.domain=DEBUG
logging.level.com.example.cqrs.domain=DEBUG
logging.level.com.example.ddd.domain=DEBUG
logging.level.com.example.hybrid.domain=DEBUG
```

### Verificar eventos de dominio

```java
// En tests
@Test
void shouldPublishEventWhenOrderCreated() {
    Order order = Order.create(customer, items);
    
    List<DomainEvent> events = order.pullDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(OrderCreatedEvent.class);
}
```

### Probar aggregates sin Spring

```java
@Test
void shouldNotAllowWithdrawalWhenInsufficientFunds() {
    Account account = new Account(new AccountId("1"), new Money(100));
    
    assertThatThrownBy(() -> account.withdraw(new Money(150)))
        .isInstanceOf(InsufficientFundsException.class);
}
```

### Verificar mappers

```java
@Test
void shouldMapOrderToEntity() {
    Order order = new Order(...);
    OrderEntity entity = mapper.toEntity(order);
    
    assertThat(entity.getId()).isEqualTo(order.getId().getValue());
    assertThat(entity.getStatus()).isEqualTo(order.getStatus());
}
```

### Usar breakpoints estratégicos

- En el constructor del aggregate
- En los métodos que modifican estado
- En el repository.save()
- En el eventPublisher.publish()

---

## Problemas de Configuración

### Puerto ya en uso

**Síntoma**: Error al iniciar la aplicación "Port 8081 is already in use"

**Solución**:
```properties
# application.properties
server.port=8085
```

O matar el proceso que usa el puerto:
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8081 | xargs kill -9
```

### Error de compilación

**Síntoma**: Error al compilar "Java version mismatch"

**Solución**:
```bash
# Verificar versión de Java
java -version

# Configurar JAVA_HOME
export JAVA_HOME=/path/to/java26
```

### Maven no encuentra dependencias

**Síntoma**: Error al ejecutar `mvn install`

**Solución**:
```bash
# Limpiar y reinstalar
mvn clean install -U

# Omitir tests si fallan
mvn clean install -DskipTests
```

### H2 console no accesible

**Síntoma**: No se puede acceder a la consola H2

**Solución**:
```properties
# application.properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## Referencias

- **GLOSARIO.md** - Definiciones de conceptos
- **docs/ANTI_PATRONES.md** - Anti-patrones comunes
- **docs/ERRORES_COMUNES.md** - Errores frecuentes
- **QUICKSTART.md** - Guía de inicio rápido
