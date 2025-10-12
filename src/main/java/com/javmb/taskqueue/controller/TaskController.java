package com.javmb.taskqueue.controller;

import com.javmb.taskqueue.dto.CreateTaskDto;
import com.javmb.taskqueue.model.Status;
import com.javmb.taskqueue.model.Task;
import com.javmb.taskqueue.repository.TaskRepository;
import com.javmb.taskqueue.service.AsyncQueue;
import com.javmb.taskqueue.service.TaskProcessorRegistry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskRepository repository;
    private final AsyncQueue asyncQueue;
    private final TaskProcessorRegistry processorRegistry;


    public TaskController(TaskRepository repository, AsyncQueue asyncQueue, TaskProcessorRegistry processorRegistry) {
        this.repository = repository;
        this.asyncQueue = asyncQueue;
        this.processorRegistry = processorRegistry;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> createTask(@Valid @RequestBody CreateTaskDto taskDto) {

        Task created = new Task(taskDto.getDescription(), taskDto.getPayload(), taskDto.getType());


        long newId = repository.saveTask(created);


        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId)
                .toUri();

        // intentar encolar (control de carga)
        boolean enqueued = asyncQueue.enqueue(newId);
        if (!enqueued) {

            created.setStatus(Status.ERROR);
            created.setResult("Queue full, try again later");
            repository.update(created);

            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .location(location)
                    .body(Map.of("id", newId));
        }

        // encolada correctamente
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .location(location)
                .body(Map.of("id", newId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
