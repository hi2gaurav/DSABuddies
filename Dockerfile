FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src
COPY frontend ./frontend

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build the project (frontend + backend as single JAR)
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/dsa-buddies-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
