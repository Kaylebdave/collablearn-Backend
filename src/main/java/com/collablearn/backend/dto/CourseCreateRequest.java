package com.collablearn.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseCreateRequest {
    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "lecturer is required")
    private String lecturer;

    @NotBlank(message = "description is required")
    private String description;
}