package com.javmb.taskqueue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class CreateTaskDto {
    @NotBlank
    @Size(max = 200)
    private String description;

}
