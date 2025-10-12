package com.javmb.taskqueue.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Procesador de tareas genérico.
 * Similar al patrón RequestHandler del Mediator.
 *
 * @param <R> Tipo del resultado que devuelve el procesador
 */
public interface TaskProcessor<R> {

    /**
     * Procesa una tarea y devuelve un resultado tipado.
     *
     * @param payload La tarea a procesar
     * @return El resultado del procesamiento
     */
    R handle(JsonNode payload);

    /**
     * Retorna el tipo de tarea que este procesador puede manejar.
     * Similar a getRequestType() en el Mediator.
     *
     * @return El tipo de tarea
     */
    Type getType();

}
