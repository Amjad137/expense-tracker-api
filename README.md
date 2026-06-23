# Expense Tracker API

A production-style REST API for managing personal income, expenses, categories, budgets, recurring expenses, dashboards, and monthly reports.

Built with Spring Boot, PostgreSQL, JWT authentication, Flyway migrations, and Docker.

## Features

* JWT authentication with access and refresh tokens
* User registration, login, refresh-token rotation, and logout
* Role support: `USER` and `ADMIN`
* System and user-created categories
* Expense and income CRUD operations
* Soft deletion support
* Monthly budgets and category budgets
* Budget status tracking: `OK`, `WARNING`, and `EXCEEDED`
* Recurring expense definitions and processing endpoint
* Dashboard summaries
* Monthly income, expense, net-balance, and category reports
* Pagination, sorting, validation, global exception handling, and standardized API responses
* Swagger/OpenAPI documentation
* Flyway database migrations
* Docker Compose local setup

---

## Tech Stack

| Area                  | Technology                  |
| --------------------- | --------------------------- |
| Language              | Java                        |
| Framework             | Spring Boot                 |
| Security              | Spring Security + JWT       |
| Database              | PostgreSQL                  |
| ORM                   | Spring Data JPA / Hibernate |
| Database Migrations   | Flyway                      |
| API Documentation     | Springdoc OpenAPI / Swagger |
| Build Tool            | Maven                       |
| Containerization      | Docker + Docker Compose     |
| Validation            | Jakarta Bean Validation     |
| Boilerplate Reduction | Lombok                      |

---

## Project Structure

```text
src/main/java/me/amjath/expense_tracker_api
├── auth/        # Registration, login, refresh tokens, logout
├── audit/       # JPA audit support
├── budget/      # Monthly and category budgets
├── category/    # System and user-created categories
├── common/      # Shared API response and pagination classes
├── config/      # Application, auditing, and OpenAPI configuration
├── dashboard/   # Dashboard summary endpoints
├── exception/   # Global exception handling
├── expense/     # Expense management
├── income/      # Income management
├── recurring/   # Recurring expense definitions and processing
├── report/      # Monthly and category reports
├── security/    # JWT filter, JWT service, security configuration
├── seeder/      # Initial admin seeding
└── user/        # User profile and user-related functionality
```

---

## Prerequisites

Install the following before starting:

* Java version configured in `pom.xml`
* Docker Desktop
* Git

Check your Java version:

```bash
java -version
```

Check Docker:

```bash
docker --version
docker compose version
```

> The Dockerfile currently uses Java 21. Ensure the Java version in `pom.xml`, Docker build image, and Docker runtime image are compatible.

---

# Quick Start With Docker

This is the easiest way to run the API and PostgreSQL together.

## 1. Clone the Repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd expense-tracker-api
```

## 2. Create Your Environment File

Create a file named `.env` in the project root.

```env
DB_NAME=expense_tracker_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432

SERVER_PORT=8080

JWT_SECRET=ZXhwZW5zZVRyYWNrZXJTZWNyZXRLZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

SPRING_PROFILES_ACTIVE=prod
```

### Important Security Note

The example `JWT_SECRET` is only for local development.

For a real deployment, generate and use a long, secure Base64-encoded secret. Never commit real passwords, database credentials, or JWT secrets to Git.

## 3. Start the Application

```bash
docker compose up --build
```

Docker will:

```text
1. Start PostgreSQL
2. Create the configured database
3. Build the Spring Boot API image
4. Start the API after PostgreSQL becomes healthy
```

## 4. Verify the Application

Open:

```text
http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

## 5. Open Swagger UI

Open:

```text
http://localhost:8080/swagger-ui/index.html
```

Use Swagger UI to inspect and test available API endpoints.

## Stop Containers

```bash
docker compose down
```

## Stop Containers and Delete Database Data

```bash
docker compose down -v
```

> Warning: `-v` removes the PostgreSQL Docker volume and deletes all local database data.

---

# Running Without Docker

You can also run PostgreSQL locally and start Spring Boot directly from IntelliJ or Maven.

## 1. Create a PostgreSQL Database

Create a database named:

```text
expense_tracker_db
```

Example SQL:

```sql
CREATE DATABASE expense_tracker_db;
```

## 2. Configure Environment Variables

Set these in your IDE run configuration or operating system:

```text
DB_URL=jdbc:postgresql://localhost:5432/expense_tracker_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

JWT_SECRET=your-base64-encoded-secret
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

SPRING_PROFILES_ACTIVE=dev
```

For IntelliJ IDEA:

```text
Run
→ Edit Configurations
→ Environment variables
```

## 3. Run With Maven

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

Or run the main class:

```text
ExpenseTrackerApiApplication.java
```

from IntelliJ IDEA.

---

# Configuration

The application reads sensitive values from environment variables.

| Variable                 | Purpose                                | Example                                               |
| ------------------------ | -------------------------------------- | ----------------------------------------------------- |
| `DB_URL`                 | PostgreSQL JDBC connection URL         | `jdbc:postgresql://localhost:5432/expense_tracker_db` |
| `DB_USERNAME`            | Database username                      | `postgres`                                            |
| `DB_PASSWORD`            | Database password                      | `postgres`                                            |
| `SERVER_PORT`            | API port                               | `8080`                                                |
| `JWT_SECRET`             | Base64 JWT signing secret              | `your-secure-secret`                                  |
| `JWT_ACCESS_EXPIRATION`  | Access token lifetime in milliseconds  | `900000`                                              |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifetime in milliseconds | `604800000`                                           |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile                  | `dev` or `prod`                                       |

