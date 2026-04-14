#!/usr/bin/env python3
"""Smoke tests for the RealWorld API (Jakarta EE migration)."""

import json
import os
import sys
import time
import urllib.request
import urllib.error
import uuid
import random
import string

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
API_URL = f"{BASE_URL}/api"

passed = 0
failed = 0
errors = []


def rand_str(n=8):
    return "".join(random.choices(string.ascii_lowercase + string.digits, k=n))


def api(method, path, data=None, token=None, expected_status=None):
    url = f"{API_URL}{path}"
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        status = resp.status
        resp_body = resp.read().decode("utf-8")
        resp_json = json.loads(resp_body) if resp_body else {}
    except urllib.error.HTTPError as e:
        status = e.code
        resp_body = e.read().decode("utf-8")
        try:
            resp_json = json.loads(resp_body) if resp_body else {}
        except json.JSONDecodeError:
            resp_json = {"raw": resp_body}

    if expected_status and status != expected_status:
        raise AssertionError(
            f"Expected status {expected_status}, got {status} for {method} {path}. Body: {resp_body[:500]}"
        )
    return status, resp_json


def test(name, fn):
    global passed, failed
    try:
        fn()
        passed += 1
        print(f"  PASS: {name}")
    except Exception as e:
        failed += 1
        errors.append(f"{name}: {e}")
        print(f"  FAIL: {name} - {e}")


def wait_for_server(timeout=120):
    """Wait for the server to become available."""
    print(f"Waiting for server at {API_URL}...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{API_URL}/health", method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print(f"Server is up after {int(time.time() - start)}s")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"Server did not start within {timeout}s")
    return False


# ---- Test functions ----

def test_health():
    status, body = api("GET", "/health")
    assert status == 200, f"Health check failed with status {status}"
    assert body.get("status") == "UP", f"Health status is not UP: {body}"


def test_health_live():
    status, body = api("GET", "/health/live")
    assert status == 200, f"Health live check failed with status {status}"


def test_health_ready():
    status, body = api("GET", "/health/ready")
    assert status == 200, f"Health ready check failed with status {status}"


def test_register_user():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"testuser_{suffix}",
            "email": f"test_{suffix}@example.com",
            "password": "password123",
        }
    }
    status, body = api("POST", "/users", data)
    assert status in (200, 201), f"Register failed with status {status}: {body}"
    assert "user" in body, f"Response missing 'user' key: {body}"
    assert "token" in body["user"], f"Response missing token: {body}"
    assert body["user"]["username"] == f"testuser_{suffix}"
    assert body["user"]["email"] == f"test_{suffix}@example.com"
    return body["user"]["token"]


def test_register_duplicate_user():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"dupuser_{suffix}",
            "email": f"dup_{suffix}@example.com",
            "password": "password123",
        }
    }
    api("POST", "/users", data)
    # Try again with same username
    status, body = api("POST", "/users", data)
    assert status == 409, f"Expected 409 for duplicate user, got {status}: {body}"


def test_login():
    suffix = rand_str()
    reg_data = {
        "user": {
            "username": f"loginuser_{suffix}",
            "email": f"login_{suffix}@example.com",
            "password": "password123",
        }
    }
    api("POST", "/users", reg_data)
    login_data = {
        "user": {"email": f"login_{suffix}@example.com", "password": "password123"}
    }
    status, body = api("POST", "/users/login", login_data)
    assert status == 200, f"Login failed with status {status}: {body}"
    assert "user" in body, f"Response missing 'user' key: {body}"
    assert "token" in body["user"], f"Login response missing token: {body}"
    return body["user"]["token"]


def test_login_wrong_password():
    suffix = rand_str()
    reg_data = {
        "user": {
            "username": f"wrongpw_{suffix}",
            "email": f"wrongpw_{suffix}@example.com",
            "password": "password123",
        }
    }
    api("POST", "/users", reg_data)
    login_data = {
        "user": {"email": f"wrongpw_{suffix}@example.com", "password": "wrongpassword"}
    }
    status, body = api("POST", "/users/login", login_data)
    assert status == 401, f"Expected 401 for wrong password, got {status}: {body}"


def test_get_current_user():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"curruser_{suffix}",
            "email": f"curr_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    status, body = api("GET", "/user", token=token)
    assert status == 200, f"Get user failed with status {status}: {body}"
    assert body["user"]["username"] == f"curruser_{suffix}"


