#!/usr/bin/env python3
"""
Smoke tests for the RealWorld Conduit API.
Tests the core endpoints: users, profiles, articles, comments, tags.
"""

import json
import sys
import time
import urllib.request
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

passed = 0
failed = 0
errors = []


def api(method, path, body=None, token=None):
    """Make an HTTP request to the API."""
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
            return e.code, {}


def check(name, condition, detail=""):
    global passed, failed
    if condition:
        passed += 1
        print(f"  PASS: {name}")
    else:
        failed += 1
        msg = f"  FAIL: {name}"
        if detail:
            msg += f" - {detail}"
        print(msg)
        errors.append(msg)


def wait_for_ready(max_wait=60):
    """Wait for the application to be ready."""
    print(f"Waiting for {BASE_URL} to be ready...")
    for i in range(max_wait):
        try:
            req = urllib.request.Request(f"{BASE_URL}/tags", method="GET")
            with urllib.request.urlopen(req, timeout=2) as resp:
                if resp.status == 200:
                    print("Application is ready!")
                    return True
        except Exception:
            pass
        time.sleep(1)
    print("Application did not become ready in time!")
    return False


def test_registration():
    print("\n--- Test: User Registration ---")
    status, body = api("POST", "/users", {
        "user": {
            "email": "smoke@test.com",
            "password": "smokepassword",
            "username": "smokeuser"
        }
    })
    check("Register returns 200", status == 200, f"got {status}")
    check("Register returns user object", "user" in body, f"body: {body}")
    if "user" in body:
        check("User has email", "email" in body["user"])
        check("User has token", "token" in body["user"])
        check("User has username", "username" in body["user"])
        return body["user"].get("token")
    return None


def test_login():
    print("\n--- Test: User Login ---")
    status, body = api("POST", "/users/login", {
        "user": {
            "email": "smoke@test.com",
            "password": "smokepassword"
        }
    })
    check("Login returns 200", status == 200, f"got {status}")
    check("Login returns user object", "user" in body)
    if "user" in body:
        check("Login user has token", "token" in body["user"])
        return body["user"].get("token")
    return None


def test_get_current_user(token):
    print("\n--- Test: Get Current User ---")
    status, body = api("GET", "/user", token=token)
    check("Get user returns 200", status == 200, f"got {status}")
    check("Get user returns user object", "user" in body)


def test_update_user(token):
    print("\n--- Test: Update User ---")
    status, body = api("PUT", "/user", {
        "user": {"bio": "I like smoke tests"}
    }, token=token)
    check("Update user returns 200", status == 200, f"got {status}")
    check("Update user returns user object", "user" in body)


def test_profiles(token):
    print("\n--- Test: Profiles ---")
    # Register another user
    api("POST", "/users", {
        "user": {
            "email": "celeb_smoke@test.com",
            "password": "smokepassword",
            "username": "celeb_smokeuser"
        }
    })

    status, body = api("GET", "/profiles/celeb_smokeuser", token=token)
    check("Get profile returns 200", status == 200, f"got {status}")
    check("Profile has profile object", "profile" in body)
    if "profile" in body:
        check("Profile has username", "username" in body["profile"])
        check("Profile has following", "following" in body["profile"])

    # Follow
    status, body = api("POST", "/profiles/celeb_smokeuser/follow", token=token)
    check("Follow returns 200", status == 200, f"got {status}")
    if "profile" in body:
        check("Following is true after follow", body["profile"].get("following") is True)

    # Unfollow
    status, body = api("DELETE", "/profiles/celeb_smokeuser/follow", token=token)
    check("Unfollow returns 200", status == 200, f"got {status}")
    if "profile" in body:
        check("Following is false after unfollow", body["profile"].get("following") is False)


