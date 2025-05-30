# 기본 이미지: OpenJDK 23
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]