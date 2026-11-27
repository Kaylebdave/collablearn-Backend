package com.collablearn.backend.dto;

public record GroupCreateRequest(String name, String course, String description, String userId) {
}