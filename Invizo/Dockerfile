# ----------- BUILD STAGE (optional for clean builds) -------------
# Use Maven with Java 21 to build the app
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory inside container
WORKDIR /app

# Copy pom.xml and download dependencies (faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# ----------- RUNTIME STAGE -------------
# Use a smaller runtime image with Java 21
FROM eclipse-temurin:21-jdk-alpine

# Set working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
