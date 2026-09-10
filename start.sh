#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
mvn package -DskipTests -q

SERVER_PORT="${SERVER_PORT:-23334}"

EUREKA_INSTANCE_HOSTNAME=localhost SERVER_PORT=21613 java -jar eureka-server/target/eureka-server.jar > eureka-server.log 2>&1 &
sleep 5
EUREKA_SERVER_URL=http://localhost:21613/eureka/ SERVER_PORT=27542 java -jar user-service/target/user-service.jar > user-service.log 2>&1 &
EUREKA_SERVER_URL=http://localhost:21613/eureka/ SERVER_PORT=21129 java -jar project-service/target/project-service.jar > project-service.log 2>&1 &
EUREKA_SERVER_URL=http://localhost:21613/eureka/ SERVER_PORT=28921 java -jar task-service/target/task-service.jar > task-service.log 2>&1 &
sleep 5
exec env EUREKA_SERVER_URL=http://localhost:21613/eureka/ java -jar gateway-service/target/gateway-service.jar --server.port="$SERVER_PORT"
