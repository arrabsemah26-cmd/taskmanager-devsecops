# Task Manager - DevSecOps Mini-Project

A simple task management REST API built with Java 17 and Spring Boot, delivered through a secure CI/CD pipeline (Jenkins, SonarQube, OWASP Dependency-Check, Trivy, Docker, Kubernetes, OWASP ZAP).

## Architecture

_To be completed: pipeline diagram and component description._

## Prerequisites

- Ubuntu / Linux
- Java 17, Maven 3.9+, Git
- Docker, Minikube, kubectl (later phases)
- Jenkins, SonarQube (later phases)

## Run the application locally

```bash
mvn spring-boot:run
```

The API listens on port 8080 by default. To use another port:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

## API endpoints

| Method | Endpoint            | Description        | Success |
|--------|---------------------|--------------------|---------|
| GET    | /api/tasks          | List all tasks     | 200     |
| GET    | /api/tasks/{id}     | Get one task       | 200     |
| POST   | /api/tasks          | Create a task      | 201     |
| PUT    | /api/tasks/{id}     | Update a task      | 200     |
| DELETE | /api/tasks/{id}     | Delete a task      | 204     |
| GET    | /actuator/health    | Health check       | 200     |

Errors: `400` for invalid input (blank title), `404` for an unknown task id.

Example:

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn DevSecOps","description":"Finish the mini-project"}'
```

## Run the tests

```bash
mvn clean test
```

## Docker

_To be completed (Phase 3)._

## Kubernetes deployment

_To be completed (Phase 4)._

## CI/CD pipeline

_To be completed (Phases 5 and 6)._

## Security reports

_To be completed: SonarQube, Dependency-Check, Trivy, OWASP ZAP._
