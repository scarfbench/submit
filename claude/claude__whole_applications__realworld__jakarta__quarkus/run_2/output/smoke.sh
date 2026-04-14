#!/bin/bash
# Smoke tests for RealWorld API - Quarkus migration
set -e

BASE_URL="${1:-http://localhost:8080}"
API_URL="${BASE_URL}/api"
PASS=0
FAIL=0
TOTAL=0

log_result() {
    TOTAL=$((TOTAL + 1))
    if [ "$1" == "PASS" ]; then
        PASS=$((PASS + 1))
        echo "[PASS] $2"
    else
        FAIL=$((FAIL + 1))
        echo "[FAIL] $2 - $3"
    fi
}

echo "=== RealWorld API Smoke Tests ==="
echo "Testing against: $API_URL"
echo ""

# Test 1: Health/tags endpoint (no auth required)
echo "--- Test 1: GET /api/tags ---"
RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/tags")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" == "200" ]; then
    log_result "PASS" "GET /api/tags returns 200"
else
    log_result "FAIL" "GET /api/tags" "Expected 200, got $HTTP_CODE. Body: $BODY"
fi

# Test 2: GET articles (no auth required)
echo "--- Test 2: GET /api/articles ---"
RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/articles")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" == "200" ]; then
    log_result "PASS" "GET /api/articles returns 200"
else
    log_result "FAIL" "GET /api/articles" "Expected 200, got $HTTP_CODE. Body: $BODY"
fi

# Test 3: Register a new user
echo "--- Test 3: POST /api/users (register) ---"
TIMESTAMP=$(date +%s)
REGISTER_BODY="{\"user\":{\"username\":\"testuser${TIMESTAMP}\",\"email\":\"test${TIMESTAMP}@example.com\",\"password\":\"password123\"}}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/users" \
    -H "Content-Type: application/json" \
    -d "$REGISTER_BODY")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" == "201" ]; then
    log_result "PASS" "POST /api/users returns 201"
    TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "")
else
    log_result "FAIL" "POST /api/users" "Expected 201, got $HTTP_CODE. Body: $BODY"
    TOKEN=""
fi

# Test 4: Login with the new user
echo "--- Test 4: POST /api/users/login ---"
LOGIN_BODY="{\"user\":{\"email\":\"test${TIMESTAMP}@example.com\",\"password\":\"password123\"}}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/users/login" \
    -H "Content-Type: application/json" \
    -d "$LOGIN_BODY")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" == "200" ]; then
    log_result "PASS" "POST /api/users/login returns 200"
    TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "$TOKEN")
else
    log_result "FAIL" "POST /api/users/login" "Expected 200, got $HTTP_CODE. Body: $BODY"
fi

