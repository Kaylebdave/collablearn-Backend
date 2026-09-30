package com.collablearn.backend.repository;

import com.collablearn.backend.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupRepository extends MongoRepository<Group, String> {
}