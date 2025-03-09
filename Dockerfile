FROM openjdk:17-jdk-slim

ARG JAR_FILE

WORKDIR /app

COPY ${JAR_FILE} /app/app.jar

ARG APP_PORT
EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]