# Test 5: Get current user (requires auth)
if [ -n "$TOKEN" ]; then
    echo "--- Test 5: GET /api/user (authenticated) ---"
    RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/user" \
        -H "Authorization: Token ${TOKEN}")
    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    if [ "$HTTP_CODE" == "200" ]; then
        log_result "PASS" "GET /api/user returns 200 with auth"
    else
        log_result "FAIL" "GET /api/user" "Expected 200, got $HTTP_CODE. Body: $BODY"
    fi

    # Test 6: Update user
    echo "--- Test 6: PUT /api/user (update bio) ---"
    UPDATE_BODY="{\"user\":{\"bio\":\"Updated bio for smoke test\"}}"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "${API_URL}/user" \
        -H "Content-Type: application/json" \
        -H "Authorization: Token ${TOKEN}" \
        -d "$UPDATE_BODY")
    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    if [ "$HTTP_CODE" == "200" ]; then
        log_result "PASS" "PUT /api/user returns 200"
    else
        log_result "FAIL" "PUT /api/user" "Expected 200, got $HTTP_CODE. Body: $BODY"
    fi

    # Test 7: Create article
    echo "--- Test 7: POST /api/articles (create article) ---"
    ARTICLE_BODY="{\"article\":{\"title\":\"Smoke Test Article ${TIMESTAMP}\",\"description\":\"Test description\",\"body\":\"Test body content\",\"tagList\":[\"test\",\"smoke\"]}}"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/articles" \
        -H "Content-Type: application/json" \
        -H "Authorization: Token ${TOKEN}" \
        -d "$ARTICLE_BODY")
    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    if [ "$HTTP_CODE" == "201" ]; then
        log_result "PASS" "POST /api/articles returns 201"
        SLUG=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['article']['slug'])" 2>/dev/null || echo "")
    else
        log_result "FAIL" "POST /api/articles" "Expected 201, got $HTTP_CODE. Body: $BODY"
        SLUG=""
    fi

    # Test 8: Get article by slug
    if [ -n "$SLUG" ]; then
        echo "--- Test 8: GET /api/articles/{slug} ---"
        RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/articles/${SLUG}")
        HTTP_CODE=$(echo "$RESPONSE" | tail -1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        if [ "$HTTP_CODE" == "200" ]; then
            log_result "PASS" "GET /api/articles/${SLUG} returns 200"
        else
            log_result "FAIL" "GET /api/articles/${SLUG}" "Expected 200, got $HTTP_CODE. Body: $BODY"
        fi

        # Test 9: Add comment to article
        echo "--- Test 9: POST /api/articles/{slug}/comments ---"
        COMMENT_BODY="{\"comment\":{\"body\":\"Smoke test comment\"}}"
        RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/articles/${SLUG}/comments" \
            -H "Content-Type: application/json" \
            -H "Authorization: Token ${TOKEN}" \
            -d "$COMMENT_BODY")
        HTTP_CODE=$(echo "$RESPONSE" | tail -1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        if [ "$HTTP_CODE" == "200" ]; then
            log_result "PASS" "POST /api/articles/${SLUG}/comments returns 200"
        else
            log_result "FAIL" "POST /api/articles/${SLUG}/comments" "Expected 200, got $HTTP_CODE. Body: $BODY"
        fi

        # Test 10: Get comments for article
        echo "--- Test 10: GET /api/articles/{slug}/comments ---"
        RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/articles/${SLUG}/comments")
        HTTP_CODE=$(echo "$RESPONSE" | tail -1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        if [ "$HTTP_CODE" == "200" ]; then
            log_result "PASS" "GET /api/articles/${SLUG}/comments returns 200"
        else
            log_result "FAIL" "GET /api/articles/${SLUG}/comments" "Expected 200, got $HTTP_CODE. Body: $BODY"
        fi

        # Test 11: Favorite article
        echo "--- Test 11: POST /api/articles/{slug}/favorite ---"
        RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/articles/${SLUG}/favorite" \
            -H "Authorization: Token ${TOKEN}")
        HTTP_CODE=$(echo "$RESPONSE" | tail -1)
        BODY=$(echo "$RESPONSE" | sed '$d')
        if [ "$HTTP_CODE" == "200" ]; then
            log_result "PASS" "POST /api/articles/${SLUG}/favorite returns 200"
        else
            log_result "FAIL" "POST /api/articles/${SLUG}/favorite" "Expected 200, got $HTTP_CODE. Body: $BODY"
        fi
    fi

    # Test 12: Get profile
    echo "--- Test 12: GET /api/profiles/{username} ---"
    RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/profiles/testuser${TIMESTAMP}")
    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    if [ "$HTTP_CODE" == "200" ]; then
        log_result "PASS" "GET /api/profiles returns 200"
    else
        log_result "FAIL" "GET /api/profiles" "Expected 200, got $HTTP_CODE. Body: $BODY"
    fi
else
    echo "Skipping authenticated tests - no token available"
    FAIL=$((FAIL + 8))
    TOTAL=$((TOTAL + 8))
fi

# Test 13: Invalid login
echo "--- Test 13: POST /api/users/login (invalid credentials) ---"
INVALID_LOGIN="{\"user\":{\"email\":\"nonexistent@example.com\",\"password\":\"wrongpassword\"}}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/users/login" \
    -H "Content-Type: application/json" \
    -d "$INVALID_LOGIN")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
if [ "$HTTP_CODE" == "401" ] || [ "$HTTP_CODE" == "422" ] || [ "$HTTP_CODE" == "404" ]; then
    log_result "PASS" "POST /api/users/login with invalid creds returns error"
else
    log_result "FAIL" "POST /api/users/login invalid" "Expected 401/422/404, got $HTTP_CODE"
fi

# Test 14: Unauthenticated access to protected resource
echo "--- Test 14: GET /api/user (unauthenticated) ---"
RESPONSE=$(curl -s -w "\n%{http_code}" "${API_URL}/user")
HTTP_CODE=$(echo "$RESPONSE" | tail -1)
if [ "$HTTP_CODE" == "401" ]; then
    log_result "PASS" "GET /api/user without auth returns 401"
else
    log_result "FAIL" "GET /api/user unauth" "Expected 401, got $HTTP_CODE"
fi

echo ""
echo "=== Results ==="
echo "Total: $TOTAL | Passed: $PASS | Failed: $FAIL"

if [ $FAIL -gt 0 ]; then
    echo "SMOKE TESTS: SOME FAILURES"
    exit 1
else
    echo "SMOKE TESTS: ALL PASSED"
    exit 0
fi
