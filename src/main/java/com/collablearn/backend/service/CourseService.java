package com.collablearn.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.collablearn.backend.dto.CourseCreateRequest;
import com.collablearn.backend.dto.CourseDetailsResponse;
import com.collablearn.backend.dto.CourseListItem;
import com.collablearn.backend.dto.EnrolledStudentResponse;
import com.collablearn.backend.model.Course;
import com.collablearn.backend.model.Material;
import com.collablearn.backend.model.User;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.collablearn.backend.repository.CourseRepository;
import com.collablearn.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;

    public List<CourseListItem> findAllForUser(String userId) {
        User user = requireUser(userId);
        List<Course> courses = switch (normalizeRole(user.getRole())) {
            case "student" -> courseRepository.findByEnrolledStudentIdsContaining(user.getId());
            case "tutor" -> findTutorCourses(user.getId());
            default -> throw new AccessDeniedException("Only students and tutors can view courses");
        };
        return courses.stream().map(this::toListItem).toList();
    }

    private List<Course> findTutorCourses(String tutorId) {
        List<Course> courses = courseRepository.findByTutorId(tutorId);
        logger.info("Tutor course list query tutorId={} resultCount={}", tutorId, courses.size());
        return courses;
    }

    public List<CourseListItem> browseForStudent(String userId) {
        User student = requireRole(userId, "student", "Only students can browse courses");
        return courseRepository.findAll().stream()
                .filter(course -> !studentIds(course).contains(student.getId()))
                .map(this::toListItem)
                .toList();
    }

    public CourseDetailsResponse create(CourseCreateRequest request, String tutorId) {
        User tutor = requireRole(tutorId, "tutor", "Only tutors can create courses");
        if (tutor.getId() == null || tutor.getId().isBlank()) {
            throw new IllegalArgumentException("Tutor user id is required");
        }
        Course course = new Course();
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setLecturer(request.getLecturer());
        course.setDescription(request.getDescription());
        course.setTutorId(tutor.getId());
        course.setTutorName(tutor.getName());
        course.setEnrolledStudentIds(new ArrayList<>());
        ensureMaterialsList(course);
        Course saved = courseRepository.save(course);
        logger.info("Created course id={} saved tutorId={}", saved.getId(), saved.getTutorId());
        return toDetails(saved);
    }

    public CourseDetailsResponse findById(String id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        ensureMaterialsList(course);
        ensureStudentIds(course);
        return toDetails(course);
    }

    public CourseDetailsResponse enroll(String courseId, String studentId) {
        User student = requireRole(studentId, "student", "Only students can enroll in courses");
        Course course = findCourse(courseId);
        ensureStudentIds(course);
        if (!course.getEnrolledStudentIds().contains(student.getId())) {
            course.getEnrolledStudentIds().add(student.getId());
            courseRepository.save(course);
        }
        return toDetails(course);
    }

    private void ensureMaterialsList(Course course) {
        if (course.getMaterials() == null) {
            course.setMaterials(new ArrayList<>());
        }
    }

    public Material addMaterial(String courseId, String title, MultipartFile file, String tutorId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Material title is required");
        }

        Course course = findCourse(courseId);
        requireRole(tutorId, "tutor", "Only tutors can upload course materials");
        if (!tutorId.trim().equals(course.getTutorId())) {
            throw new AccessDeniedException("Tutors can only upload materials to their own courses");
        }
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

    private Course findCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        ensureMaterialsList(course);
        ensureStudentIds(course);
        return course;
    }

    private User requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
        return userRepository.findById(userId.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private User requireRole(String userId, String role, String message) {
        User user = requireUser(userId);
        if (!role.equals(normalizeRole(user.getRole()))) {
            throw new AccessDeniedException(message);
        }
        return user;
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toLowerCase();
    }

    private List<String> studentIds(Course course) {
        return course.getEnrolledStudentIds() == null ? List.of() : course.getEnrolledStudentIds();
    }

    private void ensureStudentIds(Course course) {
        if (course.getEnrolledStudentIds() == null) {
            course.setEnrolledStudentIds(new ArrayList<>());
        }
    }

    private CourseListItem toListItem(Course course) {
        List<String> students = studentIds(course);
        List<Material> materials = course.getMaterials() == null ? List.of() : course.getMaterials();
        return new CourseListItem(course.getId(), course.getCode(), course.getTitle(), course.getDescription(),
                course.getTutorId(), course.getTutorName(), students.size(), materials.size());
    }

    private CourseDetailsResponse toDetails(Course course) {
        List<String> studentIds = studentIds(course);
        List<EnrolledStudentResponse> students = userRepository.findAllById(studentIds).stream()
                .map(student -> new EnrolledStudentResponse(student.getId(), student.getName(), student.getRole()))
                .toList();
        List<Material> materials = course.getMaterials() == null ? List.of() : course.getMaterials();
        return new CourseDetailsResponse(course.getId(), course.getCode(), course.getTitle(), course.getDescription(),
                course.getLecturer(), course.getTutorId(), course.getTutorName(), List.copyOf(studentIds), students,
                materials, studentIds.size(), materials.size());
    }
}