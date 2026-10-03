package com.collablearn.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.collablearn.backend.dto.AuthResponse;
import com.collablearn.backend.dto.LoginRequest;
import com.collablearn.backend.dto.ProfileUpdateRequest;
import com.collablearn.backend.dto.ResendOtpRequest;
import com.collablearn.backend.dto.SignupRequest;
import com.collablearn.backend.dto.UserProfileResponse;
import com.collablearn.backend.dto.VerifyOtpRequest;
import com.collablearn.backend.model.OtpToken;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.OtpTokenRepository;
import com.collablearn.backend.repository.UserRepository;
import com.collablearn.backend.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public String signup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String role = request.getRole().trim().toLowerCase();
        if (!role.equals("student") && !role.equals("tutor")) {
            throw new IllegalArgumentException("Role must be student or tutor");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        sendOtp(
                email,
                request.getName(),
                request.getPassword(),
                role
        );
        return "OTP generated successfully. Check server console (development mode).";
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        OtpToken token = findLatestToken(email);

        if (token.isUsed()) {
            throw new IllegalArgumentException("OTP has already been used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        if (!token.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        token.setUsed(true);
        otpTokenRepository.save(token);

        User user = new User();
        user.setName(token.getName());
        user.setEmail(email);
        user.setPassword(token.getPassword());
        user.setRole(token.getRole());
        user.setUpdatedAt(Instant.now());
        return toResponse(userRepository.save(user));
    }

    public String resendOtp(ResendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        OtpToken previousToken = findLatestToken(email);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }

        sendOtp(
                email,
                previousToken.getName(),
                previousToken.getPassword(),
                previousToken.getRole()
        );
        return "OTP generated successfully. Check server console (development mode).";
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .filter(foundUser -> foundUser.getPassword().equals(request.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        return toResponse(user);
    }

    public UserProfileResponse updateProfile(String id, ProfileUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setName(request.getName().trim());
        user.setUsername(trimToNull(request.getUsername()));
        user.setBio(trimToNull(request.getBio()));
        user.setDepartment(trimToNull(request.getDepartment()));
        user.setLevel(trimToNull(request.getLevel()));
        user.setInstitution(trimToNull(request.getInstitution()));
        user.setUpdatedAt(Instant.now());

        return UserProfileResponse.from(userRepository.save(user));
    }

    public UserProfileResponse findProfile(String userId, String email) {
        User user;
        if (userId != null && !userId.isBlank()) {
            user = userRepository.findById(userId.trim())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email.trim().toLowerCase())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            throw new IllegalArgumentException("Provide userId or email");
        }
        return UserProfileResponse.from(user);
    }

    public UserProfileResponse updateProfile(ProfileUpdateRequest request) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        return updateProfile(request.getId(), request);
    }

    public void changePassword(String id, String oldPassword, String newPassword) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id is required");
        }
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!oldPassword.equals(user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(newPassword);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void sendOtp(String email, String name, String password, String role) {
        String otp = OtpUtil.generateOtp();
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        otpTokenRepository.deleteByEmail(email);
        otpTokenRepository.save(new OtpToken(
                null,
                email,
                otp,
                expiresAt,
                false,
                name,
                password,
                role
        ));

        System.out.println("===== DEV OTP =====");
        System.out.println("Email: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("===================");

        emailService.sendOtpEmail(email, otp);
    }

    private OtpToken findLatestToken(String email) {
        return otpTokenRepository.findTopByEmailOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("No OTP request found for this email"));
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}