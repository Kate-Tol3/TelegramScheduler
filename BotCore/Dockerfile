# --- Сборка JAR-файла ---
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :BotCore:bootJar

# --- Финальный образ ---
FROM eclipse-temurin:17
WORKDIR /app
COPY --from=builder /app/BotCore/build/libs/*.jar app.jar

# Копируем .env в контейнер (для spring-dotenv)
COPY .env .env

# Обязательно укажем директорию для spring-dotenv (если не задаётся в main)
ENV DOTENV_DIRECTORY=.

ENTRYPOINT ["java", "-jar", "app.jar"]
