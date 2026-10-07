package com.collablearn.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;
import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Reply;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.DiscussionRepository;
import com.collablearn.backend.repository.CourseRepository;
import com.collablearn.backend.repository.OtpTokenRepository;
import com.collablearn.backend.repository.UserRepository;
import com.collablearn.backend.service.AuthService;
import com.collablearn.backend.service.DiscussionService;
import com.collablearn.backend.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserDataIsolationTests {

    @Test
    void profileLookupRequiresUserIdInsteadOfEmail() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService, mock(UserRepository.class));

        ResponseEntity<?> response = controller.getProfile(null, "private@example.com", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(authService);
    }

    @Test
    void profileLookupReturnsNotFoundForUnknownUserId() {
        AuthService authService = mock(AuthService.class);
        when(authService.findProfile("unknown-user"))
                .thenThrow(new IllegalArgumentException("User not found"));
        AuthController controller = new AuthController(authService, mock(UserRepository.class));

        ResponseEntity<?> response = controller.getProfile("unknown-user", null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(authService).findProfile("unknown-user");
    }

    @Test
    void replyAuthorIsResolvedFromSuppliedUserId() {
        DiscussionRepository discussionRepository = mock(DiscussionRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setId("user-1");
        user.setName("Current User");
        Discussion discussion = new Discussion();
        discussion.setId("discussion-1");
        discussion.setReplies(new ArrayList<>());
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(discussionRepository.findById("discussion-1")).thenReturn(Optional.of(discussion));
        when(discussionRepository.save(any(Discussion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DiscussionService discussionService = new DiscussionService(
            discussionRepository, userRepository, mock(CourseRepository.class));
        Reply reply = new Reply();
        reply.setUserId(" user-1 ");
        reply.setAuthor("Forged Name");
        reply.setContent("Reply content");

        Discussion saved = discussionService.addReply("discussion-1", reply);

        assertEquals("user-1", saved.getReplies().get(0).getUserId());
        assertEquals("Current User", saved.getReplies().get(0).getAuthor());
        verify(userRepository).findById("user-1");
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void replyWithoutAuthorIsRejected() {
        DiscussionService discussionService = new DiscussionService(
            mock(DiscussionRepository.class), mock(UserRepository.class), mock(CourseRepository.class));
        Reply reply = new Reply();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> discussionService.addReply("discussion-1", reply));

        assertEquals("Reply author is required", exception.getMessage());
    }
}
