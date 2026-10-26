# ==========================================
# Build Stage (Java 21 & Maven)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Runtime Stage (Java 21 JRE)
# ==========================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create logs directory
RUN mkdir -p logs

# Copy compiled jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose backend application port
EXPOSE 9090

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
