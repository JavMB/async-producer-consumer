package com.javmb.taskqueue.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.javmb.taskqueue.model.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateTaskDto {
    @NotBlank
    @Size(max = 200)
    private String description;

    @NotBlank
    private Type type;

    private JsonNode payload;


}
