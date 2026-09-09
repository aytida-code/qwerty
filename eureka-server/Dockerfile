FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -q -DskipTests -pl eureka-server -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/eureka-server/target/eureka-server.jar app.jar
EXPOSE 21613
ENTRYPOINT ["java", "-jar", "app.jar"]
