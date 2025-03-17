FROM openjdk:21

# Sett arbeidskatalog inne i containeren
WORKDIR /app

# Kopier den kompilerte JAR-filen til containeren
COPY target/*.jar app.jar

# Start Spring Boot-applikasjonen
ENTRYPOINT ["java", "-jar", "app.jar"]