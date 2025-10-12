package com.javmb.taskqueue.repository;

import com.javmb.taskqueue.model.Task;
import lombok.Data;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Repository
public class TaskRepository {
    private final ConcurrentHashMap<Long, Task> tasks;
    private final AtomicLong idGen;


    public TaskRepository() {
        this.tasks = new ConcurrentHashMap<>();
        this.idGen = new AtomicLong(1);
    }

    public long saveTask(Task task) {
        long id = idGen.getAndIncrement();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Collection<Task> findAll() {
        return tasks.values();
    }


    public void update(Task task) {
        tasks.put(task.getId(), task);
    }


    public boolean delete(Long id) {
        return tasks.remove(id) != null;
    }


}
