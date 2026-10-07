package com.collablearn.backend.controller;

import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Reply;
import com.collablearn.backend.service.DiscussionService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {
    private static final Logger logger = LoggerFactory.getLogger(DiscussionController.class);
    private final DiscussionService discussionService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            String requesterId = resolveUserId(userId, null, headerUserId, "GET /api/discussions");
            return ResponseEntity.ok(discussionService.findAll(requesterId, courseId));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable String id,
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            String requesterId = resolveUserId(userId, null, headerUserId, "GET /api/discussions/{id}");
            return ResponseEntity.ok(discussionService.findById(id, requesterId));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody Discussion discussion,
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            String requesterId = resolveUserId(discussion.getUserId(), userId, headerUserId, "POST /api/discussions");
            return ResponseEntity.status(HttpStatus.CREATED).body(discussionService.create(discussion, requesterId));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<?> addReply(
            @PathVariable String id,
            @RequestBody Reply reply,
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            String requesterId = resolveUserId(reply.getUserId(), userId, headerUserId,
                    "POST /api/discussions/{id}/replies");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(discussionService.addReply(id, reply, requesterId));
        } catch (RuntimeException exception) {
            if ("userId is required".equals(exception.getMessage())
                    || "userId values do not match".equals(exception.getMessage())) {
                logger.warn("POST /api/discussions/{}/replies rejected: missing or conflicting user id", id);
            }
            return errorResponse(exception);
        }
    }

    private String resolveUserId(String bodyUserId, String queryUserId, String headerUserId, String endpoint) {
        String normalizedBodyId = normalizeUserId(bodyUserId);
        String normalizedQueryId = normalizeUserId(queryUserId);
        String normalizedHeaderId = normalizeUserId(headerUserId);
        String resolvedId = normalizedBodyId != null ? normalizedBodyId : normalizedQueryId;
        if (resolvedId == null) {
            resolvedId = normalizedHeaderId;
        }
        if (resolvedId == null) {
            logger.warn("{} rejected: missing user id", endpoint);
            throw new IllegalArgumentException("userId is required");
        }
        if (normalizedBodyId != null && normalizedQueryId != null && !normalizedBodyId.equals(normalizedQueryId)
                || normalizedBodyId != null && normalizedHeaderId != null && !normalizedBodyId.equals(normalizedHeaderId)
                || normalizedQueryId != null && normalizedHeaderId != null && !normalizedQueryId.equals(normalizedHeaderId)) {
            logger.warn("{} rejected: conflicting user ids", endpoint);
            throw new IllegalArgumentException("userId values do not match");
        }
        return resolvedId;
    }

    private String normalizeUserId(String userId) {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }

    private ResponseEntity<?> errorResponse(RuntimeException exception) {
        HttpStatus status;
        if (exception instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if ("Course not found".equals(exception.getMessage())
                || "Discussion not found".equals(exception.getMessage())
                || "User not found".equals(exception.getMessage())) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
    }
}