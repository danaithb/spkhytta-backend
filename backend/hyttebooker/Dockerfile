FROM azul/zulu-openjdk:21.0.3-arm64

WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests