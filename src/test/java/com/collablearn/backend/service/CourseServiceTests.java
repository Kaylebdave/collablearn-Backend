package com.collablearn.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.collablearn.backend.dto.CourseCreateRequest;
import com.collablearn.backend.model.Course;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.CourseRepository;
import com.collablearn.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

class CourseServiceTests {

    @Test
    void studentCourseListUsesOnlyEnrolledCourses() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-1", "student", "Student");
        Course enrolledCourse = course("course-1", "student-1");
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(courseRepository.findByEnrolledStudentIdsContaining("student-1"))
                .thenReturn(List.of(enrolledCourse));
        CourseService service = new CourseService(courseRepository, userRepository, mock(Cloudinary.class));

        var courses = service.findAllForUser("student-1");

        assertEquals(List.of("course-1"), courses.stream().map(item -> item.id()).toList());
        verify(courseRepository).findByEnrolledStudentIdsContaining("student-1");
        verify(courseRepository, never()).findByTutorId(any());
    }

    @Test
    void studentBrowseIncludesCoursesWithEmptyEnrollmentLists() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-1", "student", "Student");
        Course availableCourse = course("course-available", null);
        availableCourse.setCode("DBS201");
        availableCourse.setTitle("Database Systems");
        availableCourse.setDescription("Relational databases");
        availableCourse.setTutorName("Course Tutor");
        Course enrolledCourse = course("course-enrolled", "student-1");
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(courseRepository.findAll()).thenReturn(List.of(availableCourse, enrolledCourse));
        CourseService service = new CourseService(courseRepository, userRepository, mock(Cloudinary.class));

        var browseResults = service.browseForStudent("student-1");

        assertEquals(1, browseResults.size());
        assertEquals("course-available", browseResults.get(0).id());
        assertEquals("DBS201", browseResults.get(0).code());
        assertEquals("Database Systems", browseResults.get(0).title());
        assertEquals("Course Tutor", browseResults.get(0).tutorName());
    }

    @Test
    void tutorCourseListUsesOnlyOwnedCourses() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findById("tutor-1")).thenReturn(Optional.of(user("tutor-1", "tutor", "Tutor")));
        when(courseRepository.findByTutorId("tutor-1")).thenReturn(List.of(course("course-1", null)));
        CourseService service = new CourseService(courseRepository, userRepository, mock(Cloudinary.class));

        var courses = service.findAllForUser("tutor-1");

        assertEquals(List.of("course-1"), courses.stream().map(item -> item.id()).toList());
        verify(courseRepository).findByTutorId("tutor-1");
        verify(courseRepository, never()).findByEnrolledStudentIdsContaining(any());
    }

    @Test
    void createSetsTutorFromPersistedUser() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User tutor = user("tutor-1", "tutor", "Verified Tutor");
        when(userRepository.findById("tutor-1")).thenReturn(Optional.of(tutor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId("course-1");
            return saved;
        });
        CourseCreateRequest request = new CourseCreateRequest();
        request.setCode("BIO101");
        request.setTitle("Biology");
        request.setLecturer("Legacy Lecturer");
        request.setDescription("Introductory biology");
        CourseService service = new CourseService(courseRepository, userRepository, mock(Cloudinary.class));

        var created = service.create(request, "tutor-1");

        assertEquals("tutor-1", created.tutorId());
        assertEquals("Verified Tutor", created.tutorName());
        assertEquals(List.of(), created.enrolledStudentIds());
    }

    @Test
    void enrollmentDoesNotAddDuplicateStudentIds() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User student = user("student-1", "student", "Student");
        Course course = course("course-1", "student-1");
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(userRepository.findAllById(List.of("student-1"))).thenReturn(List.of(student));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        CourseService service = new CourseService(courseRepository, userRepository, mock(Cloudinary.class));

        var enrolled = service.enroll("course-1", "student-1");

        assertEquals(List.of("student-1"), enrolled.enrolledStudentIds());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void tutorCannotUploadMaterialsToAnotherTutorsCourse() {
        CourseRepository courseRepository = mock(CourseRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        Cloudinary cloudinary = mock(Cloudinary.class);
        when(userRepository.findById("tutor-2")).thenReturn(Optional.of(user("tutor-2", "tutor", "Other Tutor")));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course("course-1", null)));
        CourseService service = new CourseService(courseRepository, userRepository, cloudinary);
        MockMultipartFile file = new MockMultipartFile("file", "material.pdf", "application/pdf", new byte[]{1});

        assertThrows(AccessDeniedException.class,
                () -> service.addMaterial("course-1", "Notes", file, "tutor-2"));

        verifyNoInteractions(cloudinary);
    }

    private User user(String id, String role, String name) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setName(name);
        return user;
    }

    private Course course(String id, String enrolledStudentId) {
        Course course = new Course();
        course.setId(id);
        course.setTutorId("tutor-1");
        course.setTutorName("Tutor");
        course.setMaterials(new ArrayList<>());
        course.setEnrolledStudentIds(enrolledStudentId == null ? new ArrayList<>() : new ArrayList<>(List.of(enrolledStudentId)));
        return course;
    }
}