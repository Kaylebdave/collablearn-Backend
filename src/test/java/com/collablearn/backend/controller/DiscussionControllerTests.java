package com.collablearn.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.collablearn.backend.service.DiscussionService;
import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Reply;
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

    @Test
    void createUsesUserIdFromBody() {
        DiscussionService service = mock(DiscussionService.class);
        Discussion request = new Discussion();
        request.setUserId("student-1");
        request.setCourseId("course-1");
        request.setTitle("Question");
        request.setContent("Question content");
        when(service.create(request, "student-1")).thenReturn(request);
        DiscussionController controller = new DiscussionController(service);

        ResponseEntity<?> response = controller.create(request, null, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(service).create(request, "student-1");
    }

    @Test
    void replyUsesUserIdFromBody() {
        DiscussionService service = mock(DiscussionService.class);
        Reply reply = new Reply();
        reply.setUserId("student-1");
        reply.setContent("Reply content");
        when(service.addReply("discussion-1", reply, "student-1")).thenReturn(new Discussion());
        DiscussionController controller = new DiscussionController(service);

        ResponseEntity<?> response = controller.addReply("discussion-1", reply, null, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(service).addReply("discussion-1", reply, "student-1");
    }
}