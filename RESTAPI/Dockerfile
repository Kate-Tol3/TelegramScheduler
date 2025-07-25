FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :RESTAPI:bootJar

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/RESTAPI/build/libs/RESTAPI-*.jar app.jar
COPY .env .env
ENV DOTENV_PATH=/app/.env
ENTRYPOINT ["java", "-jar", "app.jar"]