def test_update_user():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"upduser_{suffix}",
            "email": f"upd_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    update_data = {"user": {"bio": "Updated bio text"}}
    status, body = api("PUT", "/user", update_data, token=token)
    assert status == 200, f"Update user failed with status {status}: {body}"
    assert body["user"]["bio"] == "Updated bio text", f"Bio not updated: {body}"


def test_get_profile():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"profuser_{suffix}",
            "email": f"prof_{suffix}@example.com",
            "password": "password123",
        }
    }
    api("POST", "/users", data)
    status, body = api("GET", f"/profiles/profuser_{suffix}")
    assert status == 200, f"Get profile failed with status {status}: {body}"
    assert "profile" in body, f"Response missing 'profile' key: {body}"
    assert body["profile"]["username"] == f"profuser_{suffix}"


def test_follow_unfollow():
    suffix1 = rand_str()
    suffix2 = rand_str()
    data1 = {
        "user": {
            "username": f"follower_{suffix1}",
            "email": f"follower_{suffix1}@example.com",
            "password": "password123",
        }
    }
    data2 = {
        "user": {
            "username": f"followed_{suffix2}",
            "email": f"followed_{suffix2}@example.com",
            "password": "password123",
        }
    }
    _, body1 = api("POST", "/users", data1)
    api("POST", "/users", data2)
    token = body1["user"]["token"]

    # Follow
    status, body = api("POST", f"/profiles/followed_{suffix2}/follow", token=token)
    assert status == 200, f"Follow failed with status {status}: {body}"
    assert body["profile"]["following"] is True, f"Not following after follow: {body}"

    # Unfollow
    status, body = api("DELETE", f"/profiles/followed_{suffix2}/follow", token=token)
    assert status == 200, f"Unfollow failed with status {status}: {body}"
    assert body["profile"]["following"] is False, f"Still following after unfollow: {body}"


def test_create_article():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"author_{suffix}",
            "email": f"author_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]

    article_data = {
        "article": {
            "title": f"Test Article {suffix}",
            "description": "This is a test article",
            "body": "Article body content here",
            "tagList": ["test", "smoke"],
        }
    }
    status, body = api("POST", "/articles", article_data, token=token)
    assert status in (200, 201), f"Create article failed with status {status}: {body}"
    assert "article" in body, f"Response missing 'article' key: {body}"
    assert body["article"]["title"] == f"Test Article {suffix}"
    return token, body["article"]["slug"]


def test_get_article():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"getart_{suffix}",
            "email": f"getart_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    article_data = {
        "article": {
            "title": f"GetArticle {suffix}",
            "description": "Desc",
            "body": "Body",
            "tagList": [],
        }
    }
    _, art_body = api("POST", "/articles", article_data, token=token)
    slug = art_body["article"]["slug"]

    status, body = api("GET", f"/articles/{slug}")
    assert status == 200, f"Get article failed with status {status}: {body}"
    assert body["article"]["slug"] == slug


def test_list_articles():
    status, body = api("GET", "/articles")
    assert status == 200, f"List articles failed with status {status}: {body}"
    assert "articles" in body, f"Response missing 'articles' key: {body}"
    assert "articlesCount" in body, f"Response missing 'articlesCount' key: {body}"


def test_update_article():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"updart_{suffix}",
            "email": f"updart_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    article_data = {
        "article": {
            "title": f"UpdateArticle {suffix}",
            "description": "Desc",
            "body": "Body",
            "tagList": [],
        }
    }
    _, art_body = api("POST", "/articles", article_data, token=token)
    slug = art_body["article"]["slug"]

    update_data = {"article": {"body": "Updated body content"}}
    status, body = api("PUT", f"/articles/{slug}", update_data, token=token)
    assert status == 200, f"Update article failed with status {status}: {body}"


def test_delete_article():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"delart_{suffix}",
            "email": f"delart_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    article_data = {
        "article": {
            "title": f"DeleteArticle {suffix}",
            "description": "Desc",
            "body": "Body",
            "tagList": [],
        }
    }
    _, art_body = api("POST", "/articles", article_data, token=token)
    slug = art_body["article"]["slug"]

    status, _ = api("DELETE", f"/articles/{slug}", token=token)
    assert status == 200, f"Delete article failed with status {status}"


