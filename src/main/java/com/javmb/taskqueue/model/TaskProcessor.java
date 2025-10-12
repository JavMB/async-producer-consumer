package com.javmb.taskqueue.model;

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
     * @param task La tarea a procesar
     * @return El resultado del procesamiento
     */
    R handle(Task task);

    /**
     * Retorna el tipo de tarea que este procesador puede manejar.
     * Similar a getRequestType() en el Mediator.
     *
     * @return El tipo de tarea
     */
    Type getType();

}
