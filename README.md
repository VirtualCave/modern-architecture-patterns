# Tutorial de Arquitecturas Modernas: Hexagonal, CQRS, DDD y Hybrid

Este repositorio contiene **cuatro proyectos Maven independientes** con ejemplos prácticos de arquitecturas modernas usando **Java 26** y **Spring Boot 4**.

La idea no es solo mostrar “cómo se organiza el código”, sino **por qué** cada arquitectura resuelve un problema distinto:

- **Hexagonal**: aislar el dominio de los detalles técnicos.
- **CQRS**: separar claramente escritura y lectura.
- **DDD**: modelar el negocio con lenguaje y reglas del dominio.

## Estructura del repo

| Módulo | Arquitectura | Ejemplo del dominio | Puerto |
|---|---|---:|---:|
| `hexagonal/` | Ports & Adapters | Pedidos | `8081` |
| `cqrs/` | Command Query Responsibility Segregation | Productos | `8082` |
| `ddd/` | Domain-Driven Design | Cuentas y clientes | `8083` |
| `hybrid/` | Hexagonal + CQRS + DDD | Suscripciones | `8084` |

Cada carpeta se compila y ejecuta de forma independiente.

## Ejecutar cada módulo

```bash
cd hexagonal
mvn spring-boot:run

cd cqrs
mvn spring-boot:run

cd ddd
mvn spring-boot:run

cd hybrid
mvn spring-boot:run
```

## Tecnologías usadas

- **Java 26**
  - `record` para DTOs y value objects sencillos.
  - Pattern matching y sealed types donde encaja.
  - Virtual Threads en Spring Boot 4.
- **Spring Boot 4**
  - Web MVC para la capa HTTP.
  - Spring Data JPA para persistencia.
  - Validation + `ProblemDetail` para errores HTTP coherentes.
- **H2** en memoria
  - Pensado para tutorial y pruebas rápidas.
  - Sin dependencia de infraestructura externa.
- **MapStruct + Lombok**
  - MapStruct para mappings explícitos y tipados.
  - Lombok para reducir boilerplate donde sí aporta: controladores, servicios, adapters, entidades JPA.

## Leyenda de colores para diagramas

- `entry`: entrada HTTP/controladores
- `bus`: buses de CQRS
- `app`: servicios de aplicación y handlers
- `domain`: aggregates, reglas y modelos de dominio
- `port`: puertos/interfaces del dominio
- `read`: read model de CQRS
- `infra`: adaptadores e infraestructura técnica

## 1) Hexagonal Architecture

### Idea principal

La arquitectura hexagonal busca que el **núcleo del sistema** sea el dominio y que todo lo demás sea intercambiable.

El dominio no debería saber si la app usa REST, colas, JPA, MongoDB o Kafka. Para eso existen los **puertos** y **adaptadores**:

- **Puertos de entrada**: lo que el sistema ofrece al exterior.
- **Puertos de salida**: lo que el dominio necesita del mundo externo.
- **Adaptadores**: implementaciones concretas de esos puertos.

### Lo que suele costar entender

1. **El controlador no contiene negocio**
   - Solo traduce HTTP ↔ dominio.
   - No decide reglas de negocio.

2. **Los puertos viven en el dominio**
   - No son “interfaces decorativas”.
   - Representan necesidades reales del negocio.

3. **La infraestructura no manda**
   - JPA, REST, colas, etc. son detalles intercambiables.

4. **El agregado encapsula reglas**
   - `Order` protege estados válidos.
   - Evita que la aplicación “haga trampas” con el estado.

### Flujo visual

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
    A[HTTP request] --> B[OrderController]
    B --> C[Input Port<br/>CreateOrderUseCase / GetOrderUseCase / UpdateOrderStatusUseCase]
    C --> D[Application Service]
    D --> E[Order<br/>Aggregate Root]
    E --> F[Output Port<br/>OrderRepository / OrderEventPublisher]
    F --> G[Adapter de infraestructura<br/>JPA / Log]

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class A,B entry;
    class C,F port;
    class D app;
    class E domain;
    class G infra;
