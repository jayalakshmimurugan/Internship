package com.example.FirstCode.repository;

import com.example.FirstCode.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository interface for Student entity.
 */
@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    List<Student> findByDepartmentId(Long departmentId);

    boolean existsByEmail(String email);
}
