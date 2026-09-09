COMMIT_MESSAGE: Switch all business services from PostgreSQL to MySQL

## Features Added
- Switched the project, task, and user business services from PostgreSQL to MySQL.
- Made MySQL connection URL, username, and password configurable through environment variables while retaining compatible local defaults.

## Files Modified
- `project-service/pom.xml` — replaced the PostgreSQL JDBC runtime dependency with MySQL Connector/J.
- `task-service/pom.xml` — replaced the PostgreSQL JDBC runtime dependency with MySQL Connector/J.
- `user-service/pom.xml` — replaced the PostgreSQL JDBC runtime dependency with MySQL Connector/J.
- `project-service/src/main/resources/application.properties` — configured the MySQL JDBC URL and driver with environment-backed connection settings.
- `task-service/src/main/resources/application.properties` — configured the MySQL JDBC URL and driver with environment-backed connection settings.
- `user-service/src/main/resources/application.properties` — configured the MySQL JDBC URL and driver with environment-backed connection settings.

## Files Added
- None.

## Secrets Moved
- `spring.datasource.username` -> `MYSQL_USERNAME` environment variable with the existing local default.
- `spring.datasource.password` -> `MYSQL_PASSWORD` environment variable with the existing local default.

## DB URLs Resolved
- `jdbc:postgresql://localhost:5432/gen_a102c58a56e9` -> `jdbc:mysql://localhost:3306/gen_a102c58a56e9` for project-service, task-service, and user-service.

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.0.12.1.

