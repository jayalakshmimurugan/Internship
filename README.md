# Student Management REST Service

A modern, production-grade Spring Boot microservice exposing RESTful endpoints returning clean JSON responses. Built following enterprise Spring Boot design patterns with strict decoupling between database entities and API Data Transfer Objects (DTOs).

---

## 📋 Core Requirements Covered

- [x] **`@RestController` with mapped routes**: RESTful routes for CRUD operations (`GET`, `POST`, `PUT`, `DELETE`) and health/status checks.
- [x] **DTO rather than an entity in the response**: The internal `@Document` domain entity (`Student`) is completely decoupled from the API layer; clients only receive `StudentResponseDto`.
- [x] **Runs with a single command**: Fully packaged with the Maven wrapper (`mvnw` / `mvnw.cmd`). Can be started immediately with zero pre-configuration.
- [x] **Resilient storage strategy**: Supports MongoDB persistence while providing an automatic in-memory fallback, ensuring the single-command run succeeds even on machines without a running MongoDB daemon.
- [x] **Input validation & Global error handling**: `@Valid` constraints (`@NotBlank`, `@Min`, `@Max`, `@Email`, `@Size`) with centralized `@RestControllerAdvice` returning structured JSON error bodies.

---

## 🏗️ Architecture & Key Design Decisions

### 1. DTO Pattern over Entity Exposure
Directly exposing database entities (like `@Entity` or `@Document`) in controller responses creates tight coupling between internal database schema and external client contracts. This poses serious issues:
- **Security & Data Leakage**: Internal flags, audit fields, and sensitive metadata can accidentally leak.
- **API Versioning Fragility**: Any schema refactoring immediately breaks API clients.
- **Validation**: Incoming requests require distinct validation rules (`StudentRequestDto`) that differ from database constraints.
- **Clear Contract**: `StudentResponseDto` provides a clean, documented JSON schema for consumers.

```
Client  ──(JSON Request)──>  StudentRequestDto
                                    │  @Valid
                                    ▼
                             StudentController
                                    │
                                    ▼
                              StudentService  ◄──►  Student Entity (DB/Memory)
                                    │
                                    ▼
Client  <──(JSON Response)──  StudentResponseDto
```

### 2. Resilient "Zero-Friction" Single-Command Execution
The service is configured with Spring Data MongoDB. However, to guarantee that a reviewer or developer can clone the repository and run it with a **single command** on *any* computer without needing MongoDB installed:
- On startup, the service checks database connectivity with a rapid timeout (`serverSelectionTimeoutMS=2000`).
- If MongoDB is running locally on port `27017`, it seamlessly uses MongoDB.
- If MongoDB is absent or offline, it gracefully falls back to a thread-safe `ConcurrentHashMap` in-memory repository with pre-seeded demo records. No crashing, no hanging, and zero external dependencies required.

---

## 🚀 How to Run with a Single Command

### Prerequisites
- **Java 21** or later (`java -version`)

### Run the Application

#### Linux / macOS:
```bash
./mvnw spring-boot:run
```

#### Windows (PowerShell / Command Prompt):
```cmd
.\mvnw.cmd spring-boot:run
```

The application will start on **port 8081**:
`http://localhost:8081`

---

## 🧪 Running Tests

A complete suite of unit and integration tests using JUnit 5 and Spring `MockMvc` is included:

#### Linux / macOS:
```bash
./mvnw test
```

#### Windows:
```cmd
.\mvnw.cmd test
```

---

## 📡 API Endpoints Documentation

All endpoints produce and consume `application/json`. Both `/api/v1/students` and `/students` paths are mapped for flexibility.

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Service health status & endpoint directory | `200 OK` |
| `GET` | `/api/v1/students` | Retrieve all students | `200 OK` |
| `GET` | `/api/v1/students/{id}` | Retrieve student by ID | `200 OK` / `404 Not Found` |
| `POST` | `/api/v1/students` | Create a new student | `201 Created` / `400 Bad Request` |
| `PUT` | `/api/v1/students/{id}` | Update student by ID | `200 OK` / `400 Bad Request` / `404` |
| `DELETE` | `/api/v1/students/{id}` | Delete student by ID | `200 OK` / `404 Not Found` |

---

## 📝 Request & Response JSON Examples

### 1. Health Check & Directory
**Request:** `GET /api/v1/health`

