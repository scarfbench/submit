#!/usr/bin/env python3
"""Smoke tests for RealWorld API (Conduit) after Spring-to-Jakarta migration."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
TIMEOUT = 10

def req(method, path, data=None, token=None):
    """Send HTTP request and return (status_code, parsed_json_or_None)."""
    url = BASE_URL.rstrip("/") + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    body = json.dumps(data).encode() if data else None
    r = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(r, timeout=TIMEOUT)
        text = resp.read().decode()
        return resp.status, json.loads(text) if text else {}
    except urllib.error.HTTPError as e:
        text = e.read().decode() if e.fp else ""
        try:
            return e.code, json.loads(text) if text else {}
        except json.JSONDecodeError:
            return e.code, {}


def wait_for_server(max_wait=120):
    """Wait until the server is responding."""
    print(f"Waiting for server at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            urllib.request.urlopen(f"{BASE_URL}/tags", timeout=3)
            print("Server is up!")
            return True
        except Exception:
            time.sleep(2)
    print("Server did not start in time!")
    return False


passed = 0
failed = 0

def check(name, condition, detail=""):
    global passed, failed
    if condition:
        passed += 1
        print(f"  PASS: {name}")
    else:
        failed += 1
        print(f"  FAIL: {name}  {detail}")


def test_tags():
    print("\n--- GET /tags ---")
    status, body = req("GET", "/tags")
    check("status 200", status == 200, f"got {status}")
    check("has tags field", "tags" in body, str(body))


def test_register_and_login():
    print("\n--- POST /users (register) ---")
    status, body = req("POST", "/users", {
        "user": {"email": "smoke@test.com", "password": "password123", "username": "smokeuser"}
    })
    check("register status 200", status == 200, f"got {status}")
    check("register has user.token", "user" in body and "token" in body.get("user", {}), str(body))
    token = body.get("user", {}).get("token", "")

    print("\n--- POST /users/login ---")
    status, body = req("POST", "/users/login", {
        "user": {"email": "smoke@test.com", "password": "password123"}
    })
    check("login status 200", status == 200, f"got {status}")
    check("login has user.token", "user" in body and "token" in body.get("user", {}), str(body))
    token = body.get("user", {}).get("token", token)

    return token


def test_current_user(token):
    print("\n--- GET /user (current user) ---")
    status, body = req("GET", "/user", token=token)
    check("current user status 200", status == 200, f"got {status}")
    check("has user.email", "user" in body and "email" in body.get("user", {}), str(body))


def test_update_user(token):
    print("\n--- PUT /user (update) ---")
    status, body = req("PUT", "/user", {"user": {"bio": "smoke test bio"}}, token=token)
    check("update user status 200", status == 200, f"got {status}")
    check("bio updated", body.get("user", {}).get("bio") == "smoke test bio", str(body))


def test_profiles(token):
    print("\n--- GET /profiles/smokeuser ---")
    status, body = req("GET", "/profiles/smokeuser", token=token)
    check("profile status 200", status == 200, f"got {status}")
    check("has profile.username", body.get("profile", {}).get("username") == "smokeuser", str(body))


def test_articles_crud(token):
    print("\n--- POST /articles (create) ---")
    status, body = req("POST", "/articles", {
        "article": {
            "title": "Smoke Test Article",
            "description": "Testing migration",
            "body": "This is a smoke test.",
            "tagList": ["smoke", "test"]
        }
    }, token=token)
    check("create article status 200", status == 200, f"got {status}")
    slug = body.get("article", {}).get("slug", "")
    check("article has slug", bool(slug), str(body))

    print("\n--- GET /articles (list) ---")
    status, body = req("GET", "/articles?limit=10&offset=0")
    check("list articles status 200", status == 200, f"got {status}")
    check("has articles array", "articles" in body, str(body))
    check("articlesCount >= 1", body.get("articlesCount", 0) >= 1, str(body))

    print(f"\n--- GET /articles/{slug} ---")
    status, body = req("GET", f"/articles/{slug}")
    check("get single article 200", status == 200, f"got {status}")
    check("article body present", "article" in body, str(body))

    print(f"\n--- PUT /articles/{slug} ---")
    status, body = req("PUT", f"/articles/{slug}", {
        "article": {"body": "Updated smoke test body"}
    }, token=token)
    check("update article 200", status == 200, f"got {status}")
    check("body updated", body.get("article", {}).get("body") == "Updated smoke test body", str(body))

    return slug


def test_comments(token, slug):
    print(f"\n--- POST /articles/{slug}/comments ---")
    status, body = req("POST", f"/articles/{slug}/comments", {
        "comment": {"body": "Smoke test comment"}
    }, token=token)
    check("create comment 200", status == 200, f"got {status}")
    comment_id = body.get("comment", {}).get("id")
    check("comment has id", comment_id is not None, str(body))

    print(f"\n--- GET /articles/{slug}/comments ---")
    status, body = req("GET", f"/articles/{slug}/comments", token=token)
    check("get comments 200", status == 200, f"got {status}")
    check("has comments array", "comments" in body, str(body))

    if comment_id:
        print(f"\n--- DELETE /articles/{slug}/comments/{comment_id} ---")
        status, _ = req("DELETE", f"/articles/{slug}/comments/{comment_id}", token=token)
        check("delete comment 200", status == 200, f"got {status}")


def test_favorites(token, slug):
    print(f"\n--- POST /articles/{slug}/favorite ---")
    status, body = req("POST", f"/articles/{slug}/favorite", token=token)
    check("favorite 200", status == 200, f"got {status}")
    check("favorited true", body.get("article", {}).get("favorited") is True, str(body))

    print(f"\n--- DELETE /articles/{slug}/favorite ---")
    status, body = req("DELETE", f"/articles/{slug}/favorite", token=token)
    check("unfavorite 200", status == 200, f"got {status}")
    check("favorited false", body.get("article", {}).get("favorited") is False, str(body))


def test_follow_unfollow(token):
    # Register a second user to follow
    req("POST", "/users", {
        "user": {"email": "celeb@test.com", "password": "password123", "username": "celebuser"}
    })

    print("\n--- POST /profiles/celebuser/follow ---")
    status, body = req("POST", "/profiles/celebuser/follow", token=token)
    check("follow 200", status == 200, f"got {status}")
    check("following true", body.get("profile", {}).get("following") is True, str(body))

    print("\n--- DELETE /profiles/celebuser/follow ---")
    status, body = req("DELETE", "/profiles/celebuser/follow", token=token)
    check("unfollow 200", status == 200, f"got {status}")
    check("following false", body.get("profile", {}).get("following") is False, str(body))


def test_delete_article(token, slug):
    print(f"\n--- DELETE /articles/{slug} ---")
    status, _ = req("DELETE", f"/articles/{slug}", token=token)
    check("delete article 204", status == 204, f"got {status}")


def main():
    if not wait_for_server():
        sys.exit(1)

    test_tags()
    token = test_register_and_login()
    if not token:
        print("\nCannot proceed without auth token.")
        sys.exit(1)

    test_current_user(token)
    test_update_user(token)
    test_profiles(token)
    test_follow_unfollow(token)
    slug = test_articles_crud(token)
    if slug:
        test_comments(token, slug)
        test_favorites(token, slug)
        test_delete_article(token, slug)

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed, {passed+failed} total")
    print(f"{'='*50}")
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
