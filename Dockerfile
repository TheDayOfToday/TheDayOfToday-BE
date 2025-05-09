FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build/libs/thedayoftoday-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties,file:/app/application-jwt.properties", "--spring.profiles.active=jwt", "--server.address=0.0.0.0"]
