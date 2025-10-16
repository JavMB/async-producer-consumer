# Proyecto: async-producer-consumer

Este proyecto es una práctica personal para aprender y asentar conceptos fundamentales de concurrencia y asincronía en Java, utilizando el patrón Producer-Consumer. El objetivo es entender cómo gestionar tareas de forma asíncrona y concurrente, simulando una cola de trabajo y varios workers que procesan tareas en segundo plano.

## ¿Qué busco practicar?

- **Concurrencia y asincronía:** Cómo gestionar múltiples tareas que se producen y consumen de forma independiente, sin bloquear el hilo principal de la aplicación.
- **Patrón Producer-Consumer:** Implementar una arquitectura donde un componente produce tareas y otro(s) las consumen y procesan, desacoplando la generación y el procesamiento.
- **Colas de tareas:** Simular una cola en memoria para almacenar y distribuir el trabajo entre varios workers.
- **Procesamiento asíncrono en Spring Boot:** Usar componentes y servicios de Spring para lanzar y gestionar tareas en segundo plano.
- **Preparación para conceptos avanzados:** Este proyecto sirve como base para entender tecnologías más avanzadas como Spring Batch (procesamiento por lotes) o sistemas de mensajería/brokers (Kafka, RabbitMQ), donde los patrones de concurrencia y asincronía son esenciales.

## Descripción general

La aplicación expone un endpoint HTTP para crear tareas, que se almacenan en una cola en memoria. Un conjunto de workers procesa estas tareas de forma asíncrona, simulando un entorno real donde la producción y el consumo de trabajo están desacoplados. El proyecto sigue el patrón Repository para el acceso a datos y está estructurado para ser didáctico y fácil de entender.

Cada tarea tiene un tipo y es procesada por un procesador específico según su tipo. Los procesadores se despachan automáticamente y ejecutan la lógica correspondiente para cada tarea.

## Tecnologías utilizadas
- Java 17+
- Spring Boot
- JUnit para pruebas

## Cómo ejecutar

1. Clona el repositorio.
2. Ejecuta `./mvnw spring-boot:run`.
3. Usa herramientas como Postman o curl para interactuar con los endpoints:

### Crear una tarea (POST)

`POST /tasks`

Cuerpo de ejemplo:
```json
{
  "description": "Transformar texto",
  "type": "TEXT_TRANSFORM",
  "payload": { "text": "hola mundo" }
}
```

### Consultar una tarea por ID (GET)

`GET /tasks/{id}`

Sustituye `{id}` por el ID devuelto al crear la tarea. Este endpoint permite consultar el estado de una tarea (pendiente, procesando, completada, etc.) y ver el resultado una vez que ha sido procesada.

## Notas

El proyecto no utiliza procesamiento por lotes (batch) ni brokers externos; todo el procesamiento es en memoria y orientado a la práctica de los conceptos básicos de concurrencia y asincronía en Java/Spring. Cada tarea es despachada a un procesador según su tipo, permitiendo simular distintos tipos de trabajo en la cola.