```

### Pros

- Muy buen aislamiento del dominio.
- Fácil de probar sin Spring ni BD.
- Cambiar tecnología tiene menos impacto.
- Claridad de responsabilidades.

### Contras

- Más clases e interfaces.
- Puede parecer “verbosa” en proyectos pequeños.
- Si se lleva al extremo, añade demasiada ceremonia.

### Cuándo interesa usarla

- Sistemas con lógica de negocio real.
- Proyectos que cambiarán de infraestructura con el tiempo.
- Equipos que quieren separar bien negocio y técnica.
- Aplicaciones donde el dominio debe ser testeable de forma aislada.

### Cuándo no conviene

- CRUDs simples sin reglas relevantes.
- Prototipos desechables.
- Sistemas muy pequeños donde el coste de abstracción supera el beneficio.

## 2) CQRS

### Idea principal

CQRS separa **quién escribe** de **quién consulta**.

La idea no es “poner dos carpetas diferentes”, sino aceptar que leer y escribir suelen tener necesidades muy distintas:

- Al escribir, importan reglas, validación e invariantes.
- Al leer, importan rapidez, filtros y formatos de consulta.

### Lo que suele costar entender

1. **Command no devuelve datos de lectura**
   - Un command expresa intención de cambio.
   - No debería usarse como query disfrazada.

2. **Read model no tiene por qué ser igual al write model**
   - `Product` protege la escritura.
   - `ProductView` optimiza la consulta.

3. **La separación no es solo conceptual**
   - Aquí hay APIs distintas:
     - `/api/commands/products`
     - `/api/queries/products`

4. **Los buses desacoplan el transporte**
   - `CommandBus` y `QueryBus` evitan que el controlador conozca handlers concretos.

### Flujo visual

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart LR
    subgraph COMMAND[COMMAND - Escritura]
        C1[HTTP POST/PATCH/DELETE] --> C2[ProductCommandController]
        C2 --> C3[CommandBus]
        C3 --> C4[CommandHandler específico]
        C4 --> C5[Write model: Product]
        C5 --> C6[ProductWriteRepositoryAdapter]
        C6 --> C7[JPA / BD]
    end

    subgraph QUERY[QUERY - Lectura]
        Q1[HTTP GET] --> Q2[ProductQueryController]
        Q2 --> Q3[QueryBus]
        Q3 --> Q4[QueryHandler específico]
        Q4 --> Q5[Read model: ProductView]
        Q5 --> Q6[ProductReadRepositoryAdapter]
        Q6 --> Q7[JPA / BD]
    end

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef bus fill:#E0E7FF,stroke:#3730A3,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef read fill:#CCFBF1,stroke:#0F766E,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class C1,C2,Q1,Q2 entry;
    class C3,Q3 bus;
    class C4,Q4 app;
    class C5 domain;
    class Q5 read;
    class C6,C7,Q6,Q7 infra;
```

### Pros

- Muy claro para sistemas con muchas más lecturas que escrituras.
- Permite optimizar consultas de forma independiente.
- Hace más fácil evolucionar el read model.
- Puede mejorar escalabilidad y rendimiento.

### Contras

- Más complejidad que un CRUD clásico.
- Puede duplicar lógica o estructuras.
- Si el dominio es pequeño, puede sentirse excesivo.
- La consistencia entre escritura y lectura requiere disciplina.

### Cuándo interesa usarlo

- Productos con muchas consultas.
- Paneles, catálogos y dashboards.
- Sistemas donde el modelo de lectura necesita ser distinto del de escritura.
- Dominios donde la carga de lectura y escritura es muy desigual.

### Cuándo no conviene

- CRUDs sencillos.
- Sistemas con reglas de negocio mínimas.
- Equipos que aún no dominan bien la arquitectura base.
- Proyectos donde separar lectura/escritura no aporta un beneficio real.

## 3) DDD

### Idea principal

DDD no es una arquitectura de capas “bonita”; es una forma de modelar el software alrededor del **lenguaje del negocio**.

En lugar de pensar primero en tablas o endpoints, se piensa en:

- qué quiere hacer el negocio,
- cuáles son sus reglas,
- qué conceptos son importantes,
- y cómo se agrupan.

### Lo que suele costar entender

1. **Bounded Context**
   - Un mismo término puede significar cosas distintas según el contexto.
   - Por eso existen `account` y `customer` como contextos separados.

2. **Aggregate Root**
   - Es el guardián de las invariantes.
   - En este repo, `Account` decide si se puede depositar, retirar o transferir.

3. **Value Objects**
   - No tienen identidad propia, solo valor.
   - `Money`, `AccountId`, `CustomerId` son ejemplos perfectos.

4. **Domain Service**
   - Se usa cuando la lógica no pertenece claramente a un solo aggregate.
   - `TransferDomainService` coordina una transferencia entre cuentas.

5. **Domain Events**
   - Son hechos que ocurrieron en el dominio.
   - Permiten desacoplar reacciones posteriores.

