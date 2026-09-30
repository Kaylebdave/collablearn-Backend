package com.collablearn.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String id;
    @NotBlank(message = "Name is required")
    private String name;
    private String username;
    private String bio;
    private String department;
    private String level;
    private String institution;
    private String profileImageUrl;
}