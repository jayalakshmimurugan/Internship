package com.example.FirstCode.service;

import com.example.FirstCode.dto.DepartmentRequestDto;
import com.example.FirstCode.dto.DepartmentResponseDto;
import com.example.FirstCode.dto.StudentRequestDto;
import com.example.FirstCode.dto.StudentResponseDto;
import com.example.FirstCode.exception.ResourceNotFoundException;
import com.example.FirstCode.model.Department;
import com.example.FirstCode.model.Student;
import com.example.FirstCode.repository.DepartmentRepository;
import com.example.FirstCode.repository.StudentRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class implementing business logic and DTO mappings for Student and Department entities.
 * Backed by Spring Data JPA with a real relational database and managed @ManyToOne / @OneToMany relationship.
 */
@Service
@Transactional
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepo studentRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Constructor injection: dependencies are strictly provided via constructor.
     */
    public StudentService(StudentRepo studentRepository, DepartmentRepository departmentRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Pre-populates the database with initial departments and students on startup.
     */
    @PostConstruct
    public void init() {
        if (departmentRepository.count() == 0) {
            Department cs = departmentRepository.save(Department.builder()
                    .name("Computer Science")
                    .code("CS")
                    .build());

            Department it = departmentRepository.save(Department.builder()
                    .name("Information Technology")
                    .code("IT")
                    .build());

            Department ai = departmentRepository.save(Department.builder()
                    .name("Artificial Intelligence")
                    .code("AI")
                    .build());

            log.info("Initialized default departments: CS, IT, AI.");

            if (studentRepository.count() == 0) {
                studentRepository.save(Student.builder()
                        .name("Jayalakshmi Murugan")
                        .age(21)
                        .email("jayalakshmi@example.com")
                        .department(cs)
                        .createdAt(LocalDateTime.now().minusDays(10))
                        .build());

                studentRepository.save(Student.builder()
                        .name("John Doe")
                        .age(22)
                        .email("john.doe@example.com")
                        .department(it)
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .build());

                log.info("Initialized default students linked to departments via JPA relationship.");
            }
        }
    }

    /**
     * CREATE - Add a new student and associate with their Department.
     */
    public StudentResponseDto addStudent(StudentRequestDto requestDto) {
        Department department = resolveDepartment(requestDto.getDepartment());

        Student student = Student.builder()
                .name(requestDto.getName())
                .age(requestDto.getAge())
                .email(requestDto.getEmail())
                .department(department)
                .createdAt(LocalDateTime.now())
                .build();

        Student saved = studentRepository.save(student);
        log.info("Student created with ID {} in department '{}'", saved.getId(), department.getName());
        return mapToDto(saved);
    }

    /**
     * READ ALL - Retrieve all students as DTOs.
     */
    @Transactional(readOnly = true)
    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * READ ONE - Retrieve student by ID.
     */
    @Transactional(readOnly = true)
    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + id + "' not found"));
        return mapToDto(student);
    }

    /**
     * UPDATE - Update existing student.
     */
    public StudentResponseDto updateStudent(Long id, StudentRequestDto requestDto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + id + "' not found"));

        Department department = resolveDepartment(requestDto.getDepartment());

        student.setName(requestDto.getName());
        student.setAge(requestDto.getAge());
        student.setEmail(requestDto.getEmail());
        student.setDepartment(department);

        Student updated = studentRepository.save(student);
        log.info("Student with ID {} updated", updated.getId());
        return mapToDto(updated);
    }

    /**
     * DELETE - Delete student by ID.
     */
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student with ID '" + id + "' not found");
        }
        studentRepository.deleteById(id);
        log.info("Student with ID {} deleted", id);
    }

    /**
     * Retrieve all departments.
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(d -> DepartmentResponseDto.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .code(d.getCode())
                        .studentCount(d.getStudents() != null ? d.getStudents().size() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Create a new department.
     */
    public DepartmentResponseDto createDepartment(DepartmentRequestDto dto) {
        Department dept = Department.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase())
                .build();
        Department saved = departmentRepository.save(dept);
        return DepartmentResponseDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .code(saved.getCode())
                .studentCount(0)
                .build();
    }

    /**
     * Resolves department by code or name; creates on-the-fly if not found.
     */
    private Department resolveDepartment(String deptNameOrCode) {
        if (deptNameOrCode == null || deptNameOrCode.isBlank()) {
            deptNameOrCode = "General";
        }

        String search = deptNameOrCode.trim();

        return departmentRepository.findByCodeIgnoreCase(search)
                .or(() -> departmentRepository.findByNameIgnoreCase(search))
                .orElseGet(() -> {
                    String code = search.length() <= 4 ? search.toUpperCase() : search.substring(0, 3).toUpperCase();
                    Department newDept = Department.builder()
                            .name(search)
                            .code(code)
                            .build();
                    return departmentRepository.save(newDept);
                });
    }

    /**
     * Entity -> DTO mapping.
     */
    public StudentResponseDto mapToDto(Student student) {
        if (student == null) {
            return null;
        }

        String deptName = (student.getDepartment() != null) ? student.getDepartment().getName() : "General";
        String deptCode = (student.getDepartment() != null) ? student.getDepartment().getCode() : "GEN";

        return StudentResponseDto.builder()
                .id(student.getId())
                .name(student.getName())
                .age(student.getAge())
                .email(student.getEmail())
                .department(deptName)
                .departmentCode(deptCode)
                .status("ACTIVE")
                .createdAt(student.getCreatedAt())
                .build();
    }
}
