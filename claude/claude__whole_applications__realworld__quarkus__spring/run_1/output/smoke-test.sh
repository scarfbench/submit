#!/bin/bash
# Smoke tests for RealWorld API (Spring Boot migration)
# Tests: health, user registration, login, articles, comments, tags, profiles

set -e

BASE_URL="${1:-http://localhost:8080}"
API_URL="${BASE_URL}/api"
PASS=0
FAIL=0
TOTAL=0

red() { echo -e "\033[31m$1\033[0m"; }
green() { echo -e "\033[32m$1\033[0m"; }

assert_status() {
  local test_name="$1"
  local expected="$2"
  local actual="$3"
  TOTAL=$((TOTAL + 1))
  if [ "$actual" -eq "$expected" ]; then
    green "PASS: $test_name (HTTP $actual)"
    PASS=$((PASS + 1))
  else
    red "FAIL: $test_name (expected HTTP $expected, got HTTP $actual)"
    FAIL=$((FAIL + 1))
  fi
}

assert_contains() {
  local test_name="$1"
  local expected="$2"
  local body="$3"
  TOTAL=$((TOTAL + 1))
  if echo "$body" | grep -q "$expected"; then
    green "PASS: $test_name (body contains '$expected')"
    PASS=$((PASS + 1))
  else
    red "FAIL: $test_name (body missing '$expected')"
    red "  Body: $body"
    FAIL=$((FAIL + 1))
  fi
}

echo "=== RealWorld API Smoke Tests ==="
echo "Target: $API_URL"
echo ""

# Wait for app to be ready (up to 60s)
echo "Waiting for application to become ready..."
for i in $(seq 1 60); do
  if curl -sf "${API_URL}/actuator/health" > /dev/null 2>&1; then
    echo "Application is ready!"
    break
  fi
  if [ "$i" -eq 60 ]; then
    red "Application did not become ready within 60 seconds"
    exit 1
  fi
  sleep 1
done
echo ""

# 1. Health check
echo "--- Test: Health Check ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/actuator/health")
BODY=$(cat /tmp/smoke_body)
assert_status "Health endpoint returns 200" 200 "$RESP"
assert_contains "Health status UP" "UP" "$BODY"

# 2. Get Tags (empty initially)
echo ""
echo "--- Test: Get Tags ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/tags")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/tags returns 200" 200 "$RESP"
assert_contains "Tags response has tags field" "tags" "$BODY"

# 3. Register a new user
TIMESTAMP=$(date +%s)
USERNAME="testuser${TIMESTAMP}"
EMAIL="testuser${TIMESTAMP}@example.com"
echo ""
echo "--- Test: Register User ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/users" \
  -H "Content-Type: application/json" \
  -d "{\"user\":{\"username\":\"${USERNAME}\",\"email\":\"${EMAIL}\",\"password\":\"password123\"}}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/users returns 201" 201 "$RESP"
assert_contains "Register response has user" "user" "$BODY"
assert_contains "Register response has token" "token" "$BODY"
TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "")
echo "  Token: ${TOKEN:0:20}..."

# 4. Login
echo ""
echo "--- Test: Login ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"user\":{\"email\":\"${EMAIL}\",\"password\":\"password123\"}}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/users/login returns 200" 200 "$RESP"
assert_contains "Login response has token" "token" "$BODY"
TOKEN=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['user']['token'])" 2>/dev/null || echo "$TOKEN")

# 5. Get current user
echo ""
echo "--- Test: Get Current User ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/user" \
  -H "Authorization: Token ${TOKEN}")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/user returns 200" 200 "$RESP"
assert_contains "Current user has email" "${EMAIL}" "$BODY"

# 6. Update user
echo ""
echo "--- Test: Update User ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X PUT "${API_URL}/user" \
  -H "Authorization: Token ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"user\":{\"bio\":\"I am a test user\"}}")
BODY=$(cat /tmp/smoke_body)
assert_status "PUT /api/user returns 200" 200 "$RESP"
assert_contains "Updated user has bio" "I am a test user" "$BODY"

# 7. Create article
echo ""
echo "--- Test: Create Article ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/articles" \
  -H "Authorization: Token ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"article\":{\"title\":\"Test Article ${TIMESTAMP}\",\"description\":\"Test description\",\"body\":\"Test body content\",\"tagList\":[\"test\",\"smoke\"]}}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/articles returns 201" 201 "$RESP"
assert_contains "Article has slug" "slug" "$BODY"
SLUG=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['article']['slug'])" 2>/dev/null || echo "")
echo "  Slug: $SLUG"

# 8. Get article by slug
echo ""
echo "--- Test: Get Article by Slug ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/articles/${SLUG}")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/articles/{slug} returns 200" 200 "$RESP"
assert_contains "Article has title" "Test Article" "$BODY"

# 9. List articles
echo ""
echo "--- Test: List Articles ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/articles")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/articles returns 200" 200 "$RESP"
assert_contains "Articles response has articles" "articles" "$BODY"
assert_contains "Articles response has articlesCount" "articlesCount" "$BODY"

