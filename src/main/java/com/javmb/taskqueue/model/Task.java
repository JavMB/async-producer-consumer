package com.javmb.taskqueue.model;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {

    private long id;
    private String description;
    private Status status;
    private String result;
    private LocalDateTime createdAt;

    public Task(String description) {
        this.description = description;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}
