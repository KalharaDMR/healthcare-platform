# =========================
# BUILD STAGE
# =========================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom first (better caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]