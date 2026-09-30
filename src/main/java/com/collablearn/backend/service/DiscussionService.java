package com.collablearn.backend.service;

import java.util.ArrayList;
import java.util.List;
import com.collablearn.backend.model.Discussion;
import com.collablearn.backend.model.Reply;
import com.collablearn.backend.repository.DiscussionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscussionService {
    private final DiscussionRepository discussionRepository;

    public List<Discussion> findAll() { return discussionRepository.findAll(); }
    public Discussion create(Discussion discussion) {
        if (discussion.getReplies() == null) discussion.setReplies(new ArrayList<>());
        return discussionRepository.save(discussion);
    }
    public Discussion findById(String id) {
        return discussionRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Discussion not found"));
    }
    public Discussion addReply(String id, Reply reply) {
        Discussion discussion = findById(id);
        discussion.getReplies().add(reply);
        return discussionRepository.save(discussion);
    }
}