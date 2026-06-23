FROM eclipse-temurin:17-jdk
LABEL maintainer="Caltias - PymeTrack"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]