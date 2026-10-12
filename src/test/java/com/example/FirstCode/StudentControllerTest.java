package com.example.FirstCode;

import com.example.FirstCode.controller.HealthController;
import com.example.FirstCode.controller.StudentController;
import com.example.FirstCode.dto.StudentRequestDto;
import com.example.FirstCode.exception.GlobalExceptionHandler;
import com.example.FirstCode.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class StudentControllerTest {

    @Autowired
    private StudentController studentController;

    @Autowired
    private HealthController healthController;

    @Autowired
    private StudentService studentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;
    private MockMvc healthMockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        healthMockMvc = MockMvcBuilders.standaloneSetup(healthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/students should return list of student DTOs")
    void testGetAllStudents() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].age", notNullValue()))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/v1/students should create a student and return 201 with DTO")
    void testCreateStudentSuccess() throws Exception {
        StudentRequestDto request = StudentRequestDto.builder()
                .name("Alex Smith")
                .age(24)
                .email("alex.smith@example.com")
                .department("Data Science")
                .build();

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Alex Smith")))
                .andExpect(jsonPath("$.age", is(24)))
                .andExpect(jsonPath("$.email", is("alex.smith@example.com")))
                .andExpect(jsonPath("$.department", is("Data Science")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/v1/students with invalid payload should return 400 Bad Request with field errors")
    void testCreateStudentValidationFailure() throws Exception {
        StudentRequestDto invalidRequest = StudentRequestDto.builder()
                .name("") // Invalid: blank
                .age(10) // Invalid: age < 16
                .email("not-an-email") // Invalid email format
                .department("") // Invalid: blank
                .build();

        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.errors.name", notNullValue()))
                .andExpect(jsonPath("$.errors.age", notNullValue()))
                .andExpect(jsonPath("$.errors.email", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/students/{id} for non-existent student should return 404")
    void testGetStudentNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/students/non-existent-99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("GET /api/v1/health should return UP status and endpoint directory")
    void testHealthEndpoint() throws Exception {
        healthMockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.service", is("Student Management REST Service")))
                .andExpect(jsonPath("$.documentedEndpoints", notNullValue()));
    }
}
