COMMIT_MESSAGE: Fix containerized Eureka discovery and gateway routing

## Features Added
- Configured the Eureka server and all Eureka clients to use the `eureka-server` container hostname by default.
- Kept local process startup functional by setting localhost Eureka overrides only in `start.sh`.
- Preserved explicit load-balanced gateway routes for `task-service`, `user-service`, and `project-service`, each removing its service-name path prefix before forwarding.
- Aligned the gateway container exposure, Compose port mapping, health check, and routing checks with port 23334.

## Files Modified
- eureka-server/src/main/resources/application.yml — made the Eureka instance hostname environment-configurable with `eureka-server` as the container default.
- gateway-service/src/main/resources/application.yml — use the container-aware Eureka URL and default the gateway to port 23334.
- user-service/src/main/resources/application.yml — use the container-aware Eureka URL.
- project-service/src/main/resources/application.yml — use the container-aware Eureka URL.
- task-service/src/main/resources/application.yml — use the container-aware Eureka URL.
- start.sh — provide localhost Eureka settings when launching all services as local processes and default the gateway to port 23334.
- gateway-service/Dockerfile — expose the gateway's configured port 23334.
- docker-compose.yml — map and health-check the gateway on port 23334.
- gateway-service/src/test/java/com/example/gatewayservice/GatewayRoutingTest.java — point gateway routing checks to port 23334.
- api_tests/test_microservices.sh — point gateway health-route checks to port 23334.
- ai_changes.md — document the completed change and verification result.

## Files Added
- None.

## Secrets Moved
- None; the existing admin API key and datasource credentials remain environment-backed configuration.

## DB URLs Resolved
- jdbc:mysql://localhost:3306/gen_12736d347011 -> jdbc:mysql://localhost:3306/gen_12736d347011 (existing resolved datasource fallback retained without change).

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.0.12.1.

