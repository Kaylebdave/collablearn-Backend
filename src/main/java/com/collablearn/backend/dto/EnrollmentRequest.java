package com.collablearn.backend.dto;

import lombok.Data;

@Data
public class EnrollmentRequest {
    private String userId;
    private String studentId;
}