def test_articles(token):
    print("\n--- Test: Articles ---")
    # Create article
    status, body = api("POST", "/articles", {
        "article": {
            "title": "Smoke Test Article",
            "description": "Testing the API",
            "body": "This is a smoke test.",
            "tagList": ["smoke", "test"]
        }
    }, token=token)
    check("Create article returns 200", status == 200, f"got {status}")
    check("Create article returns article", "article" in body)
    if "article" in body:
        slug = body["article"].get("slug")
        check("Article has slug", slug is not None)
        check("Article has title", "title" in body["article"])
        check("Article has tagList", "tagList" in body["article"])

        # Get article by slug
        status2, body2 = api("GET", f"/articles/{slug}")
        check("Get article by slug returns 200", status2 == 200, f"got {status2}")

        # Update article
        status3, body3 = api("PUT", f"/articles/{slug}", {
            "article": {"body": "Updated body"}
        }, token=token)
        check("Update article returns 200", status3 == 200, f"got {status3}")
        if "article" in body3:
            check("Article body updated", body3["article"].get("body") == "Updated body")

        # Favorite article
        status4, body4 = api("POST", f"/articles/{slug}/favorite", token=token)
        check("Favorite article returns 200", status4 == 200, f"got {status4}")

        # Unfavorite article
        status5, body5 = api("DELETE", f"/articles/{slug}/favorite", token=token)
        check("Unfavorite article returns 200", status5 == 200, f"got {status5}")

        return slug
    return None


def test_list_articles(token):
    print("\n--- Test: List Articles ---")
    status, body = api("GET", "/articles?limit=20&offset=0")
    check("List articles returns 200", status == 200, f"got {status}")
    check("List articles has articles array", "articles" in body)
    check("List articles has articlesCount", "articlesCount" in body)

    # Articles by author
    status2, body2 = api("GET", "/articles?author=smokeuser", token=token)
    check("List articles by author returns 200", status2 == 200, f"got {status2}")

    # Articles by tag
    status3, body3 = api("GET", "/articles?tag=smoke", token=token)
    check("List articles by tag returns 200", status3 == 200, f"got {status3}")

    # Feed
    status4, body4 = api("GET", "/articles/feed", token=token)
    check("Get feed returns 200", status4 == 200, f"got {status4}")


def test_comments(token, slug):
    print("\n--- Test: Comments ---")
    if not slug:
        print("  SKIP: No article slug available")
        return

    # Create comment
    status, body = api("POST", f"/articles/{slug}/comments", {
        "comment": {"body": "Great article!"}
    }, token=token)
    check("Create comment returns 200", status == 200, f"got {status}")
    check("Create comment has comment object", "comment" in body)
    comment_id = None
    if "comment" in body:
        comment_id = body["comment"].get("id")
        check("Comment has id", comment_id is not None)
        check("Comment has body", "body" in body["comment"])

    # Get comments
    status2, body2 = api("GET", f"/articles/{slug}/comments", token=token)
    check("Get comments returns 200", status2 == 200, f"got {status2}")
    check("Get comments has comments array", "comments" in body2)

    # Delete comment
    if comment_id:
        status3, _ = api("DELETE", f"/articles/{slug}/comments/{comment_id}", token=token)
        check("Delete comment returns 200", status3 == 200, f"got {status3}")


def test_tags():
    print("\n--- Test: Tags ---")
    status, body = api("GET", "/tags")
    check("Get tags returns 200", status == 200, f"got {status}")
    check("Tags has tags array", "tags" in body)


def test_delete_article(token, slug):
    print("\n--- Test: Delete Article ---")
    if not slug:
        print("  SKIP: No article slug available")
        return
    status, _ = api("DELETE", f"/articles/{slug}", token=token)
    check("Delete article returns 204", status == 204, f"got {status}")


def test_unauthorized():
    print("\n--- Test: Unauthorized Access ---")
    status, _ = api("GET", "/user")
    check("Get user without token returns 401", status == 401, f"got {status}")


def main():
    global passed, failed
    print(f"=== RealWorld API Smoke Tests ===")
    print(f"Base URL: {BASE_URL}")

    if not wait_for_ready():
        print("FATAL: Application not ready")
        sys.exit(1)

    token = test_registration()
    if not token:
        token = test_login()
    else:
        test_login()

    if token:
        test_get_current_user(token)
        test_update_user(token)
        test_profiles(token)
        slug = test_articles(token)
        test_list_articles(token)
        test_comments(token, slug)
        test_tags()
        test_delete_article(token, slug)
        test_unauthorized()
    else:
        print("FATAL: Could not obtain auth token")
        failed += 1

    print(f"\n=== Results: {passed} passed, {failed} failed ===")
    if errors:
        print("\nFailed tests:")
        for e in errors:
            print(e)

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
