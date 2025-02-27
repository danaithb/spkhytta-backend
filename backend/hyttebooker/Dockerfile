FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/backend/hyttebooker/target/*.jar app.jar

# Eksplisitt sett PORT variabel
ENV PORT=8080
EXPOSE $PORT

# Øk timeout til 300s (5 minutter)
CMD ["java", "-jar", "-Dspring.main.lazy-initialization=true", "-Dserver.tomcat.connection-timeout=300000", "app.jar"]