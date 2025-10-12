package com.javmb.taskqueue.controller;

import com.javmb.taskqueue.dto.CreateTaskDto;
import com.javmb.taskqueue.model.Task;
import com.javmb.taskqueue.repository.TaskRepository;
import com.javmb.taskqueue.service.AsyncQueue;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController()
@RequestMapping("/tasks")
public class TasksController {

    private final TaskRepository repository;
    private final AsyncQueue queue;

    public TasksController(TaskRepository repository, AsyncQueue asyncQueue) {
        this.repository = repository;
        this.queue = asyncQueue;
    }

    @PostMapping
    public ResponseEntity<Long> createTask(@Valid @RequestBody CreateTaskDto taskDto) {

        Task created = new Task(taskDto.getDescription());
        long newId = repository.saveTask(created);

        URI location = URI.create("/tasks/" + created.getId());
        return ResponseEntity.created(location).body(newId);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
