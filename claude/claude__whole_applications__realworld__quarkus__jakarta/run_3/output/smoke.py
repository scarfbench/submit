#!/usr/bin/env python3
"""
Smoke tests for the RealWorld API application.
Tests core endpoints: users, articles, profiles, tags, health.
"""
import json
import os
import sys
import time
import urllib.request
import urllib.error
import uuid
import traceback

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
API_URL = f"{BASE_URL}/api"

passed = 0
failed = 0
errors = []


def test(name, fn):
    global passed, failed
    try:
        fn()
        passed += 1
        print(f"  PASS: {name}")
    except Exception as e:
        failed += 1
        errors.append((name, str(e)))
        print(f"  FAIL: {name} - {e}")
        traceback.print_exc()


def http(method, path, data=None, token=None, expect_status=None):
    url = f"{API_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        status = resp.status
        resp_body = resp.read().decode()
        try:
            resp_data = json.loads(resp_body) if resp_body.strip() else {}
        except Exception:
            resp_data = {}
    except urllib.error.HTTPError as e:
        status = e.code
        try:
            resp_data = json.loads(e.read().decode())
        except Exception:
            resp_data = {}
        if expect_status and status == expect_status:
            return status, resp_data
        if not expect_status:
            raise
    if expect_status and status != expect_status:
        raise AssertionError(f"Expected status {expect_status}, got {status}")
    return status, resp_data


def http_get(path, token=None, expect_status=200):
    return http("GET", path, token=token, expect_status=expect_status)


def http_post(path, data=None, token=None, expect_status=None):
    return http("POST", path, data=data, token=token, expect_status=expect_status)


def http_put(path, data=None, token=None, expect_status=None):
    return http("PUT", path, data=data, token=token, expect_status=expect_status)


def http_delete(path, token=None, expect_status=None):
    return http("DELETE", path, token=token, expect_status=expect_status)


