#!/bin/bash
cd "$(dirname "$0")"
export SERVER_FORWARD_HEADERS_STRATEGY=framework
# spring-cloud 2023.0.1 does not officially list Spring Boot 3.5.0 as compatible;
# disable the strict startup verifier (works fine in practice).
export SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false

# Eureka is SHARED and already running at http://localhost:8761/eureka — do not start it here.

# ---- user-service (port 27542) ----
EXISTING_PID=$(pgrep -f -- "--server.port=27542" || true)
if [ -n "$EXISTING_PID" ]; then
  echo "user-service: killing existing PID $EXISTING_PID on port 27542" >> user-service.log
  kill $EXISTING_PID 2>/dev/null || true
  for i in 1 2 3 4 5 6 7 8 9 10; do
    bash -c "exec 3<>/dev/tcp/127.0.0.1/27542" 2>/dev/null || break
    sleep 1
  done
  if bash -c "exec 3<>/dev/tcp/127.0.0.1/27542" 2>/dev/null; then
    echo "user-service: still bound after 10s, force killing" >> user-service.log
    kill -9 $EXISTING_PID 2>/dev/null || true
    sleep 1
  fi
fi
nohup java -jar user-service/target/user-service.jar --server.port=27542 >> user-service.log 2>&1 &
sleep 5

# ---- project-service (port 21129) ----
EXISTING_PID=$(pgrep -f -- "--server.port=21129" || true)
if [ -n "$EXISTING_PID" ]; then
  echo "project-service: killing existing PID $EXISTING_PID on port 21129" >> project-service.log
  kill $EXISTING_PID 2>/dev/null || true
  for i in 1 2 3 4 5 6 7 8 9 10; do
    bash -c "exec 3<>/dev/tcp/127.0.0.1/21129" 2>/dev/null || break
    sleep 1
  done
  if bash -c "exec 3<>/dev/tcp/127.0.0.1/21129" 2>/dev/null; then
    echo "project-service: still bound after 10s, force killing" >> project-service.log
    kill -9 $EXISTING_PID 2>/dev/null || true
    sleep 1
  fi
fi
nohup java -jar project-service/target/project-service.jar --server.port=21129 >> project-service.log 2>&1 &
sleep 5

# ---- task-service (port 28921) ----
EXISTING_PID=$(pgrep -f -- "--server.port=28921" || true)
if [ -n "$EXISTING_PID" ]; then
  echo "task-service: killing existing PID $EXISTING_PID on port 28921" >> task-service.log
  kill $EXISTING_PID 2>/dev/null || true
  for i in 1 2 3 4 5 6 7 8 9 10; do
    bash -c "exec 3<>/dev/tcp/127.0.0.1/28921" 2>/dev/null || break
    sleep 1
  done
  if bash -c "exec 3<>/dev/tcp/127.0.0.1/28921" 2>/dev/null; then
    echo "task-service: still bound after 10s, force killing" >> task-service.log
    kill -9 $EXISTING_PID 2>/dev/null || true
    sleep 1
  fi
fi
nohup java -jar task-service/target/task-service.jar --server.port=28921 >> task-service.log 2>&1 &
sleep 5

# ---- gateway-service (port 24348) — LAST, foreground ----
EXISTING_PID=$(pgrep -f -- "--server.port=24348" || true)
if [ -n "$EXISTING_PID" ]; then
  echo "gateway-service: killing existing PID $EXISTING_PID on port 24348" >> gateway-service.log
  kill $EXISTING_PID 2>/dev/null || true
  for i in 1 2 3 4 5 6 7 8 9 10; do
    bash -c "exec 3<>/dev/tcp/127.0.0.1/24348" 2>/dev/null || break
    sleep 1
  done
  if bash -c "exec 3<>/dev/tcp/127.0.0.1/24348" 2>/dev/null; then
    echo "gateway-service: still bound after 10s, force killing" >> gateway-service.log
    kill -9 $EXISTING_PID 2>/dev/null || true
    sleep 1
  fi
fi
exec java -jar gateway-service/target/gateway-service.jar --server.port=24348
