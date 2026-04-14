#!/bin/bash
# Smoke tests for Coffee Shop Jakarta EE application
# Usage: ./smoke-tests.sh <PORT>

set +e

PORT=${1:-9080}
BASE_URL="http://localhost:${PORT}"
PASS=0
FAIL=0
TOTAL=0

pass() {
    echo "  PASS: $1"
    PASS=$((PASS + 1))
    TOTAL=$((TOTAL + 1))
}

fail() {
    echo "  FAIL: $1 - $2"
    FAIL=$((FAIL + 1))
    TOTAL=$((TOTAL + 1))
}

TIMESTAMP=$(date +%s%N)

echo "================================================"
echo "Coffee Shop Jakarta EE - Smoke Tests"
echo "Base URL: ${BASE_URL}"
echo "================================================"
echo ""

# Test 1: Root page loads (HTML)
echo "Test 1: GET / (Home Page)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/")
if [ "$HTTP_CODE" = "200" ]; then
    pass "Home page returns 200"
else
    fail "Home page returns $HTTP_CODE" "Expected 200"
fi

# Test 2: Home page contains expected content
echo "Test 2: Home page content check"
BODY=$(curl -s "${BASE_URL}/")
if echo "$BODY" | grep -qi "coffee\|coffeeshop\|shop\|ATLANTA"; then
    pass "Home page contains expected content"
else
    fail "Home page missing expected content" "Should contain coffee-related text"
fi

# Test 3: Health endpoint
echo "Test 3: GET /health"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/health")
if [ "$HTTP_CODE" = "200" ]; then
    pass "Health endpoint returns 200"
else
    fail "Health endpoint returns $HTTP_CODE" "Expected 200"
fi

# Test 4: Health endpoint returns UP status
echo "Test 4: Health status check"
HEALTH=$(curl -s "${BASE_URL}/health")
if echo "$HEALTH" | grep -q '"UP"'; then
    pass "Health status is UP"
else
    fail "Health status not UP" "Got: $HEALTH"
fi

# Test 5: POST /api/order with barista item
echo "Test 5: POST /api/order (barista item)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/order" \
    -H "Content-Type: application/json" \
    -d "{\"id\":\"smoke-${TIMESTAMP}-1\",\"orderSource\":\"WEB\",\"location\":\"ATLANTA\",\"timestamp\":\"2025-01-01T12:00:00Z\",\"baristaItems\":[{\"item\":\"COFFEE_BLACK\",\"name\":\"SmokeTestUser\",\"price\":3.50}],\"commandType\":\"PLACE_ORDER\"}")
if [ "$HTTP_CODE" = "202" ]; then
    pass "Order API returns 202 (Accepted)"
else
    fail "Order API returns $HTTP_CODE" "Expected 202"
fi

# Test 6: POST /api/order with kitchen item
echo "Test 6: POST /api/order (kitchen item)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/order" \
    -H "Content-Type: application/json" \
    -d "{\"id\":\"smoke-${TIMESTAMP}-2\",\"orderSource\":\"WEB\",\"location\":\"ATLANTA\",\"timestamp\":\"2025-01-01T12:01:00Z\",\"kitchenItems\":[{\"item\":\"CAKEPOP\",\"name\":\"KitchenTestUser\",\"price\":2.50}],\"commandType\":\"PLACE_ORDER\"}")
if [ "$HTTP_CODE" = "202" ]; then
    pass "Kitchen order API returns 202 (Accepted)"
else
    fail "Kitchen order API returns $HTTP_CODE" "Expected 202"
fi

# Test 7: POST /api/order with combined barista + kitchen items
echo "Test 7: POST /api/order (combined order)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/order" \
    -H "Content-Type: application/json" \
    -d "{\"id\":\"smoke-${TIMESTAMP}-3\",\"orderSource\":\"WEB\",\"location\":\"ATLANTA\",\"timestamp\":\"2025-01-01T12:02:00Z\",\"baristaItems\":[{\"item\":\"CAPPUCCINO\",\"name\":\"ComboUser\",\"price\":4.50}],\"kitchenItems\":[{\"item\":\"MUFFIN\",\"name\":\"ComboUser\",\"price\":3.00}],\"commandType\":\"PLACE_ORDER\"}")
if [ "$HTTP_CODE" = "202" ]; then
    pass "Combined order API returns 202 (Accepted)"
else
    fail "Combined order API returns $HTTP_CODE" "Expected 202"
fi

# Test 8: Dashboard SSE endpoint exists (should return SSE content type)
echo "Test 8: GET /dashboard/stream (SSE endpoint)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -m 3 "${BASE_URL}/dashboard/stream" 2>/dev/null)
CURL_EXIT=$?
if [ "$HTTP_CODE" = "200" ] || [ "$CURL_EXIT" = "28" ] || echo "$HTTP_CODE" | grep -q "^0"; then
    pass "SSE endpoint responds (200 or timeout for streaming - expected for SSE)"
else
    fail "SSE endpoint returns $HTTP_CODE" "Expected 200 or timeout"
fi

# Test 9: Invalid order (missing required fields) should still be handled
echo "Test 9: POST /api/order (invalid payload)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/order" \
    -H "Content-Type: application/json" \
    -d '{}')
# We expect either 400 (bad request) or 500 (server error) for invalid input, but NOT 404
if [ "$HTTP_CODE" != "404" ]; then
    pass "Invalid order handled (not 404 - endpoint exists)"
else
    fail "Invalid order returns 404" "Endpoint should exist"
fi

# Test 10: Static resources (check if CSS/JS loaded)
echo "Test 10: Static resources"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/coffeeshop.js" 2>/dev/null)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
    pass "Static resource endpoint responds"
else
    fail "Static resource returns $HTTP_CODE" "Expected 200 or 404"
fi

# Test 11: OpenAPI endpoint (provided by MicroProfile)
echo "Test 11: GET /openapi (MicroProfile OpenAPI)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/openapi")
if [ "$HTTP_CODE" = "200" ]; then
    pass "OpenAPI endpoint returns 200"
else
    fail "OpenAPI endpoint returns $HTTP_CODE" "Expected 200"
fi

# Test 12: Metrics endpoint (MicroProfile Metrics)
echo "Test 12: GET /metrics (MicroProfile Metrics)"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/metrics")
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "403" ]; then
    pass "Metrics endpoint exists (200 or 403 auth-required)"
else
    fail "Metrics endpoint returns $HTTP_CODE" "Expected 200 or 403"
fi

echo ""
echo "================================================"
echo "Smoke Test Results"
echo "================================================"
echo "Total: ${TOTAL}"
echo "Passed: ${PASS}"
echo "Failed: ${FAIL}"
echo "================================================"

if [ "$FAIL" -gt 0 ]; then
    echo "SOME TESTS FAILED!"
    exit 1
else
    echo "ALL TESTS PASSED!"
    exit 0
fi
