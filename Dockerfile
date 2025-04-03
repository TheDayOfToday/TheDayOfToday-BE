FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY ./build/libs/thedayoftoday-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/application.properties /app/application.properties
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties", "--spring.profiles.active=jwt", "--server.address=0.0.0.0"]
