package com.example.FirstCode.service;

import com.example.FirstCode.dto.StudentRequestDto;
import com.example.FirstCode.dto.StudentResponseDto;
import com.example.FirstCode.exception.ResourceNotFoundException;
import com.example.FirstCode.model.Student;
import com.example.FirstCode.repository.StudentRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service class handling business logic and DTO <-> Entity mappings for Students.
 * Implements a resilient storage pattern: uses MongoDB when reachable, with seamless
 * in-memory fallback to guarantee "runs with a single command" across any environment.
 */
@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepo repository;

    // Resilient in-memory store used as fallback or local storage
    private final ConcurrentHashMap<String, Student> inMemoryStore = new ConcurrentHashMap<>();

    @Autowired
    public StudentService(StudentRepo repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        // Seed default demo records in memory to ensure ready-to-test JSON endpoints
        Student defaultStudent1 = Student.builder()
                .id("std-101")
                .name("Jayalakshmi Murugan")
                .age(21)
                .email("jayalakshmi@example.com")
                .department("Computer Science")
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        Student defaultStudent2 = Student.builder()
                .id("std-102")
                .name("John Doe")
                .age(22)
                .email("john.doe@example.com")
                .department("Information Technology")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        inMemoryStore.put(defaultStudent1.getId(), defaultStudent1);
        inMemoryStore.put(defaultStudent2.getId(), defaultStudent2);

        // Try syncing with MongoDB if available
        try {
            if (repository.count() == 0) {
                repository.save(defaultStudent1);
                repository.save(defaultStudent2);
                log.info("Initialized MongoDB with default demo students.");
            }
        } catch (Exception ex) {
            log.warn("MongoDB is currently offline or unreachable ({}). In-Memory storage active.", ex.getMessage());
        }
    }

    /**
     * Create a new student from request DTO and return response DTO.
     */
    public StudentResponseDto addStudent(StudentRequestDto requestDto) {
        Student student = mapToEntity(requestDto);
        student.setCreatedAt(LocalDateTime.now());

        // Try persisting to MongoDB
        try {
            Student savedMongo = repository.save(student);
            inMemoryStore.put(savedMongo.getId(), savedMongo);
            log.info("Student created in MongoDB with ID: {}", savedMongo.getId());
            return mapToDto(savedMongo);
        } catch (Exception ex) {
            log.warn("MongoDB save failed ({}), persisting to in-memory store.", ex.getMessage());
            if (student.getId() == null || student.getId().isBlank()) {
                student.setId("std-" + UUID.randomUUID().toString().substring(0, 8));
            }
            inMemoryStore.put(student.getId(), student);
            return mapToDto(student);
        }
    }

    /**
     * Retrieve all students as a list of response DTOs.
     */
    public List<StudentResponseDto> getAllStudents() {
        try {
            List<Student> mongoStudents = repository.findAll();
            if (mongoStudents != null && !mongoStudents.isEmpty()) {
                // Update in-memory cache
                mongoStudents.forEach(s -> inMemoryStore.put(s.getId(), s));
                return mongoStudents.stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList());
            }
        } catch (Exception ex) {
            log.warn("MongoDB fetch failed ({}), retrieving from in-memory store.", ex.getMessage());
        }

        return inMemoryStore.values().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve student by ID as a response DTO.
     */
    public StudentResponseDto getStudentById(String id) {
        Student student = null;

        // Try MongoDB first
        try {
            student = repository.findById(id).orElse(null);
        } catch (Exception ex) {
            log.warn("MongoDB findById failed ({}). Checking in-memory store.", ex.getMessage());
        }

        // Fallback to in-memory store
        if (student == null) {
            student = inMemoryStore.get(id);
        }

        if (student == null) {
            throw new ResourceNotFoundException("Student with ID '" + id + "' not found");
        }

        return mapToDto(student);
    }

    /**
     * Update an existing student and return updated response DTO.
     */
    public StudentResponseDto updateStudent(String id, StudentRequestDto requestDto) {
        // Ensure student exists
        getStudentById(id);

        Student student = mapToEntity(requestDto);
        student.setId(id);
        student.setCreatedAt(LocalDateTime.now());

        try {
            Student updatedMongo = repository.save(student);
            inMemoryStore.put(updatedMongo.getId(), updatedMongo);
            log.info("Student with ID '{}' updated in MongoDB.", id);
            return mapToDto(updatedMongo);
        } catch (Exception ex) {
            log.warn("MongoDB update failed ({}), updating in-memory store.", ex.getMessage());
            inMemoryStore.put(id, student);
            return mapToDto(student);
        }
    }

    /**
     * Delete student by ID.
     */
    public void deleteStudent(String id) {
        // Ensure student exists before deletion
        getStudentById(id);

        try {
            repository.deleteById(id);
            log.info("Student with ID '{}' deleted from MongoDB.", id);
        } catch (Exception ex) {
            log.warn("MongoDB delete failed ({}). Removing from in-memory store.", ex.getMessage());
        }

        inMemoryStore.remove(id);
    }

    /**
     * Entity -> DTO mapping logic.
     * Decouples domain model from API response structure.
     */
    public StudentResponseDto mapToDto(Student student) {
        if (student == null) {
            return null;
        }
        return StudentResponseDto.builder()
                .id(student.getId())
                .name(student.getName())
                .age(student.getAge())
                .email(student.getEmail() != null ? student.getEmail() : "N/A")
                .department(student.getDepartment() != null ? student.getDepartment() : "General")
                .status("ACTIVE")
                .createdAt(student.getCreatedAt() != null ? student.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    /**
     * DTO -> Entity mapping logic.
     */
    public Student mapToEntity(StudentRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Student.builder()
                .name(dto.getName())
                .age(dto.getAge())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .build();
    }
}
