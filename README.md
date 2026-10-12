# Secured Student & Department JPA Management REST Service

A containerized, production-grade Spring Boot microservice backed by **Spring Data JPA** and secured with **Spring Security**. Features bidirectional relational entity mapping (`@OneToMany` / `@ManyToOne`), automated DDL schema management, comprehensive service layer unit testing with Mockito, and a multi-stage Docker containerization pipeline.

---

## 📋 Core Requirements Covered

- [x] **Spring Security Protecting the Routes**:
  - Stateless HTTP Basic Authentication with role-based access control (`ROLE_USER`, `ROLE_ADMIN`).
  - Public access granted to health check endpoints (`/api/v1/health`).
  - Protected routes (`/api/v1/students/**`, `/api/v1/departments/**`) require authentication.
  - Custom JSON entry point returning `401 Unauthorized` and `403 Forbidden` error payloads.
- [x] **Comprehensive Tests Covering the Service Layer**:
  - `StudentServiceTest`: 10 isolated unit tests with Mockito covering all business logic, department resolution, DTO mapping, and exception throwing.
  - `SecurityIntegrationTest`: 5 integration tests verifying route protection, 401 unauthenticated, 403 unauthorized, and admin privileges.
  - Total test suite: 24 automated tests passing (`BUILD SUCCESS`).
- [x] **Dockerfile that Builds and Runs Anywhere**:
  - Multi-stage Dockerfile: Stage 1 builds the production executable JAR using Eclipse Temurin JDK 21; Stage 2 runs it in a minimal, secure non-root JRE 21 container.
- [x] **JPA Entity Relationship & DDL Configuration**:
  - `Department` (`@OneToMany`) and `Student` (`@ManyToOne`) with foreign key constraints and automated schema migration via `spring.jpa.hibernate.ddl-auto=update`.

---

## 🔐 Security Configuration & Credentials

### Default User Accounts

| Username | Password | Roles | Permissions |
| :--- | :--- | :--- | :--- |
| `user` | `user123` | `ROLE_USER` | Can read data (`GET /api/v1/students`, `GET /api/v1/departments`) |
| `admin` | `admin123` | `ROLE_ADMIN`, `ROLE_USER` | Full access (`GET`, `POST`, `PUT`, `DELETE`) |

### Route Protection Rules

- **Public (No Auth Required)**: `GET /api/v1/health`, `/health`, `/api/v1/status`, `/h2-console/**`
- **Read Operations (`USER` or `ADMIN`)**: `GET /api/v1/students/**`, `GET /api/v1/departments/**`
- **Write Operations (`ADMIN` only)**: `POST /api/v1/students/**`, `PUT /api/v1/students/**`, `DELETE /api/v1/students/**`, `POST /api/v1/departments/**`

---

## 🐳 Running with Docker (Containerized)

The repository includes a multi-stage `Dockerfile` that compiles, packages, and runs the application inside a lightweight container.

### 1. Build the Docker Image
```bash
docker build -t student-management-api .
```

### 2. Run the Container
```bash
docker run -p 8081:8081 --name student-service student-management-api
```

The service is accessible at `http://localhost:8081`.

---

## 🚀 Running Locally with a Single Command

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

---

## 🧪 Running Automated Tests

Run the full test suite (24 tests covering Service, Controller, and Security layers):

```cmd
.\mvnw.cmd test
```

---

## 📡 API Endpoints Documentation

| Method | Endpoint | Access Required | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/health` | **Public** | Service health status & endpoint directory |
| `GET` | `/api/v1/departments` | `USER` or `ADMIN` | Retrieve all departments with student counts |
| `POST` | `/api/v1/departments` | `ADMIN` | Create a new department |
| `GET` | `/api/v1/students` | `USER` or `ADMIN` | Retrieve all students linked to departments |
| `GET` | `/api/v1/students/{id}` | `USER` or `ADMIN` | Retrieve student by ID |
| `POST` | `/api/v1/students` | `ADMIN` | Create student and link with Department |
| `PUT` | `/api/v1/students/{id}` | `ADMIN` | Update student or change department |
| `DELETE`| `/api/v1/students/{id}` | `ADMIN` | Delete student by ID |

---

## 📝 Request & Response JSON Examples

### 1. Create Student (Requires ADMIN credentials)
**Request:** `POST /api/v1/students` *(HTTP Basic Auth: admin / admin123)*
```json
{
  "name": "Sophia Chen",
  "age": 22,
  "email": "sophia.chen@example.com",
  "department": "AI"
}
```

**Response (`201 Created`):**
```json
{
  "id": 3,
  "name": "Sophia Chen",
  "age": 22,
  "email": "sophia.chen@example.com",
  "department": "Artificial Intelligence",
  "departmentCode": "AI",
  "status": "ACTIVE",
  "createdAt": "2026-10-12T22:34:23"
}
```

---

### 2. Unauthenticated Access (Security Rejection)
**Request:** `GET /api/v1/students` *(Without Credentials)*

**Response (`401 Unauthorized`):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource: Unauthorized",
  "timestamp": "2026-10-12T22:42:00"
}
```

---

### 3. Insufficient Permissions (USER attempting mutation)
**Request:** `DELETE /api/v1/students/1` *(HTTP Basic Auth: user / user123)*

**Response (`403 Forbidden`):**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: insufficient permissions to perform this operation",
  "timestamp": "2026-10-12T22:42:15"
}
```

---

## 🛠️ Testing with cURL / PowerShell

### PowerShell Examples
```powershell
# 1. Public Health Check
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/health"

# 2. Get Students (using USER credentials)
$userCreds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("user:user123"))
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/students" -Headers @{ Authorization = "Basic $userCreds" }

# 3. Create Student (using ADMIN credentials)
$adminCreds = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("admin:admin123"))
$newStudent = @{
    name = "Alex Turner"
    age = 23
    email = "alex.turner@example.com"
    department = "CS"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/v1/students" -Method Post -Body $newStudent -ContentType "application/json" -Headers @{ Authorization = "Basic $adminCreds" }
```

### cURL Examples
```bash
# 1. Public Health Check
curl -X GET http://localhost:8081/api/v1/health

# 2. Get Students with USER auth
curl -u user:user123 -X GET http://localhost:8081/api/v1/students

# 3. Create Student with ADMIN auth
curl -u admin:admin123 -X POST http://localhost:8081/api/v1/students \
  -H "Content-Type: application/json" \
  -d '{"name":"Alex Turner","age":23,"email":"alex.turner@example.com","department":"CS"}'
```
