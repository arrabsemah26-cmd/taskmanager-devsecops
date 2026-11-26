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

The image is based on `eclipse-temurin:17-jre-alpine` (official, minimal, JRE only) and runs the application as a non-root user (`spring`).

Build the jar, then the image:

```bash
mvn clean package
docker build -t taskmanager:1.0 .
```

Run the container (host port 9090 mapped to container port 8080):

```bash
docker run -d --name taskmanager -p 9090:8080 taskmanager:1.0
```

Check it:

```bash
curl http://localhost:9090/actuator/health
docker exec taskmanager whoami   # prints: spring
```

Stop and remove it:

```bash
docker stop taskmanager && docker rm taskmanager
```

## Kubernetes deployment

Manifests are in the `k8s/` folder: a Deployment (1 replica, liveness/readiness probes on the Actuator endpoints, CPU/memory requests and limits, non-root security context) and a NodePort Service on port 30080.

Start Minikube and load the image (until the pipeline pushes to Docker Hub):

```bash
minikube start --driver=docker --cpus=2 --memory=3072
minikube image load taskmanager:1.0
```

Deploy and check:

```bash
kubectl apply -f k8s/
kubectl rollout status deployment/taskmanager
kubectl get pods
curl http://$(minikube ip):30080/actuator/health
```

Note: the application uses an in-memory H2 database, so it runs with a single replica and data is lost when the pod restarts.

## CI/CD pipeline

_To be completed (Phases 5 and 6)._

## Security reports

_To be completed: SonarQube, Dependency-Check, Trivy, OWASP ZAP._
