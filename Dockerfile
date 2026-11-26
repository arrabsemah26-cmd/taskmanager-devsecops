FROM eclipse-temurin:17-jre-alpine

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app
COPY target/*.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
