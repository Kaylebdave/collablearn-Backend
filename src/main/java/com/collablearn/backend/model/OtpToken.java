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
@Document("otp_tokens")
public class OtpToken {
    @Id
    private String id;
    private String email;
    private String otp;
    private Instant expiresAt;
    private boolean used;

    // These fields keep the signup data until the OTP is verified.
    private String name;
    private String password;
    private String role;
}
