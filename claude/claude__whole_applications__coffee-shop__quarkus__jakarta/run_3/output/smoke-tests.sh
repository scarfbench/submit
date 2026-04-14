#!/bin/bash
# Smoke tests for the Jakarta EE Coffee Shop application
# Usage: ./smoke-tests.sh <port>

# Note: do not use set -e as curl timeouts return non-zero exit codes

PORT=${1:-8080}
BASE_URL="http://localhost:${PORT}"
PASSED=0
FAILED=0
# Generate unique run ID to avoid duplicate key conflicts
RUN_ID="$(date +%s)-$$"

echo "=== Coffee Shop Jakarta EE Smoke Tests ==="
echo "Testing against: ${BASE_URL}"
echo "Run ID: ${RUN_ID}"
echo ""

# Helper function
run_test() {
    local test_name="$1"
    local url="$2"
    local method="${3:-GET}"
    local expected_status="$4"
    local body="$5"
    local content_type="${6:-application/json}"

    echo -n "TEST: ${test_name}... "

    if [ "${method}" == "GET" ]; then
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${url}" --max-time 10 2>/dev/null)
    else
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X "${method}" \
            -H "Content-Type: ${content_type}" \
            -d "${body}" \
            "${url}" --max-time 10 2>/dev/null)
    fi

    if [ "${HTTP_CODE}" == "${expected_status}" ]; then
        echo "PASSED (HTTP ${HTTP_CODE})"
        PASSED=$((PASSED + 1))
    else
        echo "FAILED (Expected ${expected_status}, got ${HTTP_CODE})"
        FAILED=$((FAILED + 1))
    fi
}

# Test 1: Homepage returns HTML
run_test "Homepage loads" "${BASE_URL}/" "GET" "200"

# Test 2: Place order API accepts POST
ORDER_JSON="{\"id\":\"smoke-${RUN_ID}-001\",\"orderSource\":\"COUNTER\",\"location\":\"ATLANTA\",\"loyaltyMemberId\":\"TestCustomer\",\"baristaItems\":[{\"item\":\"COFFEE_BLACK\",\"name\":\"TestUser\",\"price\":3.00}],\"commandType\":\"PLACE_ORDER\"}"
run_test "Place order API" "${BASE_URL}/api/order" "POST" "202" "${ORDER_JSON}"

# Test 3: Place order with kitchen items
ORDER_JSON2="{\"id\":\"smoke-${RUN_ID}-002\",\"orderSource\":\"WEB\",\"location\":\"CHARLOTTE\",\"baristaItems\":[{\"item\":\"CAPPUCCINO\",\"name\":\"Alice\",\"price\":4.50}],\"kitchenItems\":[{\"item\":\"CROISSANT\",\"name\":\"Alice\",\"price\":3.25}],\"commandType\":\"PLACE_ORDER\"}"
run_test "Place order with kitchen items" "${BASE_URL}/api/order" "POST" "202" "${ORDER_JSON2}"

# Test 4: Place order with only barista items
ORDER_JSON3="{\"id\":\"smoke-${RUN_ID}-003\",\"orderSource\":\"COUNTER\",\"location\":\"RALEIGH\",\"baristaItems\":[{\"item\":\"ESPRESSO\",\"name\":\"Bob\",\"price\":3.50}],\"commandType\":\"PLACE_ORDER\"}"
run_test "Place order barista only" "${BASE_URL}/api/order" "POST" "202" "${ORDER_JSON3}"

# Test 5: Dashboard SSE endpoint exists
echo -n "TEST: Dashboard SSE endpoint... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/dashboard/stream" -H "Accept: text/event-stream" --max-time 3 2>/dev/null)
CURL_EXIT=$?
if [ "${HTTP_CODE}" == "200" ] || [ "${CURL_EXIT}" -eq 28 ]; then
    echo "PASSED (SSE endpoint accessible)"
    PASSED=$((PASSED + 1))
else
    echo "FAILED (HTTP ${HTTP_CODE}, curl exit ${CURL_EXIT})"
    FAILED=$((FAILED + 1))
fi

# Test 6: Message endpoint
run_test "Message endpoint" "${BASE_URL}/api/message" "POST" "204" '"test message"' "application/json"

echo ""
echo "=== Results ==="
echo "Passed: ${PASSED}"
echo "Failed: ${FAILED}"
echo "Total:  $((PASSED + FAILED))"

if [ ${FAILED} -gt 0 ]; then
    echo "SMOKE TESTS FAILED"
    exit 1
else
    echo "ALL SMOKE TESTS PASSED"
    exit 0
fi