**Response (`200 OK`):**
```json
{
  "service": "Student Management REST Service",
  "version": "1.0.0",
  "status": "UP",
  "timestamp": "2026-10-12T21:08:34",
  "documentedEndpoints": {
    "GET /api/v1/students": "Retrieve all students (returns List<StudentResponseDto>)",
    "GET /api/v1/students/{id}": "Retrieve single student by ID (returns StudentResponseDto)",
    "POST /api/v1/students": "Create new student (accepts StudentRequestDto, returns StudentResponseDto)",
    "PUT /api/v1/students/{id}": "Update student by ID (accepts StudentRequestDto, returns StudentResponseDto)",
    "DELETE /api/v1/students/{id}": "Delete student by ID",
    "GET /api/v1/health": "Service health check and endpoint directory"
  }
}
```

---

### 2. Get All Students
**Request:** `GET /api/v1/students`

**Response (`200 OK`):**
```json
[
  {
    "id": "std-101",
    "name": "Jayalakshmi Murugan",
    "age": 21,
    "email": "jayalakshmi@example.com",
    "department": "Computer Science",
    "status": "ACTIVE",
    "createdAt": "2026-10-02T20:57:47"
  },
  {
    "id": "std-102",
    "name": "John Doe",
    "age": 22,
    "email": "john.doe@example.com",
    "department": "Information Technology",
    "status": "ACTIVE",
    "createdAt": "2026-10-07T20:57:47"
  }
]
```

---

### 3. Create Student
**Request:** `POST /api/v1/students`

**Request Body (`StudentRequestDto`):**
```json
{
  "name": "Sophia Chen",
  "age": 22,
  "email": "sophia.chen@example.com",
  "department": "Artificial Intelligence"
}
```

**Response (`201 Created`):**
```json
{
  "id": "6accff2203c3ebe8e53e4fdd",
  "name": "Sophia Chen",
  "age": 22,
  "email": "sophia.chen@example.com",
  "department": "Artificial Intelligence",
  "status": "ACTIVE",
  "createdAt": "2026-10-12T21:09:14"
}
```

---

### 4. Get Student by ID
**Request:** `GET /api/v1/students/std-101`

**Response (`200 OK`):**
```json
{
  "id": "std-101",
  "name": "Jayalakshmi Murugan",
  "age": 21,
  "email": "jayalakshmi@example.com",
  "department": "Computer Science",
  "status": "ACTIVE",
  "createdAt": "2026-10-02T20:57:47"
}
```

---

### 5. Update Student
**Request:** `PUT /api/v1/students/std-101`

**Request Body (`StudentRequestDto`):**
```json
{
  "name": "Jayalakshmi Murugan",
  "age": 22,
  "email": "jayalakshmi.updated@example.com",
  "department": "Advanced Data Science"
}
```

**Response (`200 OK`):**
```json
{
  "id": "std-101",
  "name": "Jayalakshmi Murugan",
  "age": 22,
  "email": "jayalakshmi.updated@example.com",
  "department": "Advanced Data Science",
  "status": "ACTIVE",
  "createdAt": "2026-10-12T21:10:00"
}
```

---

### 6. Delete Student
**Request:** `DELETE /api/v1/students/std-102`

**Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Student with ID 'std-102' deleted successfully",
  "deletedId": "std-102"
}
```

---

### 7. Validation Error Handling
**Request:** `POST /api/v1/students` with invalid payload:
```json
{
  "name": "",
  "age": 12,
  "email": "invalid-email",
  "department": ""
}
```

**Response (`400 Bad Request`):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "errors": {
    "name": "Name is required and cannot be empty",
    "age": "Age must be at least 16",
    "email": "Please provide a valid email address",
    "department": "Department is required"
  },
  "timestamp": "2026-10-12T21:11:58"
}
```

---

## 🛠️ Testing with cURL / PowerShell

### PowerShell Examples
```powershell
# 1. Health check
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/health"

# 2. Get all students
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/students"

# 3. Create a student
$newStudent = @{
    name = "Alex Turner"
    age = 23
    email = "alex.turner@example.com"
    department = "Cybersecurity"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/students" -Method Post -Body $newStudent -ContentType "application/json"
```

### cURL Examples
```bash
# 1. Health check
curl -X GET http://localhost:8081/api/v1/health

# 2. Get all students
curl -X GET http://localhost:8081/api/v1/students

# 3. Create a student
curl -X POST http://localhost:8081/api/v1/students \
  -H "Content-Type: application/json" \
  -d '{"name":"Alex Turner","age":23,"email":"alex.turner@example.com","department":"Cybersecurity"}'

# 4. Get student by ID
curl -X GET http://localhost:8081/api/v1/students/std-101

# 5. Delete student
curl -X DELETE http://localhost:8081/api/v1/students/std-101
```
