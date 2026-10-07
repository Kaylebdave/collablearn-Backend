package com.collablearn.backend.controller;

import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Reply;
import com.collablearn.backend.service.DiscussionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<Discussion> findAll() { return discussionService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {
        try { return ResponseEntity.ok(discussionService.findById(id)); }
        catch (IllegalArgumentException exception) { return ResponseEntity.notFound().build(); }
    }

    @PostMapping
    public ResponseEntity<Discussion> create(@RequestBody Discussion discussion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discussionService.create(discussion));
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<?> addReply(@PathVariable String id, @RequestBody Reply reply) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(discussionService.addReply(id, reply)); }
        catch (IllegalArgumentException exception) {
            if ("User not found".equals(exception.getMessage())) {
                logger.warn("POST /api/discussions/{}/replies rejected: invalid user id {}", id, reply.getUserId());
                return ResponseEntity.notFound().build();
            }
            if ("Reply author is required".equals(exception.getMessage())) {
                return ResponseEntity.badRequest().body(exception.getMessage());
            }
            return ResponseEntity.notFound().build();
        }
    }
}