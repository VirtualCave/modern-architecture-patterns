# Glosario de conceptos para arquitecturas modernas

## Índice de contenidos

- [Arquitecturales](#arquitecturales)
- [Técnicos](#tecnicos)
- [Antipatrones](#antipatrones)
- [Comparativas](#comparativas)
- [Patrones Relacionados](#patrones-relacionados)
- [Acrónimos](#acronyms)
- [Referencias Cruzadas](#referencias-cruzadas)

## Arquitecturales

### Aggregate Root

**Definición**: Entidad que actúa como punto de entrada a un aggregate y garantiza la consistencia de sus invariantes.

**Ejemplo**: `Account` en el módulo `ddd` es el aggregate root del contexto bancario. Protege invariantes como no poder retirar más saldo del disponible o no transferir a la misma cuenta.

**Por qué importa**: Es el único objeto del aggregate que puede ser cargado/salvado desde el repositorio. Todas las operaciones externas deben pasar por él.

**Anti-patrón**: Exponer entidades internas del aggregate directamente o permitir modificar su estado sin pasar por el aggregate root.

**Ver también**: Entity, Value Object, Invariant, Aggregate

---

### Aggregate

**Definición**: Cluster de objetos de dominio que se tratan como una unidad para garantizar la consistencia de datos.

**Ejemplo**: El aggregate `Account` en el módulo `ddd` contiene `Account` (root) y usa `Money` (value object) para todo lo monetario.

**Por qué importa**: Los cambios dentro de un aggregate deben ser atómicos. No puedes modificar parte de un aggregate sin garantizar su consistencia completa.

**Regla de oro**: Un aggregate debe ser lo suficientemente pequeño como para mantener su consistencia, pero lo suficientemente grande como para proteger sus invariantes.

**Ver también**: Aggregate Root, Invariant, Consistency Boundary

---

### Bounded Context

**Definición**: Límite conceptual donde un término tiene un significado específico y consistente.

**Ejemplo**: "Cuenta" significa algo distinto en el contexto `account` (bancario) vs `customer` (cliente).

**Por qué importa**: Evita ambigüedades y permite modelar dominios complejos. Un mismo concepto puede tener diferentes reglas según el contexto.

**En este proyecto**: El módulo DDD tiene dos bounded contexts: `account` y `customer`.

**Ver también**: Ubiquitous Language, Context Mapping, DDD

---

### Port (Hexagonal)

**Definición**: Interfaz que define una necesidad del dominio sin especificar cómo se implementa.

**Tipos**:
- **Input Port**: Casos de uso que el dominio ofrece (ej: `CreateOrderUseCase`)
- **Output Port**: Servicios que el dominio necesita (ej: `OrderRepository`)

**Por qué importa**: Permite que el dominio defina qué necesita sin depender de cómo se implementa. Esto hace el dominio testeable y la infraestructura intercambiable.

**Ejemplo**: `OrderRepository` es un port que define `save()`, `findById()`, pero no sabe si se usará JPA, MongoDB o archivos.

**Ver también**: Adapter, Hexagonal Architecture, Dependency Inversion

---

### Adapter (Hexagonal)

**Definición**: Implementación concreta de un port que conecta el dominio con el mundo exterior.

**Tipos**:
- **Input Adapter**: Implementa un input port (ej: `OrderController` implementa `CreateOrderUseCase`)
- **Output Adapter**: Implementa un output port (ej: `OrderJpaAdapter` implementa `OrderRepository`)

**Por qué importa**: Permite cambiar la tecnología sin afectar el dominio. Puedes cambiar de JPA a MongoDB solo cambiando el adapter.

**Ejemplos**: 
- `OrderJpaAdapter` implementa persistencia con JPA (módulo `hexagonal`)
- `OrderController` implementa acceso HTTP REST (módulo `hexagonal`)
- `ProductWriteRepositoryAdapter` implementa persistencia de escritura (módulo `cqrs`)

**Ver también**: Port, Hexagonal Architecture

---

### Command (CQRS)

**Definición**: Objeto que expresa una intención de cambio de estado.

**Características**:
- No devuelve datos de lectura
- Solo confirma éxito/fallo
- Representa una acción, no una consulta

**Ejemplo**: `CreateProductCommand` contiene datos para crear un producto.

**Por qué importa**: Separa claramente las intenciones de cambio de las consultas, permitiendo optimizar cada lado independientemente.

**Anti-patrón**: Usar commands para consultas disfrazadas (ej: un command que devuelve datos).

**Ver también**: Query, CommandHandler, CQRS, Write Model

---

### Query (CQRS)

**Definición**: Objeto que expresa una solicitud de datos sin intención de modificar estado.

**Características**:
- Solo lectura
- Optimizado para el caso de uso específico
- No debe modificar estado

**Ejemplo**: `GetProductByIdQuery` solicita un producto por ID.

**Por qué importa**: Permite optimizar las consultas sin preocuparse por la consistencia de escritura. El read model puede ser denormalizado y orientado a consultas específicas.

**Ver también**: Command, QueryHandler, CQRS, Read Model

---

### CommandBus / QueryBus

**Definición**: Mecanismo que desacopla quien envía un command/query de quien lo procesa.

**Por qué importa**: Permite cambiar handlers sin afectar a los controladores. Facilita añadir logging, validación, o middleware sin modificar el código de negocio.

**Implementación en este proyecto**: `SimpleCommandBus` y `SimpleQueryBus` son implementaciones simples que mapean commands/queries a sus handlers.

**Ejemplo de flujo**:
```
Controller → CommandBus → CommandHandler → Write Model
Controller → QueryBus → QueryHandler → Read Model
```

**Ver también**: Command, Query, Handler, Mediator Pattern

---

### Value Object

**Definición**: Objeto identificado por sus valores, no por identidad. Inmutable por diseño.

**Características**:
- No tiene identidad propia (no ID)
- Dos value objects son iguales si tienen los mismos valores
- Inmutables (no cambian después de crearlos)

**Ejemplos**: `Money`, `OrderId`, `Email`, `Currency`.

**Cuándo usar**: Cuando el atributo no tiene vida propia y solo importa su valor. Cuando quieres encapsular validación y comportamiento relacionado con ese valor.

**Anti-patrón**: Dar ID a un value object o hacerlo mutable.

**Ejemplo**:
```java
// ✅ BIEN - Value Object
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

// ❌ MAL - No es Value Object (tiene ID)
public class Money {
    private Long id; // ❌ No debe tener ID
    private BigDecimal amount;
}
```

**Ver también**: Entity, Aggregate Root, Immutability

---

### Domain Event

**Definición**: Hecho que ocurrió en el dominio y que otros componentes pueden necesitar conocer.

**Características**:
- Inmutables
- Representan algo que ya ocurrió
- Tienen timestamp
- Se publican después de que la operación se completa con éxito

**Ejemplos**: 
- `OrderCreatedEvent` - Se creó un pedido
- `MoneyDepositedEvent` - Se depositó dinero en una cuenta
- `SubscriptionActivatedEvent` - Se activó una suscripción

**Por qué usar**: Desacopla reacciones posteriores (enviar email, actualizar estadísticas, sincronizar con otros sistemas). Permite extensibilidad sin modificar el código existente.

**Flujo típico**:
```
Operación de dominio → Genera eventos → Se publican → Handlers reaccionan
```

**Ver también**: Event Publisher, Event Handler, Event Sourcing

---

### Domain Service

**Definición**: Servicio que contiene lógica de dominio que no pertenece a un solo aggregate.

**Cuándo usar**:
- Cuando la operación involucra múltiples aggregates
- Cuando la lógica no es responsabilidad natural de ningún aggregate
- Cuando necesitas coordinar operaciones entre aggregates

**Ejemplo**: `TransferDomainService` coordina transferencia entre cuentas (involucra dos aggregates: cuenta origen y cuenta destino).

**Anti-patrón**: Poner lógica de aplicación (coordenar repositorios, transacciones) en domain services. Eso pertenece a Application Services.

**Diferencia con Application Service**:
- **Domain Service**: Lógica de negocio pura
- **Application Service**: Orquestación, coordinación, infraestructura

**Ver también**: Application Service, Aggregate Root, Domain Logic

---

### Invariant

**Definición**: Regla de negocio que siempre debe cumplirse para que el estado sea válido.

**Ejemplos**:
- Una cuenta no puede tener saldo negativo
- Un pedido cancelado no puede entregarse
- Un pedido no puede tener más de 10 items

**Dónde viven**: En el Aggregate Root. El aggregate es responsable de proteger sus invariantes.

**Por qué importa**: Garantiza que el dominio nunca esté en un estado inválido. Si se rompe un invariante, el sistema está en un estado inconsistente.

**Ejemplo**:
```java
public class Account {
    public void withdraw(Money amount) {
        if (this.balance.lessThan(amount)) {
            throw new InsufficientFundsException(); // Protege invariante
        }
        this.balance = this.balance.subtract(amount);
    }
}
```

**Ver también**: Aggregate Root, Domain Logic, Business Rule

---

### Ubiquitous Language

**Definición**: Lenguaje compartido entre equipo técnico y negocio para describir el dominio.

**Por qué importa**: Elimina barreras de comunicación y malentendidos. El código usa el mismo lenguaje que el negocio.

**Ejemplo**: Usar "cuenta", "depósito", "transferencia" en código y conversaciones, en lugar de "AccountEntity", "insert", "update".

**En este proyecto**:
- `Account` (no `AccountEntity`)
- `deposit()` (no `addMoney()`)
- `transfer()` (no `moveFundsBetweenAccounts()`)

**Ver también**: Bounded Context, DDD, Domain Modeling

---

### Entity

**Definición**: Objeto con identidad propia que se distingue de otros objetos por esa identidad, no por sus atributos.

**Características**:
- Tiene identidad (ID)
- Puede cambiar sus atributos sin cambiar su identidad
- Tiene ciclo de vida

**Ejemplo**: `Order`, `Account`, `Customer`, `Product`.

**Diferencia con Value Object**:
- **Entity**: Identidad importa, puede cambiar
- **Value Object**: Valor importa, inmutable

**Ver también**: Value Object, Aggregate Root, Identity

---

<a id="tecnicos"></a>
## Técnicos

### Application Service

**Definición**: Servicio que orquesta casos de uso sin contener lógica de dominio.

**Responsabilidades**:
- Cargar aggregates desde repositorios
- Invocar lógica de dominio (métodos del aggregate)
- Guardar resultados en repositorios
- Publicar eventos de dominio
- Coordinar transacciones

**Anti-patrón**: Poner reglas de negocio en application services. La lógica de negocio debe estar en el dominio.

**Ejemplo**:
```java
// Módulo hexagonal: carga → lógica de dominio → guarda
@Service
public class CreateOrderService implements CreateOrderUseCase {
    public Order createOrder(String customerId, List<OrderItem> items) {
        var orderId = OrderId.generate();
        var order = Order.create(orderId, customerId, items); // Lógica en dominio
        return orderRepository.save(order);
    }
}

// Módulo ddd: carga → lógica de dominio → guarda → publica eventos
@Service
public class AccountApplicationService {
    public Account deposit(DepositMoneyInput input) {
        var account = repository.findById(AccountId.of(input.accountId())).orElseThrow();
        account.deposit(new Money(input.amount(), "EUR")); // Lógica en dominio
        return repository.save(account);
    }
}
```

**Ver también**: Domain Service, Use Case, Transaction Management

---

### Repository (Domain)

**Definición**: Interfaz del dominio que define cómo se persiste/recupera un aggregate.

**Características**:
- Vive en el dominio
- No depende de tecnología concreta
- Define operaciones desde la perspectiva del dominio

**Ejemplo**: `OrderRepository` en `domain/port/output` define `save()`, `findById()`, `delete()`.

**Por qué importa**: Permite que el dominio defina qué necesita sin saber cómo se implementa. Facilita cambiar de tecnología.

**Ver también**: Repository Adapter, Port, Persistence

---

### Repository Adapter

**Definición**: Implementación concreta de un repository que usa una tecnología específica.

**Ejemplo**: `OrderJpaAdapter` implementa `OrderRepository` usando Spring Data JPA.

**Responsabilidades**:
- Convertir aggregates a entidades de persistencia
- Usar la tecnología concreta (JPA, MongoDB, etc.)
- Manejar detalles técnicos (transacciones, conexiones)

**Ver también**: Repository, Port, Adapter, Mapper

---

### Mapper (MapStruct)

**Definición**: Componente que convierte entre diferentes modelos (DTOs, entidades, dominio).

**Por qué MapStruct**:
- Tipado y seguro en compile-time
- Sin reflection (más rápido)
- Explícito y mantenible
- Genera código automático

**Ejemplo**: `OrderPersistenceMapper` convierte `Order` ↔ `OrderEntity`.

**En este proyecto**: Se usa para mapear entre:
- Dominio ↔ Entidad JPA (persistencia)
- Dominio ↔ DTO (REST)
- Write Model ↔ Read Model (CQRS)

**Ver también**: DTO, Entity, Domain Model, MapStruct

---

### Write Model (CQRS)

**Definición**: Modelo optimizado para escritura, con todas las reglas de negocio.

**Características**:
- Rico en comportamiento
- Protege invariantes
- Es la fuente de verdad del estado

**Ejemplo**: `Product` con métodos para cambiar precio, stock, etc.

**Por qué importa**: Es donde viven las reglas de negocio. Las operaciones de escritura siempre pasan por aquí para garantizar consistencia.

**Ver también**: Read Model, Command, CQRS

---

### Read Model (CQRS)

**Definición**: Modelo optimizado para lectura, sin lógica de negocio compleja.

**Características**:
- Puede ser denormalizado
- Orientado a consultas específicas
- No protege invariantes (solo lectura)

**Ejemplo**: `ProductView` con campos pre-calculados para listados.

**Por qué importa**: Permite optimizar consultas sin ensuciar el write model. Puedes tener múltiples read models para diferentes casos de uso.

**Ver también**: Write Model, Query, CQRS, Projection

---

### Projection

**Definición**: Vista de datos derivada del write model, optimizada para consultas.

**En CQRS**: El read model es una proyección del write model.

**Ejemplo**: `SubscriptionView` en el módulo hybrid es una proyección de `Subscription` con campos calculados.

**Tipos**:
- **Proyección en tiempo real**: Se actualiza cuando cambia el write model
- **Proyección eventual**: Se actualiza de forma asíncrona

**Ver también**: Read Model, CQRS, Event Sourcing

---

### DTO (Data Transfer Object)

**Definición**: Objeto simple usado para transferir datos entre capas o sistemas.

**Características**:
- Sin lógica de negocio
- Solo getters/setters (o records en Java)
- Usado para APIs, integraciones

**Ejemplo**: `CreateOrderRequest`, `OrderResponse`.

**Por qué usar**: Separar el modelo de dominio del modelo de API. Permite cambiar la API sin afectar el dominio.

**Ver también**: Domain Model, API, Mapper

---

### Use Case (Input Port)

**Definición**: Interfaz que define una operación que el dominio ofrece al mundo exterior.

**Características**:
- Vive en el dominio (`domain/port/input`)
- Define el contrato sin implementación
- Representa una intención clara del exterior hacia el sistema

**Ejemplo**: `CreateOrderUseCase`, `GetOrderUseCase`, `UpdateOrderStatusUseCase` en el módulo `hexagonal`.

**Por qué importa**: Hace explícito qué puede hacer el sistema. El controlador solo conoce el use case, no la implementación concreta.

**Ver también**: Application Service, Port, Input Adapter

---

### Input (DDD Application Layer)

**Definición**: DTO simple que transporta datos desde la capa de infraestructura hacia el servicio de aplicación en el módulo DDD.

**Características**:
- Sin lógica de negocio
- Sin dependencia de frameworks
- Solo transporta datos

**Diferencia con Command (CQRS)**: Un `Command` en CQRS viaja a través de un `CommandBus` hacia un `CommandHandler` específico. Un `Input` en DDD es simplemente un parámetro tipado que recibe el `ApplicationService` directamente, sin bus.

**Ejemplo en este proyecto**: `OpenAccountInput`, `DepositMoneyInput`, `TransferMoneyInput` en el módulo `ddd`.

**Ver también**: Command (CQRS), Application Service, DTO

---

## Antipatrones

> Para ejemplos detallados con código, comparativas y cómo evitar cada antipatrón, consulta el fichero `ANTI_PATRONES.md` de esta misma carpeta.

| Antipatrón | Ámbito | Problema |
|------------|--------|----------|
| Anemic Domain Model | Transversal / DDD | Lógica dispersa en servicios; el dominio es solo un contenedor de datos |
| Leaking Abstraction | Transversal / Hexagonal | El dominio depende de tecnología (JPA, Spring) |
| Command como Query | CQRS | Un command devuelve datos de lectura |
| God Service | Transversal | Un servicio hace demasiado, violando SRP |
| Primitive Obsession | Transversal / DDD | Uso de `String`/`int` donde debería haber Value Objects |
| Database-Driven Design | Transversal | Se diseña la BD antes que el dominio |
| No proteger invariantes | DDD | El aggregate permite estados inválidos |
| Exponer entidades JPA | Transversal / Hexagonal | La API expone directamente entidades de persistencia |
| Mezclar bounded contexts | DDD | Un contexto depende de objetos de otro contexto |
| No usar Value Objects | DDD | Conceptos de dominio representados con primitivos |


## Comparativas

### Hexagonal vs Layered Architecture

**Layered Architecture**:
- Capas rígidas (presentation → business → data)
- Dependencias siempre hacia abajo
- La capa de data puede "filtrarse" hacia arriba
- Difícil cambiar la infraestructura

**Hexagonal Architecture**:
- Dominio en el centro
- Dependencias hacia adentro (dominio no depende de nada)
- Adaptadores intercambiables
- Infraestructura es un detalle

**Cuándo usar Hexagonal**: Cuando la infraestructura puede cambiar o el dominio es complejo.

**Cuándo usar Layered**: Para proyectos simples donde la infraestructura no cambiará.

---

### CQRS vs Classic CRUD

**Classic CRUD**:
- Mismo modelo para lectura y escritura
- Operaciones simples (create, read, update, delete)
- Fácil de implementar
- Puede no escalar bien con muchas lecturas

**CQRS**:
- Modelos separados para lectura y escritura
- Operaciones especializadas (commands, queries)
- Más complejidad
- Mejor escalabilidad y rendimiento

**Cuándo usar CQRS**: Cuando lectura y escritura tienen necesidades distintas (muchas más lecturas, consultas complejas, etc.).

**Cuándo usar CRUD**: Para sistemas simples donde lectura y escritura son similares.

---

### DDD vs Data-Driven

**Data-Driven**:
- Centrado en datos y tablas
- El modelo refleja la estructura de datos
- Lógica dispersa en servicios
- Difícil de entender para el negocio

**DDD**:
- Centrado en lenguaje y reglas del negocio
- El modelo refleja el dominio
- Lógica concentrada en el dominio
- Fácil comunicación negocio-tecnología

**Cuándo usar DDD**: Cuando el dominio es complejo, el lenguaje importa, o hay reglas de negocio importantes.

**Cuándo usar Data-Driven**: Para sistemas simples donde el dominio no es complejo.

---

## Patrones Relacionados

### Factory

**Definición**: Objeto encargado de crear otros objetos.

**En este proyecto**: Los aggregates tienen métodos estáticos de fábrica como `Order.create()`, `Account.open()`.

**Por qué usar**: Encapsula la lógica de creación y garantiza que los objetos se crean en un estado válido.

---

### Builder

**Definición**: Patrón para construir objetos complejos paso a paso.

**En este proyecto**: Se usa para crear commands y queries complejos.

**Por qué usar**: Hace el código más legible cuando hay muchos parámetros opcionales.

---

### Strategy

**Definición**: Patrón que permite cambiar el comportamiento de un objeto en tiempo de ejecución.

**En CQRS**: Los handlers son estrategias que procesan diferentes commands/queries.

**Por qué usar**: Facilita añadir nuevos comportamientos sin modificar el código existente.

---

### Observer

**Definición**: Patrón donde un objeto notifica a otros sobre cambios.

**En Domain Events**: Los event publishers notifican a los event handlers cuando ocurre un evento de dominio.

**Por qué usar**: Desacopla la generación de eventos de su procesamiento.

---

## Acrónimos

- **CQRS**: Command Query Responsibility Segregation
- **DDD**: Domain-Driven Design
- **SOLID**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **DTO**: Data Transfer Object
- **JPA**: Java Persistence API
- **CRUD**: Create, Read, Update, Delete
- **API**: Application Programming Interface
- **SRP**: Single Responsibility Principle
- **DRY**: Don't Repeat Yourself

---

## Referencias Cruzadas

**Para entender arquitecturas**:
- Hexagonal → Port, Adapter, Use Case, Application Service
- CQRS → Command, Query, Write Model, Read Model, CommandBus, QueryBus
- DDD → Bounded Context, Ubiquitous Language, Domain Event, Domain Service, Invariant

**Para evitar errores** → ver `ERRORES_COMUNES.md` en esta misma carpeta

**Para evitar antipatrones** → ver `ANTI_PATRONES.md` en esta misma carpeta

**Para implementar**:
- Application Service → Domain Service, Repository, Use Case
- Mapper → DTO, Entity, Domain Model
- Input (DDD) vs Command (CQRS) → Application Service

