package com.collablearn.backend.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupPost {
    private String id;
    private String content;
    private String userId;
    private String author;
    private Instant createdAt;
}