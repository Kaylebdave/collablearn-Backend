package com.collablearn.backend.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("users")
public class User {
    @Id
    private String id;
    private String name;
    private String username;
    private String email;
    private String bio;
    private String department;
    private String level;
    private String institution;
    private String profileImageUrl;
    private Instant updatedAt;
    private String password;
    private String role;
}