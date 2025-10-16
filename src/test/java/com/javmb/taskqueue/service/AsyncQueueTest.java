package com.javmb.taskqueue.service;

import com.fasterxml.jackson.databind.node.TextNode;
import com.javmb.taskqueue.model.Status;
import com.javmb.taskqueue.model.Task;
import com.javmb.taskqueue.model.TaskProcessor;
import com.javmb.taskqueue.model.Type;
import com.javmb.taskqueue.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncQueueTest {
    @Mock
    private TaskRepository repo;
    @Mock
    private TaskProcessorRegistry processorRegistry;
    @Mock
    private TaskProcessor<Object> processor;
    @InjectMocks
    private AsyncQueue asyncQueue;

    @Test
    void shouldEnqueueTaskSuccessfully() {
        asyncQueue.afterPropertiesSet();
        boolean result = asyncQueue.enqueue(1L);
        assertTrue(result);
        asyncQueue.destroy();
    }

    @Test
    void shouldNotEnqueueTaskWhenQueueIsFull() {
       // asyncQueue.afterPropertiesSet();
        for (int i = 0; i < 1000; i++) {
            asyncQueue.enqueue((long) i);
        }
        boolean result = asyncQueue.enqueue(1001L);
        assertFalse(result);
        asyncQueue.destroy();
    }

    @Test
    void shouldProcessTaskSuccessfully() {
        asyncQueue.afterPropertiesSet();
        Task task = new Task("desc", new TextNode("payload"), Type.TEXT_TRANSFORM);
        task.setId(1L);
        task.setStatus(Status.PENDING);
        when(repo.findById(1L)).thenReturn(Optional.of(task));
        when(processorRegistry.get(Type.TEXT_TRANSFORM)).thenReturn(processor);
        when(processor.handle(any())).thenReturn("result");

        asyncQueue.enqueue(1L);

        await().atMost(5, TimeUnit.SECONDS).until(() -> task.getStatus() == Status.DONE);
        verify(repo, atLeastOnce()).update(task);
        assertEquals("result", task.getResult());
        asyncQueue.destroy();
    }

    @Test
    void shouldSetTaskToErrorWhenProcessorThrowsException() {
        asyncQueue.afterPropertiesSet();
        Task task = new Task("desc", new TextNode("payload"), Type.TEXT_TRANSFORM);
        task.setId(1L);
        task.setStatus(Status.PENDING);
        when(repo.findById(1L)).thenReturn(Optional.of(task));
        when(processorRegistry.get(Type.TEXT_TRANSFORM)).thenThrow(new RuntimeException("Error"));

        asyncQueue.enqueue(1L);

        await().atMost(5, TimeUnit.SECONDS).until(() -> task.getStatus() == Status.ERROR);
        verify(repo, atLeastOnce()).update(task);
        assertTrue(task.getResult().contains("Error"));
        asyncQueue.destroy();
    }

    @Test
    void shouldSetTaskToErrorWhenNoProcessorFound() {
        asyncQueue.afterPropertiesSet();
        Task task = new Task("desc", new TextNode("payload"), Type.TEXT_TRANSFORM);
        task.setId(1L);
        task.setStatus(Status.PENDING);
        when(repo.findById(1L)).thenReturn(Optional.of(task));
        when(processorRegistry.get(Type.TEXT_TRANSFORM)).thenThrow(new IllegalArgumentException("No processor"));

        asyncQueue.enqueue(1L);

        await().atMost(5, TimeUnit.SECONDS).until(() -> task.getStatus() == Status.ERROR);
        verify(repo, atLeastOnce()).update(task);
        assertTrue(task.getResult().contains("No processor"));
        asyncQueue.destroy();
    }
}