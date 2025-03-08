FROM openjdk:17-jdk-slim

ARG JAR_FILE

WORKDIR /app

COPY ${JAR_FILE} /app/app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]