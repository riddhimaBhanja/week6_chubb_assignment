#!/bin/bash

# SonarQube Analysis Script for Flight Microservices
# Usage: ./run-sonar.sh

echo "========================================"
echo "Flight Microservices - SonarQube Analysis"
echo "========================================"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed"
    exit 1
fi

# Check if SonarQube server is running
SONAR_HOST=${SONAR_HOST:-"http://localhost:9000"}
echo "Checking SonarQube server at ${SONAR_HOST}..."

if ! curl -s ${SONAR_HOST}/api/system/status | grep -q "UP"; then
    echo "ERROR: SonarQube server is not running at ${SONAR_HOST}"
    echo "Please start SonarQube server first"
    exit 1
fi

echo "SonarQube server is running"
echo ""

# Navigate to project root
cd ../../

# Run tests and generate coverage report
echo "Running tests and generating coverage reports..."
mvn clean verify

# Run SonarQube analysis for each service
echo ""
echo "Running SonarQube analysis..."

services=("eureka-server" "api-gateway" "flight-service" "booking-service")

for service in "${services[@]}"; do
    echo ""
    echo "Analyzing ${service}..."
    cd ${service}
    mvn sonar:sonar \
      -Dsonar.host.url=${SONAR_HOST} \
      -Dsonar.login=${SONAR_TOKEN:-admin} \
      -Dsonar.password=${SONAR_PASSWORD:-admin}
    cd ..
done

echo ""
echo "========================================"
echo "SonarQube analysis completed!"
echo "========================================"
echo "View results at: ${SONAR_HOST}"
