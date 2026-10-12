package com.example.FirstCode.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for outgoing student responses.
 * Returned by @RestController endpoints to decouple external API contract
 * from database/persistence entity structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseDto {

    private String id;
    private String name;
    private int age;
    private String email;
    private String department;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
