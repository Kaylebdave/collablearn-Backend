package com.collablearn.backend.controller;

import com.collablearn.backend.dto.GroupPostRequest;
import com.collablearn.backend.dto.GroupPostResponse;
import com.collablearn.backend.model.Group;
import com.collablearn.backend.model.GroupPost;
import com.collablearn.backend.service.GroupService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping
    public List<Group> findAll() { return groupService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        try { return ResponseEntity.ok(groupService.findById(id)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping
    public ResponseEntity<Group> create(@RequestBody Group group) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(group));
    }

    @PostMapping({"/{id}/posts", "/{id}/messages"})
    public ResponseEntity<?> addPost(
            @PathVariable String id,
            @RequestBody GroupPostRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId
    ) {
        String userId = normalizeUserId(request == null ? null : request.userId());
        String authenticatedUserId = normalizeUserId(headerUserId);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
        }
        if (authenticatedUserId == null || !userId.equals(authenticatedUserId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId does not match X-User-Id"));
        }
        if (request.content() == null || request.content().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "content is required"));
        }
        try {
            GroupPost post = groupService.addPost(id, request.content(), userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new GroupPostResponse(
                    post.getId(), id, post.getContent(), post.getUserId(), post.getAuthor(), post.getCreatedAt()));
        } catch (IllegalArgumentException exception) {
            HttpStatus status = "Group not found".equals(exception.getMessage())
                    || "User not found".equals(exception.getMessage())
                    ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
        }
    }

    private String normalizeUserId(String userId) {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }
}