package com.collablearn.backend.repository;

import java.util.Optional;
import com.collablearn.backend.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {
    Optional<OtpToken> findTopByEmailOrderByExpiresAtDesc(String email);
    void deleteByEmail(String email);
}
