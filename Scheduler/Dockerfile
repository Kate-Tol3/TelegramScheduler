# --- Сборка JAR-файла ---
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :Scheduler:bootJar

# --- Финальный образ ---
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=builder /app/Scheduler/build/libs/*.jar app.jar

# Копируем .env в контейнер
COPY .env .env

# Указываем директорию для spring-dotenv
ENV DOTENV_DIRECTORY=.

ENTRYPOINT ["java", "-jar", "app.jar"]
