package com.collablearn.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.collablearn.backend.service.DiscussionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class DiscussionControllerTests {

    @Test
    void getDiscussionsReturnsEmptyListForAuthorizedUserWithNoThreads() {
        DiscussionService service = mock(DiscussionService.class);
        when(service.findAll("student-1", "course-1")).thenReturn(List.of());
        DiscussionController controller = new DiscussionController(service);

        ResponseEntity<?> response = controller.findAll("course-1", "student-1", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(), response.getBody());
        verify(service).findAll("student-1", "course-1");
    }

    @Test
    void getDiscussionsRequiresUserId() {
        DiscussionService service = mock(DiscussionService.class);
        DiscussionController controller = new DiscussionController(service);

        ResponseEntity<?> response = controller.findAll("course-1", null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("userId is required", ((java.util.Map<?, ?>) response.getBody()).get("message"));
        verifyNoInteractions(service);
    }
}