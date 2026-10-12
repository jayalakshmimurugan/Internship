package com.example.FirstCode.controller;

import com.example.FirstCode.dto.StudentRequestDto;
import com.example.FirstCode.dto.StudentResponseDto;
import com.example.FirstCode.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing endpoints for Student management.
 * Mapped to both '/api/v1/students' and '/students'.
 * Crucially, ALL endpoints return DTOs (StudentResponseDto) rather than
 * domain entities, strictly adhering to API design best practices.
 */
@RestController
@RequestMapping({"/api/v1/students", "/students"})
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    /**
     * CREATE - Add a new student.
     * Takes validated StudentRequestDto and returns created StudentResponseDto.
     */
    @PostMapping
    public ResponseEntity<StudentResponseDto> addStudent(@Valid @RequestBody StudentRequestDto requestDto) {
        StudentResponseDto createdStudent = service.addStudent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    /**
     * READ ALL - Retrieve all students.
     * Returns a JSON array of StudentResponseDto.
     */
    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        List<StudentResponseDto> students = service.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * READ ONE - Retrieve a student by ID.
     * Returns 200 with StudentResponseDto or 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable String id) {
        StudentResponseDto student = service.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    /**
     * UPDATE - Update an existing student by ID.
     * Takes validated StudentRequestDto and returns updated StudentResponseDto.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable String id,
                                                            @Valid @RequestBody StudentRequestDto requestDto) {
        StudentResponseDto updatedStudent = service.updateStudent(id, requestDto);
        return ResponseEntity.ok(updatedStudent);
    }

    /**
     * DELETE - Delete a student by ID.
     * Returns 200 with confirmation JSON.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable String id) {
        service.deleteStudent(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Student with ID '" + id + "' deleted successfully");
        response.put("deletedId", id);

        return ResponseEntity.ok(response);
    }
}
