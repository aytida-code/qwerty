@echo off
setlocal
cd /d "%~dp0"
set SERVER_FORWARD_HEADERS_STRATEGY=framework
set SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false

rem Eureka is SHARED and already running at http://localhost:8761/eureka — do not start it here.

echo Starting user-service on port 27542...
start /B java -jar user-service\target\user-service.jar --server.port=27542 >> user-service.log 2>&1
timeout /T 5 /NOBREAK > NUL

echo Starting project-service on port 21129...
start /B java -jar project-service\target\project-service.jar --server.port=21129 >> project-service.log 2>&1
timeout /T 5 /NOBREAK > NUL

echo Starting task-service on port 28921...
start /B java -jar task-service\target\task-service.jar --server.port=28921 >> task-service.log 2>&1
timeout /T 5 /NOBREAK > NUL

echo Starting gateway-service on port 24348 (foreground)...
java -jar gateway-service\target\gateway-service.jar --server.port=24348

endlocal
