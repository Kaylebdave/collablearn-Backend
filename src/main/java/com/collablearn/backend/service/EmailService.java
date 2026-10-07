package com.collablearn.backend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final URI BREVO_EMAIL_ENDPOINT = URI.create("https://api.brevo.com/v3/smtp/email");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String senderAddress;

    public EmailService(
            ObjectMapper objectMapper,
            @Value("${brevo.api.key}") String apiKey,
            @Value("${mail.from}") String senderAddress) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.senderAddress = senderAddress;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("Brevo email request failed: status=not-sent, response=BREVO_API_KEY is not configured");
            return;
        }

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", "CollabLearn", "email", senderAddress),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Your CollabLearn verification code",
                "htmlContent", "<p>Your CollabLearn OTP is <b>" + otp + "</b>.</p>"
                        + "<p>It expires in 5 minutes.</p>"
        );

        try {
            HttpRequest request = HttpRequest.newBuilder(BREVO_EMAIL_ENDPOINT)
                    .timeout(Duration.ofSeconds(20))
                    .header("api-key", apiKey)
                    .header("accept", "application/json")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("OTP email sent to {}", toEmail);
            } else {
                logger.error("Brevo email request failed: status={}, response={}",
                        response.statusCode(), response.body());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            logger.error("Brevo email request failed: status=interrupted, response={}", exception.getMessage(), exception);
        } catch (Exception exception) {
            logger.error("Brevo email request failed: status=unavailable, response={}", exception.getMessage(), exception);
        }
    }
}