# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install necessary libraries for Selenium/Chrome
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver \
    wv

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Set environment variables for Chrome
ENV CHROME_BIN=/usr/bin/chromium-browser \
    CHROME_PATH=/usr/lib/chromium/

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]