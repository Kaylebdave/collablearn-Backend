package com.collablearn.backend.dto;

import com.collablearn.backend.model.Material;
import java.util.List;

public record CourseDetailsResponse(
        String id,
        String code,
        String title,
        String description,
        String lecturer,
        String tutorId,
        String tutorName,
        List<String> enrolledStudentIds,
        List<EnrolledStudentResponse> enrolledStudents,
        List<Material> materials,
        int studentsCount,
        int materialsCount
) {
}