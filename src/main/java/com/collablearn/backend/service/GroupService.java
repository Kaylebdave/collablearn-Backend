package com.collablearn.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.UUID;
import com.collablearn.backend.model.Group;
import com.collablearn.backend.model.GroupMember;
import com.collablearn.backend.model.GroupPost;
import com.collablearn.backend.model.User;
import com.collablearn.backend.repository.GroupRepository;
import com.collablearn.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public List<Group> findAll() {
        List<Group> groups = groupRepository.findAll();
        groups.forEach(this::initializeLists);
        return groups;
    }
    public Group create(Group group, String userId) {
        User user = findUser(userId);
        group.setMembers(new ArrayList<>(List.of(new GroupMember(userId, user.getName(), "owner"))));
        if (group.getResources() == null) group.setResources(new ArrayList<>());
        if (group.getPosts() == null) group.setPosts(new ArrayList<>());
        return groupRepository.save(group);
    }
    public Group findById(String id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        initializeLists(group);
        return group;
    }

    public Group join(String groupId, String userId) {
        Group group = findById(groupId);
        if (group.getMembers().stream().anyMatch(member -> userId.equals(member.getMemberId()))) {
            return group;
        }
        User user = findUser(userId);
        group.getMembers().add(new GroupMember(userId, user.getName(), "member"));
        return groupRepository.save(group);
    }

    public GroupPost addPost(String groupId, String content, String userId) {
        Group group = findById(groupId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        GroupPost post = new GroupPost(UUID.randomUUID().toString(), content, userId, user.getName(), Instant.now());
        group.getPosts().add(post);
        groupRepository.save(group);
        return post;
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void initializeLists(Group group) {
        if (group.getMembers() == null) group.setMembers(new ArrayList<>());
        if (group.getResources() == null) group.setResources(new ArrayList<>());
        if (group.getPosts() == null) group.setPosts(new ArrayList<>());
    }
}