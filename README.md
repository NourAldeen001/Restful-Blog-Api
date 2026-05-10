# Restful Blog API

A production-ready RESTful API built with Spring Boot, featuring JWT authentication, role-based access control, pagination, full-text search, and a global exception handling strategy.

---

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.5**
- **Spring Security** — stateless JWT-based authentication
- **Spring Data JPA / Hibernate** — ORM with JPA Auditing for auto-timestamps
- **MySQL** — relational database
- **JJWT 0.12.3** — JWT generation and validation
- **Springdoc OpenAPI 3** — interactive Swagger UI
- **Lombok** — boilerplate reduction
- **Maven** — dependency management and build

---

## Features

- **JWT Authentication** — register and login return a signed Bearer token; all protected endpoints validate it via a custom filter
- **Role-Based Access Control** — `USER` and `ADMIN` roles enforced at both the route and method level (`@PreAuthorize`)
- **Post Management** — full CRUD with ownership checks (only the author or an ADMIN can update/delete)
- **Comment Management** — nested under posts, with comment-to-post validation and ownership enforcement
- **Pagination & Sorting** — all list endpoints support `page`, `size`, `sortBy`, and `sortDir` query parameters
- **Full-Text Search** — posts support keyword search across title and content; admin user list supports search by username or email
- **Global Exception Handling** — `@RestControllerAdvice` maps every domain exception to a structured JSON error response with the correct HTTP status code
- **Input Validation** — all request bodies use Bean Validation (`@NotBlank`, `@Size`, `@Email`)
- **DTO Pattern** — entities are never exposed directly; custom mappers convert between layers
- **JPA Auditing** — `createdAt` and `updatedAt` fields are managed automatically

---

## Project Structure

```
src/main/java/com/master/Restful_Blog_Api/
├── config/          # SecurityConfig, JwtConfig
├── controller/      # AuthController, PostRestController, CommentRestController, AdminController
├── dto/             # Request/Response DTOs, ApiResponse, PagedResponse, ErrorResponseDTO
├── entity/          # User, Post, Comment, Role
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── mapper/          # PostMapper, CommentMapper, UserMapper
├── repository/      # JPA repositories with custom JPQL queries
├── security/        # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
└── service/         # Service interfaces + implementations, AuthorizationService
```

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8+
- Maven 3.9+

### 1. Clone the repository

```bash
git clone https://github.com/NourAldeen001/Restful-Blog-Api.git
cd Restful-Blog-Api
```

### 2. Create the database

```sql
CREATE DATABASE blog;
```

Then import the schema:

```bash
mysql -u your_username -p blog < blog.sql
```

### 3. Configure the application

Open `src/main/resources/application.properties` and set your database credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/blog
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

Set your JWT secret as an environment variable (minimum 32 characters for HMAC-SHA256):

```bash
# Linux / macOS
export JWT_SECRET=your-very-long-secret-key-at-least-32-chars

# Windows (Command Prompt)
set JWT_SECRET=your-very-long-secret-key-at-least-32-chars
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

---

## API Documentation

Interactive Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login and receive a JWT token |

**Register — request body:**
```json
{
  "username": "nour",
  "email": "nour@example.com",
  "password": "password123"
}
```

**Login — response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "nour",
  "email": "nour@example.com",
  "role": "USER"
}
```

Use the token in subsequent requests:
```
Authorization: Bearer <token>
```

---

### Posts

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/posts` | Public | Get all posts (paginated) |
| GET | `/api/posts/{id}` | Public | Get a single post by ID |
| POST | `/api/posts` | Authenticated | Create a new post |
| PUT | `/api/posts/{id}` | Author or ADMIN | Update a post |
| DELETE | `/api/posts/{id}` | Author or ADMIN | Delete a post |

**Pagination & search parameters:**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number (zero-based) |
| `size` | `10` | Items per page |
| `sortBy` | `createdAt` | Field to sort by |
| `sortDir` | `desc` | Sort direction: `asc` or `desc` |
| `search` | — | Keyword search across title and content |

**Example — get posts with search:**
```
GET /api/posts?page=0&size=5&sortBy=createdAt&sortDir=desc&search=spring
```

**Create post — request body:**
```json
{
  "title": "Getting Started with Spring Boot",
  "content": "Spring Boot makes it easy to create stand-alone, production-grade applications..."
}
```

**Paginated response:**
```json
{
  "content": ["..."],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false,
  "empty": false
}
```

---

### Comments

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/posts/{postId}/comments` | Public | Get all comments for a post (paginated) |
| GET | `/api/posts/{postId}/comments/{commentId}` | Public | Get a single comment |
| POST | `/api/posts/{postId}/comments` | Authenticated | Add a comment to a post |
| PUT | `/api/posts/{postId}/comments/{commentId}` | Author or ADMIN | Update a comment |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | Author or ADMIN | Delete a comment |

**Add comment — request body:**
```json
{
  "content": "Great article! Very helpful explanation."
}
```

---

### Admin

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/admin/users` | ADMIN only | List all users (paginated, searchable) |
| GET | `/api/admin/users/{id}` | ADMIN only | Get a user by ID |
| DELETE | `/api/admin/users/{id}` | ADMIN only | Delete a user |

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "status": 404,
  "message": "Post not found with id: 99",
  "path": "/api/posts/99",
  "timestamp": "2025-05-11T14:30:00"
}
```

Validation errors include a field-level breakdown:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "content": "Content must be at least 10 characters"
  },
  "path": "/api/posts",
  "timestamp": "2025-05-11T14:30:00"
}
```

---

## Security Model

- Public endpoints: `GET /api/posts/**`, `GET /api/posts/*/comments/**`, `POST /api/auth/**`, Swagger UI
- All other endpoints require a valid JWT token in the `Authorization: Bearer <token>` header
- Tokens expire after **24 hours**
- Ownership is enforced at the service layer: users can only modify their own posts and comments; ADMINs can modify any resource

---

## Author

**Nour Eldin Elhanouney**
- GitHub: [@NourAldeen001](https://github.com/NourAldeen001)
- LinkedIn: [noureldin-elhanouney](https://www.linkedin.com/in/noureldin-elhanouney/)