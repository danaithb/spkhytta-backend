# Første stage: Bygg med Maven (JDK 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Andre stage: Kjør appen (JDK 21)
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD echo "$FIREBASE_CONFIG" > /app/firebase-adminsdk.json && java -jar app.jar


