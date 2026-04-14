#!/bin/bash
# Smoke tests for Cargo Tracker Jakarta EE migration
# Tests basic HTTP endpoints to verify the application is running correctly

set -e

BASE_URL="${1:-http://localhost:8080/cargo-tracker}"
PASS=0
FAIL=0
TOTAL=0

test_endpoint() {
  local description="$1"
  local url="$2"
  local expected_status="${3:-200}"
  local expected_content="$4"

  TOTAL=$((TOTAL + 1))

  echo -n "TEST $TOTAL: $description ... "

  HTTP_RESPONSE=$(curl -s -o /tmp/smoke_response_body -w "%{http_code}" --max-time 30 "$url" 2>/dev/null) || {
    echo "FAIL (connection error)"
    FAIL=$((FAIL + 1))
    return 1
  }

  RESPONSE_BODY=$(cat /tmp/smoke_response_body)

  if [ "$HTTP_RESPONSE" != "$expected_status" ]; then
    echo "FAIL (expected status $expected_status, got $HTTP_RESPONSE)"
    FAIL=$((FAIL + 1))
    return 1
  fi

  if [ -n "$expected_content" ]; then
    if echo "$RESPONSE_BODY" | grep -q "$expected_content"; then
      echo "PASS"
      PASS=$((PASS + 1))
    else
      echo "FAIL (expected content '$expected_content' not found)"
      FAIL=$((FAIL + 1))
      return 1
    fi
  else
    echo "PASS"
    PASS=$((PASS + 1))
  fi

  return 0
}

echo "========================================="
echo "Cargo Tracker Smoke Tests"
echo "Base URL: $BASE_URL"
echo "========================================="
echo ""

# Test 1: Main page loads (JSF index page)
test_endpoint "Main page loads" "$BASE_URL/" "200"

# Test 2: REST API - Graph traversal service
test_endpoint "Graph traversal REST API" \
  "$BASE_URL/rest/graph-traversal/shortest-path?origin=CNHKG&destination=FIHEL" \
  "200" "transitPaths"

# Test 3: Admin dashboard page (JSF)
test_endpoint "Admin dashboard page" "$BASE_URL/admin/dashboard.xhtml" "200"

# Test 4: Public tracking page
test_endpoint "Public tracking page" "$BASE_URL/public/track.xhtml" "200"

# Test 5: Event logger page
test_endpoint "Event logger page" "$BASE_URL/event-logger/index.xhtml" "200"

# Test 6: REST API returns JSON with transit paths
test_endpoint "REST API returns JSON transit paths" \
  "$BASE_URL/rest/graph-traversal/shortest-path?origin=USNYC&destination=SESTO" \
  "200" "transitEdges"

# Test 7: About page
test_endpoint "Public about page" "$BASE_URL/public/about.xhtml" "200"

# Test 8: Admin about page
test_endpoint "Admin about page" "$BASE_URL/admin/about.xhtml" "200"

# Test 9: Cargo SSE endpoint exists
test_endpoint "Cargo REST endpoint" "$BASE_URL/rest/cargo" "200"

echo ""
echo "========================================="
echo "Results: $PASS passed, $FAIL failed, $TOTAL total"
echo "========================================="

if [ "$FAIL" -gt 0 ]; then
  echo "SMOKE TESTS: SOME FAILURES"
  exit 1
else
  echo "SMOKE TESTS: ALL PASSED"
  exit 0
fi
