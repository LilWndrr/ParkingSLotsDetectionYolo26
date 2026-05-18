# =================================================================
# Stage 1: BUILD the Spring Boot application
# =================================================================
FROM eclipse-temurin:21-jdk-jammy AS app-builder

WORKDIR /app

# Copy Maven wrapper and POM
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies (caching step)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# =================================================================
# Stage 2: RUNTIME — Slim image
# =================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the Spring Boot fat JAR from Stage 1
COPY --from=app-builder /app/target/*.jar app.jar

EXPOSE 8085

# Run the application normally
ENTRYPOINT ["java", "-jar", "app.jar"]