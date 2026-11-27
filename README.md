# Task Manager - DevSecOps mini-project

Spring Boot REST API for task management, delivered through a secured CI/CD pipeline
(Jenkins, SonarQube, OWASP Dependency-Check, Docker, Trivy, Docker Hub, Minikube, OWASP ZAP).

## Architecture

~~~
GitHub (develop) -> Jenkins (poll every 2 min)
  1. Checkout
  2. Build and unit tests (Maven)
  3. SonarQube analysis + Quality Gate
  4. OWASP Dependency-Check (dependencies)
  5. Docker build
  6. Trivy image scan
  7. Publish to Docker Hub
  8. Deploy to Minikube (Kubernetes)
  9. OWASP ZAP baseline scan (deployed app)
~~~

## Application

REST API on `/api/tasks` (create, list, get, update, delete). Input validation with
Bean Validation, H2 in-memory database, health probes on `/actuator/health`.

| Method | Path | Description |
|---|---|---|
| GET | /api/tasks | List tasks |
| GET | /api/tasks/{id} | Get one task |
| POST | /api/tasks | Create a task |
| PUT | /api/tasks/{id} | Update a task |
| DELETE | /api/tasks/{id} | Delete a task |

## Prerequisites

Ubuntu, Java 17, Maven 3.9, Git, Docker, Minikube and kubectl, Jenkins (port 8090),
SonarQube (Docker, port 9000), Trivy.

## Run locally

~~~sh
mvn clean test
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
curl http://localhost:9090/actuator/health
~~~

## Docker

~~~sh
docker build -t taskmanager:local .
docker run -p 9090:8080 taskmanager:local
~~~

## Kubernetes (Minikube)

~~~sh
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
kubectl get pods
curl http://$(minikube ip):30080/actuator/health
~~~

The Deployment sets resource requests and limits, liveness and readiness probes,
and runs as a non-root user without privilege escalation.

## CI/CD pipeline

The `Jenkinsfile` is versioned with the project. Jenkins polls the repository and runs
the stages above on every change. Credentials (Docker Hub, SonarQube, NVD API key) are
stored in Jenkins credentials, never in the repository.

## Security policy

| Control | Tool | Policy |
|---|---|---|
| Code quality and SAST | SonarQube | Quality Gate must pass, otherwise the pipeline stops |
| Dependencies | OWASP Dependency-Check | Build fails on a CVE with CVSS >= 9 (CRITICAL); lower severities are reported |
| Image | Trivy | Build fails on a CRITICAL vulnerability with an available fix; HIGH is reported; unfixed vulnerabilities are ignored |
| Running app | OWASP ZAP | Baseline (passive) scan; report archived, alerts analysed manually |

A ZAP scan does not guarantee the absence of vulnerabilities; it complements the other controls.

## Published image

`arrab/taskmanager` on Docker Hub, tagged with the Jenkins build number and `latest`.

## Reports

Archived by Jenkins in each build: `trivy-report.txt`, `dependency-check-report.html`, `zap-reports/zap-report.html`.
SonarQube results are available in the SonarQube project dashboard.

## Branches

- `main`: stable version
- `develop`: working branch, built by Jenkins
