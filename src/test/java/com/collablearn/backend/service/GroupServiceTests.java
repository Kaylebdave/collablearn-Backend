package com.collablearn.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collablearn.backend.model.Group;
import com.collablearn.backend.model.GroupPost;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.GroupRepository;
import com.collablearn.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GroupServiceTests {

    @Test
    void addPostStoresAuthorAndAppendsToGroup() {
        GroupRepository groupRepository = mock(GroupRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        Group group = new Group();
        group.setId("group-1");
        group.setPosts(null);
        User user = new User();
        user.setId("user-1");
        user.setName("Verified Name");
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GroupService service = new GroupService(groupRepository, userRepository);

        GroupPost post = service.addPost("group-1", "hi", "user-1");

        assertEquals("hi", post.getContent());
        assertEquals("user-1", post.getUserId());
        assertEquals("Verified Name", post.getAuthor());
        assertNotNull(post.getId());
        assertNotNull(post.getCreatedAt());
        assertEquals(new ArrayList<>(java.util.List.of(post)), group.getPosts());
        verify(groupRepository).save(group);
    }

    @Test
    void findByIdInitializesPostsForLegacyGroup() {
        GroupRepository groupRepository = mock(GroupRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        Group group = new Group();
        group.setPosts(null);
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group));
        GroupService service = new GroupService(groupRepository, userRepository);

        assertEquals(java.util.List.of(), service.findById("group-1").getPosts());
    }
}