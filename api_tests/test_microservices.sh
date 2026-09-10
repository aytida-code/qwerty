#!/bin/bash
BASE_URL="http://localhost:23334"
TOKEN=""

check_route() {
  local service="$1"
  local response
  local status

  response=$(curl -s --max-time 60 -w "\n%{http_code}" "$BASE_URL/$service/actuator/health")
  status=$(printf '%s' "$response" | tail -n 1)

  if [ "$status" != "200" ]; then
    echo "FAILED: $service gateway health route returned $status"
    exit 1
  fi
}

check_route "task-service"
check_route "user-service"
check_route "project-service"

echo "PASSED"
exit 0
