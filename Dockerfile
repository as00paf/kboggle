# Build stage
FROM gradle:8-jdk17 AS build
WORKDIR /app

# Copy gradle files first for caching
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

# Copy all source code
COPY . .

# Build only the server module
RUN gradle :server:shadowJar --no-daemon

# Runtime stage
FROM openjdk:17-slim
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/server/build/libs/*-all.jar app.jar

# Expose port (Render uses PORT env var)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]