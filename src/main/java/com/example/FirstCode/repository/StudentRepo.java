package com.example.FirstCode.repository;

import com.example.FirstCode.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for Student entity.
 */
@Repository
public interface StudentRepo extends MongoRepository<Student, String> {

    Optional<Student> findByEmail(String email);
}
