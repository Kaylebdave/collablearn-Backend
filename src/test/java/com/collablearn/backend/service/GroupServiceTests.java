package com.collablearn.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collablearn.backend.model.Group;
import com.collablearn.backend.model.GroupPost;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.GroupRepository;
import com.collablearn.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GroupServiceTests {

    @Test
    void createStoresCreatorAsOwner() {
        GroupRepository groupRepository = mock(GroupRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setName("Owner Name");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GroupService service = new GroupService(groupRepository, userRepository);
        Group group = new Group();

        Group created = service.create(group, "user-1");

        assertEquals(List.of(new com.collablearn.backend.model.GroupMember("user-1", "Owner Name", "owner")),
                created.getMembers());
        verify(groupRepository).save(group);
    }

    @Test
    void joinAddsAnotherMemberAndIsIdempotent() {
        GroupRepository groupRepository = mock(GroupRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        Group group = new Group();
        group.setId("group-1");
        group.setMembers(new ArrayList<>(List.of(
                new com.collablearn.backend.model.GroupMember("owner-1", "Owner Name", "owner"))));
        User user = new User();
        user.setName("Member Name");
        when(groupRepository.findById("group-1")).thenReturn(Optional.of(group));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(user));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        GroupService service = new GroupService(groupRepository, userRepository);

        Group joined = service.join("group-1", "user-2");
        Group joinedAgain = service.join("group-1", "user-2");

        assertEquals(2, joined.getMembers().size());
        assertEquals(new com.collablearn.backend.model.GroupMember("user-2", "Member Name", "member"),
                joined.getMembers().get(1));
        assertEquals(2, joinedAgain.getMembers().size());
        verify(groupRepository).save(group);
        verify(userRepository).findById("user-2");
        verify(userRepository, never()).findById("owner-1");
    }

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