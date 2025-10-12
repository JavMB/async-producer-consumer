package com.javmb.taskqueue.model;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {

    private long id;
    private Type type;
    private JsonNode payload;
    private String description;
    private Status status;
    private String result;
    private LocalDateTime createdAt;

    public Task(String description,JsonNode payload, Type type) {
        this.description = description;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.payload=payload;
        this.type=type;
    }
}
