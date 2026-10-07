package com.collablearn.backend.service;

import java.util.ArrayList;
import java.util.List;
import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Reply;
import com.collablearn.backend.repository.DiscussionRepository;
import com.collablearn.backend.repository.CourseRepository;
import com.collablearn.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    private final DiscussionRepository discussionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<Discussion> findAll(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId is required");
        }
        List<Discussion> discussions = new ArrayList<>(discussionRepository.findByCourseId(courseId.trim()));
        discussionRepository.findByCourse(courseId.trim()).stream()
                .filter(discussion -> discussions.stream().noneMatch(existing -> existing.getId().equals(discussion.getId())))
                .forEach(discussions::add);
        return discussions;
    }

    public Discussion create(Discussion discussion) {
        String courseId = discussion.getCourseId();
        if (courseId == null || courseId.isBlank()) {
            courseId = discussion.getCourse();
        }
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId is required");
        }
        courseId = courseId.trim();
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found");
        }
        discussion.setCourseId(courseId);
        discussion.setCourse(courseId);
        if (discussion.getReplies() == null) discussion.setReplies(new ArrayList<>());
        return discussionRepository.save(discussion);
    }
    public Discussion findById(String id) {
        return discussionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Discussion not found"));
    }
    public Discussion addReply(String id, Reply reply) {
        return addReply(id, reply, reply.getUserId());
    }

    public Discussion addReply(String id, Reply reply, String headerUserId) {
        String bodyUserId = normalizeUserId(reply.getUserId());
        String normalizedHeaderUserId = normalizeUserId(headerUserId);
        if (bodyUserId != null && normalizedHeaderUserId != null && !bodyUserId.equals(normalizedHeaderUserId)) {
            throw new IllegalArgumentException("userId does not match X-User-Id");
        }
        String userId = bodyUserId == null ? normalizedHeaderUserId : bodyUserId;
        if (userId == null) {
            throw new IllegalArgumentException("Reply author is required");
        }
        UserAuthor author = userRepository.findById(userId)
                .map(user -> new UserAuthor(user.getId(), user.getName()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        reply.setUserId(author.id());
        reply.setAuthor(author.name());
        Discussion discussion = findById(id);
        if (discussion.getReplies() == null) {
            discussion.setReplies(new ArrayList<>());
        }
        discussion.getReplies().add(reply);
        return discussionRepository.save(discussion);
    }

    private String normalizeUserId(String userId) {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }

    private record UserAuthor(String id, String name) { }
}