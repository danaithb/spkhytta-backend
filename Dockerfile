# Byggtrinn for Java-backend
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY backend/hyttebooker/pom.xml ./backend/hyttebooker/
WORKDIR /app/backend/hyttebooker
RUN mvn -B dependency:go-offline
COPY backend/hyttebooker/src ./src
RUN mvn -B clean package -DskipTests

# Runtime-image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/backend/hyttebooker/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]