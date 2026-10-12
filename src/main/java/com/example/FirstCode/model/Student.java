package com.example.FirstCode.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Domain entity representing a Student.
 * Internal persistence model mapped to MongoDB (or stored in-memory).
 * This entity is NEVER exposed directly through REST controllers;
 * it is mapped to StudentResponseDto before being returned to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "student")
public class Student {

    @Id
    private String id;

    private String name;

    private int age;

    private String email;

    private String department;

    private LocalDateTime createdAt;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
        this.createdAt = LocalDateTime.now();
    }

    public Student(String name, int age, String email, String department) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.department = department;
        this.createdAt = LocalDateTime.now();
    }
}
