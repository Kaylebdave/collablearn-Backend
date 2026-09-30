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
@Document("discussions")
public class Discussion {
    @Id
    private String id;
    private String title;
    private String content;
    private String author;
    private String course;
    private List<Reply> replies = new ArrayList<>();
    private String status;
}