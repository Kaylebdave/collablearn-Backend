package com.collablearn.backend.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("courses")
public class Course {
    @Id
    private String id;
    private String code;
    private String title;
    private String lecturer;
    private String description;
    private String tutorId;
    private String tutorName;
    private List<String> enrolledStudentIds = new ArrayList<>();
    private int materialsCount;
    private List<Material> materials = new ArrayList<>();
}