def wait_for_server(max_retries=30, delay=2):
    """Wait for the server to become available."""
    print(f"Waiting for server at {BASE_URL}...")
    for i in range(max_retries):
        try:
            req = urllib.request.Request(f"{BASE_URL}/api/tags", method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            print(f"Server is ready (attempt {i+1})")
            return True
        except Exception:
            time.sleep(delay)
    print("Server did not become ready in time")
    return False


# Global state for tests
user_token = None
user2_token = None
unique = uuid.uuid4().hex[:8]


def test_register_user():
    global user_token
    status, data = http_post("/users", {
        "user": {
            "username": f"testuser{unique}",
            "email": f"test{unique}@example.com",
            "password": "password123"
        }
    }, expect_status=201)
    assert "user" in data, f"Expected 'user' key in response, got: {data}"
    assert "token" in data["user"], "Expected token in user response"
    user_token = data["user"]["token"]
    assert data["user"]["username"] == f"testuser{unique}"
    assert data["user"]["email"] == f"test{unique}@example.com"


def test_register_user2():
    global user2_token
    status, data = http_post("/users", {
        "user": {
            "username": f"testuser2{unique}",
            "email": f"test2{unique}@example.com",
            "password": "password123"
        }
    }, expect_status=201)
    assert "user" in data
    user2_token = data["user"]["token"]


def test_login_user():
    global user_token
    status, data = http_post("/users/login", {
        "user": {
            "email": f"test{unique}@example.com",
            "password": "password123"
        }
    }, expect_status=200)
    assert "user" in data
    assert "token" in data["user"]
    user_token = data["user"]["token"]


def test_get_current_user():
    status, data = http_get("/user", token=user_token, expect_status=200)
    assert "user" in data
    assert data["user"]["username"] == f"testuser{unique}"


def test_update_user():
    status, data = http_put("/user", {
        "user": {
            "bio": "Updated bio"
        }
    }, token=user_token, expect_status=200)
    assert "user" in data
    assert data["user"]["bio"] == "Updated bio"


def test_get_profile():
    status, data = http_get(f"/profiles/testuser{unique}", expect_status=200)
    assert "profile" in data
    assert data["profile"]["username"] == f"testuser{unique}"


def test_follow_user():
    status, data = http_post(
        f"/profiles/testuser2{unique}/follow",
        token=user_token,
        expect_status=200
    )
    assert "profile" in data
    assert data["profile"]["following"] is True


def test_unfollow_user():
    status, data = http_delete(
        f"/profiles/testuser2{unique}/follow",
        token=user_token,
        expect_status=200
    )
    assert "profile" in data
    assert data["profile"]["following"] is False


article_slug = None


def test_create_article():
    global article_slug
    status, data = http_post("/articles", {
        "article": {
            "title": f"Test Article {unique}",
            "description": "Test description",
            "body": "Test body content",
            "tagList": ["test", "smoke"]
        }
    }, token=user_token, expect_status=201)
    assert "article" in data
    assert data["article"]["title"] == f"Test Article {unique}"
    article_slug = data["article"]["slug"]


def test_get_article():
    status, data = http_get(f"/articles/{article_slug}", expect_status=200)
    assert "article" in data
    assert data["article"]["slug"] == article_slug


def test_list_articles():
    status, data = http_get("/articles", expect_status=200)
    assert "articles" in data
    assert "articlesCount" in data


def test_list_articles_by_tag():
    status, data = http_get("/articles?tag=test", expect_status=200)
    assert "articles" in data


def test_list_articles_by_author():
    status, data = http_get(f"/articles?author=testuser{unique}", expect_status=200)
    assert "articles" in data
    assert len(data["articles"]) > 0


def test_update_article():
    status, data = http_put(f"/articles/{article_slug}", {
        "article": {
            "body": "Updated body content"
        }
    }, token=user_token, expect_status=200)
    assert "article" in data


def test_favorite_article():
    status, data = http_post(
        f"/articles/{article_slug}/favorite",
        token=user_token,
        expect_status=200
    )
    assert "article" in data
    assert data["article"]["favorited"] is True
    assert data["article"]["favoritesCount"] >= 1


def test_unfavorite_article():
    status, data = http_delete(
        f"/articles/{article_slug}/favorite",
        token=user_token,
        expect_status=200
    )
    assert "article" in data
    assert data["article"]["favorited"] is False


def test_feed():
    # user follows user2, user2 creates article -> feed should have article
    status, data = http_get("/articles/feed", token=user_token, expect_status=200)
    assert "articles" in data
    assert "articlesCount" in data


comment_id = None


def test_create_comment():
    global comment_id
    status, data = http_post(f"/articles/{article_slug}/comments", {
        "comment": {
            "body": "Test comment"
        }
    }, token=user_token, expect_status=200)
    assert "comment" in data
    assert data["comment"]["body"] == "Test comment"
    comment_id = data["comment"]["id"]


def test_get_comments():
    status, data = http_get(f"/articles/{article_slug}/comments", expect_status=200)
    assert "comments" in data
    assert len(data["comments"]) > 0


def test_delete_comment():
    status, data = http_delete(
        f"/articles/{article_slug}/comments/{comment_id}",
        token=user_token,
        expect_status=200
    )


def test_get_tags():
    status, data = http_get("/tags", expect_status=200)
    assert "tags" in data


def test_delete_article():
    status, data = http_delete(
        f"/articles/{article_slug}",
        token=user_token,
        expect_status=200
    )


def test_unauthorized_access():
    """Test that protected endpoints require authentication."""
    status, data = http_get("/user", expect_status=401)


if __name__ == "__main__":
    if not wait_for_server():
        print("FATAL: Server not available")
        sys.exit(1)

    print("\n=== Running Smoke Tests ===\n")

    # User registration & auth
    test("Register user", test_register_user)
    test("Register user 2", test_register_user2)
    test("Login user", test_login_user)
    test("Get current user", test_get_current_user)
    test("Update user", test_update_user)

    # Profiles
    test("Get profile", test_get_profile)
    test("Follow user", test_follow_user)
    test("Unfollow user", test_unfollow_user)

    # Articles
    test("Create article", test_create_article)
    test("Get article by slug", test_get_article)
    test("List articles", test_list_articles)
    test("List articles by tag", test_list_articles_by_tag)
    test("List articles by author", test_list_articles_by_author)
    test("Update article", test_update_article)
    test("Favorite article", test_favorite_article)
    test("Unfavorite article", test_unfavorite_article)
    test("Feed", test_feed)

    # Comments
    test("Create comment", test_create_comment)
    test("Get comments", test_get_comments)
    test("Delete comment", test_delete_comment)

    # Tags
    test("Get tags", test_get_tags)

    # Cleanup
    test("Delete article", test_delete_article)

    # Security
    test("Unauthorized access", test_unauthorized_access)

    print(f"\n=== Results: {passed} passed, {failed} failed ===\n")

    if errors:
        print("Failures:")
        for name, err in errors:
            print(f"  - {name}: {err}")

    sys.exit(0 if failed == 0 else 1)
