package com.collablearn.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.collablearn.backend.model.Course;
import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.CourseRepository;
import com.collablearn.backend.repository.DiscussionRepository;
import com.collablearn.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DiscussionServiceTests {

    @Test
    void userWithNoDiscussionsGetsEmptyList() {
        DiscussionRepository discussionRepository = mock(DiscussionRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-1", "student");
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(courseRepository.findByEnrolledStudentIdsContaining("student-1")).thenReturn(List.of());
        when(discussionRepository.findAll()).thenReturn(List.of());
        DiscussionService service = new DiscussionService(discussionRepository, userRepository, courseRepository);

        assertEquals(List.of(), service.findAll("student-1", null));
    }

    @Test
    void courseFilterDoesNotMixDiscussionsFromOtherCourses() {
        DiscussionRepository discussionRepository = mock(DiscussionRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-1", "student");
        Course course = course("course-1", "student-1");
        Discussion matching = discussion("discussion-1", "course-1");
        Discussion otherCourse = discussion("discussion-2", "course-2");
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(discussionRepository.findAll()).thenReturn(List.of(matching, otherCourse));
        DiscussionService service = new DiscussionService(discussionRepository, userRepository, courseRepository);

        List<Discussion> results = service.findAll("student-1", "course-1");

        assertEquals(List.of("discussion-1"), results.stream().map(Discussion::getId).toList());
    }

    @Test
    void createUsesStoredAuthorNameAndCourseScope() {
        DiscussionRepository discussionRepository = mock(DiscussionRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-1", "student");
        student.setName("Verified Student");
        Course course = course("course-1", "student-1");
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(courseRepository.findByEnrolledStudentIdsContaining("student-1")).thenReturn(List.of(course));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(discussionRepository.save(any(Discussion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DiscussionService service = new DiscussionService(discussionRepository, userRepository, courseRepository);
        Discussion request = new Discussion();
        request.setTitle("Question");
        request.setContent("How does indexing work?");
        request.setAuthor("Forged author");
        request.setCourseId("course-1");

        Discussion created = service.create(request, "student-1");

        assertEquals("student-1", created.getUserId());
        assertEquals("Verified Student", created.getAuthor());
        assertEquals("course-1", created.getCourseId());
    }

    @Test
    void createRejectsUserWithoutCourseAccess() {
        DiscussionRepository discussionRepository = mock(DiscussionRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-2", "student");
        when(userRepository.findById("student-2")).thenReturn(Optional.of(student));
        when(courseRepository.findByEnrolledStudentIdsContaining("student-2")).thenReturn(List.of());
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("course-1", "student-1")));
        DiscussionService service = new DiscussionService(discussionRepository, userRepository, courseRepository);
        Discussion request = new Discussion();
        request.setTitle("Question");
        request.setContent("Content");
        request.setCourseId("course-1");

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.create(request, "student-2"));
    }

    private User user(String id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private Course course(String id, String studentId) {
        Course course = new Course();
        course.setId(id);
        course.setTutorId("tutor-1");
        course.setEnrolledStudentIds(List.of(studentId));
        return course;
    }

    private Discussion discussion(String id, String courseId) {
        Discussion discussion = new Discussion();
        discussion.setId(id);
        discussion.setCourseId(courseId);
        return discussion;
    }
}