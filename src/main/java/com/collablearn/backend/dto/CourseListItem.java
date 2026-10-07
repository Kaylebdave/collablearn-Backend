package com.collablearn.backend.dto;

public record CourseListItem(
        String id,
        String code,
        String title,
        String description,
        String tutorId,
        String tutorName,
        int studentsCount,
        int materialsCount
) {
}