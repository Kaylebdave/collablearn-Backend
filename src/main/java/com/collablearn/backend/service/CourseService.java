package com.collablearn.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.collablearn.backend.dto.CourseCreateRequest;
import com.collablearn.backend.model.Course;
import com.collablearn.backend.model.Material;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.collablearn.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final Cloudinary cloudinary;

    public List<Course> findAll() {
        List<Course> courses = courseRepository.findAll();
        courses.forEach(this::ensureMaterialsList);
        return courses;
    }

    public Course create(CourseCreateRequest request) {
        Course course = new Course();
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setLecturer(request.getLecturer());
        course.setDescription(request.getDescription());
        ensureMaterialsList(course);
        return courseRepository.save(course);
    }

    public Course findById(String id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        ensureMaterialsList(course);
        return course;
    }

    private void ensureMaterialsList(Course course) {
        if (course.getMaterials() == null) {
            course.setMaterials(new ArrayList<>());
        }
    }

    public Material addMaterial(String courseId, String title, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Material title is required");
        }

        Course course = findById(courseId);
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "collablearn/courses/" + courseId,
                "resource_type", "auto"
            )
        );

        Material material = new Material(
            java.util.UUID.randomUUID().toString(),
            title.trim(),
            uploadResult.get("secure_url").toString(),
            file.getContentType() == null ? "application/octet-stream" : file.getContentType(),
            file.getSize(),
            Instant.now()
        );
        course.getMaterials().add(material);
        course.setMaterialsCount(course.getMaterials().size());
        courseRepository.save(course);
        return material;
    }
}