# 10. Add comment
echo ""
echo "--- Test: Add Comment ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/articles/${SLUG}/comments" \
  -H "Authorization: Token ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"comment\":{\"body\":\"Great article!\"}}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/articles/{slug}/comments returns 200" 200 "$RESP"
assert_contains "Comment has body" "Great article" "$BODY"
COMMENT_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['comment']['id'])" 2>/dev/null || echo "")

# 11. Get comments
echo ""
echo "--- Test: Get Comments ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/articles/${SLUG}/comments")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/articles/{slug}/comments returns 200" 200 "$RESP"
assert_contains "Comments response has comments" "comments" "$BODY"

# 12. Favorite article
echo ""
echo "--- Test: Favorite Article ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/articles/${SLUG}/favorite" \
  -H "Authorization: Token ${TOKEN}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/articles/{slug}/favorite returns 200" 200 "$RESP"
assert_contains "Favorited article" "favorited" "$BODY"

# 13. Unfavorite article
echo ""
echo "--- Test: Unfavorite Article ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X DELETE "${API_URL}/articles/${SLUG}/favorite" \
  -H "Authorization: Token ${TOKEN}")
BODY=$(cat /tmp/smoke_body)
assert_status "DELETE /api/articles/{slug}/favorite returns 200" 200 "$RESP"

# 14. Get profile
echo ""
echo "--- Test: Get Profile ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/profiles/${USERNAME}")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/profiles/{username} returns 200" 200 "$RESP"
assert_contains "Profile has username" "${USERNAME}" "$BODY"

# 15. Register second user for follow test
USERNAME2="testuser2_${TIMESTAMP}"
EMAIL2="testuser2_${TIMESTAMP}@example.com"
echo ""
echo "--- Test: Register Second User ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/users" \
  -H "Content-Type: application/json" \
  -d "{\"user\":{\"username\":\"${USERNAME2}\",\"email\":\"${EMAIL2}\",\"password\":\"password123\"}}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/users (second user) returns 201" 201 "$RESP"

# 16. Follow user
echo ""
echo "--- Test: Follow User ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X POST "${API_URL}/profiles/${USERNAME2}/follow" \
  -H "Authorization: Token ${TOKEN}")
BODY=$(cat /tmp/smoke_body)
assert_status "POST /api/profiles/{username}/follow returns 200" 200 "$RESP"
assert_contains "Following is true" "\"following\":true" "$BODY"

# 17. Unfollow user
echo ""
echo "--- Test: Unfollow User ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X DELETE "${API_URL}/profiles/${USERNAME2}/follow" \
  -H "Authorization: Token ${TOKEN}")
BODY=$(cat /tmp/smoke_body)
assert_status "DELETE /api/profiles/{username}/follow returns 200" 200 "$RESP"

# 18. Feed (authenticated)
echo ""
echo "--- Test: Feed ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/articles/feed" \
  -H "Authorization: Token ${TOKEN}")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/articles/feed returns 200" 200 "$RESP"
assert_contains "Feed has articles" "articles" "$BODY"

# 19. Delete comment
if [ -n "$COMMENT_ID" ]; then
  echo ""
  echo "--- Test: Delete Comment ---"
  RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X DELETE "${API_URL}/articles/${SLUG}/comments/${COMMENT_ID}" \
    -H "Authorization: Token ${TOKEN}")
  assert_status "DELETE /api/articles/{slug}/comments/{id} returns 200" 200 "$RESP"
fi

# 20. Update article
echo ""
echo "--- Test: Update Article ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X PUT "${API_URL}/articles/${SLUG}" \
  -H "Authorization: Token ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"article\":{\"title\":\"Updated Title ${TIMESTAMP}\"}}")
BODY=$(cat /tmp/smoke_body)
assert_status "PUT /api/articles/{slug} returns 200" 200 "$RESP"

# 21. Delete article
echo ""
echo "--- Test: Delete Article ---"
# Get the updated slug first
UPDATED_SLUG=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['article']['slug'])" 2>/dev/null || echo "$SLUG")
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" -X DELETE "${API_URL}/articles/${UPDATED_SLUG}" \
  -H "Authorization: Token ${TOKEN}")
assert_status "DELETE /api/articles/{slug} returns 200" 200 "$RESP"

# 22. Get tags after operations
echo ""
echo "--- Test: Get Tags After Operations ---"
RESP=$(curl -s -o /tmp/smoke_body -w "%{http_code}" "${API_URL}/tags")
BODY=$(cat /tmp/smoke_body)
assert_status "GET /api/tags returns 200 (after operations)" 200 "$RESP"
assert_contains "Tags include test tag" "test" "$BODY"

# Summary
echo ""
echo "=================================="
echo "  SMOKE TEST RESULTS"
echo "=================================="
echo "  Total: $TOTAL"
green "  Passed: $PASS"
if [ "$FAIL" -gt 0 ]; then
  red "  Failed: $FAIL"
  exit 1
else
  echo "  Failed: $FAIL"
  green "  ALL TESTS PASSED!"
  exit 0
fi
