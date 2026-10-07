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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AuthService authService;

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody ProfileUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String requestedId = id == null || id.isBlank() ? null : id.trim();
        String bodyUserId = request.getId() == null || request.getId().isBlank() ? null : request.getId().trim();
        String headerId = headerUserId == null || headerUserId.isBlank() ? null : headerUserId.trim();
        if (requestedId == null) {
            logger.warn("PUT /api/users rejected: missing user id");
            return ResponseEntity.badRequest().body("userId is required");
        }
        if ((bodyUserId != null && !requestedId.equals(bodyUserId))
                || (headerId != null && !requestedId.equals(headerId))) {
            logger.warn("PUT /api/users/{} rejected: request user id does not match path id", requestedId);
            return ResponseEntity.badRequest().body("userId does not match path id");
        }
        try {
            UserProfileResponse updatedUser = authService.updateProfile(requestedId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException exception) {
            if ("User not found".equals(exception.getMessage())) {
                logger.warn("PUT /api/users/{} rejected: user id not found", requestedId);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }
}