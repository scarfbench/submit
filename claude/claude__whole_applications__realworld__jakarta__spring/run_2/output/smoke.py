#!/usr/bin/env python3
"""Smoke tests for the RealWorld API after migration from Jakarta EE to Spring Boot."""

import json
import time
import sys
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
PASSED = 0
FAILED = 0

def test(name, method, path, body=None, headers=None, expected_status=200, check_body=None):
    global PASSED, FAILED
    url = f"{BASE_URL}{path}"
    if headers is None:
        headers = {}
    headers.setdefault("Content-Type", "application/json")

    data = json.dumps(body).encode("utf-8") if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)

    try:
        resp = urllib.request.urlopen(req)
        status = resp.status
        resp_body = resp.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        status = e.code
        resp_body = e.read().decode("utf-8")
    except Exception as e:
        print(f"  FAIL {name}: Connection error: {e}")
        FAILED += 1
        return None

    if status != expected_status:
        print(f"  FAIL {name}: Expected status {expected_status}, got {status}")
        print(f"       Response: {resp_body[:500]}")
        FAILED += 1
        return None

    result = None
    if resp_body:
        try:
            result = json.loads(resp_body)
        except json.JSONDecodeError:
            result = resp_body

    if check_body and result:
        try:
            check_body(result)
            print(f"  PASS {name}")
            PASSED += 1
        except AssertionError as e:
            print(f"  FAIL {name}: Body check failed: {e}")
            FAILED += 1
            return result
    else:
        print(f"  PASS {name}")
        PASSED += 1

    return result

class AssertionError(Exception):
    pass

def assert_has_key(d, key):
    if key not in d:
        raise AssertionError(f"Missing key: {key}")

def assert_equals(actual, expected):
    if actual != expected:
        raise AssertionError(f"Expected {expected}, got {actual}")

def wait_for_server(max_wait=120):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(f"{BASE_URL}/actuator/health")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print("Server is ready!")
                return True
        except Exception:
            pass
        time.sleep(2)
    print("Server did not start in time!")
    return False

