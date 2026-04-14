#!/usr/bin/env python3
"""Smoke tests for the RealWorld API (Spring Boot migration)."""

import json
import sys
import time
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080/api"
API = BASE_URL
TOKEN = None
SLUG = None
COMMENT_ID = None
PASSED = 0
FAILED = 0


def req(method, path, body=None, auth=False, expected=None):
    global TOKEN
    url = f"{API}{path}"
    headers = {"Content-Type": "application/json"}
    if auth and TOKEN:
        headers["Authorization"] = f"Bearer {TOKEN}"
    data = json.dumps(body).encode() if body else None
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(r)
        status = resp.status
        resp_body = json.loads(resp.read().decode()) if resp.read else {}
        # re-read
        return status, resp_body
    except urllib.error.HTTPError as e:
        body_text = e.read().decode() if e.fp else ""
        try:
            return e.code, json.loads(body_text)
        except Exception:
            return e.code, body_text


def request(method, path, body=None, auth=False):
    """Make an HTTP request and return (status, json_body)."""
    global TOKEN
    url = f"{API}{path}"
    headers = {"Content-Type": "application/json"}
    if auth and TOKEN:
        headers["Authorization"] = f"Bearer {TOKEN}"
    data = json.dumps(body).encode() if body else None
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(r)
        raw = resp.read().decode()
        return resp.status, json.loads(raw) if raw else {}
    except urllib.error.HTTPError as e:
        raw = e.read().decode() if e.fp else ""
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw


def check(name, condition):
    global PASSED, FAILED
    if condition:
        PASSED += 1
        print(f"  PASS: {name}")
    else:
        FAILED += 1
        print(f"  FAIL: {name}")


def test_register():
    global TOKEN
    print("\n--- Register ---")
    status, body = request("POST", "/users", {
        "user": {"username": "testuser", "email": "test@example.com", "password": "password"}
    })
    check("register status 201", status == 201)
    check("register has user", "user" in body)
    if "user" in body:
        check("register email", body["user"].get("email") == "test@example.com")
        check("register username", body["user"].get("username") == "testuser")
        check("register has token", body["user"].get("token") is not None)
        TOKEN = body["user"]["token"]


def test_login():
    global TOKEN
    print("\n--- Login ---")
    status, body = request("POST", "/users/login", {
        "user": {"email": "test@example.com", "password": "password"}
    })
    check("login status 200", status == 200)
    check("login has user", "user" in body)
    if "user" in body:
        check("login email", body["user"].get("email") == "test@example.com")
        check("login username", body["user"].get("username") == "testuser")
        check("login has token", body["user"].get("token") is not None)
        TOKEN = body["user"]["token"]


def test_current_user():
    print("\n--- Current User ---")
    status, body = request("GET", "/user", auth=True)
    check("current user status 200", status == 200)
    check("current user has user", "user" in body)
    if "user" in body:
        check("current user email", body["user"].get("email") == "test@example.com")
        check("current user username", body["user"].get("username") == "testuser")


def test_update_user():
    print("\n--- Update User ---")
    status, body = request("PUT", "/user", {"user": {"bio": "my-new-bio"}}, auth=True)
    check("update user status 200", status == 200)
    if "user" in body:
        check("update user bio", body["user"].get("bio") == "my-new-bio")


def test_list_articles_empty():
    print("\n--- List Articles (empty) ---")
    status, body = request("GET", "/articles")
    check("list articles status 200", status == 200)
    check("articles is array", isinstance(body.get("articles"), list))
    check("articlesCount is 0", body.get("articlesCount") == 0)


def test_create_article():
    global SLUG
    print("\n--- Create Article ---")
    status, body = request("POST", "/articles", {
        "article": {
            "title": "How to train your dragon",
            "description": "Ever wonder how?",
            "body": "Very carefully.",
            "tagList": ["dragons", "training"]
        }
    }, auth=True)
    check("create article status 201", status == 201)
    check("create article has article", "article" in body)
    if "article" in body:
        art = body["article"]
        SLUG = art.get("slug")
        check("article title", art.get("title") == "How to train your dragon")
        check("article slug", art.get("slug") == "how-to-train-your-dragon")
        check("article body", art.get("body") == "Very carefully.")
        check("article description", art.get("description") == "Ever wonder how?")
        check("article has tagList", isinstance(art.get("tagList"), list))
        check("article tagList members", set(art.get("tagList", [])) == {"dragons", "training"})
        check("article favoritesCount", art.get("favoritesCount") == 0)
        check("article has createdAt", art.get("createdAt") is not None)
        check("article has updatedAt", art.get("updatedAt") is not None)


def test_get_article_by_slug():
    print("\n--- Get Article by Slug ---")
    status, body = request("GET", f"/articles/{SLUG}")
    check("get article status 200", status == 200)
    if "article" in body:
        check("get article title", body["article"].get("title") == "How to train your dragon")


def test_list_articles_after_create():
    print("\n--- List Articles (after create) ---")
    status, body = request("GET", "/articles")
    check("list articles status 200", status == 200)
    check("articlesCount is 1", body.get("articlesCount") == 1)


def test_articles_by_author():
    print("\n--- Articles by Author ---")
    status, body = request("GET", "/articles?author=testuser")
    check("by author status 200", status == 200)
    check("by author count 1", body.get("articlesCount") == 1)


