#!/bin/bash
# Smoke tests for PetClinic application (Jakarta EE / WildFly)
# Tests core functionality: welcome page, owners, vets, error handling

BASE_URL="${1:-http://localhost:8080/petclinic}"
PASS=0
FAIL=0
TOTAL=0

pass() {
    PASS=$((PASS + 1))
    TOTAL=$((TOTAL + 1))
    echo "  PASS: $1"
}

fail() {
    FAIL=$((FAIL + 1))
    TOTAL=$((TOTAL + 1))
    echo "  FAIL: $1 - $2"
}

test_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="$3"
    local expected_content="$4"

    local http_code

    http_code=$(curl -s -o /tmp/smoke_response.html -w "%{http_code}" -L "$url" 2>/dev/null || echo "000")

    if [ "$http_code" = "$expected_status" ]; then
        if [ -n "$expected_content" ]; then
            if grep -qi "$expected_content" /tmp/smoke_response.html 2>/dev/null; then
                pass "$name (status=$http_code, content matched)"
            else
                fail "$name" "Status $http_code OK, but content '$expected_content' not found"
            fi
        else
            pass "$name (status=$http_code)"
        fi
    else
        fail "$name" "Expected status $expected_status, got $http_code"
    fi
}

test_json_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="$3"
    local expected_content="$4"

    local http_code

    http_code=$(curl -s -o /tmp/smoke_response.json -w "%{http_code}" -H "Accept: application/json" "$url" 2>/dev/null || echo "000")

    if [ "$http_code" = "$expected_status" ]; then
        if [ -n "$expected_content" ]; then
            if grep -qi "$expected_content" /tmp/smoke_response.json 2>/dev/null; then
                pass "$name (status=$http_code, content matched)"
            else
                fail "$name" "Status $http_code OK, but content '$expected_content' not found"
            fi
        else
            pass "$name (status=$http_code)"
        fi
    else
        fail "$name" "Expected status $expected_status, got $http_code"
    fi
}

test_post() {
    local name="$1"
    local url="$2"
    local data="$3"
    local expected_status="$4"
    local expected_content="$5"

    local http_code

    http_code=$(curl -s -o /tmp/smoke_response.html -w "%{http_code}" -L -X POST -d "$data" -H "Content-Type: application/x-www-form-urlencoded" "$url" 2>/dev/null || echo "000")

    if [ "$http_code" = "$expected_status" ]; then
        if [ -n "$expected_content" ]; then
            if grep -qi "$expected_content" /tmp/smoke_response.html 2>/dev/null; then
                pass "$name (status=$http_code, content matched)"
            else
                fail "$name" "Status $http_code OK, but content '$expected_content' not found"
            fi
        else
            pass "$name (status=$http_code)"
        fi
    else
        fail "$name" "Expected status $expected_status, got $http_code"
    fi
}

echo "============================================"
echo "PetClinic Smoke Tests"
echo "Base URL: $BASE_URL"
echo "============================================"

echo ""
echo "--- Welcome Page ---"
test_endpoint "GET /" "$BASE_URL/" "200" "Welcome"

echo ""
echo "--- Owner Pages ---"
test_endpoint "GET /owners/find" "$BASE_URL/owners/find" "200" "Find Owners"
test_endpoint "GET /owners?lastName=" "$BASE_URL/owners?lastName=" "200" "owner"
test_endpoint "GET /owners?lastName=Davis" "$BASE_URL/owners?lastName=Davis" "200" "Davis"
test_endpoint "GET /owners/new" "$BASE_URL/owners/new" "200" "Owner"
test_endpoint "GET /owners/1" "$BASE_URL/owners/1" "200" "George"
test_endpoint "GET /owners/1/edit" "$BASE_URL/owners/1/edit" "200" "George"

echo ""
echo "--- Pet Pages ---"
test_endpoint "GET /owners/1/pets/new" "$BASE_URL/owners/1/pets/new" "200" "Pet"
test_endpoint "GET /owners/1/pets/1/edit" "$BASE_URL/owners/1/pets/1/edit" "200" "Pet"

echo ""
echo "--- Visit Pages ---"
test_endpoint "GET /owners/1/pets/1/visits/new" "$BASE_URL/owners/1/pets/1/visits/new" "200" "Visit"

echo ""
echo "--- Vet Pages ---"
test_endpoint "GET /vets.html" "$BASE_URL/vets.html" "200" "Veterinarian"

echo ""
echo "--- API Endpoints ---"
test_json_endpoint "GET /api/vets (JSON)" "$BASE_URL/api/vets" "200" "vetList"
test_json_endpoint "GET /api/owners/list (JSON)" "$BASE_URL/api/owners/list" "200" "George"

echo ""
echo "--- Error Handling ---"
test_endpoint "GET /oups" "$BASE_URL/oups" "200" ""

echo ""
echo "--- Create Owner ---"
test_post "POST /owners/new" "$BASE_URL/owners/new" "firstName=Test&lastName=Owner&address=123+Test+St&city=TestCity&telephone=1234567890" "200" "Test"

echo ""
echo "============================================"
echo "Results: $PASS passed, $FAIL failed, $TOTAL total"
echo "============================================"

# Clean up
rm -f /tmp/smoke_response.html /tmp/smoke_response.json

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
exit 0
