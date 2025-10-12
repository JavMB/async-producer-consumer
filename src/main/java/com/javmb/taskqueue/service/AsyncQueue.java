package com.javmb.taskqueue.service;

import com.javmb.taskqueue.model.Status;
import com.javmb.taskqueue.model.TaskProcessor;
import com.javmb.taskqueue.repository.TaskRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * Cola asíncrona que procesa tareas usando workers concurrentes.
 * Similar a cómo un Mediator despacha requests, esta clase despacha tareas
 * al procesador adecuado según su tipo.
 */
@Data
@Service
@Slf4j
public class AsyncQueue implements InitializingBean, DisposableBean {
    private BlockingQueue<Long> queue;
    private final ExecutorService workers;
    private final TaskRepository repo;
    private final TaskProcessorRegistry processorRegistry;
    private final int WORKER_COUNT = 4;


    public AsyncQueue(TaskRepository repo, TaskProcessorRegistry processorRegistry) {
        this.queue = new LinkedBlockingDeque<>(1000);
        this.repo = repo;
        this.workers = Executors.newFixedThreadPool(WORKER_COUNT);
        this.processorRegistry = processorRegistry;
        log.info("AsyncQueue initialized with {} workers and queue capacity {}", WORKER_COUNT, 1000);
    }


    /**
     * Encola una tarea para ser procesada.
     *
     * @param taskId ID de la tarea a procesar
     * @return true si se encoló correctamente, false si la cola está llena
     */
    public boolean enqueue(Long taskId) {
        try {
            // intenta encolar 200 ms; si no cabe, devuelve false
            boolean enqueued = queue.offer(taskId, 200, TimeUnit.MILLISECONDS);
            if (enqueued) {
                log.debug("Task {} enqueued successfully", taskId);
            } else {
                log.warn("Failed to enqueue task {}: queue is full", taskId);
            }
            return enqueued;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while enqueueing task {}", taskId, e);
            return false;
        }
    }


    @Override
    public void afterPropertiesSet() {
        log.info("Starting {} worker threads", WORKER_COUNT);
        for (int i = 0; i < WORKER_COUNT; i++) {
            workers.submit(this::workerLoop);
        }
    }

    /**
     * Loop principal de cada worker.
     * Espera por tareas en la cola y las procesa.
     */
    private void workerLoop() {
        String workerName = Thread.currentThread().getName();
        log.info("Worker {} started", workerName);

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Long id = queue.take(); // bloqueante hasta que haya id
                log.debug("Worker {} picked up task {}", workerName, id);
                process(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Worker {} interrupted, shutting down", workerName);
        }
    }

    /**
     * Procesa una tarea usando el procesador adecuado según su tipo.
     * Similar a cómo Mediator.dispatch() encuentra el handler correcto.
     */
    private void process(Long id) {
        repo.findById(id).ifPresent(task -> {
            log.info("Processing task {} of type {}", id, task.getType());


            task.setStatus(Status.PROCESSING);
            repo.update(task);

            try {

                TaskProcessor<?> processor = processorRegistry.get(task.getType());

                Object result = processor.handle(task);

                task.setResult(result != null ? result.toString() : null);
                task.setStatus(Status.DONE);

                log.info("Task {} completed successfully", id);
            } catch (IllegalArgumentException ex) {

                log.error("No processor found for task {} of type {}", id, task.getType(), ex);
                task.setStatus(Status.ERROR);
                task.setResult("Error: No processor available for task type " + task.getType());
            } catch (Exception ex) {

                log.error("Error processing task {} of type {}", id, task.getType(), ex);
                task.setStatus(Status.ERROR);
                task.setResult("Error: " + ex.getMessage());
            } finally {
                repo.update(task);
                // punto para publicar evento SSE , colas de mensajeria o webhooks
            }
        });
    }

    @Override
    public void destroy() {
        log.info("Shutting down AsyncQueue workers");
        workers.shutdownNow();
        try {
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Workers did not terminate in time");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for workers to terminate", e);
        }
    }
}
