package com.collablearn.backend.controller;

import com.collablearn.backend.dto.AuthResponse;
import com.collablearn.backend.dto.LoginRequest;
import com.collablearn.backend.dto.ResendOtpRequest;
import com.collablearn.backend.dto.SignupRequest;
import com.collablearn.backend.dto.VerifyOtpRequest;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.UserRepository;
import com.collablearn.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try { return ResponseEntity.ok(authService.signup(request)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.badRequest().body(exception.getMessage()); }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try { return ResponseEntity.ok(authService.verifyOtp(request)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.badRequest().body(exception.getMessage()); }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try { return ResponseEntity.ok(authService.resendOtp(request)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.badRequest().body(exception.getMessage()); }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try { return ResponseEntity.ok(authService.login(request)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage()); }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String email
    ) {
        try {
            return ResponseEntity.ok(authService.findProfile(userId, email));
        } catch (IllegalArgumentException exception) {
            return profileError(exception);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "id is required"));
        }

        return userRepository.findById(id)
                .map(user -> {
                    if (body.get("name") != null) user.setName(String.valueOf(body.get("name")));
                    if (body.get("username") != null) user.setUsername(String.valueOf(body.get("username")));
                    if (body.get("bio") != null) user.setBio(String.valueOf(body.get("bio")));
                    if (body.get("department") != null) user.setDepartment(String.valueOf(body.get("department")));
                    if (body.get("level") != null) user.setLevel(String.valueOf(body.get("level")));
                    if (body.get("institution") != null) user.setInstitution(String.valueOf(body.get("institution")));

                    User saved = userRepository.save(user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("id", saved.getId());
                    response.put("name", saved.getName());
                    response.put("email", saved.getEmail());
                    response.put("role", saved.getRole());
                    response.put("username", saved.getUsername());
                    response.put("bio", saved.getBio());
                    response.put("department", saved.getDepartment());
                    response.put("level", saved.getLevel());
                    response.put("institution", saved.getInstitution());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("message", "User not found")));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(request.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok().body(java.util.Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException exception) {
            return profileError(exception);
        }
    }

    private ResponseEntity<?> profileError(IllegalArgumentException exception) {
        if ("User not found".equals(exception.getMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("message", "User not found"));
        }
        return ResponseEntity.badRequest().body(java.util.Map.of("message", exception.getMessage()));
    }

    @lombok.Data
    private static class ChangePasswordRequest {
        private String id;
        private String oldPassword;
        private String newPassword;
    }
}