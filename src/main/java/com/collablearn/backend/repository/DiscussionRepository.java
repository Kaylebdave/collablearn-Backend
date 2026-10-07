package com.collablearn.backend.repository;

import com.collablearn.backend.model.Discussion;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiscussionRepository extends MongoRepository<Discussion, String> {
	List<Discussion> findByCourseId(String courseId);
	List<Discussion> findByCourse(String course);
}