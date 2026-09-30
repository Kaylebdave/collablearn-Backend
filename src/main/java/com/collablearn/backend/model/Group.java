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
@Document("groups")
public class Group {
    @Id
    private String id;
    private String name;
    private String course;
    private String description;
    private List<String> members = new ArrayList<>();
    private List<String> resources = new ArrayList<>();
    private String status;
}