### Flujo visual

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
    A[HTTP request] --> B[Controller]
    B --> C[Application Service]
    C --> D[Aggregate Root / Domain Service]
    D --> E[Repository de dominio]
    E --> F[Persistencia JPA]

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class A,B entry;
    class C app;
    class D domain;
    class E port;
    class F infra;
```

### Pros

- Excelente para dominios con reglas complejas.
- Favorece lenguaje común negocio-tecnología.
- Hace visibles los límites del sistema.
- Los invariantes viven donde deben vivir: en el dominio.

### Contras

- Requiere entender bien el negocio.
- Puede parecer sobreingeniería si el dominio es simple.
- No te da automáticamente una solución técnica; exige criterio.
- Si se aplica mal, se convierte en puro “patrón decorativo”.

### Cuándo interesa usarlo

- Dominios complejos o cambiantes.
- Sistemas con reglas de negocio importantes.
- Productos donde la comprensión del negocio es clave.
- Equipos que quieren una base robusta y evolutiva.

### Cuándo no conviene

- CRUDs triviales.
- Aplicaciones de poca vida útil.
- Cuando no existe complejidad de negocio real.
- Cuando el equipo aún no puede sostener el coste conceptual.

## Cuándo mezclar arquitecturas

Estas arquitecturas no compiten entre sí; muchas veces se complementan muy bien.

### Hexagonal + DDD

**Cuándo:** casi siempre que el dominio tenga peso real.

**Qué aporta la mezcla:**

- DDD te dice **cómo modelar el negocio**.
- Hexagonal te dice **cómo proteger ese modelo del mundo exterior**.

**Beneficio real en un proyecto:**

- el aggregate no depende de Spring,
- los casos de uso se vuelven explícitos,
- y los adaptadores técnicos quedan fuera del núcleo.

**Resultado típico:** dominio fuerte, infraestructura intercambiable.

### Hexagonal + CQRS

**Cuándo:** cuando escribir y leer empiezan a tener formas muy distintas.

**Qué aporta la mezcla:**

- Hexagonal separa el sistema de los detalles técnicos.
- CQRS separa el comportamiento de escritura del de lectura.

**Beneficio real en un proyecto:**

- puedes tener un write model rico y un read model simple,
- evitar que el controlador conozca la persistencia,
- y optimizar queries sin ensuciar el dominio.

**Resultado típico:** APIs más claras y mejor escalado de lectura.

### DDD + CQRS

**Cuándo:** en dominios complejos donde además hay muchas consultas.

**Qué aporta la mezcla:**

- DDD modela bien la escritura.
- CQRS permite un read model adaptado a cada caso de uso.

**Beneficio real en un proyecto:**

- aggregates pequeños y claros,
- consultas rápidas y específicas,
- posibilidad de evolucionar lectura/escritura por separado.

**Resultado típico:** dominio rico con lecturas muy eficientes.

### Las tres juntas: Hexagonal + DDD + CQRS

**Cuándo:** sistemas serios, con evolución prevista y suficiente complejidad.

**Qué aporta la combinación:**

- **DDD** define el lenguaje y las reglas.
- **Hexagonal** protege el dominio de la tecnología.
- **CQRS** optimiza la experiencia de lectura/escritura.

**Beneficio real en un proyecto:**

- código más mantenible a largo plazo,
- negocio mejor modelado,
- adaptadores intercambiables,
- consultas eficientes,
- y un núcleo de dominio muy testable.

**Pero ojo:** no hay que usar las tres por moda. La combinación solo tiene sentido cuando el problema lo justifica.

## Regla práctica para elegir

- **Si el sistema es pequeño y simple** → probablemente un CRUD bien hecho sea suficiente.
- **Si el dominio importa y la infraestructura puede cambiar** → Hexagonal.
- **Si la escritura y la lectura son claramente distintas** → CQRS.
- **Si hay reglas de negocio complejas y vocabulario del negocio importante** → DDD.
- **Si el sistema es complejo y además necesita lectura optimizada** → combinar Hexagonal + DDD + CQRS.

## APIs de ejemplo

### Hexagonal - Pedidos

```bash
# Crear pedido
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1","items":[{"productId":"P1","productName":"Laptop","price":1200.00,"quantity":2}]}'

# Consultar pedido
curl http://localhost:8081/api/orders/{id}

# Confirmar pedido
curl -X PATCH http://localhost:8081/api/orders/{id}/confirm
```

### CQRS - Productos

```bash
# Crear producto (COMMAND)
curl -X POST http://localhost:8082/api/commands/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro","description":"Laptop Apple","price":2500.00,"stock":10}'

