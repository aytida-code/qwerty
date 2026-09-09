FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -q -DskipTests -pl task-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/task-service/target/task-service.jar app.jar
EXPOSE 28921
ENV SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false
ENTRYPOINT ["java", "-jar", "app.jar"]
