FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . /build
RUN mvn -q -DskipTests -pl gateway-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/gateway-service/target/gateway-service.jar app.jar
EXPOSE 23334
ENTRYPOINT ["java", "-jar", "app.jar"]
