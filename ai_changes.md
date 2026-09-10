COMMIT_MESSAGE: Fix gateway routing for registered microservices

## Features Added
- Corrected Eureka registration endpoints so gateway and business microservices use the running Eureka server.
- Added explicit load-balanced gateway routes for task-service, user-service, and project-service, each stripping its service path prefix before forwarding.
- Set the gateway's default server port to 29472 for platform deployment.
- Made MySQL seed data compatible and idempotent so all routed microservices start successfully.

## Files Modified
- gateway-service/src/main/resources/application.yml — set port 29472, corrected Eureka URL, and added explicit routes to the three business services.
- user-service/src/main/resources/application.yml — corrected Eureka URL.
- project-service/src/main/resources/application.yml — corrected Eureka URL.
- task-service/src/main/resources/application.yml — corrected Eureka URL.
- user-service/src/main/resources/application.properties — use APP_DATASOURCE_URL and define an environment-overridable admin API key default.
- project-service/src/main/resources/application.properties — use APP_DATASOURCE_URL to avoid an incompatible injected URL.
- task-service/src/main/resources/application.properties — use APP_DATASOURCE_URL to avoid an incompatible injected URL.
- user-service/src/main/resources/data.sql — converted seed statements to MySQL syntax and normalized generated identifiers.
- project-service/src/main/resources/data.sql — converted seed statements to MySQL syntax and normalized generated identifiers.
- task-service/src/main/resources/data.sql — converted seed statements to MySQL syntax and normalized generated identifiers.
- gateway-service/src/test/java/com/example/gatewayservice/GatewayRoutingTest.java — aligned the integration test port with the platform gateway port.
- ai_changes.md — documented final changes and verification.

## Files Added
- api_tests/test_microservices.sh — checks all three microservice health endpoints through the gateway.

## Secrets Moved
- admin API key -> app.secret.admin-api-key (environment override: ADMIN_API_KEY).

## DB URLs Resolved
- jdbc:mysql://localhost:3306/gen_12736d347011 -> jdbc:mysql://localhost:3306/gen_12736d347011 (used as the APP_DATASOURCE_URL fallback for all business services).

## Test Results Summary
4 PASSED, 0 FAILED, 0 SKIPPED
- Gateway actuator health: PASSED (200).
- task-service gateway health route: PASSED (200).
- user-service gateway health route: PASSED (200).
- project-service gateway health route: PASSED (200).
- Maven suite: PASSED (26 tests).

