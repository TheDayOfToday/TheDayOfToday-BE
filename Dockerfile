FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY ./build/libs/thedayoftoday-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=jwt", "--server.address=0.0.0.0"]
