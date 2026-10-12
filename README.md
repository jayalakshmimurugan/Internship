# Student Management REST Service

A modern, production-grade Spring Boot microservice exposing RESTful endpoints returning clean JSON responses. Built following enterprise Spring Boot design patterns with strict separation of concerns, constructor injection, Jakarta Bean Validation, and centralized exception handling.

---

## 📋 Core Requirements Covered

- [x] **Constructor Injection**: All dependencies in controllers and services are injected exclusively via constructors (`StudentController`, `StudentService`), avoiding fragile field injection.
- [x] **Controller Handles Only HTTP Concerns**: Controllers deal exclusively with routing, HTTP status codes, and delegating requests. Zero business logic or persistence code exists in the controller.
- [x] **Bean Validation on Request Body**: Incoming payloads (`StudentRequestDto`) are validated using Jakarta constraints (`@NotBlank`, `@Min`, `@Max`, `@Email`, `@Size`).
- [x] **Handler Turning Errors into JSON**: `GlobalExceptionHandler` annotated with `@RestControllerAdvice` converts validation failures and exceptions into standardized `ErrorResponseDto` JSON payloads.
- [x] **DTO Pattern over Entity Exposure**: Internal persistence entities (`Student`) are completely decoupled from API contracts; endpoints return `StudentResponseDto`.
- [x] **Runs with a Single Command**: Packaged with Maven Wrapper (`mvnw` / `mvnw.cmd`) and equipped with resilient in-memory fallback so the application runs immediately on any machine.

---

## 🏗️ Architecture & Design Decisions

### 1. HTTP Transport vs Business Logic Isolation
The API controller strictly isolates transport-layer concerns from domain logic:
- **`StudentController`**: Exposes `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, triggers `@Valid` validation, sets HTTP statuses (`200 OK`, `201 CREATED`), and returns DTOs.
- **`StudentService`**: Contains 100% of the business logic, ID generation, entity persistence, cache synchronization, and DTO <-> Entity mappings.
- **`StudentRepo`**: Spring Data repository interface for MongoDB data access.

```
HTTP Client ──(JSON Request)──> StudentController (@Valid trigger)
                                       │
                              (Constructor Injection)
                                       ▼
                                 StudentService  ◄──►  Student Entity (DB/Memory)
                                       │
                              (Returns DTO)
                                       ▼
HTTP Client <──(JSON Response)── StudentController (HTTP Status + DTO)
```

### 2. Constructor Injection
Field injection with `@Autowired` is avoided. Constructor injection ensures:
- Immutability of injected dependencies (`final` fields).
- Ease of unit testing (dependencies can be passed without reflection or Spring test context).
- Clear dependency graphs during application startup.

### 3. Bean Validation & Centralized Error Handling
Requests are validated automatically at the controller boundary:
- Invalid payloads trigger `MethodArgumentNotValidException`.
- `GlobalExceptionHandler` intercepts validation failures and translates them into an HTTP 400 response with a field-by-field error map.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more request fields",
  "errors": {
    "name": "Name is required and cannot be empty",
    "email": "Please provide a valid email address",
    "age": "Age must be at least 16"
  },
  "timestamp": "2026-10-12T22:21:58"
}
```

### 4. Zero-Friction Single-Command Execution
To guarantee that reviewers can clone and run the service without needing a local MongoDB daemon:
- On startup, the service attempts to connect to MongoDB with a short timeout (`serverSelectionTimeoutMS=2000`).
- If MongoDB is available, it uses MongoDB.
- If MongoDB is absent or unreachable, it smoothly falls back to a thread-safe `ConcurrentHashMap` in-memory repository with pre-seeded demo records.

---

## 🚀 How to Run with a Single Command

### Prerequisites
- **Java 21** or later (`java -version`)

### Run the Application

#### Windows (PowerShell / Command Prompt):
```cmd
.\mvnw.cmd spring-boot:run
```

#### Linux / macOS:
```bash
./mvnw spring-boot:run
```

The application will start on **port 8081**:
`http://localhost:8081`

---

## 🧪 Running Tests

A complete suite of unit and integration tests using JUnit 5 and Spring `MockMvc` is included:

```cmd
.\mvnw.cmd test
```

---

## 📡 API Endpoints Documentation

All endpoints produce and consume `application/json`. Both `/api/v1/students` and `/students` routes are supported.

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | Service health status & endpoint directory | `200 OK` |
| `GET` | `/api/v1/students` | Retrieve all students (returns `List<StudentResponseDto>`) | `200 OK` |
| `GET` | `/api/v1/students/{id}` | Retrieve student by ID (returns `StudentResponseDto`) | `200 OK` / `404 Not Found` |
| `POST` | `/api/v1/students` | Create student from validated `StudentRequestDto` | `201 Created` / `400 Bad Request` |
| `PUT` | `/api/v1/students/{id}` | Update student by ID | `200 OK` / `400 Bad Request` / `404` |
| `DELETE` | `/api/v1/students/{id}` | Delete student by ID | `200 OK` / `404 Not Found` |

---

## 📝 Request & Response JSON Examples

### 1. Create Student (Valid Request)
**Request:** `POST /api/v1/students`
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

### 2. Validation Error Handling
**Request:** `POST /api/v1/students` with invalid payload:
```json
{
  "name": "",
  "age": 12,
  "email": "not-an-email",
  "department": ""
}
```

**Response (`400 Bad Request`):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more request fields",
  "errors": {
    "name": "Name is required and cannot be empty",
    "age": "Age must be at least 16",
    "email": "Please provide a valid email address",
    "department": "Department is required"
  },
  "timestamp": "2026-10-12T22:21:58"
}
```

---

### 3. Resource Not Found Error Handling
**Request:** `GET /api/v1/students/unknown-id-999`

**Response (`404 Not Found`):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Student with ID 'unknown-id-999' not found",
  "timestamp": "2026-10-12T22:22:00"
}
```

---

### 4. Delete Student
**Request:** `DELETE /api/v1/students/std-102`

**Response (`200 OK`):**
```json
{
  "success": true,
  "message": "Student with ID 'std-102' deleted successfully",
  "data": "std-102",
  "timestamp": "2026-10-12T22:22:15"
}
```

---

## 🛠️ Testing with cURL / PowerShell

```powershell
# 1. Health check
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/health"

# 2. Get all students
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/students"

# 3. Create a student with validation
$body = @{
    name = "Alex Turner"
    age = 23
    email = "alex.turner@example.com"
    department = "Cybersecurity"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/students" -Method Post -Body $body -ContentType "application/json"
```
