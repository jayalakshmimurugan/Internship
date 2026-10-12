package com.example.FirstCode.controller;

import com.example.FirstCode.dto.DepartmentRequestDto;
import com.example.FirstCode.dto.DepartmentResponseDto;
import com.example.FirstCode.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Department management.
 * Highlights the parent entity in the @OneToMany / @ManyToOne JPA relationship.
 */
@RestController
@RequestMapping({"/api/v1/departments", "/departments"})
public class DepartmentController {

    private final StudentService service;

    public DepartmentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        return ResponseEntity.ok(service.getAllDepartments());
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDto> createDepartment(@Valid @RequestBody DepartmentRequestDto requestDto) {
        DepartmentResponseDto created = service.createDepartment(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
