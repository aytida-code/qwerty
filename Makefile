.PHONY: build \
	run-user-service stop-user-service \
	run-project-service stop-project-service \
	run-task-service stop-task-service \
	run-gateway-service stop-gateway-service

build:
	mvn clean install -DskipTests

export SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false

run-user-service:
	nohup java -jar user-service/target/user-service.jar --server.port=27542 >> user-service.log 2>&1 & echo $$! > user-service.pid

stop-user-service:
	-kill $$(cat user-service.pid) 2>/dev/null || true
	-rm -f user-service.pid

run-project-service:
	nohup java -jar project-service/target/project-service.jar --server.port=21129 >> project-service.log 2>&1 & echo $$! > project-service.pid

stop-project-service:
	-kill $$(cat project-service.pid) 2>/dev/null || true
	-rm -f project-service.pid

run-task-service:
	nohup java -jar task-service/target/task-service.jar --server.port=28921 >> task-service.log 2>&1 & echo $$! > task-service.pid

stop-task-service:
	-kill $$(cat task-service.pid) 2>/dev/null || true
	-rm -f task-service.pid

run-gateway-service:
	nohup java -jar gateway-service/target/gateway-service.jar --server.port=24348 >> gateway-service.log 2>&1 & echo $$! > gateway-service.pid

stop-gateway-service:
	-kill $$(cat gateway-service.pid) 2>/dev/null || true
	-rm -f gateway-service.pid