def test_articles_by_tag():
    print("\n--- Articles by Tag ---")
    status, body = request("GET", "/articles?tag=dragons")
    check("by tag status 200", status == 200)
    check("by tag count 1", body.get("articlesCount") == 1)


def test_feed():
    print("\n--- Feed ---")
    status, body = request("GET", "/articles/feed", auth=True)
    check("feed status 200", status == 200)
    check("feed articles array", isinstance(body.get("articles"), list))


def test_update_article():
    print("\n--- Update Article ---")
    status, body = request("PUT", f"/articles/{SLUG}", {
        "article": {"body": "With two hands"}
    }, auth=True)
    check("update article status 200", status == 200)
    if "article" in body:
        check("update article body", body["article"].get("body") == "With two hands")


def test_favorite_article():
    print("\n--- Favorite Article ---")
    status, body = request("POST", f"/articles/{SLUG}/favorite", auth=True)
    check("favorite status 200", status == 200)
    if "article" in body:
        check("favorite is true", body["article"].get("favorited") is True)
        check("favoritesCount is 1", body["article"].get("favoritesCount") == 1)


def test_unfavorite_article():
    print("\n--- Unfavorite Article ---")
    status, body = request("DELETE", f"/articles/{SLUG}/favorite", auth=True)
    check("unfavorite status 200", status == 200)
    if "article" in body:
        check("unfavorite is false", body["article"].get("favorited") is False)
        check("favoritesCount is 0", body["article"].get("favoritesCount") == 0)


def test_create_comment():
    global COMMENT_ID
    print("\n--- Create Comment ---")
    status, body = request("POST", f"/articles/{SLUG}/comments", {
        "comment": {"body": "Thank you so much!"}
    }, auth=True)
    check("create comment status 200", status == 200)
    if "comment" in body:
        COMMENT_ID = body["comment"].get("id")
        check("comment body", body["comment"].get("body") == "Thank you so much!")


def test_get_comments():
    print("\n--- Get Comments ---")
    status, body = request("GET", f"/articles/{SLUG}/comments")
    check("get comments status 200", status == 200)
    check("comments is array", isinstance(body.get("comments"), list))
    if body.get("comments"):
        check("first comment body", body["comments"][0].get("body") == "Thank you so much!")


def test_delete_comment():
    print("\n--- Delete Comment ---")
    status, body = request("DELETE", f"/articles/{SLUG}/comments/{COMMENT_ID}", auth=True)
    check("delete comment status 200", status == 200)


def test_profiles():
    print("\n--- Register Celeb ---")
    status, body = request("POST", "/users", {
        "user": {"username": "celeb_testuser", "email": "celeb_test@example.com", "password": "password"}
    })
    check("register celeb status 201", status == 201)

    print("\n--- Get Profile ---")
    status, body = request("GET", "/profiles/celeb_testuser", auth=True)
    check("get profile status 200", status == 200)
    if "profile" in body:
        check("profile username", body["profile"].get("username") == "celeb_testuser")
        check("profile following false", body["profile"].get("following") is False)

    print("\n--- Follow Profile ---")
    status, body = request("POST", "/profiles/celeb_testuser/follow", auth=True)
    check("follow status 200", status == 200)
    if "profile" in body:
        check("follow following true", body["profile"].get("following") is True)

    print("\n--- Unfollow Profile ---")
    status, body = request("DELETE", "/profiles/celeb_testuser/follow", auth=True)
    check("unfollow status 200", status == 200)
    if "profile" in body:
        check("unfollow following false", body["profile"].get("following") is False)


def test_tags():
    print("\n--- Tags ---")
    status, body = request("GET", "/tags")
    check("tags status 200", status == 200)
    check("tags is array", isinstance(body.get("tags"), list))
    if body.get("tags"):
        check("tags contain dragons", "dragons" in body["tags"])
        check("tags contain training", "training" in body["tags"])


def test_delete_article():
    print("\n--- Delete Article ---")
    status, body = request("DELETE", f"/articles/{SLUG}", auth=True)
    check("delete article status 200", status == 200)


def wait_for_server(max_wait=60):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {API}...")
    for i in range(max_wait):
        try:
            r = urllib.request.urlopen(f"{API}/actuator/health")
            if r.status == 200:
                print("Server is ready!")
                return True
        except Exception:
            pass
        time.sleep(1)
    print("Server did not become ready in time")
    return False


if __name__ == "__main__":
    if not wait_for_server():
        sys.exit(1)

    test_register()
    test_login()
    test_current_user()
    test_update_user()
    test_list_articles_empty()
    test_create_article()
    test_get_article_by_slug()
    test_list_articles_after_create()
    test_articles_by_author()
    test_articles_by_tag()
    test_feed()
    test_update_article()
    test_favorite_article()
    test_unfavorite_article()
    test_create_comment()
    test_get_comments()
    test_delete_comment()
    test_profiles()
    test_tags()
    test_delete_article()

    print(f"\n{'='*50}")
    print(f"Results: {PASSED} passed, {FAILED} failed out of {PASSED+FAILED} checks")
    print(f"{'='*50}")

    sys.exit(0 if FAILED == 0 else 1)