def test_add_comment():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"comment_{suffix}",
            "email": f"comment_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    article_data = {
        "article": {
            "title": f"CommentArticle {suffix}",
            "description": "Desc",
            "body": "Body",
            "tagList": [],
        }
    }
    _, art_body = api("POST", "/articles", article_data, token=token)
    slug = art_body["article"]["slug"]

    comment_data = {"comment": {"body": "This is a test comment"}}
    status, body = api("POST", f"/articles/{slug}/comments", comment_data, token=token)
    assert status == 200, f"Add comment failed with status {status}: {body}"
    assert "comment" in body, f"Response missing 'comment' key: {body}"


def test_get_comments():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"getcom_{suffix}",
            "email": f"getcom_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    article_data = {
        "article": {
            "title": f"GetComments {suffix}",
            "description": "Desc",
            "body": "Body",
            "tagList": [],
        }
    }
    _, art_body = api("POST", "/articles", article_data, token=token)
    slug = art_body["article"]["slug"]

    status, body = api("GET", f"/articles/{slug}/comments")
    assert status == 200, f"Get comments failed with status {status}: {body}"
    assert "comments" in body, f"Response missing 'comments' key: {body}"


def test_favorite_unfavorite():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"fav_{suffix}",
            "email": f"fav_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    article_data = {
        "article": {
            "title": f"FavoriteArticle {suffix}",
            "description": "Desc",
            "body": "Body",
            "tagList": [],
        }
    }
    _, art_body = api("POST", "/articles", article_data, token=token)
    slug = art_body["article"]["slug"]

    # Favorite
    status, body = api("POST", f"/articles/{slug}/favorite", token=token)
    assert status == 200, f"Favorite failed with status {status}: {body}"
    assert body["article"]["favorited"] is True, f"Not favorited: {body}"

    # Unfavorite
    status, body = api("DELETE", f"/articles/{slug}/favorite", token=token)
    assert status == 200, f"Unfavorite failed with status {status}: {body}"
    assert body["article"]["favorited"] is False, f"Still favorited: {body}"


def test_get_tags():
    status, body = api("GET", "/tags")
    assert status == 200, f"Get tags failed with status {status}: {body}"
    assert "tags" in body, f"Response missing 'tags' key: {body}"


def test_articles_feed():
    suffix = rand_str()
    data = {
        "user": {
            "username": f"feed_{suffix}",
            "email": f"feed_{suffix}@example.com",
            "password": "password123",
        }
    }
    _, reg_body = api("POST", "/users", data)
    token = reg_body["user"]["token"]
    status, body = api("GET", "/articles/feed", token=token)
    assert status == 200, f"Articles feed failed with status {status}: {body}"
    assert "articles" in body, f"Response missing 'articles' key: {body}"


def test_unauthenticated_access():
    status, _ = api("GET", "/user")
    assert status == 401, f"Expected 401 for unauthenticated /user, got {status}"


# ---- Main ----

if __name__ == "__main__":
    if not wait_for_server():
        print("FATAL: Server not available")
        sys.exit(1)

    print("\n=== Running RealWorld API Smoke Tests ===\n")

    test("Health check", test_health)
    test("Health live", test_health_live)
    test("Health ready", test_health_ready)
    test("Register user", lambda: test_register_user())
    test("Register duplicate user", test_register_duplicate_user)
    test("Login user", lambda: test_login())
    test("Login with wrong password", test_login_wrong_password)
    test("Get current user", test_get_current_user)
    test("Update user", test_update_user)
    test("Get profile", test_get_profile)
    test("Follow/unfollow user", test_follow_unfollow)
    test("Create article", lambda: test_create_article())
    test("Get article by slug", test_get_article)
    test("List articles", test_list_articles)
    test("Update article", test_update_article)
    test("Delete article", test_delete_article)
    test("Add comment", test_add_comment)
    test("Get comments", test_get_comments)
    test("Favorite/unfavorite article", test_favorite_unfavorite)
    test("Get tags", test_get_tags)
    test("Articles feed", test_articles_feed)
    test("Unauthenticated access returns 401", test_unauthenticated_access)

    print(f"\n=== Results: {passed} passed, {failed} failed ===")
    if errors:
        print("\nFailures:")
        for err in errors:
            print(f"  - {err}")

    sys.exit(0 if failed == 0 else 1)
