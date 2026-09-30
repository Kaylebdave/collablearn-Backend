package com.collablearn.backend.controller;

import com.collablearn.backend.dto.ProfileUpdateRequest;
import com.collablearn.backend.dto.UserProfileResponse;
import com.collablearn.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        try {
            UserProfileResponse updatedUser = authService.updateProfile(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException exception) {
            if ("User not found".equals(exception.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}