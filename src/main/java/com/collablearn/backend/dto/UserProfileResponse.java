package com.collablearn.backend.dto;

import java.time.Instant;
import com.collablearn.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
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
    private String role;

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getName(),
            user.getUsername(),
            user.getEmail(),
            user.getBio(),
            user.getDepartment(),
            user.getLevel(),
            user.getInstitution(),
            user.getProfileImageUrl(),
            user.getUpdatedAt(),
            user.getRole()
        );
    }
}