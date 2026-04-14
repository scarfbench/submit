#!/usr/bin/env python3
"""Smoke tests for RealWorld Spring Boot API."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080/api")
TIMEOUT = 10


def req(method, path, body=None, token=None, expected_status=200):
    """Make an HTTP request and return parsed JSON."""
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"

    data = json.dumps(body).encode("utf-8") if body else None
    request = urllib.request.Request(url, data=data, headers=headers, method=method)

    try:
        resp = urllib.request.urlopen(request, timeout=TIMEOUT)
        status = resp.status
        resp_body = resp.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        status = e.code
        resp_body = e.read().decode("utf-8")

    if status != expected_status:
        print(f"  FAIL: {method} {path} -> {status} (expected {expected_status})")
        print(f"  Response: {resp_body[:500]}")
        return None

    if resp_body:
        return json.loads(resp_body)
    return {}


def wait_for_server(max_wait=120):
    """Wait for the server to become available."""
    print(f"Waiting for server at {BASE_URL}...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            r = urllib.request.urlopen(f"{BASE_URL}/tags", timeout=5)
            if r.status == 200:
                print("Server is ready!")
                return True
        except Exception:
            pass
        time.sleep(2)
    print("Server did not start in time!")
    return False


def test_tags():
    """Test GET /tags endpoint."""
    print("TEST: GET /tags")
    result = req("GET", "/tags")
    assert result is not None, "Tags request failed"
    assert "tags" in result, f"Response missing 'tags' key: {result}"
    assert isinstance(result["tags"], list), "tags should be a list"
    print("  PASS")
    return True


def test_user_registration():
    """Test POST /users (registration)."""
    print("TEST: POST /users (register)")
    body = {
        "user": {
            "username": "smoketest",
            "email": "smoketest@example.com",
            "password": "password123"
        }
    }
    result = req("POST", "/users", body=body, expected_status=201)
    assert result is not None, "Registration failed"
    assert "user" in result, f"Response missing 'user' key: {result}"
    user = result["user"]
    assert user["username"] == "smoketest", f"Username mismatch: {user}"
    assert user["email"] == "smoketest@example.com", f"Email mismatch: {user}"
    assert "token" in user, f"Missing token: {user}"
    print("  PASS")
    return user["token"]


def test_user_login():
    """Test POST /users/login."""
    print("TEST: POST /users/login")
    body = {
        "user": {
            "email": "smoketest@example.com",
            "password": "password123"
        }
    }
    result = req("POST", "/users/login", body=body)
    assert result is not None, "Login failed"
    assert "user" in result, f"Response missing 'user' key: {result}"
    user = result["user"]
    assert user["email"] == "smoketest@example.com", f"Email mismatch: {user}"
    assert "token" in user, f"Missing token: {user}"
    print("  PASS")
    return user["token"]


def test_get_current_user(token):
    """Test GET /user (current user)."""
    print("TEST: GET /user")
    result = req("GET", "/user", token=token)
    assert result is not None, "Get current user failed"
    assert "user" in result, f"Response missing 'user' key: {result}"
    assert result["user"]["username"] == "smoketest", f"Username mismatch: {result}"
    print("  PASS")
    return True


def test_update_user(token):
    """Test PUT /user (update user)."""
    print("TEST: PUT /user")
    body = {
        "user": {
            "bio": "I am a smoke test user"
        }
    }
    result = req("PUT", "/user", body=body, token=token)
    assert result is not None, "Update user failed"
    assert "user" in result, f"Response missing 'user' key: {result}"
    assert result["user"]["bio"] == "I am a smoke test user", f"Bio mismatch: {result}"
    print("  PASS")
    return True


def test_get_profile(token):
    """Test GET /profiles/:username."""
    print("TEST: GET /profiles/smoketest")
    result = req("GET", "/profiles/smoketest", token=token)
    assert result is not None, "Get profile failed"
    assert "profile" in result, f"Response missing 'profile' key: {result}"
    assert result["profile"]["username"] == "smoketest", f"Username mismatch: {result}"
    print("  PASS")
    return True


def test_create_article(token):
    """Test POST /articles."""
    print("TEST: POST /articles")
    body = {
        "article": {
            "title": "Smoke Test Article",
            "description": "A test article for smoke testing",
            "body": "This is the body of the smoke test article.",
            "tagList": ["smoke", "test"]
        }
    }
    result = req("POST", "/articles", body=body, token=token, expected_status=201)
    assert result is not None, "Create article failed"
    assert "article" in result, f"Response missing 'article' key: {result}"
    article = result["article"]
    assert article["title"] == "Smoke Test Article", f"Title mismatch: {article}"
    assert "slug" in article, f"Missing slug: {article}"
    print("  PASS")
    return article["slug"]


def test_get_article(slug):
    """Test GET /articles/:slug."""
    print(f"TEST: GET /articles/{slug}")
    result = req("GET", f"/articles/{slug}")
    assert result is not None, "Get article failed"
    assert "article" in result, f"Response missing 'article' key: {result}"
    assert result["article"]["slug"] == slug, f"Slug mismatch: {result}"
    print("  PASS")
    return True


def test_list_articles():
    """Test GET /articles."""
    print("TEST: GET /articles")
    result = req("GET", "/articles")
    assert result is not None, "List articles failed"
    assert "articles" in result, f"Response missing 'articles' key: {result}"
    assert "articlesCount" in result, f"Response missing 'articlesCount' key: {result}"
    assert isinstance(result["articles"], list), "articles should be a list"
    print("  PASS")
    return True


def test_feed(token):
    """Test GET /articles/feed."""
    print("TEST: GET /articles/feed")
    result = req("GET", "/articles/feed", token=token)
    assert result is not None, "Feed failed"
    assert "articles" in result, f"Response missing 'articles' key: {result}"
    assert "articlesCount" in result, f"Response missing 'articlesCount' key: {result}"
    print("  PASS")
    return True


def test_create_comment(token, slug):
    """Test POST /articles/:slug/comments."""
    print(f"TEST: POST /articles/{slug}/comments")
    body = {
        "comment": {
            "body": "This is a smoke test comment."
        }
    }
    result = req("POST", f"/articles/{slug}/comments", body=body, token=token)
    assert result is not None, "Create comment failed"
    assert "comment" in result, f"Response missing 'comment' key: {result}"
    assert result["comment"]["body"] == "This is a smoke test comment.", f"Body mismatch: {result}"
    print("  PASS")
    return result["comment"]["id"]


def test_get_comments(slug):
    """Test GET /articles/:slug/comments."""
    print(f"TEST: GET /articles/{slug}/comments")
    result = req("GET", f"/articles/{slug}/comments")
    assert result is not None, "Get comments failed"
    assert "comments" in result, f"Response missing 'comments' key: {result}"
    assert isinstance(result["comments"], list), "comments should be a list"
    assert len(result["comments"]) >= 1, "Should have at least one comment"
    print("  PASS")
    return True


def test_favorite_article(token, slug):
    """Test POST /articles/:slug/favorite."""
    print(f"TEST: POST /articles/{slug}/favorite")
    result = req("POST", f"/articles/{slug}/favorite", token=token)
    assert result is not None, "Favorite failed"
    assert "article" in result, f"Response missing 'article' key: {result}"
    assert result["article"]["favorited"] is True, f"Article should be favorited: {result}"
    print("  PASS")
    return True


def test_unfavorite_article(token, slug):
    """Test DELETE /articles/:slug/favorite."""
    print(f"TEST: DELETE /articles/{slug}/favorite")
    result = req("DELETE", f"/articles/{slug}/favorite", token=token)
    assert result is not None, "Unfavorite failed"
    assert "article" in result, f"Response missing 'article' key: {result}"
    assert result["article"]["favorited"] is False, f"Article should be unfavorited: {result}"
    print("  PASS")
    return True


def test_follow_user(token):
    """Test POST /profiles/:username/follow (need a second user)."""
    print("TEST: Register second user + follow")
    # Register second user
    body = {
        "user": {
            "username": "smoketest2",
            "email": "smoketest2@example.com",
            "password": "password123"
        }
    }
    result = req("POST", "/users", body=body, expected_status=201)
    assert result is not None, "Second user registration failed"

    # Follow
    result = req("POST", "/profiles/smoketest2/follow", token=token)
    assert result is not None, "Follow failed"
    assert "profile" in result, f"Response missing 'profile' key: {result}"
    assert result["profile"]["following"] is True, f"Should be following: {result}"
    print("  PASS (register + follow)")
    return True


def test_unfollow_user(token):
    """Test DELETE /profiles/:username/follow."""
    print("TEST: DELETE /profiles/smoketest2/follow")
    result = req("DELETE", "/profiles/smoketest2/follow", token=token)
    assert result is not None, "Unfollow failed"
    assert "profile" in result, f"Response missing 'profile' key: {result}"
    assert result["profile"]["following"] is False, f"Should not be following: {result}"
    print("  PASS")
    return True


def test_update_article(token, slug):
    """Test PUT /articles/:slug."""
    print(f"TEST: PUT /articles/{slug}")
    body = {
        "article": {
            "title": "Updated Smoke Test Article"
        }
    }
    result = req("PUT", f"/articles/{slug}", body=body, token=token)
    assert result is not None, "Update article failed"
    assert "article" in result, f"Response missing 'article' key: {result}"
    assert result["article"]["title"] == "Updated Smoke Test Article", f"Title mismatch: {result}"
    print("  PASS")
    return result["article"]["slug"]


def test_delete_article(token, slug):
    """Test DELETE /articles/:slug."""
    print(f"TEST: DELETE /articles/{slug}")
    result = req("DELETE", f"/articles/{slug}", token=token)
    # delete returns 200 with empty body
    assert result is not None or result == {}, "Delete article failed"
    print("  PASS")
    return True


def test_unauthorized_access():
    """Test that protected endpoints return 401 without token."""
    print("TEST: Unauthorized access (GET /user without token)")
    result = req("GET", "/user", expected_status=401)
    print("  PASS")
    return True


def main():
    passed = 0
    failed = 0
    errors = []

    if not wait_for_server():
        print("FATAL: Server not available")
        sys.exit(1)

    tests = []

    # Basic tests
    try:
        test_tags()
        passed += 1
    except Exception as e:
        failed += 1
        errors.append(f"test_tags: {e}")

    try:
        test_unauthorized_access()
        passed += 1
    except Exception as e:
        failed += 1
        errors.append(f"test_unauthorized_access: {e}")

    # User tests
    token = None
    try:
        token = test_user_registration()
        passed += 1
    except Exception as e:
        failed += 1
        errors.append(f"test_user_registration: {e}")

    if token:
        try:
            token = test_user_login()
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_user_login: {e}")

        try:
            test_get_current_user(token)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_get_current_user: {e}")

        try:
            test_update_user(token)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_update_user: {e}")

        try:
            test_get_profile(token)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_get_profile: {e}")

        # Article tests
        slug = None
        try:
            slug = test_create_article(token)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_create_article: {e}")

        if slug:
            try:
                test_get_article(slug)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_get_article: {e}")

            try:
                test_list_articles()
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_list_articles: {e}")

            try:
                test_feed(token)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_feed: {e}")

            try:
                test_create_comment(token, slug)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_create_comment: {e}")

            try:
                test_get_comments(slug)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_get_comments: {e}")

            try:
                test_favorite_article(token, slug)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_favorite_article: {e}")

            try:
                test_unfavorite_article(token, slug)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_unfavorite_article: {e}")

            try:
                new_slug = test_update_article(token, slug)
                passed += 1
                slug = new_slug  # slug may change after title update
            except Exception as e:
                failed += 1
                errors.append(f"test_update_article: {e}")

        # Follow tests
        try:
            test_follow_user(token)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_follow_user: {e}")

        try:
            test_unfollow_user(token)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"test_unfollow_user: {e}")

        # Delete article last
        if slug:
            try:
                test_delete_article(token, slug)
                passed += 1
            except Exception as e:
                failed += 1
                errors.append(f"test_delete_article: {e}")

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed")

    if errors:
        print("\nFailures:")
        for err in errors:
            print(f"  - {err}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
