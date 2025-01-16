FROM openjdk:17-jdk-slim
COPY ./build/libs/Schedules-0.0.1-SNAPSHOT.jar /opt/service.jar
EXPOSE 8080
CMD ["java", "-jar", "/opt/service.jar"]
