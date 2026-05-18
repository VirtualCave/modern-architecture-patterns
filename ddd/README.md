# DDD Tutorial

Ejemplo práctico de **Domain-Driven Design (DDD)** con dominio bancario.

## Idea principal (en sencillo)

DDD organiza el código para que el centro sea el **modelo de dominio** y su lenguaje de negocio.

- El dominio expresa reglas y comportamientos reales (`Account`, `Customer`, `Money`).
- La aplicación coordina casos de uso.
- La infraestructura conecta con HTTP y base de datos.

## Bounded Contexts de este módulo

- **`account`**: apertura de cuenta, depósitos, retiros, transferencias.
- **`customer`**: gestión de clientes.

Cada contexto sigue la misma estructura: `domain -> application -> infrastructure`.

## Conceptos clave en este módulo

- **Aggregate Root**: `account/domain/Account`
  - Protege invariantes (no sobregiro, cuenta activa, no transferir a la misma cuenta).
- **Value Object**: `shared/domain/Money`
  - Encapsula operaciones monetarias seguras.
- **Domain Service**: `TransferDomainService`
  - Modela lógica entre aggregates.
- **Domain Events**: `account/domain/event/AccountEvent`
  - Representan hechos del negocio (cuenta abierta, dinero depositado, etc.).
- **Application Services**:
  - `AccountApplicationService`
  - `CustomerApplicationService`
- **Repositories**:
  - Interfaces en `domain/repository`
  - Implementaciones en `infrastructure/persistence`
- **MapStruct (recomendado en DDD)**
  - Contexto `account`: `account/infrastructure/persistence/mapper/AccountPersistenceMapper`
  - Contexto `customer`: `customer/infrastructure/persistence/mapper/CustomerPersistenceMapper`
  - Se usa en infraestructura para mapear dominio <-> entidad sin romper el modelo de dominio.

## Flujo visual (Mermaid)

### 1) Abrir cuenta

```mermaid
flowchart TD
    A[HTTP POST /api/accounts] --> B[AccountController]
    B --> C[AccountApplicationService.openAccount]
    C --> D[Account.open aggregate root]
    C --> E[AccountRepository.save]
    E --> F[AccountRepositoryAdapter]
    F --> G[SpringDataAccountRepository]

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
    classDef port fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1px,color:#4A148C;
    classDef infra fill:#FBE9E7,stroke:#D84315,stroke-width:1px,color:#BF360C;

    class A,B entry;
    class C app;
    class D domain;
    class E port;
    class F,G infra;
```

### 2) Transferir dinero

```mermaid
flowchart TD
    A[HTTP POST /api/accounts/:id/transfer] --> B[AccountController]
    B --> C[AccountApplicationService.transfer]
    C --> D[AccountRepository.findById from/to]
    C --> E[TransferDomainService.transfer]
    E --> F[Account.transferTo]
    F --> G[withdraw + deposit]
    C --> H[AccountRepository.save from]
    C --> I[AccountRepository.save to]

    classDef entry fill:#E3F2FD,stroke:#1565C0,stroke-width:1px,color:#0D47A1;
    classDef app fill:#E8F5E9,stroke:#2E7D32,stroke-width:1px,color:#1B5E20;
    classDef domain fill:#FFF8E1,stroke:#EF6C00,stroke-width:1px,color:#E65100;
    classDef port fill:#F3E5F5,stroke:#6A1B9A,stroke-width:1px,color:#4A148C;

    class A,B entry;
    class C app;
    class E,F,G domain;
    class D,H,I port;
```

## Ejecutar

```bash
mvn spring-boot:run
```

## Tests

```bash
mvn test
```


