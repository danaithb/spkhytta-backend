# Bruk Amazon Corretto 21 som runtime (støtter ARM)
FROM amazoncorretto:21
WORKDIR /app

# Kopier den ferdige JAR-filen fra lokal maskin til containeren
COPY backend/hyttebooker/target/*.jar app.jar

# Eksponer port 8080
EXPOSE 8080

# Kjør applikasjonen
CMD ["java", "-jar", "app.jar"]
