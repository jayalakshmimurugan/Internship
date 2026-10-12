package com.example.FirstCode.controller;

import com.example.FirstCode.dto.ApiResponseDto;
import com.example.FirstCode.dto.StudentRequestDto;
import com.example.FirstCode.dto.StudentResponseDto;
import com.example.FirstCode.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing endpoints for Student management.
 * Mapped to both '/api/v1/students' and '/students'.
 *
 * DESIGN PRINCIPLE:
 * This controller exclusively handles HTTP concerns:
 * - Route mappings and HTTP methods (@GetMapping, @PostMapping, etc.)
 * - Request validation trigger via Bean Validation (@Valid)
 * - HTTP Status codes (200 OK, 201 CREATED)
 * - Delegation of all business logic, mappings, and storage to StudentService.
 * - Clean constructor injection.
 */
@RestController
@RequestMapping({"/api/v1/students", "/students"})
public class StudentController {

    private final StudentService service;

    /**
     * Constructor injection: dependencies are explicitly provided via constructor.
     */
    public StudentController(StudentService service) {
        this.service = service;
    }

    /**
     * CREATE - Add a new student.
     * Takes validated StudentRequestDto and returns created StudentResponseDto with HTTP 201.
     */
    @PostMapping
    public ResponseEntity<StudentResponseDto> addStudent(@Valid @RequestBody StudentRequestDto requestDto) {
        StudentResponseDto createdStudent = service.addStudent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    /**
     * READ ALL - Retrieve all students.
     * Returns a JSON array of StudentResponseDto with HTTP 200.
     */
    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getAllStudents() {
        return ResponseEntity.ok(service.getAllStudents());
    }

    /**
     * READ ONE - Retrieve a student by ID.
     * Returns HTTP 200 with StudentResponseDto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable String id) {
        return ResponseEntity.ok(service.getStudentById(id));
    }

    /**
     * UPDATE - Update an existing student by ID.
     * Takes validated StudentRequestDto and returns updated StudentResponseDto with HTTP 200.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable String id,
                                                            @Valid @RequestBody StudentRequestDto requestDto) {
        return ResponseEntity.ok(service.updateStudent(id, requestDto));
    }

    /**
     * DELETE - Delete a student by ID.
     * Returns HTTP 200 with standardized ApiResponseDto confirming deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteStudent(@PathVariable String id) {
        service.deleteStudent(id);
        return ResponseEntity.ok(ApiResponseDto.success("Student with ID '" + id + "' deleted successfully", id));
    }
}
