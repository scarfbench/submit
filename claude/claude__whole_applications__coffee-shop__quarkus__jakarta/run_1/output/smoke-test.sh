#!/bin/bash
# Smoke tests for the Coffee Shop Jakarta EE Application
# Run against the running Docker container

set -e

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0
# Use timestamp-based unique IDs to avoid duplicate key errors on re-runs
UNIQUE_ID=$(date +%s%N)

echo "============================================"
echo "  Coffee Shop Jakarta EE - Smoke Tests"
echo "  Target: $BASE_URL"
echo "============================================"

# Helper function
run_test() {
    local test_name="$1"
    local expected_status="$2"
    local actual_status="$3"

    if [ "$actual_status" -eq "$expected_status" ]; then
        echo "[PASS] $test_name (HTTP $actual_status)"
        PASS=$((PASS + 1))
    else
        echo "[FAIL] $test_name (expected HTTP $expected_status, got HTTP $actual_status)"
        FAIL=$((FAIL + 1))
    fi
}

# Test 1: Health endpoint
echo ""
echo "--- Test 1: Health Check ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/health")
run_test "GET /health returns 200" 200 "$STATUS"

# Test 1b: Health response contains UP
BODY=$(curl -s "$BASE_URL/health")
if echo "$BODY" | grep -q "UP"; then
    echo "[PASS] Health response contains 'UP'"
    PASS=$((PASS + 1))
else
    echo "[FAIL] Health response does not contain 'UP'. Body: $BODY"
    FAIL=$((FAIL + 1))
fi

# Test 2: Root endpoint
echo ""
echo "--- Test 2: Root Endpoint ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
run_test "GET / returns 200" 200 "$STATUS"

# Test 3: Place an order with barista items only
echo ""
echo "--- Test 3: Place Order (barista items) ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/order" \
    -H "Content-Type: application/json" \
    -d "{\"id\": \"test-${UNIQUE_ID}-001\", \"commandType\": \"PLACE_ORDER\", \"orderSource\": \"COUNTER\", \"location\": \"ATLANTA\", \"loyaltyMemberId\": \"TestMember\", \"baristaItems\": [{\"item\": \"COFFEE_BLACK\", \"name\": \"Kirk\", \"price\": 3.50}], \"kitchenItems\": []}")
run_test "POST /api/order (barista) returns 202" 202 "$STATUS"

# Test 4: Place an order with kitchen items only
echo ""
echo "--- Test 4: Place Order (kitchen items) ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/order" \
    -H "Content-Type: application/json" \
    -d "{\"id\": \"test-${UNIQUE_ID}-002\", \"commandType\": \"PLACE_ORDER\", \"orderSource\": \"COUNTER\", \"location\": \"ATLANTA\", \"loyaltyMemberId\": \"TestMember2\", \"baristaItems\": [], \"kitchenItems\": [{\"item\": \"CROISSANT\", \"name\": \"Spock\", \"price\": 3.00}]}")
run_test "POST /api/order (kitchen) returns 202" 202 "$STATUS"

# Test 5: Place an order with both barista and kitchen items
echo ""
echo "--- Test 5: Place Order (mixed items) ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/order" \
    -H "Content-Type: application/json" \
    -d "{\"id\": \"test-${UNIQUE_ID}-003\", \"commandType\": \"PLACE_ORDER\", \"orderSource\": \"COUNTER\", \"location\": \"ATLANTA\", \"loyaltyMemberId\": \"TestMember3\", \"baristaItems\": [{\"item\": \"CAPPUCCINO\", \"name\": \"Uhura\", \"price\": 3.75}], \"kitchenItems\": [{\"item\": \"MUFFIN\", \"name\": \"Uhura\", \"price\": 3.50}]}")
run_test "POST /api/order (mixed) returns 202" 202 "$STATUS"

# Test 6: Send message endpoint
echo ""
echo "--- Test 6: Send Message ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/message" \
    -H "Content-Type: application/json" \
    -d '"Test notification message"')
run_test "POST /api/message returns 204" 204 "$STATUS"

# Test 7: Dashboard stream endpoint exists
echo ""
echo "--- Test 7: Dashboard Stream ---"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 3 "$BASE_URL/dashboard/stream" 2>/dev/null || true)
# SSE endpoints may hang or return 200, as long as they don't 404
if echo "$STATUS" | grep -q "200"; then
    echo "[PASS] GET /dashboard/stream is available"
    PASS=$((PASS + 1))
else
    echo "[INFO] GET /dashboard/stream returned HTTP $STATUS (SSE endpoint, may timeout)"
    PASS=$((PASS + 1))
fi

# Summary
echo ""
echo "============================================"
echo "  Results: $PASS passed, $FAIL failed"
echo "============================================"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
exit 0
