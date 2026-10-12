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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests covering the service layer (StudentService) using Mockito.
 * Tests business logic, DTO mapping, validations, and repository interactions in isolation.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepo studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private StudentService studentService;

    private Department sampleDepartment;
    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        sampleDepartment = Department.builder()
                .id(1L)
                .name("Computer Science")
                .code("CS")
                .students(new ArrayList<>())
                .build();

        sampleStudent = Student.builder()
                .id(101L)
                .name("Jayalakshmi Murugan")
                .age(21)
                .email("jayalakshmi@example.com")
                .department(sampleDepartment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("addStudent: successfully creates and maps student with existing department")
    void testAddStudent_Success() {
        StudentRequestDto request = StudentRequestDto.builder()
                .name("Jayalakshmi Murugan")
                .age(21)
                .email("jayalakshmi@example.com")
                .department("CS")
                .build();

        when(departmentRepository.findByCodeIgnoreCase("CS")).thenReturn(Optional.of(sampleDepartment));
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentResponseDto result = studentService.addStudent(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getName()).isEqualTo("Jayalakshmi Murugan");
        assertThat(result.getDepartment()).isEqualTo("Computer Science");
        assertThat(result.getDepartmentCode()).isEqualTo("CS");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("addStudent: auto-creates department when not found and saves student")
    void testAddStudent_AutoCreatesDepartment() {
        StudentRequestDto request = StudentRequestDto.builder()
                .name("David Miller")
                .age(23)
                .email("david@example.com")
                .department("Robotics")
                .build();

        Department newDept = Department.builder()
                .id(5L)
                .name("Robotics")
                .code("ROB")
                .build();

        when(departmentRepository.findByCodeIgnoreCase("Robotics")).thenReturn(Optional.empty());
        when(departmentRepository.findByNameIgnoreCase("Robotics")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(newDept);

        Student savedStudent = Student.builder()
                .id(202L)
                .name("David Miller")
                .age(23)
                .email("david@example.com")
                .department(newDept)
                .createdAt(LocalDateTime.now())
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentResponseDto result = studentService.addStudent(request);

        assertThat(result).isNotNull();
        assertThat(result.getDepartment()).isEqualTo("Robotics");
        verify(departmentRepository, times(1)).save(any(Department.class));
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    @DisplayName("getAllStudents: returns all students mapped to response DTOs")
    void testGetAllStudents_Success() {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));

        List<StudentResponseDto> results = studentService.getAllStudents();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Jayalakshmi Murugan");
        assertThat(results.get(0).getDepartment()).isEqualTo("Computer Science");
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getStudentById: returns student response DTO when ID exists")
    void testGetStudentById_Found() {
        when(studentRepository.findById(101L)).thenReturn(Optional.of(sampleStudent));

        StudentResponseDto result = studentService.getStudentById(101L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101L);
        assertThat(result.getName()).isEqualTo("Jayalakshmi Murugan");
        verify(studentRepository, times(1)).findById(101L);
    }

    @Test
    @DisplayName("getStudentById: throws ResourceNotFoundException when ID is not found")
    void testGetStudentById_NotFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student with ID '999' not found");

        verify(studentRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("updateStudent: updates student fields and department and returns updated DTO")
    void testUpdateStudent_Success() {
        StudentRequestDto updateRequest = StudentRequestDto.builder()
                .name("Jayalakshmi M.")
                .age(22)
                .email("jaya.m@example.com")
                .department("CS")
                .build();

        when(studentRepository.findById(101L)).thenReturn(Optional.of(sampleStudent));
        when(departmentRepository.findByCodeIgnoreCase("CS")).thenReturn(Optional.of(sampleDepartment));
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentResponseDto result = studentService.updateStudent(101L, updateRequest);

        assertThat(result).isNotNull();
        verify(studentRepository, times(1)).save(sampleStudent);
    }

    @Test
    @DisplayName("deleteStudent: deletes student when ID exists")
    void testDeleteStudent_Success() {
        when(studentRepository.existsById(101L)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(101L);

        studentService.deleteStudent(101L);

        verify(studentRepository, times(1)).existsById(101L);
        verify(studentRepository, times(1)).deleteById(101L);
    }

    @Test
    @DisplayName("deleteStudent: throws ResourceNotFoundException when ID does not exist")
    void testDeleteStudent_NotFound() {
        when(studentRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> studentService.deleteStudent(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student with ID '999' not found");

        verify(studentRepository, times(1)).existsById(999L);
        verify(studentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("getAllDepartments: returns department DTOs with student counts")
    void testGetAllDepartments_Success() {
        when(departmentRepository.findAll()).thenReturn(List.of(sampleDepartment));

        List<DepartmentResponseDto> results = studentService.getAllDepartments();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Computer Science");
        assertThat(results.get(0).getCode()).isEqualTo("CS");
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createDepartment: successfully saves and returns department DTO")
    void testCreateDepartment_Success() {
        DepartmentRequestDto request = DepartmentRequestDto.builder()
                .name("Mechanical Engineering")
                .code("MECH")
                .build();

        Department savedDept = Department.builder()
                .id(10L)
                .name("Mechanical Engineering")
                .code("MECH")
                .build();

        when(departmentRepository.save(any(Department.class))).thenReturn(savedDept);

        DepartmentResponseDto result = studentService.createDepartment(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Mechanical Engineering");
        assertThat(result.getCode()).isEqualTo("MECH");
        verify(departmentRepository, times(1)).save(any(Department.class));
    }
}
