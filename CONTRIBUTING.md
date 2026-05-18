# Guía de Contribución

Guía para contribuir al proyecto de arquitecturas modernas.

---

## Cómo Contribuir

### Reportar Bugs

Si encuentras un bug:

1. Abre un issue en el repositorio
2. Usa la plantilla de bug report
3. Incluye:
   - Descripción detallada del problema
   - Pasos para reproducir
   - Comportamiento esperado vs actual
   - Logs relevantes si es posible
   - Entorno (OS, Java version, Maven version)

### Sugerir Mejoras

Para sugerir mejoras:

1. Abre un issue con la etiqueta `enhancement`
2. Explica claramente:
   - Qué problema resuelve la mejora
   - Por qué es importante
   - Propuesta de solución si es posible
3. Considera el impacto didáctico de la mejora

### Añadir Ejemplos

Para añadir nuevos ejemplos:

1. Sigue la estructura existente de los módulos
2. Incluye:
   - Código bien documentado
   - Tests que pasen
   - Actualización de la documentación relevante
   - Explicación didáctica del ejemplo
3. Mantén la consistencia con el estilo del proyecto

### Corregir Documentación

Para corregir la documentación:

1. Sé claro y conciso
2. Incluye ejemplos cuando sea posible
3. Mantén la consistencia con otros documentos
4. Actualiza el índice si añades nuevas secciones
5. Revisa la ortografía y gramática

---

## Estilo de Código

### Java

- Sigue las convenciones de código de Java
- Usa nombres descriptivos y significativos
- Añade Javadoc en clases y métodos públicos
- Mantén los métodos cortos y enfocados
- Usa constantes para valores mágicos

### Ejemplo:

```java
/**
 * Service for creating orders in the hexagonal architecture.
 * 
 * <p>This service orchestrates the order creation process by:
 * <ul>
 *   <li>Loading the customer from the repository</li>
 *   <li>Creating the order aggregate</li>
 *   <li>Saving the order</li>
 *   <li>Publishing domain events</li>
 * </ul>
 */
@Service
public class CreateOrderService implements CreateOrderUseCase {
    
    private static final int MAX_ITEMS = 10;
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderEventPublisher eventPublisher;
    
    /**
     * Creates a new order with the given command.
     *
     * @param command the command containing order details
     * @return the created order
     * @throws CustomerNotFoundException if customer does not exist
     * @throws InvalidOrderException if order data is invalid
     */
    @Transactional
    @Override
    public Order create(CreateOrderCommand command) {
        Customer customer = customerRepository.findById(command.getCustomerId())
            .orElseThrow(() -> new CustomerNotFoundException(command.getCustomerId()));
        
        Order order = Order.create(customer, command.getItems());
        orderRepository.save(order);
        eventPublisher.publish(order.pullDomainEvents());
        
        return order;
    }
}
```

### Markdown

