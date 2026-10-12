package com.example.FirstCode.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service Information and Health Check Controller.
 * Provides service metadata and documentation of endpoints as JSON.
 */
@RestController
@RequestMapping({"/api/v1/health", "/api/v1/status", "/health"})
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Student Management REST Service");
        health.put("version", "1.0.0");
        health.put("timestamp", LocalDateTime.now().toString());

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/v1/students", "Retrieve all students (returns List<StudentResponseDto>)");
        endpoints.put("GET /api/v1/students/{id}", "Retrieve single student by ID (returns StudentResponseDto)");
        endpoints.put("POST /api/v1/students", "Create new student (accepts StudentRequestDto, returns StudentResponseDto)");
        endpoints.put("PUT /api/v1/students/{id}", "Update student by ID (accepts StudentRequestDto, returns StudentResponseDto)");
        endpoints.put("DELETE /api/v1/students/{id}", "Delete student by ID");
        endpoints.put("GET /api/v1/health", "Service health check and endpoint directory");

        health.put("documentedEndpoints", endpoints);

        return ResponseEntity.ok(health);
    }
}
