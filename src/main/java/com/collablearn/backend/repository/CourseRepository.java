package com.collablearn.backend.repository;

import com.collablearn.backend.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {
}