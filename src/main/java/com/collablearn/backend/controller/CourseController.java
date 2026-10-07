package com.collablearn.backend.controller;

import com.collablearn.backend.dto.CourseCreateRequest;
import com.collablearn.backend.dto.EnrollmentRequest;
import com.collablearn.backend.model.Course;
import com.collablearn.backend.service.CourseService;
import java.io.IOException;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String userId,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            return ResponseEntity.ok(courseService.findAllForUser(resolveUserId(userId, headerUserId, "GET /api/courses")));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @GetMapping("/browse")
    public ResponseEntity<?> browse(
            @RequestParam(required = false) String userId,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            return ResponseEntity.ok(courseService.browseForStudent(
                    resolveUserId(userId, headerUserId, "GET /api/courses/browse")));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @GetMapping("/enrolled")
    public ResponseEntity<?> deprecatedEnrolledPath() {
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Use GET /api/courses with userId for enrolled courses"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        try { return ResponseEntity.ok(courseService.findById(id)); }
        catch (RuntimeException exception) { return errorResponse(exception); }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(
            @Valid @RequestBody CourseCreateRequest request,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            String tutorId = resolveUserId(request.getUserId(), headerUserId, "POST /api/courses");
            return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request, tutorId));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<?> enroll(
            @PathVariable String id,
            @RequestBody(required = false) EnrollmentRequest request,
            @RequestParam(required = false) String userId,
            @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String bodyUserId = request == null ? null
                : (request.getStudentId() == null ? request.getUserId() : request.getStudentId());
        try {
            String studentId = resolveUserId(bodyUserId, userId, headerUserId, "POST /api/courses/{id}/enroll");
            return ResponseEntity.ok(courseService.enroll(id, studentId));
        } catch (RuntimeException exception) {
            return errorResponse(exception);
        }
    }

    @PostMapping(value = "/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMaterial(
        @PathVariable String courseId,
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam(required = false) String userId,
        @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        try {
            String tutorId = resolveUserId(userId, headerUserId, "POST /api/courses/{courseId}/materials");
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Course material uploaded successfully",
                "material", courseService.addMaterial(courseId, title, file, tutorId)
            ));
        } catch (RuntimeException exception) {
            ResponseEntity<?> error = errorResponse(exception);
            return ResponseEntity.status(error.getStatusCode()).body(Map.of(
                    "success", false,
                    "error", exception.getMessage()
            ));
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "The material could not be uploaded"
            ));
        }
    }

    private String resolveUserId(String suppliedId, String headerUserId, String endpoint) {
        return resolveUserId(suppliedId, null, headerUserId, endpoint);
    }

    private String resolveUserId(String bodyUserId, String queryUserId, String headerUserId, String endpoint) {
        String normalizedId = normalizeUserId(bodyUserId);
        String normalizedQueryId = normalizeUserId(queryUserId);
        String normalizedHeaderId = normalizeUserId(headerUserId);
        if (normalizedId == null && normalizedQueryId == null && normalizedHeaderId == null) {
            logger.warn("{} rejected: missing user id", endpoint);
            throw new IllegalArgumentException("userId is required");
        }
        String resolvedId = normalizedId != null ? normalizedId : normalizedQueryId;
        if (normalizedId != null && normalizedQueryId != null && !normalizedId.equals(normalizedQueryId)
                || resolvedId != null && normalizedHeaderId != null && !resolvedId.equals(normalizedHeaderId)) {
            logger.warn("{} rejected: request user id does not match X-User-Id", endpoint);
            throw new IllegalArgumentException("userId values do not match");
        }
        if (resolvedId != null) {
            return resolvedId;
        }
        return normalizedHeaderId;
    }

    private String normalizeUserId(String userId) {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }

    private ResponseEntity<?> errorResponse(RuntimeException exception) {
        HttpStatus status;
        if (exception instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if ("Course not found".equals(exception.getMessage())
                || "User not found".equals(exception.getMessage())) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
    }
}