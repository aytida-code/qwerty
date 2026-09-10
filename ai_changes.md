COMMIT_MESSAGE: Add project summary message to README

## Features Added
- Added a project summary message to the README.

## Files Modified
- README.md — added a Project Summary section describing the multi-module Spring Boot and Spring Cloud application.
- project-service/src/main/resources/application.properties — replaced the unreachable datasource fallback URL with the pre-resolved MySQL URL.
- task-service/src/main/resources/application.properties — replaced the unreachable datasource fallback URL with the pre-resolved MySQL URL.
- user-service/src/main/resources/application.properties — replaced the unreachable datasource fallback URL with the pre-resolved MySQL URL.
- ai_changes.md — documented the completed change and verification result.

## Files Added
- None.

## Secrets Moved
- None; no hardcoded Java secrets were found requiring extraction.

## DB URLs Resolved
- jdbc:mysql://localhost:3306/gen_12736d347011 -> jdbc:mysql://mysql:3306/gen_b7b8808f756c

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.0.12.1.
