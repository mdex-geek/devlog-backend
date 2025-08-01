
# DevLog - Project & Task Manager

DevLog is a full-featured backend application built with **Spring Boot** and **PostgreSQL**, designed to help developers and teams track projects, tasks, comments, subtasks, and more. It includes secure authentication, email verification via OTP, refresh token handling, and role-based access control.

## Table of Contents

* [Features](#features)
* [Tech Stack](#tech-stack)
* [Getting Started](#getting-started)
* [Running with Docker](#running-with-docker)
* [API Overview](#api-overview)
* [Authentication Flow](#authentication-flow)
* [Refresh Token Flow](#refresh-token-flow)
* [Email Verification](#email-verification)
* [Future Enhancements](#future-enhancements)

---

## Features

* User registration and login with **JWT authentication**
* **Refresh token** support with device tracking
* **Email verification via OTP**
* Role-based access control
* Create and manage **Projects**
* Add members to a project
* Create, assign, and track **Tasks** with:

  * Priority and status filters
  * Subtasks and parent-child relationships
  * Task history logs
  * Commenting system
* Basic search and filtering on tasks
* **Security best practices** implemented via Spring Security
* PostgreSQL integration with **Docker Compose**

---

## Tech Stack

* Java 21
* Spring Boot 3.x
* Spring Security
* Spring Data JPA
* PostgreSQL (Dockerized)
* JWT (JSON Web Token) - `jjwt 0.12.6`
* Lombok
* Swagger/OpenAPI (for API documentation)
* Logback/SLF4J
* Mail API (JavaMailSender)

---

## Getting Started

### Prerequisites

* Java 21
* Maven
* Docker & Docker Compose

### Clone the Repository

```bash
git clone https://github.com/mdex-geek/devlog-backend.git
cd devlog-backend
```

---

## Running with Docker

Make sure Docker is running and then execute:

```bash
docker-compose up -d
```

This will start a PostgreSQL container with default credentials:

* Username: `devlog`
* Password: `devlog123`
* Database: `devlogdb`

Update `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/devlogdb
spring.datasource.username=devlog
spring.datasource.password=devlog123
```

Then run your Spring Boot application via:

```bash
./mvnw spring-boot:run
```

---

## API Overview

Once the app is running, Swagger is available at:

```
http://localhost:8080/swagger-ui/index.html
```

Or import the `api-docs.json` file into Postman.

---

## Authentication Flow

1. **Register**
   `POST /api/v1/auth/register`
   Payload:

   ```json
   {
     "username": "testuser",
     "password": "testpass",
     "email": "user@example.com"
   }
   ```

2. **OTP Verification**
   After registration, an OTP is sent to your email.
   `POST /api/v1/auth/verify-otp`

   ```json
   {
     "otp": "123456"
   }
   ```

3. **Login**
   `POST /api/v1/auth/login`

   ```json
   {
     "username": "testuser",
     "password": "testpass"
   }
   ```

   Response:

   ```json
   {
     "accessToken": "JWT_ACCESS_TOKEN",
     "refreshToken": "REFRESH_TOKEN"
   }
   ```

   You can store these in Postman environment variables.

---

## Refresh Token Flow

To get a new access token when the old one expires:

`POST /api/v1/auth/refresh`

```json
{
  "refreshToken": "{{refresh_token}}"
}
```

The response will contain a new access token.

---

## Email Verification

Email is verified through an OTP mechanism:

* OTP is stored in a temporary table `email_verification_token`
* Expired OTPs are cleaned automatically
* After verification, the user's status is updated from `UNVERIFIED` to `ACTIVE`

---

## Role and Account Status

Each user has:

* `username` (unique)
* `email` (unique)
* `status`: `UNVERIFIED`, `ACTIVE`, or `BANNED`
* `isVerified` and `isBanned` flags

---

## Project and Task Management

* Users can create projects
* Invite other users as project members
* Add tasks to projects
* Assign tasks to users
* Subtasks (via parentTask)
* Task filters: by status, priority, assignee
* Add comments to tasks
* Audit history of task status/priority changes

---

## Future Enhancements

* File attachments on tasks
* In-app notification system
* Admin dashboard for managing users
* Role-specific permissions (Admin, Manager, Developer)
* Global search across projects and tasks
* Deployment on cloud (Render, Railway, AWS)

---

## Contributing

Feel free to fork this repository and open a PR. For larger changes, raise an issue first.

---

## License

This project is licensed under the MIT License.

