#!/usr/bin/env bash
#
# Smoke tests for the Spring Boot Petclinic REST API
# Usage: ./smoke.sh <base_url>
# Example: ./smoke.sh http://localhost:8080/petclinic
#

set -euo pipefail

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
    echo "  FAIL: $1"
    if [ -n "${2:-}" ]; then
        echo "        $2"
    fi
}

check_status() {
    local url="$1"
    local expected_status="${2:-200}"
    local description="$3"

    local http_code
    local body
    body=$(curl -s -o /tmp/smoke_body.txt -w "%{http_code}" "$url" 2>/dev/null || echo "000")
    http_code="$body"
    body=$(cat /tmp/smoke_body.txt 2>/dev/null || echo "")

    if [ "$http_code" = "$expected_status" ]; then
        pass "$description (HTTP $http_code)"
    else
        fail "$description" "Expected HTTP $expected_status, got HTTP $http_code"
    fi
    echo "$body" > /tmp/smoke_last_body.txt
}

check_json_array_not_empty() {
    local url="$1"
    local description="$2"

    local http_code
    local body
    body=$(curl -s -o /tmp/smoke_body.txt -w "%{http_code}" "$url" 2>/dev/null || echo "000")
    http_code="$body"
    body=$(cat /tmp/smoke_body.txt 2>/dev/null || echo "")

    if [ "$http_code" != "200" ]; then
        fail "$description" "Expected HTTP 200, got HTTP $http_code"
        return
    fi

    # Check it's a non-empty JSON array
    if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert isinstance(d,list) and len(d)>0" 2>/dev/null; then
        pass "$description (HTTP 200, non-empty array, ${#body} bytes)"
    else
        fail "$description" "Response is not a non-empty JSON array"
    fi
}

check_json_object_has_field() {
    local url="$1"
    local field="$2"
    local description="$3"

    local http_code
    local body
    body=$(curl -s -o /tmp/smoke_body.txt -w "%{http_code}" "$url" 2>/dev/null || echo "000")
    http_code="$body"
    body=$(cat /tmp/smoke_body.txt 2>/dev/null || echo "")

    if [ "$http_code" != "200" ]; then
        fail "$description" "Expected HTTP 200, got HTTP $http_code"
        return
    fi

    if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert '$field' in d" 2>/dev/null; then
        pass "$description (HTTP 200, has '$field')"
    else
        fail "$description" "Response JSON missing field '$field'"
    fi
}

echo "============================================"
echo "Smoke Tests for Petclinic REST API"
echo "Base URL: $BASE_URL"
echo "============================================"
echo ""

# ---- Actuator Health ----
echo "[Actuator]"
check_json_object_has_field "$BASE_URL/actuator/health" "status" "GET /actuator/health"
echo ""

# ---- Owner Endpoints ----
echo "[Owner]"
check_json_array_not_empty "$BASE_URL/rest/owner/list" "GET /rest/owner/list"
check_json_object_has_field "$BASE_URL/rest/owner/1" "firstName" "GET /rest/owner/1"
check_json_object_has_field "$BASE_URL/rest/owner/2" "lastName" "GET /rest/owner/2"
echo ""

# ---- PetType Endpoints ----
echo "[PetType]"
check_json_array_not_empty "$BASE_URL/rest/petType/list" "GET /rest/petType/list"
check_json_object_has_field "$BASE_URL/rest/petType/1" "name" "GET /rest/petType/1"
echo ""

# ---- Specialty Endpoints ----
echo "[Specialty]"
check_json_array_not_empty "$BASE_URL/rest/specialty/list" "GET /rest/specialty/list"
check_json_object_has_field "$BASE_URL/rest/specialty/1" "name" "GET /rest/specialty/1"
echo ""

# ---- Vet Endpoints ----
echo "[Vet]"
check_json_array_not_empty "$BASE_URL/rest/vet/list" "GET /rest/vet/list"
check_json_object_has_field "$BASE_URL/rest/vet/1" "firstName" "GET /rest/vet/1"
echo ""

# ---- Visit Endpoints ----
echo "[Visit]"
check_json_array_not_empty "$BASE_URL/rest/visit/list" "GET /rest/visit/list"
check_json_object_has_field "$BASE_URL/rest/visit/1" "description" "GET /rest/visit/1"
echo ""

# ---- Data Validation ----
echo "[Data Validation]"

# Verify owner data
body=$(curl -s "$BASE_URL/rest/owner/1" 2>/dev/null || echo "{}")
if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert d.get('firstName')=='Thomas' and d.get('lastName')=='Woehlke'" 2>/dev/null; then
    pass "Owner 1 is Thomas Woehlke"
else
    fail "Owner 1 data validation" "Expected Thomas Woehlke"
fi

# Verify pet types count
body=$(curl -s "$BASE_URL/rest/petType/list" 2>/dev/null || echo "[]")
if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert len(d)==12" 2>/dev/null; then
    pass "PetType list has 12 entries"
else
    fail "PetType list count" "Expected 12 pet types"
fi

# Verify specialties count
body=$(curl -s "$BASE_URL/rest/specialty/list" 2>/dev/null || echo "[]")
if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert len(d)==7" 2>/dev/null; then
    pass "Specialty list has 7 entries"
else
    fail "Specialty list count" "Expected 7 specialties"
fi

# Verify vet count
body=$(curl -s "$BASE_URL/rest/vet/list" 2>/dev/null || echo "[]")
if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert len(d)==2" 2>/dev/null; then
    pass "Vet list has 2 entries"
else
    fail "Vet list count" "Expected 2 vets"
fi

# Verify visit count
body=$(curl -s "$BASE_URL/rest/visit/list" 2>/dev/null || echo "[]")
if echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); assert len(d)==7" 2>/dev/null; then
    pass "Visit list has 7 entries"
else
    fail "Visit list count" "Expected 7 visits"
fi

echo ""
echo "============================================"
echo "Results: $PASS passed, $FAIL failed, $TOTAL total"
echo "============================================"

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi

exit 0
