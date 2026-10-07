package com.collablearn.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Course;
import com.collablearn.backend.model.Reply;
import com.collablearn.backend.model.User;
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

    public List<Discussion> findAll(String userId, String courseId) {
        User user = requireUser(userId);
        Set<String> accessibleCourseIds = accessibleCourses(user).stream()
                .map(Course::getId)
                .collect(Collectors.toSet());
        if (courseId != null && !courseId.isBlank()) {
            String requestedCourseId = courseId.trim();
            Course course = courseRepository.findById(requestedCourseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            requireCourseAccess(user, course);
            accessibleCourseIds = Set.of(course.getId());
        }
        Set<String> permittedCourseIds = accessibleCourseIds;
        return discussionRepository.findAll().stream()
                .filter(discussion -> permittedCourseIds.contains(discussionCourseId(discussion)))
                .toList();
    }

    public Discussion create(Discussion discussion, String userId) {
        User author = requireUser(userId);
        String courseId = discussion.getCourseId();
        if (courseId == null || courseId.isBlank()) {
            courseId = discussion.getCourse();
        }
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("courseId is required");
        }
        courseId = courseId.trim();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        requireCourseAccess(author, course);
        if (discussion.getTitle() == null || discussion.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (discussion.getContent() == null || discussion.getContent().isBlank()) {
            throw new IllegalArgumentException("content is required");
        }
        discussion.setCourseId(courseId);
        discussion.setCourse(courseId);
        discussion.setUserId(author.getId());
        discussion.setAuthor(author.getName());
        if (discussion.getReplies() == null) discussion.setReplies(new ArrayList<>());
        return discussionRepository.save(discussion);
    }

    public Discussion findById(String id, String userId) {
        User user = requireUser(userId);
        Discussion discussion = findDiscussion(id);
        String courseId = discussionCourseId(discussion);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        requireCourseAccess(user, course);
        return discussion;
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
        User author = requireUser(userId);
        Discussion discussion = findDiscussion(id);
        Course course = courseRepository.findById(discussionCourseId(discussion))
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        requireCourseAccess(author, course);
        reply.setUserId(author.getId());
        reply.setAuthor(author.getName());
        if (discussion.getReplies() == null) {
            discussion.setReplies(new ArrayList<>());
        }
        discussion.getReplies().add(reply);
        return discussionRepository.save(discussion);
    }

    private User requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userRepository.findById(userId.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private List<Course> accessibleCourses(User user) {
        return switch (user.getRole() == null ? "" : user.getRole().trim().toLowerCase()) {
            case "student" -> courseRepository.findByEnrolledStudentIdsContaining(user.getId());
            case "tutor" -> courseRepository.findByTutorId(user.getId());
            default -> List.of();
        };
    }

    private void requireCourseAccess(User user, Course course) {
        boolean tutorOwnsCourse = "tutor".equals(user.getRole() == null ? "" : user.getRole().trim().toLowerCase())
                && user.getId().equals(course.getTutorId());
        boolean studentEnrolled = "student".equals(user.getRole() == null ? "" : user.getRole().trim().toLowerCase())
                && course.getEnrolledStudentIds() != null
                && course.getEnrolledStudentIds().contains(user.getId());
        if (!tutorOwnsCourse && !studentEnrolled) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have access to this course");
        }
    }

    private Discussion findDiscussion(String id) {
        return discussionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found"));
    }

    private String discussionCourseId(Discussion discussion) {
        return discussion.getCourseId() == null || discussion.getCourseId().isBlank()
                ? discussion.getCourse()
                : discussion.getCourseId();
    }

    private String normalizeUserId(String userId) {
        return userId == null || userId.isBlank() ? null : userId.trim();
    }
}