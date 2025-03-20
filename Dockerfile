FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем все файлы проекта
COPY . .

# Делаем gradlew исполняемым и собираем проект
#RUN chmod +x gradlew && ./gradlew assemble

# Открываем порт
EXPOSE 8080

# Запуск контейнера
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "/app/build/libs/Schedules-0.0.1-SNAPSHOT.jar"]
