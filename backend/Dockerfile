# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy maven dependency definitions first to cache dependencies
COPY pom.xml .
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
# Skip tests to speed up build (tests should be run in CI/CD pipeline)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 80
EXPOSE 443

# Define environment variables (can be overridden by docker-compose)
# Note: We don't hardcode sensitive data here, defaults are for safety or reference
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
