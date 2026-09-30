package com.collablearn.backend.service;

import java.util.ArrayList;
import java.util.List;
import com.collablearn.backend.model.Group;
import com.collablearn.backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

    public List<Group> findAll() { return groupRepository.findAll(); }
    public Group create(Group group) {
        if (group.getMembers() == null) group.setMembers(new ArrayList<>());
        if (group.getResources() == null) group.setResources(new ArrayList<>());
        return groupRepository.save(group);
    }
    public Group findById(String id) {
        return groupRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }
}