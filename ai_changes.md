COMMIT_MESSAGE: Add summary support to projects

## Features Added
- Added an optional `summary` field to the Project entity.
- Added `summary` support to project create, update, and response DTO flows.
- Documented the Project summary field in the README project summary.

## Files Modified
- project-service/src/main/java/com/example/projectservice/entity/Project.java — added persistent summary property and accessors.
- project-service/src/main/java/com/example/projectservice/dto/ProjectRequest.java — accepts summary values for project creation and updates.
- project-service/src/main/java/com/example/projectservice/dto/ProjectResponse.java — returns the Project summary value.
- project-service/src/main/java/com/example/projectservice/service/ProjectService.java — persists summary values during create and update operations.
- project-service/src/main/resources/application.properties — replaced the resolved MySQL URL host.
- task-service/src/main/resources/application.properties — replaced the resolved MySQL URL host.
- user-service/src/main/resources/application.properties — replaced the resolved MySQL URL host.
- README.md — added a Project summary field description.
- ai_changes.md — documented completed work and verification.

## Files Added
- None.

## Secrets Moved
- None; the existing API-key configuration already externalizes its configured secret.

## DB URLs Resolved
- jdbc:mysql://mysql:3306/gen_b7b8808f756c} -> jdbc:mysql://localhost:3306/gen_b7b8808f756c

## Compilation Result
PASSED — `mvn compile -q` and `mvn package -DskipTests -q` completed successfully with Java 21.0.12.1.