# Listar productos (QUERY)
curl http://localhost:8082/api/queries/products

# Actualizar precio (COMMAND)
curl -X PATCH http://localhost:8082/api/commands/products/{id}/price \
  -H "Content-Type: application/json" \
  -d '{"newPrice":2300.00}'
```

### DDD - Cuentas bancarias

```bash
# Abrir cuenta
curl -X POST http://localhost:8083/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"ownerId":"CUST-1","ownerName":"Alice","initialDeposit":500.00}'

# Depositar dinero
curl -X POST http://localhost:8083/api/accounts/{id}/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":200.00}'

# Transferir dinero
curl -X POST http://localhost:8083/api/accounts/{fromId}/transfer \
  -H "Content-Type: application/json" \
  -d '{"toAccountId":"{toId}","amount":100.00}'
```

### Hybrid - Suscripciones combinando Hexagonal + CQRS + DDD

```bash
# Crear suscripción (command side)
curl -X POST http://localhost:8084/api/commands/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"customerId":"CUST-1","planCode":"PRO","monthlyFee":29.99}'

# Activar suscripción (command side)
curl -X PATCH http://localhost:8084/api/commands/subscriptions/{subscriptionId}/activate

# Consultar una suscripción (query side)
curl http://localhost:8084/api/queries/subscriptions/{subscriptionId}

# Ver suscripciones activas (query side)
curl http://localhost:8084/api/queries/subscriptions
```

### ¿Qué aporta esta mezcla en un proyecto real?

- El **modelo de negocio** queda limpio y protegido.
- Las **consultas** pueden optimizarse sin tocar la escritura.
- La **infraestructura** puede cambiar con poco impacto en el núcleo.
- Cada parte del sistema tiene una razón clara de existir.

## Diagramas de Arquitectura

### Arquitectura Hexagonal - Componentes

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
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

    linkStyle default stroke:#94A3B8,stroke-width:2px;
    
    classDef entry fill:#DBEAFE,stroke:#1D4ED8,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;
    
    class REST,CLI,MQ entry
    class InputPorts,AppServices app
    class Aggregates,VOs,OutputPorts domain
    class JPA,EventPublisher,Log infra
```

### Arquitectura CQRS - Componentes

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
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

    linkStyle default stroke:#94A3B8,stroke-width:2px;
    
    classDef command fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef query fill:#CCFBF1,stroke:#0F766E,stroke-width:1px,color:#111827;
    classDef db fill:#E5E7EB,stroke:#4B5563,stroke-width:1px,color:#111827;
    
    class CController,CBus,CHandlers,WModel,WRepo command
    class QController,QBus,QHandlers,RModel,RRepo query
    class WDB,RDB db
```

### Comparativa de Arquitecturas

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

### Diagrama de Decisión

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
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

    linkStyle default stroke:#94A3B8,stroke-width:2px;
    
    classDef simple fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef medium fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef complex fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;
    classDef hybrid fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
    
    class B,I simple
    class D,F,H medium
    class K,N,Q complex
    class T,U hybrid
```

### Diagrama visual de la mezcla

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'transparent','primaryTextColor':'#111827','lineColor':'#94A3B8'}}}%%
flowchart TD
    A[DDD<br/>negocio + reglas + bounded contexts] -->|modela el dominio| B[Hexagonal<br/>puertos + adaptadores + aislamiento técnico]
    B -->|expone casos de uso| C[CQRS - Commands<br/>escribir / cambiar]
    B -->|expone casos de uso| D[CQRS - Queries<br/>leer / consultar]
    C --> E[Write Model<br/>reglas de negocio]
    D --> F[Read Model<br/>vistas optimizadas]
    E --> G[Infraestructura<br/>REST / JPA / H2]
    F --> G

    linkStyle default stroke:#94A3B8,stroke-width:2px;

    classDef domain fill:#FEF3C7,stroke:#D97706,stroke-width:1px,color:#111827;
    classDef port fill:#F3E8FF,stroke:#7E22CE,stroke-width:1px,color:#111827;
    classDef app fill:#DCFCE7,stroke:#15803D,stroke-width:1px,color:#111827;
    classDef read fill:#CCFBF1,stroke:#0F766E,stroke-width:1px,color:#111827;
    classDef infra fill:#FFEDD5,stroke:#C2410C,stroke-width:1px,color:#111827;

    class A,E domain;
    class B port;
    class C app;
    class D,F read;
    class G infra;
```


