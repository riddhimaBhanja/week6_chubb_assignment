#!/bin/bash

# JMeter Load Testing Script for Flight Microservices
# Usage: ./run-jmeter.sh

echo "========================================"
echo "Flight Microservices - JMeter Load Test"
echo "========================================"

# Check if JMeter is installed
if ! command -v jmeter &> /dev/null; then
    echo "ERROR: JMeter is not installed or not in PATH"
    echo "Please install JMeter from https://jmeter.apache.org/download_jmeter.cgi"
    exit 1
fi

# Variables
JMETER_HOME=${JMETER_HOME:-"/usr/local/jmeter"}
TEST_PLAN="../jmeter/FlightApp-LoadTest.jmx"
RESULTS_DIR="../jmeter/results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULTS_FILE="${RESULTS_DIR}/results_${TIMESTAMP}.jtl"
REPORT_DIR="${RESULTS_DIR}/report_${TIMESTAMP}"

# Create results directory if it doesn't exist
mkdir -p ${RESULTS_DIR}

echo "Starting JMeter test..."
echo "Test Plan: ${TEST_PLAN}"
echo "Results will be saved to: ${RESULTS_FILE}"
echo "HTML Report will be generated in: ${REPORT_DIR}"
echo ""

# Run JMeter in non-GUI mode
jmeter -n -t ${TEST_PLAN} -l ${RESULTS_FILE} -e -o ${REPORT_DIR}

# Check if test was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "JMeter test completed successfully!"
    echo "========================================"
    echo "Results: ${RESULTS_FILE}"
    echo "HTML Report: ${REPORT_DIR}/index.html"
    echo ""
    echo "To view the report, open:"
    echo "  open ${REPORT_DIR}/index.html"
else
    echo ""
    echo "ERROR: JMeter test failed!"
    exit 1
fi
