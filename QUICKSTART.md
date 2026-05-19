# Quick Start Guide

Guía rápida para empezar a usar el tutorial de arquitecturas modernas.

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


## Ejecutar Todos los Módulos

Cada módulo es un proyecto Maven independiente y se ejecuta en su propio puerto.

### Terminal 1 - Hexagonal (Pedidos)
```bash
cd hexagonal
mvn spring-boot:run
```
Se ejecuta en el puerto **8081**

### Terminal 2 - CQRS (Productos)
```bash
cd cqrs
mvn spring-boot:run
```
Se ejecuta en el puerto **8082**

### Terminal 3 - DDD (Cuentas Bancarias)
```bash
cd ddd
mvn spring-boot:run
```
Se ejecuta en el puerto **8083**

### Terminal 4 - Hybrid (Suscripciones)
```bash
cd hybrid
mvn spring-boot:run
```
Se ejecuta en el puerto **8084**

---

## Probar Cada Módulo

### Hexagonal - Gestión de Pedidos

**Crear un pedido**
```bash
curl -X POST http://localhost:8081/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1",
    "items": [
      {
        "productId": "P1",
        "productName": "Laptop",
        "price": 1200.00,
        "quantity": 2
      }
    ]
  }'
```

**Consultar un pedido**
```bash
curl http://localhost:8081/api/orders/{order-id}
```

**Confirmar un pedido**
```bash
curl -X PATCH http://localhost:8081/api/orders/{order-id}/confirm
```

**Enviar un pedido**
```bash
curl -X PATCH http://localhost:8081/api/orders/{order-id}/ship
```

**Entregar un pedido**
```bash
curl -X PATCH http://localhost:8081/api/orders/{order-id}/deliver
```

**Cancelar un pedido**
```bash
curl -X PATCH http://localhost:8081/api/orders/{order-id}/cancel
```

---

### CQRS - Catálogo de Productos

**Crear un producto (COMMAND)**
```bash
curl -X POST http://localhost:8082/api/commands/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro",
    "description": "Laptop Apple de alta gama",
    "price": 2500.00,
    "stock": 10
  }'
```

**Listar todos los productos (QUERY)**
```bash
curl http://localhost:8082/api/queries/products
```

**Consultar un producto por ID (QUERY)**
```bash
curl http://localhost:8082/api/queries/products/{product-id}
```

**Actualizar el precio de un producto (COMMAND)**
```bash
curl -X PATCH http://localhost:8082/api/commands/products/{product-id}/price \
  -H "Content-Type: application/json" \
  -d '{
    "newPrice": 2300.00
  }'
```

**Actualizar el stock de un producto (COMMAND)**
```bash
curl -X PATCH http://localhost:8082/api/commands/products/{product-id}/stock \
  -H "Content-Type: application/json" \
  -d '{
    "newStock": 15
  }'
```

**Eliminar un producto (COMMAND)**
```bash
curl -X DELETE http://localhost:8082/api/commands/products/{product-id}
```

---

### DDD - Cuentas Bancarias

**Abrir una cuenta**
```bash
curl -X POST http://localhost:8083/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "ownerId": "CUST-1",
    "ownerName": "Alice",
    "initialDeposit": 500.00
  }'
```

**Consultar una cuenta**
```bash
curl http://localhost:8083/api/accounts/{account-id}
```

**Depositar dinero**
```bash
curl -X POST http://localhost:8083/api/accounts/{account-id}/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 200.00
  }'
```

**Retirar dinero**
```bash
curl -X POST http://localhost:8083/api/accounts/{account-id}/withdraw \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00
  }'
```

**Transferir dinero entre cuentas**
```bash
curl -X POST http://localhost:8083/api/accounts/{from-account-id}/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "toAccountId": "{to-account-id}",
    "amount": 50.00
  }'
```

**Consultar todos los clientes**
```bash
curl http://localhost:8083/api/customers
```

---

### Hybrid - Suscripciones (Hexagonal + CQRS + DDD)

**Crear una suscripción (COMMAND)**
```bash
curl -X POST http://localhost:8084/api/commands/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-1",
    "planCode": "PRO",
    "monthlyFee": 29.99
  }'
```

**Activar una suscripción (COMMAND)**
```bash
curl -X PATCH http://localhost:8084/api/commands/subscriptions/{subscription-id}/activate
```

**Cancelar una suscripción (COMMAND)**
```bash
curl -X PATCH http://localhost:8084/api/commands/subscriptions/{subscription-id}/cancel
```

**Consultar una suscripción (QUERY)**
```bash
curl http://localhost:8084/api/queries/subscriptions/{subscription-id}
```

**Listar todas las suscripciones (QUERY)**
```bash
curl http://localhost:8084/api/queries/subscriptions
```

**Listar suscripciones activas (QUERY)**
```bash
curl http://localhost:8084/api/queries/subscriptions?status=ACTIVE
```

---

## Ejecutar Tests

Cada módulo tiene su suite de tests.

```bash
# Tests de Hexagonal
cd hexagonal
mvn test

# Tests de CQRS
cd cqrs
mvn test

# Tests de DDD
cd ddd
mvn test

# Tests de Hybrid
cd hybrid
mvn test
```

---

## Acceder a la Base de Datos H2

Todos los módulos usan H2 en memoria. Puedes acceder a la consola H2 mientras la aplicación está corriendo:

**URL**: `http://localhost:{port}/h2-console`
**JDBC URL**: `jdbc:h2:mem:testdb`
**Username**: `sa`
**Password**: (vacío)

| Módulo | Puerto |
|--------|--------|
| hexagonal | 8081 |
| cqrs | 8082 |
| ddd | 8083 |
| hybrid | 8084 |

---

## Estructura de Cada Módulo

Todos los módulos siguen una estructura similar:

```
módulo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── domain/          # Lógica de negocio pura
│   │   │       ├── application/     # Casos de uso y servicios
│   │   │       ├── infrastructure/  # Adaptadores técnicos
│   │   │       └── shared/          # Componentes compartidos
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/
│               └── domain/          # Tests del dominio
├── pom.xml
└── documentación del módulo
```

---

## Siguientes Pasos

1. **Lee el README principal** para entender las arquitecturas
2. **Revisa la sección de conceptos clave** para entender el vocabulario del proyecto
3. **Explora cada módulo en detalle** dentro de su propia documentación
4. **Prueba los ejercicios prácticos** para afianzar lo aprendido
5. **Revisa los anti-patrones** para saber qué evitar
6. **Consulta la guía de errores comunes** si tienes problemas

---

## Problemas Comunes

### Puerto ya en uso
Si un puerto ya está en uso, puedes cambiarlo en `application.properties`:
```properties
server.port=8085
```

### Error de compilación
Asegúrate de tener Java 26+ instalado:
```bash
java -version
```

### Maven no encuentra dependencias
Limpia y reinstala:
```bash
mvn clean install
```

### Tests fallan
Asegúrate de que no haya otra instancia corriendo en el mismo puerto.

---

## Recursos Adicionales

- Plan completo de mejoras de documentación
- Diccionario de conceptos
- Anti-patrones comunes
- Guía de errores comunes y troubleshooting
- Ejercicios prácticos
- Roadmap de aprendizaje

---

## ¿Necesitas Ayuda?

Si encuentras algún problema:
1. Revisa la guía de errores comunes
2. Consulta el glosario para entender los conceptos
3. Revisa la documentación de cada módulo
4. Abre un issue en el repositorio
