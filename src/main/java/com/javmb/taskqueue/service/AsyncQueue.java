package com.javmb.taskqueue.service;

import com.javmb.taskqueue.model.Status;
import com.javmb.taskqueue.repository.TaskRepository;
import lombok.Data;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Data
@Service
public class AsyncQueue implements InitializingBean, DisposableBean {
    private BlockingQueue<Long> queue;
    private final ExecutorService workers;
    private final TaskRepository repo;
    private final int WORKER_COUNT = 4;

    public AsyncQueue(TaskRepository repo) {
        this.queue = new LinkedBlockingDeque<>(1000);
        this.repo = repo;
        this.workers = Executors.newFixedThreadPool(WORKER_COUNT);
    }


    public boolean enqueue(Long taskId) {
        try {
            // intenta encolar 200 ms; si no cabe, devuelve false
            return queue.offer(taskId, 200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }


    @Override
    public void afterPropertiesSet() {
        for (int i = 0; i < WORKER_COUNT; i++) {
            workers.submit(this::workerLoop);
        }

    }

    private void workerLoop() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Long id = queue.take(); // bloqueante hasta que haya id
                process(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void process(Long id) {
        repo.findById(id).ifPresent(task -> {

            task.setStatus(Status.PROCESSING);
            repo.update(task);

            try {
                // TODO: logica real
                int sleepMs = 2000;
                Thread.sleep(sleepMs);

                String res = "PROCESADO: " + task.getDescription().toUpperCase();
                task.setResult(res);
                task.setStatus(Status.DONE);
            } catch (Exception ex) {
                task.setStatus(Status.ERROR);
                task.setResult("Error: " + ex.getMessage());
            } finally {
                repo.update(task);
                // Punto para publicar evento SSE / WebSocket o llamar callbackUrl si lo añades
            }
        });
    }

    @Override
    public void destroy() {
        workers.shutdownNow();
    }
}
