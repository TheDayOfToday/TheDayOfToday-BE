FROM eclipse-temurin:21-jdk
WORKDIR /app

# JAR 복사
COPY build/libs/thedayoftoday-0.0.1-SNAPSHOT.jar app.jar

# 설정 파일 복사
COPY src/main/resources/application.properties /app/application.properties
COPY src/main/resources/application-jwt.properties /app/application-jwt.properties

EXPOSE 8080

# 설정 경로 명시!
CMD ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.properties,file:/app/application-jwt.properties", "--spring.profiles.active=jwt", "--server.address=0.0.0.0"]

