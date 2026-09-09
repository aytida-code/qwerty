FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -q -DskipTests -pl project-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/project-service/target/project-service.jar app.jar
EXPOSE 21129
ENV SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false
ENTRYPOINT ["java", "-jar", "app.jar"]
