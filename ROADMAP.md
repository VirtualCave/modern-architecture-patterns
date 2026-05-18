# Roadmap de Aprendizaje

Guía estructurada para aprender las arquitecturas modernas presentadas en este tutorial.

---

## Nivel 1: Fundamentos (1-2 semanas)

### Objetivos

- Entender los conceptos básicos de cada arquitectura
- Saber cuándo usar cada una
- Reconocer anti-patrones comunes
- Familiarizarse con la terminología

### Actividades

#### Semana 1

- [ ] Leer README principal del proyecto
- [ ] Estudiar GLOSARIO.md - entender los conceptos clave
- [ ] Ejecutar los 4 módulos (hexagonal, cqrs, ddd, hybrid)
- [ ] Probar los endpoints de ejemplo de cada módulo
- [ ] Revisar la estructura de código de cada módulo
- [ ] Leer los READMEs específicos de cada arquitectura

#### Semana 2

- [ ] Revisar docs/ANTI_PATRONES.md - entender qué evitar
- [ ] Estudiar docs/ERRORES_COMUNES.md - errores frecuentes
- [ ] Completar ejercicios nivel 1 de docs/EJERCICIOS.md:
  - [ ] Ejercicio 1: Crear Value Object Email
  - [ ] Ejercicio 2: Proteger invariante
  - [ ] Ejercicio 3: Implementar método de dominio
- [ ] Revisar QUICKSTART.md para referencia rápida

### Evaluación

Al finalizar el Nivel 1, deberías ser capaz de:

- ✅ Explicar con tus palabras qué es cada arquitectura (Hexagonal, CQRS, DDD)
- ✅ Identificar cuándo usar Hexagonal vs CQRS vs DDD
- ✅ Reconocer un modelo anémico y sus problemas
- ✅ Entender qué son ports, adapters, aggregates, value objects
- ✅ Saber la diferencia entre command y query en CQRS
- ✅ Ejecutar y probar los 4 módulos sin ayuda

### Recursos

- **README.md** - Visión general del proyecto
- **GLOSARIO.md** - Diccionario de conceptos
- **QUICKSTART.md** - Guía de inicio rápido
- **docs/ANTI_PATRONES.md** - Anti-patrones a evitar
- **docs/ERRORES_COMUNES.md** - Errores frecuentes

---

## Nivel 2: Práctica (2-4 semanas)

### Objetivos

- Implementar features simples en cada arquitectura
- Entender la estructura de código en profundidad
- Escribir tests de dominio
- Debuggear problemas comunes

### Actividades

#### Semana 3

- [ ] Completar ejercicios nivel 2 de docs/EJERCICIOS.md:
  - [ ] Ejercicio 4: Implementar Input Port (hexagonal)
  - [ ] Ejercicio 5: CQRS para categorías (cqrs)
  - [ ] Ejercicio 6: Bounded Context (ddd)
- [ ] Escribir tests para los ejercicios completados
- [ ] Revisar docs/TROUBLESHOOTING.md para resolver problemas

#### Semana 4

- [ ] Añadir un nuevo endpoint en cada módulo:
  - [ ] Hexagonal: endpoint para listar pedidos por cliente
  - [ ] CQRS: endpoint para buscar productos por nombre
  - [ ] DDD: endpoint para obtener historial de transacciones
- [ ] Escribir tests de integración para los nuevos endpoints
- [ ] Revisar los diagramas de secuencia en los READMEs

#### Semana 5 (Opcional)

- [ ] Implementar un nuevo Value Object en cada módulo
- [ ] Añadir validaciones adicionales a los aggregates
- [ ] Implementar domain events adicionales
- [ ] Escribir tests de dominio sin Spring

### Evaluación

Al finalizar el Nivel 2, deberías ser capaz de:

- ✅ Implementar un caso de uso sin ayuda
- ✅ Escribir tests que pasen
- ✅ Debuggear problemas comunes usando TROUBLESHOOTING.md
- ✅ Añadir nuevas features a cada arquitectura
- ✅ Entender el flujo completo de una petición HTTP
- ✅ Escribir tests de dominio aislados

### Recursos

- **docs/EJERCICIOS.md** - Ejercicios prácticos
- **docs/TROUBLESHOOTING.md** - Guía de resolución de problemas
- Diagramas de secuencia en cada README

---

## Nivel 3: Avanzado (4-8 semanas)

### Objetivos

- Combinar arquitecturas
- Entender patrones complejos
- Diseñar bounded contexts
- Resolver challenges reales

### Actividades

#### Semana 6-7

- [ ] Completar ejercicios nivel 3 de docs/EJERCICIOS.md:
  - [ ] Ejercicio 7: Mezclar Hexagonal + CQRS
  - [ ] Ejercicio 8: Implementar Event Sourcing
- [ ] Estudiar el módulo hybrid en profundidad
- [ ] Entender cómo conviven las tres arquitecturas

#### Semana 8-9

- [ ] Resolver challenges de docs/EJERCICIOS.md:
  - [ ] Challenge 1: Migración gradual
  - [ ] Challenge 2: Consistencia eventual
  - [ ] Challenge 3: DDD en equipo grande
- [ ] Diseñar una arquitectura para un caso real
- [ ] Documentar la decisión arquitectónica

#### Semana 10-12 (Opcional)

- [ ] Implementar un módulo desde cero usando las arquitecturas aprendidas
- [ ] Contribuir al proyecto con mejoras
- [ ] Escribir un artículo o blog post sobre lo aprendido
- [ ] Presentar el conocimiento al equipo

### Evaluación

Al finalizar el Nivel 3, deberías ser capaz de:

- ✅ Diseñar una arquitectura para un caso real
- ✅ Explicar trade-offs de decisiones arquitectónicas
- ✅ Combinar múltiples arquitecturas cuando sea necesario
- ✅ Resolver problemas complejos de arquitectura
- ✅ Mentor a otros desarrolladores
- ✅ Contribuir efectivamente al proyecto

### Recursos

- **docs/EJERCICIOS.md** - Ejercicios avanzados y challenges
- **hybrid/README.md** - Ejemplo de combinación de arquitecturas
- Diagramas de componentes y comparativas en README principal

---

## Ruta Recomendada por Rol

### Para Desarrolladores Junior

1. **Nivel 1** (2 semanas): Fundamentos
2. **Nivel 2** (4 semanas): Práctica
3. Revisar conceptos y reforzar conocimiento

### Para Desarrolladores Mid-Level

1. **Nivel 1** (1 semana): Fundamentos acelerados
2. **Nivel 2** (2 semanas): Práctica intensiva
3. **Nivel 3** (4 semanas): Avanzado

### Para Desarrolladores Senior / Arquitectos

1. **Nivel 1** (3 días): Repaso rápido
2. **Nivel 2** (1 semana): Práctica
3. **Nivel 3** (2 semanas): Avanzado + Challenges
4. Contribuir al proyecto y documentar

---

## Hitos de Progreso

### Hito 1: Fundamentos Completados ✅

- [ ] Completados todos los ejercicios del Nivel 1
- [ ] Entendidos todos los conceptos del GLOSARIO.md
- [ ] Ejecutados y probados todos los módulos
- [ ] Reconocidos anti-patrones y errores comunes

### Hito 2: Práctica Completada ✅

- [ ] Completados todos los ejercicios del Nivel 2
- [ ] Implementados nuevos endpoints en cada módulo
- [ ] Escritos tests de dominio e integración
- [ ] Debuggeados problemas usando TROUBLESHOOTING.md

### Hito 3: Avanzado Completado ✅

- [ ] Completados todos los ejercicios del Nivel 3
- [ ] Resueltos al menos 2 challenges
- [ ] Diseñada una arquitectura para un caso real
- [ ] Contribuido al proyecto

---

## Consejos de Aprendizaje

### 1. Aprende haciendo

No solo leas la documentación. Ejecuta el código, modifica, rompe cosas, arreglalas. La mejor forma de aprender es practicando.

### 2. Comprende el "por qué"

No solo aprendas "cómo" implementar cada patrón. Entiende "por qué" existe y qué problema resuelve.

### 3. Empieza simple

No intentes implementar todo de golpe. Empieza con los ejercicios del Nivel 1 y avanza gradualmente.

### 4. Usa los recursos

- **GLOSARIO.md** cuando no entiendas un término
- **docs/TROUBLESHOOTING.md** cuando tengas problemas
- **docs/ERRORES_COMUNES.md** para evitar errores típicos
- **docs/ANTI_PATRONES.md** para qué no hacer

### 5. Escribe tests

Los tests te ayudan a entender el código y te dan confianza para hacer cambios. Escribir tests es parte del aprendizaje.

### 6. Comparte tu conocimiento

Explicar lo que has aprendido a otros te ayuda a consolidar el conocimiento. Considera hacer presentaciones o escribir artículos.

### 7. Sé paciente

Las arquitecturas modernas son complejas. Tómate tu tiempo para entenderlas bien. No te frustres si algo no queda claro al principio.

---

## Recursos Adicionales

### Libros Recomendados

- **Domain-Driven Design** de Eric Evans
- **Implementing Domain-Driven Design** de Vaughn Vernon
- **Patterns of Enterprise Application Architecture** de Martin Fowler
- **Clean Architecture** de Robert C. Martin

### Artículos y Blogs

- Martin Fowler's Blog
- Vaughn Vernon's Blog
- Microsoft Architecture Guide

### Comunidades

- DDD Community
- Architecture communities en Reddit
- Stack Overflow para preguntas específicas

---

## Checklist de Preparación

Antes de empezar el roadmap:

- [ ] Tener Java 26+ instalado
- [ ] Tener Maven 3.8+ instalado
- [ ] Tener un IDE (IntelliJ IDEA recomendado)
- [ ] Tener curl o Postman para probar APIs
- [ ] Tener Git instalado
- [ ] Leer el README principal del proyecto

---

## Soporte

Si tienes dudas durante el aprendizaje:

1. Consulta el **GLOSARIO.md** para entender conceptos
2. Revisa **docs/TROUBLESHOOTING.md** para problemas técnicos
3. Mira los **diagramas** en los READMEs para visualizar arquitecturas
4. Revisa los **ejemplos de código** en docs/ANTI_PATRONES.md y docs/ERRORES_COMUNES.md
5. Abre un issue en el repositorio si encuentras un bug
6. Participa en las discusiones del proyecto

---

## Próximos Pasos

Una vez completado el roadmap:

1. **Aplica lo aprendido** en tu proyecto actual
2. **Contribuye** al proyecto con mejoras o nuevos ejemplos
3. **Comparte** tu conocimiento con el equipo
4. **Explora** patrones avanzados adicionales
5. **Mantente actualizado** con las últimas tendencias en arquitectura

---

## Referencias

- **README.md** - Visión general del proyecto
- **GLOSARIO.md** - Diccionario de conceptos
- **QUICKSTART.md** - Guía de inicio rápido
- **docs/EJERCICIOS.md** - Ejercicios prácticos
- **docs/TROUBLESHOOTING.md** - Guía de resolución de problemas
- **docs/ANTI_PATRONES.md** - Anti-patrones a evitar
- **docs/ERRORES_COMUNES.md** - Errores frecuentes
