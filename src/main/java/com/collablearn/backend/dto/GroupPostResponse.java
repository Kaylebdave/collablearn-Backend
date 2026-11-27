package com.collablearn.backend.dto;

import java.time.Instant;

public record GroupPostResponse(
        String id,
        String groupId,
        String content,
        String userId,
        String author,
        Instant createdAt
) {
}