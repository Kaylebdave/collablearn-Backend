package com.collablearn.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collablearn.backend.dto.CourseListItem;
import com.collablearn.backend.model.Material;
import com.collablearn.backend.service.CourseService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

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

            @Test
            void materialUploadAcceptsPdfAndImageMultipartRequests() throws Exception {
            CourseService courseService = mock(CourseService.class);
            when(courseService.addMaterial(any(), any(), any(MultipartFile.class), any()))
                .thenReturn(new Material("material-1", "Lecture notes", "https://example.test/file", "application/pdf", 3, null));
            MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(courseService)).build();

            mockMvc.perform(multipart("/api/courses/course-1/materials")
                    .file(new MockMultipartFile("file", "notes.pdf", "application/pdf", new byte[]{1, 2, 3}))
                    .param("title", "Lecture notes")
                    .param("userId", "tutor-1"))
                .andExpect(status().isCreated());
            mockMvc.perform(multipart("/api/courses/course-1/materials")
                    .file(new MockMultipartFile("file", "diagram.png", "image/png", new byte[]{1, 2, 3}))
                    .param("title", "Diagram")
                    .param("userId", "tutor-1"))
                .andExpect(status().isCreated());

            verify(courseService, org.mockito.Mockito.times(2))
                .addMaterial(any(), any(), any(MultipartFile.class), any());
            }

            @Test
            void materialUploadReturnsClearErrorsForMissingOrEmptyFile() throws Exception {
            MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CourseController(mock(CourseService.class))).build();

            mockMvc.perform(multipart("/api/courses/course-1/materials")
                    .param("title", "Lecture notes")
                    .param("userId", "tutor-1"))
                .andExpect(status().isBadRequest());
            mockMvc.perform(multipart("/api/courses/course-1/materials")
                    .file(new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]))
                    .param("title", "Lecture notes")
                    .param("userId", "tutor-1"))
                .andExpect(status().isBadRequest());
            }

    @Test
    void courseDetailRequiresUserIdAndMapsAccessDeniedToForbidden() {
        CourseService courseService = mock(CourseService.class);
        CourseController controller = new CourseController(courseService);

        ResponseEntity<?> missingUser = controller.findById("course-1", null, null);
        when(courseService.findById("course-1", "student-2"))
                .thenThrow(new org.springframework.security.access.AccessDeniedException("You do not have access to this course"));
        ResponseEntity<?> denied = controller.findById("course-1", "student-2", null);

        assertEquals(HttpStatus.BAD_REQUEST, missingUser.getStatusCode());
        assertEquals("userId is required", ((java.util.Map<?, ?>) missingUser.getBody()).get("message"));
        assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());
    }
}
