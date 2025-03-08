FROM openjdk:17-jdk-slim
COPY src/main/java/ru/tpu/hostel/schedules/schedules/schedules.json /app/schedules.json
COPY ./build/libs/Schedules-0.0.1-SNAPSHOT.jar /opt/service.jar
EXPOSE 8080
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "/opt/service.jar"]
