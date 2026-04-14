#!/usr/bin/env python3
"""Smoke tests for the RealWorld Conduit API (Quarkus migration)."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

def api(method, path, body=None, token=None):
    """Helper to make HTTP requests to the API."""
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    data = json.dumps(body).encode("utf-8") if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            resp_body = resp.read().decode("utf-8")
            return resp.status, json.loads(resp_body) if resp_body else {}
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8") if e.fp else ""
        try:
            return e.code, json.loads(resp_body) if resp_body else {}
        except json.JSONDecodeError:
            return e.code, {"raw": resp_body}
    except urllib.error.URLError as e:
        return 0, {"error": str(e)}


def wait_for_server(timeout=60):
    """Wait for the server to be available."""
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{BASE_URL}/tags", method="GET")
            with urllib.request.urlopen(req, timeout=5):
                print(f"Server is up after {time.time() - start:.1f}s")
                return True
        except Exception:
            time.sleep(2)
    print(f"Server failed to start within {timeout}s")
    return False


def test_tags():
    """Test GET /tags endpoint."""
    status, body = api("GET", "/tags")
    assert status == 200, f"GET /tags returned {status}: {body}"
    assert "tags" in body, f"Response missing 'tags' key: {body}"
    print("  PASS: GET /tags")


def test_register_user():
    """Test POST /users (user registration)."""
    unique = int(time.time() * 1000) % 1000000
    payload = {
        "user": {
            "email": f"smoke{unique}@test.com",
            "username": f"smokeuser{unique}",
            "password": "password123"
        }
    }
    status, body = api("POST", "/users", payload)
    assert status == 200, f"POST /users returned {status}: {body}"
    assert "user" in body, f"Response missing 'user' key: {body}"
    user = body["user"]
    assert "token" in user, f"User missing 'token': {user}"
    assert "email" in user, f"User missing 'email': {user}"
    assert "username" in user, f"User missing 'username': {user}"
    print("  PASS: POST /users (register)")
    return user["token"], f"smokeuser{unique}"


def test_login_user():
    """Test POST /users/login and related user operations."""
    unique = int(time.time() * 1000) % 1000000
    # First register
    reg_payload = {
        "user": {
            "email": f"login{unique}@test.com",
            "username": f"loginuser{unique}",
            "password": "password123"
        }
    }
    status, body = api("POST", "/users", reg_payload)
    assert status == 200, f"Register for login test returned {status}: {body}"

    # Then login
    login_payload = {
        "user": {
            "email": f"login{unique}@test.com",
            "password": "password123"
        }
    }
    status, body = api("POST", "/users/login", login_payload)
    assert status == 200, f"POST /users/login returned {status}: {body}"
    assert "user" in body, f"Response missing 'user': {body}"
    assert "token" in body["user"], f"Login response missing token: {body}"
    print("  PASS: POST /users/login")
    return body["user"]["token"]


def test_get_current_user(token):
    """Test GET /user (current user)."""
    status, body = api("GET", "/user", token=token)
    assert status == 200, f"GET /user returned {status}: {body}"
    assert "user" in body, f"Response missing 'user': {body}"
    print("  PASS: GET /user")


def test_update_user(token):
    """Test PUT /user (update user)."""
    payload = {
        "user": {
            "bio": "Updated bio from smoke test"
        }
    }
    status, body = api("PUT", "/user", payload, token=token)
    assert status == 200, f"PUT /user returned {status}: {body}"
    assert "user" in body, f"Response missing 'user': {body}"
    print("  PASS: PUT /user")


def test_get_profile(username, token=None):
    """Test GET /profiles/:username."""
    status, body = api("GET", f"/profiles/{username}", token=token)
    assert status == 200, f"GET /profiles/{username} returned {status}: {body}"
    assert "profile" in body, f"Response missing 'profile': {body}"
    print(f"  PASS: GET /profiles/{username}")


def test_articles_crud(token):
    """Test article CRUD operations."""
    unique = int(time.time() * 1000) % 1000000

    # Create article
    payload = {
        "article": {
            "title": f"Smoke Test Article {unique}",
            "description": "A test article",
            "body": "This is the body of the test article",
            "tagList": ["test", "smoke"]
        }
    }
    status, body = api("POST", "/articles", payload, token=token)
    assert status == 200, f"POST /articles returned {status}: {body}"
    assert "article" in body, f"Response missing 'article': {body}"
    slug = body["article"]["slug"]
    print("  PASS: POST /articles (create)")

    # Get article by slug
    status, body = api("GET", f"/articles/{slug}")
    assert status == 200, f"GET /articles/{slug} returned {status}: {body}"
    assert "article" in body, f"Response missing 'article': {body}"
    print(f"  PASS: GET /articles/{slug}")

    # List articles
    status, body = api("GET", "/articles")
    assert status == 200, f"GET /articles returned {status}: {body}"
    assert "articles" in body, f"Response missing 'articles': {body}"
    print("  PASS: GET /articles (list)")

    # Update article
    update_payload = {
        "article": {
            "body": "Updated body from smoke test"
        }
    }
    status, body = api("PUT", f"/articles/{slug}", update_payload, token=token)
    assert status == 200, f"PUT /articles/{slug} returned {status}: {body}"
    print(f"  PASS: PUT /articles/{slug}")

    # Favorite article
    status, body = api("POST", f"/articles/{slug}/favorite", token=token)
    assert status == 200, f"POST /articles/{slug}/favorite returned {status}: {body}"
    print(f"  PASS: POST /articles/{slug}/favorite")

    # Unfavorite article
    status, body = api("DELETE", f"/articles/{slug}/favorite", token=token)
    assert status == 200, f"DELETE /articles/{slug}/favorite returned {status}: {body}"
    print(f"  PASS: DELETE /articles/{slug}/favorite")

    return slug


def test_comments(token, slug):
    """Test comment operations."""
    # Add comment
    payload = {
        "comment": {
            "body": "This is a smoke test comment"
        }
    }
    status, body = api("POST", f"/articles/{slug}/comments", payload, token=token)
    assert status == 200, f"POST /articles/{slug}/comments returned {status}: {body}"
    assert "comment" in body, f"Response missing 'comment': {body}"
    print(f"  PASS: POST /articles/{slug}/comments")

    # Get comments
    status, body = api("GET", f"/articles/{slug}/comments", token=token)
    assert status == 200, f"GET /articles/{slug}/comments returned {status}: {body}"
    assert "comments" in body, f"Response missing 'comments': {body}"
    print(f"  PASS: GET /articles/{slug}/comments")


def test_delete_article(token, slug):
    """Test DELETE /articles/:slug."""
    status, _ = api("DELETE", f"/articles/{slug}", token=token)
    assert status == 204, f"DELETE /articles/{slug} returned {status}"
    print(f"  PASS: DELETE /articles/{slug}")


def main():
    print(f"Running smoke tests against {BASE_URL}")

    if not wait_for_server():
        sys.exit(1)

    passed = 0
    failed = 0

    tests = [
        ("Tags", lambda: test_tags()),
    ]

    # Run simple tests first
    for name, test_fn in tests:
        try:
            print(f"Testing {name}...")
            test_fn()
            passed += 1
        except AssertionError as e:
            print(f"  FAIL: {name}: {e}")
            failed += 1
        except Exception as e:
            print(f"  ERROR: {name}: {e}")
            failed += 1

    # Registration and authenticated tests
    try:
        print("Testing User Registration...")
        token, username = test_register_user()
        passed += 1

        print("Testing User Login...")
        login_token = test_login_user()
        passed += 1

        print("Testing Get Current User...")
        test_get_current_user(token)
        passed += 1

        print("Testing Update User...")
        test_update_user(token)
        passed += 1

        print("Testing Get Profile...")
        test_get_profile(username, token)
        passed += 1

        print("Testing Article CRUD...")
        slug = test_articles_crud(token)
        passed += 1

        print("Testing Comments...")
        test_comments(token, slug)
        passed += 1

        print("Testing Delete Article...")
        test_delete_article(token, slug)
        passed += 1

    except AssertionError as e:
        print(f"  FAIL: {e}")
        failed += 1
    except Exception as e:
        print(f"  ERROR: {e}")
        failed += 1

    print(f"\nResults: {passed} passed, {failed} failed")
    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
