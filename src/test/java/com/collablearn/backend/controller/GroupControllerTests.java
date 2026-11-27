package com.collablearn.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.collablearn.backend.dto.GroupCreateRequest;
import com.collablearn.backend.dto.GroupJoinRequest;
import com.collablearn.backend.dto.GroupPostRequest;
import com.collablearn.backend.dto.GroupPostResponse;
import com.collablearn.backend.model.Group;
import com.collablearn.backend.model.GroupPost;
import com.collablearn.backend.service.GroupService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GroupControllerTests {

    @Test
    void createRequiresMatchingUserIdAndReturnsCreatedGroup() {
        GroupService service = mock(GroupService.class);
        Group group = new Group();
        when(service.create(org.mockito.ArgumentMatchers.any(Group.class), org.mockito.ArgumentMatchers.eq("user-1")))
                .thenReturn(group);
        GroupController controller = new GroupController(service);

        ResponseEntity<?> response = controller.create(
                new GroupCreateRequest("Study", "Math", "Description", "user-1"), "user-1");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(group, response.getBody());
    }

    @Test
    void joinRejectsMismatchedUserId() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);

        ResponseEntity<?> response = controller.join("group-1", new GroupJoinRequest("user-1"), "user-2");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void joinReturnsNotFoundWhenGroupDoesNotExist() {
        GroupService service = mock(GroupService.class);
        doThrow(new IllegalArgumentException("Group not found")).when(service).join("group-1", "user-1");
        GroupController controller = new GroupController(service);

        ResponseEntity<?> response = controller.join("group-1", new GroupJoinRequest("user-1"), "user-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addPostReturnsCreatedPostWithGroupId() {
        GroupService service = mock(GroupService.class);
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        GroupPost post = new GroupPost("post-1", "hi", "user-1", "Student", createdAt);
        when(service.addPost("group-1", "hi", "user-1")).thenReturn(post);
        GroupController controller = new GroupController(service);

        ResponseEntity<?> response = controller.addPost("group-1", new GroupPostRequest("hi", "user-1"), "user-1");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(new GroupPostResponse("post-1", "group-1", "hi", "user-1", "Student", createdAt),
                response.getBody());
    }

    @Test
    void addPostRejectsMismatchedHeaderUserId() {
        GroupService service = mock(GroupService.class);
        GroupController controller = new GroupController(service);

        ResponseEntity<?> response = controller.addPost("group-1", new GroupPostRequest("hi", "user-1"), "user-2");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void findByIdReturnsGroupWithPosts() {
        GroupService service = mock(GroupService.class);
        Group group = new Group();
        group.setPosts(List.of(new GroupPost("post-1", "hi", "user-1", "Student", Instant.now())));
        when(service.findById("group-1")).thenReturn(group);
        GroupController controller = new GroupController(service);

        ResponseEntity<?> response = controller.findById("group-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(group, response.getBody());
        assertEquals(1, ((Group) response.getBody()).getPosts().size());
    }
}