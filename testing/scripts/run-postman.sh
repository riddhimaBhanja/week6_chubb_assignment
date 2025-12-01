#!/bin/bash

# Postman Collection Runner Script for Flight Microservices
# Usage: ./run-postman.sh

echo "========================================"
echo "Flight Microservices - Postman Tests"
echo "========================================"

# Check if Newman is installed
if ! command -v newman &> /dev/null; then
    echo "ERROR: Newman is not installed"
    echo "Install Newman using: npm install -g newman newman-reporter-htmlextra"
    exit 1
fi

# Variables
COLLECTION="../postman/Flight-Microservices.postman_collection.json"
RESULTS_DIR="../postman/results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="${RESULTS_DIR}/report_${TIMESTAMP}.html"

# Create results directory if it doesn't exist
mkdir -p ${RESULTS_DIR}

echo "Running Postman collection..."
echo "Collection: ${COLLECTION}"
echo "Report will be saved to: ${REPORT_FILE}"
echo ""

# Run Newman with HTML reporter
newman run ${COLLECTION} \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export ${REPORT_FILE} \
  --timeout-request 10000 \
  --bail

# Check if test was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Postman tests completed successfully!"
    echo "========================================"
    echo "HTML Report: ${REPORT_FILE}"
    echo ""
    echo "To view the report, open:"
    echo "  open ${REPORT_FILE}"
else
    echo ""
    echo "ERROR: Postman tests failed!"
    exit 1
fi
