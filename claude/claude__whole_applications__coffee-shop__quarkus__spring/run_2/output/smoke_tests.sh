#!/bin/bash
# Smoke tests for Coffee Shop Spring Boot application
# Usage: ./smoke_tests.sh <port>

PORT=${1:-8080}
BASE_URL="http://localhost:${PORT}"
PASS=0
FAIL=0

echo "========================================="
echo "  Coffee Shop Smoke Tests"
echo "  Target: ${BASE_URL}"
echo "========================================="

# Test 1: Health Check via actuator
echo ""
echo "TEST 1: Health endpoint"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null)
if [ "$RESPONSE" = "200" ]; then
    echo "  PASS - /actuator/health returned 200"
    PASS=$((PASS + 1))
else
    echo "  FAIL - /actuator/health returned $RESPONSE (expected 200)"
    FAIL=$((FAIL + 1))
fi

# Test 2: Home page loads
echo ""
echo "TEST 2: Home page"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/" 2>/dev/null)
if [ "$RESPONSE" = "200" ]; then
    echo "  PASS - / returned 200"
    PASS=$((PASS + 1))
else
    echo "  FAIL - / returned $RESPONSE (expected 200)"
    FAIL=$((FAIL + 1))
fi

# Test 3: Home page contains expected content
echo ""
echo "TEST 3: Home page content"
BODY=$(curl -s "${BASE_URL}/" 2>/dev/null)
if echo "$BODY" | grep -q "Coffee Shop"; then
    echo "  PASS - Home page contains 'Coffee Shop'"
    PASS=$((PASS + 1))
else
    echo "  FAIL - Home page does not contain 'Coffee Shop'"
    FAIL=$((FAIL + 1))
fi

# Test 4: Place an order (POST /api/order)
echo ""
echo "TEST 4: Place order endpoint"
ORDER_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/order" \
    -H "Content-Type: application/json" \
    -d '{
        "id": "test-order-001",
        "orderSource": "WEB",
        "storeId": "ATLANTA",
        "rewardsId": null,
        "baristaItems": [{"item": "COFFEE_BLACK", "name": "John", "price": 3.50}],
        "kitchenItems": null,
        "commandType": "PLACE_ORDER"
    }' 2>/dev/null)
if [ "$ORDER_RESPONSE" = "202" ]; then
    echo "  PASS - POST /api/order returned 202"
    PASS=$((PASS + 1))
else
    echo "  FAIL - POST /api/order returned $ORDER_RESPONSE (expected 202)"
    FAIL=$((FAIL + 1))
fi

# Test 5: SSE Dashboard endpoint
echo ""
echo "TEST 5: Dashboard SSE endpoint"
# SSE endpoints keep the connection open, so curl --max-time will timeout the body.
# Use -D to dump headers to a temp file and extract HTTP status from there.
SSE_HEADER_FILE=$(mktemp)
curl -s -o /dev/null -D "$SSE_HEADER_FILE" --max-time 3 "${BASE_URL}/dashboard/stream" 2>/dev/null
STREAM_RESPONSE=$(head -1 "$SSE_HEADER_FILE" 2>/dev/null | grep -o '[0-9][0-9][0-9]' | head -1)
rm -f "$SSE_HEADER_FILE"
if [ "$STREAM_RESPONSE" = "200" ]; then
    echo "  PASS - /dashboard/stream returned 200"
    PASS=$((PASS + 1))
else
    echo "  FAIL - /dashboard/stream returned $STREAM_RESPONSE (expected 200)"
    FAIL=$((FAIL + 1))
fi

# Test 6: Static resources accessible
echo ""
echo "TEST 6: Static CSS resource"
CSS_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/css/cafe.css" 2>/dev/null)
if [ "$CSS_RESPONSE" = "200" ]; then
    echo "  PASS - /css/cafe.css returned 200"
    PASS=$((PASS + 1))
else
    echo "  FAIL - /css/cafe.css returned $CSS_RESPONSE (expected 200)"
    FAIL=$((FAIL + 1))
fi

# Test 7: Place an order with kitchen items
echo ""
echo "TEST 7: Place order with kitchen items"
ORDER_RESPONSE2=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${BASE_URL}/api/order" \
    -H "Content-Type: application/json" \
    -d '{
        "id": "test-order-002",
        "orderSource": "WEB",
        "storeId": "ATLANTA",
        "rewardsId": null,
        "baristaItems": null,
        "kitchenItems": [{"item": "CAKEPOP", "name": "Jane", "price": 2.50}],
        "commandType": "PLACE_ORDER"
    }' 2>/dev/null)
if [ "$ORDER_RESPONSE2" = "202" ]; then
    echo "  PASS - POST /api/order (kitchen) returned 202"
    PASS=$((PASS + 1))
else
    echo "  FAIL - POST /api/order (kitchen) returned $ORDER_RESPONSE2 (expected 202)"
    FAIL=$((FAIL + 1))
fi

# Test 8: Actuator info endpoint
echo ""
echo "TEST 8: Info endpoint"
INFO_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/info" 2>/dev/null)
if [ "$INFO_RESPONSE" = "200" ]; then
    echo "  PASS - /actuator/info returned 200"
    PASS=$((PASS + 1))
else
    echo "  FAIL - /actuator/info returned $INFO_RESPONSE (expected 200)"
    FAIL=$((FAIL + 1))
fi

echo ""
echo "========================================="
echo "  RESULTS: $PASS passed, $FAIL failed"
echo "========================================="

if [ $FAIL -gt 0 ]; then
    exit 1
fi
exit 0
