package com.collablearn.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.collablearn.backend.dto.CourseListItem;
import com.collablearn.backend.service.CourseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CourseControllerTests {

    @Test
    void browseAcceptsUserIdFromHeader() {
        CourseService courseService = mock(CourseService.class);
        when(courseService.browseForStudent("student-1")).thenReturn(List.of(
                new CourseListItem("course-1", "DBS201", "Database Systems", "Databases",
                        "tutor-1", "Tutor", 0, 2)));
        CourseController controller = new CourseController(courseService);

        ResponseEntity<?> response = controller.browse(null, "student-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(courseService).browseForStudent("student-1");
    }

    @Test
    void browseRequiresUserIdWhenNeitherQueryNorHeaderIsProvided() {
        CourseService courseService = mock(CourseService.class);
        CourseController controller = new CourseController(courseService);

        ResponseEntity<?> response = controller.browse(null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("userId is required", ((java.util.Map<?, ?>) response.getBody()).get("message"));
        verifyNoInteractions(courseService);
    }

    @Test
    void myCoursesAcceptsUserIdFromQueryOrHeader() {
        CourseService courseService = mock(CourseService.class);
        when(courseService.findAllForUser("student-1")).thenReturn(List.of());
        CourseController controller = new CourseController(courseService);

        ResponseEntity<?> queryResponse = controller.findAll("student-1", null);
        ResponseEntity<?> headerResponse = controller.findAll(null, "student-1");

        assertEquals(HttpStatus.OK, queryResponse.getStatusCode());
        assertEquals(HttpStatus.OK, headerResponse.getStatusCode());
        verify(courseService, org.mockito.Mockito.times(2)).findAllForUser("student-1");
    }

    @Test
    void enrollmentAcceptsQueryUserId() {
        CourseService courseService = mock(CourseService.class);
        CourseController controller = new CourseController(courseService);

        ResponseEntity<?> response = controller.enroll("course-1", null, "student-1", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(courseService).enroll("course-1", "student-1");
    }

    @Test
    void deprecatedEnrolledPathDoesNotFallThroughAsCourseId() {
        CourseController controller = new CourseController(mock(CourseService.class));

        ResponseEntity<?> response = controller.deprecatedEnrolledPath();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Use GET /api/courses with userId for enrolled courses",
                ((java.util.Map<?, ?>) response.getBody()).get("message"));
    }
}
