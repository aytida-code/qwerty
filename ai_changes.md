COMMIT_MESSAGE: Update MySQL database URL across business services

## Features Added
- Updated all business services to use the resolved MySQL database URL.
- Preserved the existing MySQL Connector/J, API-key authentication, CORS, and service functionality.

## Files Modified
- `project-service/src/main/resources/application.properties` — changed the environment-backed MySQL JDBC fallback URL.
- `task-service/src/main/resources/application.properties` — changed the environment-backed MySQL JDBC fallback URL.
- `user-service/src/main/resources/application.properties` — changed the environment-backed MySQL JDBC fallback URL.
- `ai_changes.md` — recorded this database endpoint update and verification result.

## Files Added
- None.

## Secrets Moved
- None; existing datasource credentials remain environment-backed as `MYSQL_USERNAME` and `MYSQL_PASSWORD`.

## DB URLs Resolved
- `jdbc:mysql://localhost:3306/gen_a102c58a56e9` -> `jdbc:mysql://localhost:3306/gen_12736d347011` for project-service, task-service, and user-service.

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.0.12.1.