def run_tests():
    global PASSED, FAILED

    print("\n=== RealWorld API Smoke Tests ===\n")

    if not wait_for_server():
        print("ABORT: Server not available")
        sys.exit(1)

    # 1. Health check
    test("Health check", "GET", "/actuator/health",
         check_body=lambda b: assert_has_key(b, "status"))

    # 2. Get tags (empty initially)
    test("Get tags", "GET", "/api/tags",
         check_body=lambda b: assert_has_key(b, "tags"))

    # 3. Get articles (empty initially)
    test("Get articles", "GET", "/api/articles",
         check_body=lambda b: (assert_has_key(b, "articles"), assert_has_key(b, "articlesCount")))

    # 4. Register a user
    ts = str(int(time.time()))
    user1_data = {
        "user": {
            "username": f"testuser{ts}",
            "email": f"test{ts}@example.com",
            "password": "password123"
        }
    }
    result = test("Register user", "POST", "/api/users", body=user1_data, expected_status=201,
                  check_body=lambda b: (assert_has_key(b, "user"), assert_has_key(b["user"], "token")))

    token = None
    if result and "user" in result:
        token = result["user"]["token"]

    # 5. Login with the created user
    login_data = {
        "user": {
            "email": f"test{ts}@example.com",
            "password": "password123"
        }
    }
    result = test("Login user", "POST", "/api/users/login", body=login_data,
                  check_body=lambda b: (assert_has_key(b, "user"), assert_has_key(b["user"], "token")))

    if result and "user" in result:
        token = result["user"]["token"]

    if not token:
        print("  SKIP remaining tests: no auth token available")
        print(f"\n=== Results: {PASSED} passed, {FAILED} failed ===")
        sys.exit(1 if FAILED > 0 else 0)

    auth_headers = {"Authorization": f"Token {token}", "Content-Type": "application/json"}

    # 6. Get current user
    test("Get current user", "GET", "/api/user", headers=auth_headers,
         check_body=lambda b: (assert_has_key(b, "user"), assert_has_key(b["user"], "email")))

    # 7. Update user
    update_data = {"user": {"bio": "I am a test user"}}
    test("Update user", "PUT", "/api/user", body=update_data, headers=auth_headers,
         check_body=lambda b: assert_equals(b["user"]["bio"], "I am a test user"))

    # 8. Create article
    article_data = {
        "article": {
            "title": f"Test Article {ts}",
            "description": "Test Description",
            "body": "Test Body Content",
            "tagList": ["test", "smoke"]
        }
    }
    result = test("Create article", "POST", "/api/articles", body=article_data,
                  headers=auth_headers, expected_status=201,
                  check_body=lambda b: (assert_has_key(b, "article"), assert_has_key(b["article"], "slug")))

    slug = None
    if result and "article" in result:
        slug = result["article"]["slug"]

    # 9. Get article by slug
    if slug:
        test("Get article by slug", "GET", f"/api/articles/{slug}",
             check_body=lambda b: assert_equals(b["article"]["slug"], slug))

    # 10. Get articles with filter
    test("Get articles (with filter)", "GET", f"/api/articles?tag=test",
         check_body=lambda b: assert_has_key(b, "articles"))

    # 11. Update article
    if slug:
        update_article_data = {"article": {"description": "Updated Description"}}
        test("Update article", "PUT", f"/api/articles/{slug}",
             body=update_article_data, headers=auth_headers,
             check_body=lambda b: assert_equals(b["article"]["description"], "Updated Description"))

    # 12. Favorite article
    if slug:
        test("Favorite article", "POST", f"/api/articles/{slug}/favorite",
             headers=auth_headers,
             check_body=lambda b: assert_equals(b["article"]["favorited"], True))

    # 13. Unfavorite article
    if slug:
        test("Unfavorite article", "DELETE", f"/api/articles/{slug}/favorite",
             headers=auth_headers,
             check_body=lambda b: assert_equals(b["article"]["favorited"], False))

    # 14. Add comment
    if slug:
        comment_data = {"comment": {"body": "This is a test comment"}}
        result = test("Add comment", "POST", f"/api/articles/{slug}/comments",
                      body=comment_data, headers=auth_headers,
                      check_body=lambda b: assert_has_key(b, "comment"))

    # 15. Get comments
    if slug:
        test("Get comments", "GET", f"/api/articles/{slug}/comments",
             check_body=lambda b: assert_has_key(b, "comments"))

    # 16. Get tags (should now have tags)
    test("Get tags (populated)", "GET", "/api/tags",
         check_body=lambda b: assert_has_key(b, "tags"))

    # 17. Register second user
    user2_data = {
        "user": {
            "username": f"testuser2_{ts}",
            "email": f"test2_{ts}@example.com",
            "password": "password123"
        }
    }
    result = test("Register second user", "POST", "/api/users", body=user2_data,
                  expected_status=201,
                  check_body=lambda b: assert_has_key(b, "user"))

    # 18. Get profile
    test("Get profile", "GET", f"/api/profiles/testuser{ts}", headers=auth_headers,
         check_body=lambda b: (assert_has_key(b, "profile"), assert_equals(b["profile"]["username"], f"testuser{ts}")))

    # 19. Follow user
    test("Follow user", "POST", f"/api/profiles/testuser2_{ts}/follow",
         headers=auth_headers,
         check_body=lambda b: assert_equals(b["profile"]["following"], True))

    # 20. Unfollow user
    test("Unfollow user", "DELETE", f"/api/profiles/testuser2_{ts}/follow",
         headers=auth_headers,
         check_body=lambda b: assert_equals(b["profile"]["following"], False))

    # 21. Delete article
    if slug:
        test("Delete article", "DELETE", f"/api/articles/{slug}",
             headers=auth_headers)

    # 22. Login with wrong password should fail
    bad_login = {"user": {"email": f"test{ts}@example.com", "password": "wrongpassword"}}
    test("Login with wrong password", "POST", "/api/users/login",
         body=bad_login, expected_status=401)

    # 23. Duplicate registration should fail
    test("Duplicate registration", "POST", "/api/users", body=user1_data,
         expected_status=409)

    print(f"\n=== Results: {PASSED} passed, {FAILED} failed ===")
    sys.exit(1 if FAILED > 0 else 0)

if __name__ == "__main__":
    run_tests()
