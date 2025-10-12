package com.javmb.taskqueue.service;

import com.javmb.taskqueue.model.Type;
import com.javmb.taskqueue.model.TaskProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registro de procesadores de tareas.
 * Similar al Mediator, mantiene un mapa de Type -> TaskProcessor.
 * Usa el Type de la tarea como clave (similar a usar la clase de Request en el Mediator).
 */
@Service
@Slf4j
public class TaskProcessorRegistry {
    private final Map<Type, TaskProcessor<?>> processors;

    /**
     * Constructor que registra automáticamente todos los procesadores disponibles.
     * Spring inyecta todos los beans que implementan TaskProcessor.
     */
    public TaskProcessorRegistry(List<TaskProcessor<?>> processorsList) {
        this.processors = processorsList.stream()
                .collect(Collectors.toMap(
                        TaskProcessor::getType,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        () -> new EnumMap<>(Type.class)
                ));

        log.info("Registered {} task processors", processors.size());
        processors.keySet().forEach(type ->
            log.debug("Processor registered for type: {}", type)
        );
    }

    /**
     * Obtiene el procesador adecuado para un tipo de tarea.
     * Similar a cómo el Mediator busca el handler por la clase de Request.
     *
     * @param type El tipo de tarea
     * @return El procesador correspondiente
     * @throws IllegalArgumentException si no existe procesador para ese tipo
     */
    @SuppressWarnings("unchecked")
    public <R> TaskProcessor<R> get(Type type) {
        TaskProcessor<?> processor = processors.get(type);
        if (processor == null) {
            log.error("No processor found for task type: {}", type);
            throw new IllegalArgumentException("No processor found for task type: " + type);
        }
        return (TaskProcessor<R>) processor;
    }
}