- Usa encabezados jerárquicos (#, ##, ###)
- Usa listas con viñetas para items
- Usa bloques de código con especificación de lenguaje
- Usa negrita para términos importantes
- Usa enlaces para referencias cruzadas

### Ejemplo:

```markdown
# Título Principal

## Subtítulo

### Sub-subtítulo

- **Concepto clave**: Descripción
- Otro item

```java
// Código
```

Ver también [GLOSARIO.md](GLOSARIO.md) para más información.
```

---

## Estructura de Commits

### Formato de Mensajes

Usa el formato: `tipo: descripción`

### Tipos

- `feat`: Nueva feature
- `fix`: Corrección de bug
- `docs`: Cambios en documentación
- `refactor`: Refactorización de código
- `test`: Añadir o modificar tests
- `chore`: Tareas de mantenimiento
- `style`: Cambios de formato (sin lógica)

### Ejemplos

```
feat: add cancel order use case in hexagonal module
fix: resolve null pointer exception in product service
docs: update troubleshooting guide with common errors
refactor: simplify order creation logic
test: add integration tests for account transfer
chore: upgrade Maven dependencies
style: format code with checkstyle
```

### Commits de Múltiples Líneas

Para cambios más complejos:

```
feat: implement event sourcing for account aggregate

- Add EventStore interface and JPA implementation
- Implement snapshot mechanism for performance
- Add event reconstruction logic
- Update tests to use event sourcing
- Update documentation with event sourcing explanation

Closes #123
```

---

## Proceso de Pull Request

### Antes de Abrir un PR

1. **Fork el repositorio** y crea una rama desde `main`
2. **Haz tus cambios** siguiendo las guías de estilo
3. **Ejecuta los tests**: `mvn test` en cada módulo modificado
4. **Asegura que los tests pasen**
5. **Actualiza la documentación** si es necesario
6. **Commitea tus cambios** con mensajes claros
7. **Push a tu fork** y abre un Pull Request

### Plantilla de PR

```markdown
## Descripción
Breve descripción de los cambios

## Tipo de Cambio
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Tests
- [ ] Tests añadidos
- [ ] Tests modificados
- [ ] Todos los tests pasan

## Documentación
- [ ] Documentación actualizada
- [ ] READMEs actualizados
- [ ] Glosario actualizado si es necesario

## Checklist
- [ ] Código sigue el estilo del proyecto
- [ ] Commits tienen mensajes claros
- [ ] PR tiene descripción clara
- [ ] No hay merge conflicts
```

### Revisión de PR

- Todos los PRs requieren al menos una aprobación
- Los cambios deben pasar todos los tests
- La documentación debe estar actualizada
- El código debe seguir las guías de estilo
- Responde a los comentarios del revisor

---

## Testing

### Escribir Tests

- Escribe tests para cualquier lógica de dominio nueva
- Escribe tests para cualquier cambio en lógica existente
- Usa nombres descriptivos para los tests
- Usa el patrón Arrange-Act-Assert

### Ejemplo:

```java
class OrderServiceTest {
    
    @Test
    void shouldCreateOrderWhenCustomerExists() {
        // Arrange
        Customer customer = new Customer(new CustomerId("CUST-1"), "Alice");
        CreateOrderCommand command = new CreateOrderCommand(
            customer.getId(),
            List.of(new OrderItem("P1", "Product", new Money(10), 1))
        );
        
        when(customerRepository.findById(customer.getId()))
            .thenReturn(Optional.of(customer));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Order order = orderService.create(command);
        
        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        verify(orderRepository).save(any());
        verify(eventPublisher).publish(any());
    }
    
    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        CustomerId customerId = new CustomerId("NON-EXISTENT");
        CreateOrderCommand command = new CreateOrderCommand(customerId, List.of());
        
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> orderService.create(command))
            .isInstanceOf(CustomerNotFoundException.class);
    }
}
```

### Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un módulo específico
cd hexagonal && mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

---

## Documentación

### Actualizar READMEs

Cuando añades o modificas funcionalidad:

1. Actualiza el README principal si afecta al proyecto general
2. Actualiza el README del módulo específico
3. Añade diagramas Mermaid si ayuda a la comprensión
4. Incluye ejemplos de uso
5. Actualiza la sección de APIs si es necesario

### Actualizar Glosario

Cuando añades nuevos conceptos:

1. Añade el término al GLOSARIO.md
2. Incluye definición clara
3. Añade ejemplo si es posible
4. Incluye referencias cruzadas
5. Mantén el orden alfabético

### Actualizar Ejercicios

Cuando añades nuevos ejemplos:

1. Añade el ejercicio a docs/EJERCICIOS.md
2. Incluye requisitos claros
3. Proporciona solución esperada
4. Incluye tests de ejemplo
5. Especifica el nivel de dificultad

---

## Código de Conducta

### Ser Respetuoso

- Trata a todos con respeto
- Sé constructivo en tus críticas
- Acepta críticas constructivas
- Sé paciente con los nuevos contribuidores

### Ser Inclusivo

- Bienvenido a nuevos contribuidores
- Ayuda a quienes aprenden
- Respeta diferentes perspectivas
- Fomenta un ambiente inclusivo

### Ser Profesional

- Mantén discusiones técnicas
- Evita debates personales
- Enfócate en el código y la documentación
- Sigue las guías del proyecto

---

## Preguntas Frecuentes

### ¿Puedo contribuir si soy nuevo en estas arquitecturas?

¡Sí! Este proyecto es educativo. Apreciamos contribuciones de todos los niveles. Empieza con correcciones de documentación o tests simples.

### ¿Necesito permiso para contribuir?

No, pero para cambios grandes te recomendamos abrir un issue primero para discutir la propuesta.

### ¿Qué pasa si mi PR es rechazado?

No te desanimes. Pide feedback sobre cómo mejorar. Aprendemos de todos los PRs, aceptados o no.

### ¿Puedo contribuir con un nuevo módulo?

Sí, pero primero abre un issue para discutir la propuesta. Queremos mantener el proyecto enfocado y coherente.

### ¿Cómo puedo ayudar sin escribir código?

Puedes:
- Corregir errores en la documentación
- Mejorar los ejemplos
- Añadir más diagramas
- Traducir la documentación
- Reportar bugs
- Dar feedback en issues

---

## Reconocimiento

Los contribuidores serán reconocidos en:

- Lista de contribuidores en el README
- Historial de commits
- Release notes para cambios significativos

---

## Contacto

Para preguntas sobre contribución:

- Abre un issue con la etiqueta `question`
- Participa en las discusiones del proyecto
- Revisa los issues existentes para ver si alguien ya tuvo la misma pregunta

---

## Licencia

Al contribuir, acuerdas que tus contribuciones serán licenciadas bajo la misma licencia del proyecto.

---

## Recursos

- **README.md** - Visión general del proyecto
- **GLOSARIO.md** - Diccionario de conceptos
- **docs/EJERCICIOS.md** - Ejercicios prácticos
- **ROADMAP.md** - Roadmap de aprendizaje