## Local Docker Database URL

When running through Docker Compose, the API connects to PostgreSQL using:

```text
jdbc:postgresql://postgres:5432/expense_tracker_db
```

`postgres` is the Docker Compose service name.

## Local Non-Docker Database URL

When running Spring Boot directly from IntelliJ or Maven, use:

```text
jdbc:postgresql://localhost:5432/expense_tracker_db
```

---

# Database Migrations

Flyway manages database schema migrations.

When the application starts, Flyway automatically applies migration files from:

```text
src/main/resources/db/migration
```

Migration files generally follow this pattern:

```text
V1__initial_schema.sql
V2__add_refresh_tokens.sql
V3__add_categories.sql
```

Do not modify an already-applied migration in a shared environment. Create a new migration instead.

---

# Authentication Flow

The API uses JWT authentication with access and refresh tokens.

```text
Login
  ↓
Access token + refresh token returned
  ↓
Frontend sends access token in Authorization header
  ↓
API validates token for protected endpoints
  ↓
When access token expires, use refresh token endpoint
```

Protected requests should include:

```http
Authorization: Bearer <access_token>
```

Example:

```http
GET /api/expenses
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

# Example API Flow

## Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "firstName": "Amjath",
  "lastName": "Husain",
  "email": "amjath@example.com",
  "password": "SecurePassword123!",
  "deviceInfo": "Chrome on Windows"
}
```

## Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "amjath@example.com",
  "password": "SecurePassword123!",
  "deviceInfo": "Chrome on Windows"
}
```

## Create an Expense

```http
POST /api/expenses
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "categoryId": "YOUR_CATEGORY_UUID",
  "title": "Lunch",
  "description": "Lunch with colleagues",
  "amount": 1500.00,
  "expenseDate": "2026-06-23"
}
```

## Get Monthly Report

```http
GET /api/reports/monthly?year=2026&month=6
Authorization: Bearer <access_token>
```

The report includes:

```text
Total income
Total expense
Net balance
Expense breakdown by category
Income breakdown by category
Category transaction counts
Category percentage of the total
```

---

# Standard API Response Format

Successful responses follow this shape:

```json
{
  "success": true,
  "message": "Request completed successfully",
  "data": {}
}
```

Failed responses follow a similar consistent structure:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null
}
```

---

# Health Check

The application exposes Spring Boot Actuator health information.

```text
GET /actuator/health
```

Example response:

```json
{
  "status": "UP"
}
```

This endpoint is also used by Docker Compose health checks.

---

# Production Deployment Notes

The Docker Compose configuration is intended mainly for local development.

For production deployment:

```text
Cloud Run
→ runs the Spring Boot API container

Managed PostgreSQL service
→ stores application data
```

Recommended database options:

```text
Cloud SQL PostgreSQL
or
Supabase PostgreSQL
```

Do not deploy PostgreSQL inside the same Cloud Run container as the API.

For production:

* Store secrets in a secret manager.
* Use a managed PostgreSQL database.
* Use strong database passwords.
* Use a secure, long JWT signing secret.
* Set `SPRING_PROFILES_ACTIVE=prod`.
* Configure CORS only for approved frontend origins.
* Enable backups for the database.
* Avoid exposing PostgreSQL publicly unless strictly required.

---

# Useful Commands

## Build the JAR

Linux/macOS:

```bash
./mvnw clean package
```

Windows:

```bat
mvnw.cmd clean package
```

## Run Tests

Linux/macOS:

```bash
./mvnw test
```

Windows:

```bat
mvnw.cmd test
```

## Build and Run Docker Containers

```bash
docker compose up --build
```

## View Container Logs

```bash
docker compose logs -f api
```

```bash
docker compose logs -f postgres
```

## View Running Containers

```bash
docker compose ps
```

---

# Troubleshooting

## API Cannot Connect to PostgreSQL

Check whether PostgreSQL is running:

```bash
docker compose ps
```

Check database logs:

```bash
docker compose logs postgres
```

When running with Docker Compose, ensure your JDBC host is:

```text
postgres
```

not:

```text
localhost
```

Correct Docker JDBC URL:

```text
jdbc:postgresql://postgres:5432/expense_tracker_db
```

## Port 8080 Is Already in Use

Change the port in `.env`:

```env
SERVER_PORT=8081
```

Then start Docker again:

```bash
docker compose up --build
```

Access the API at:

```text
http://localhost:8081
```

## Port 5432 Is Already in Use

Change the external database port in `.env`:

```env
DB_PORT=5433
```

The API container will still connect internally to PostgreSQL on port `5432`.

Your local PostgreSQL client would connect using:

```text
localhost:5433
```

## Reset Local Database

```bash
docker compose down -v
docker compose up --build
```

This permanently removes local Docker database data and starts with a fresh database.

---

# License

This project is intended for learning, portfolio, and personal expense-management purposes.

Add your preferred license before publishing or distributing the project.
