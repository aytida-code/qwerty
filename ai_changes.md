COMMIT_MESSAGE: Fix Spring Cloud startup, secure admin API key, and restore service routing

## Features Added
- Started the complete Spring Boot/Spring Cloud topology locally: Eureka registry, three business services, and the gateway.
- Configured the gateway to listen on port 21690 while retaining Eureka discovery-based routing to the business microservices.
- Preserved the existing admin API-key endpoints at `/api/v1/api-keys` and moved their configuration to the application configuration namespace.
- Added the missing Eureka server and gateway Dockerfiles referenced by Docker Compose.

## Files Modified
- `pom.xml` — upgraded the Spring Cloud release train to 2025.0.0 for Spring Boot 3.5.0 compatibility.
- `user-service/src/main/java/com/example/userservice/controller/ApiKeyController.java` — reads the admin key from the configured application property.
- `user-service/src/main/resources/application.properties` — added the environment-backed admin API-key property; retained the resolved PostgreSQL URL.
- `user-service/src/test/java/com/example/userservice/controller/ApiKeyControllerIntegrationTest.java` — aligned test configuration with the new admin API-key property.
- `gateway-service/src/main/resources/application.yml` — uses port 21690 by default, with `SERVER_PORT` override support.
- `docker-compose.yml` — aligns gateway port and health check with port 21690.
- `start.sh` — packages the Maven reactor, starts a local Eureka registry and business services, then runs the gateway in the foreground.
- `README.md` — documents the gateway's port 21690 URL.
- `admin_keys.env` — removed because it contained a committed active credential.

## Files Added
- `eureka-server/Dockerfile` — builds and runs the Eureka registry image referenced by Docker Compose.
- `gateway-service/Dockerfile` — builds and runs the gateway image referenced by Docker Compose.

## Secrets Moved
- `ADMIN_API_KEY` -> `app.secret.admin-api-key` in `user-service/src/main/resources/application.properties`.

## DB URLs Resolved
- `jdbc:postgresql://localhost:5432/gen_a102c58a56e9` -> `jdbc:postgresql://localhost:5432/gen_a102c58a56e9`.

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.0.12.1.
