FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/*.jar gateway.jar
ENTRYPOINT ["java", "-jar", "gateway.jar"]