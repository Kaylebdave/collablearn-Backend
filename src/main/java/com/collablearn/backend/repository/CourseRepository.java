package com.collablearn.backend.repository;

import com.collablearn.backend.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {
	java.util.List<Course> findByTutorId(String tutorId);
	java.util.List<Course> findByEnrolledStudentIdsContaining(String studentId);
	java.util.Optional<Course> findByCodeIgnoreCase(String code);
}