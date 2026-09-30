package com.collablearn.backend.repository;

import com.collablearn.backend.model.Discussion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiscussionRepository extends MongoRepository<Discussion, String> {
}