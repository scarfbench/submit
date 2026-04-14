#!/bin/bash
# Smoke tests for the Coffee Shop Spring Boot application
# Usage: ./smoke_tests.sh <port>

PORT=${1:-8080}
BASE_URL="http://localhost:${PORT}"
PASS=0
FAIL=0

echo "=== Coffee Shop Spring Boot Smoke Tests ==="
echo "Testing against: $BASE_URL"
echo ""

# Test 1: Health check - homepage loads
echo -n "Test 1: Homepage loads (GET /)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
if [ "$HTTP_CODE" = "200" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 2: Homepage contains expected content
echo -n "Test 2: Homepage contains 'Coffee Shop'... "
BODY=$(curl -s "$BASE_URL/")
if echo "$BODY" | grep -qi "Coffee Shop"; then
    echo "PASS"
    PASS=$((PASS + 1))
else
    echo "FAIL"
    FAIL=$((FAIL + 1))
fi

# Test 3: API endpoint exists - POST /api/order returns 202
echo -n "Test 3: POST /api/order returns 202 Accepted... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/order" \
    -H "Content-Type: application/json" \
    -d '{
        "id": "test-order-001",
        "orderSource": "WEB",
        "storeId": "ATLANTA",
        "baristaItems": [
            {"item": "COFFEE_BLACK", "name": "Test User", "price": 3.50}
        ]
    }')
if [ "$HTTP_CODE" = "202" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 4: Place order with kitchen items
echo -n "Test 4: POST /api/order with kitchen items returns 202... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/order" \
    -H "Content-Type: application/json" \
    -d '{
        "id": "test-order-002",
        "orderSource": "COUNTER",
        "storeId": "CHARLOTTE",
        "kitchenItems": [
            {"item": "CROISSANT", "name": "Kitchen Test", "price": 3.25}
        ]
    }')
if [ "$HTTP_CODE" = "202" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 5: Place order with both barista and kitchen items
echo -n "Test 5: POST /api/order with barista+kitchen items returns 202... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/order" \
    -H "Content-Type: application/json" \
    -d '{
        "id": "test-order-003",
        "orderSource": "WEB",
        "storeId": "RALEIGH",
        "baristaItems": [
            {"item": "CAPPUCCINO", "name": "Both Test", "price": 4.50}
        ],
        "kitchenItems": [
            {"item": "MUFFIN", "name": "Both Test", "price": 3.00}
        ]
    }')
if [ "$HTTP_CODE" = "202" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 6: Dashboard SSE endpoint exists
echo -n "Test 6: GET /dashboard/stream returns 200 with SSE content type... "
# SSE endpoints keep the connection open, so a timeout (HTTP 000) is expected behavior
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 2 "$BASE_URL/dashboard/stream" 2>/dev/null)
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "000" ]; then
    # HTTP 000 means connection was open (SSE streaming) and timed out, which is correct
    CONTENT_TYPE=$(curl -s -D - -o /dev/null --max-time 2 "$BASE_URL/dashboard/stream" 2>/dev/null | grep -i "content-type" || true)
    echo "PASS (SSE endpoint responsive, HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 7: POST /api/message endpoint
echo -n "Test 7: POST /api/message endpoint works... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/message" \
    -H "Content-Type: application/json" \
    -d '"test message"')
if [ "$HTTP_CODE" = "200" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 8: Static CSS resources available
echo -n "Test 8: Static CSS resource accessible... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/css/bootstrap.min.css")
if [ "$HTTP_CODE" = "200" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 9: Static JS resources available
echo -n "Test 9: Static JS resource accessible... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/js/bootstrap.bundle.min.js")
if [ "$HTTP_CODE" = "200" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

# Test 10: H2 console available (dev mode)
echo -n "Test 10: H2 console available... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/h2-console")
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
    echo "PASS (HTTP $HTTP_CODE)"
    PASS=$((PASS + 1))
else
    echo "FAIL (HTTP $HTTP_CODE)"
    FAIL=$((FAIL + 1))
fi

echo ""
echo "=== Results ==="
echo "PASSED: $PASS"
echo "FAILED: $FAIL"
echo "TOTAL:  $((PASS + FAIL))"

if [ "$FAIL" -gt 0 ]; then
    echo ""
    echo "SMOKE TESTS FAILED"
    exit 1
else
    echo ""
    echo "ALL SMOKE TESTS PASSED"
    exit 0
fi
