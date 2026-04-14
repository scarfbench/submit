#!/usr/bin/env python3
"""Smoke tests for the RealWorld API application."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080/api")

passed = 0
failed = 0
errors = []


def request(method, path, data=None, token=None):
    """Make an HTTP request and return (status_code, response_body_dict)."""
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        resp_body = resp.read().decode("utf-8")
        return resp.status, json.loads(resp_body) if resp_body else {}
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        try:
            return e.code, json.loads(resp_body)
        except Exception:
            return e.code, {"raw": resp_body}


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


def wait_for_server(max_wait=120):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(f"{BASE_URL}/tags", method="GET")
            resp = urllib.request.urlopen(req)
            if resp.status == 200:
                print("Server is ready!")
                return True
        except Exception:
            pass
        time.sleep(2)
    print("Server did not become ready in time.")
    return False


def test_register():
    """Test user registration."""
    print("\n--- Test: Register User ---")
    status, body = request("POST", "/users", {
        "user": {
            "username": "smokeuser",
            "email": "smoke@example.com",
            "password": "password123"
        }
    })
    check("Register returns 201", status == 201, f"got {status}")
    check("Register returns user object", "user" in body)
    if "user" in body:
        user = body["user"]
        check("User has email", user.get("email") == "smoke@example.com")
        check("User has username", user.get("username") == "smokeuser")
        check("User has token", user.get("token") is not None)
        return user.get("token")
    return None


def test_login():
    """Test user login."""
    print("\n--- Test: Login User ---")
    status, body = request("POST", "/users/login", {
        "user": {
            "email": "smoke@example.com",
            "password": "password123"
        }
    })
    check("Login returns 200", status == 200, f"got {status}")
    check("Login returns user object", "user" in body)
    if "user" in body:
        user = body["user"]
        check("Login user has email", user.get("email") == "smoke@example.com")
        check("Login user has username", user.get("username") == "smokeuser")
        check("Login user has token", user.get("token") is not None)
        return user.get("token")
    return None


def test_get_current_user(token):
    """Test getting current user."""
    print("\n--- Test: Get Current User ---")
    status, body = request("GET", "/user", token=token)
    check("Get user returns 200", status == 200, f"got {status}")
    check("Get user returns user object", "user" in body)
    if "user" in body:
        user = body["user"]
        check("Current user has email", user.get("email") == "smoke@example.com")
        check("Current user has username", user.get("username") == "smokeuser")


def test_update_user(token):
    """Test updating current user."""
    print("\n--- Test: Update User ---")
    status, body = request("PUT", "/user", {
        "user": {
            "bio": "I am a smoke test user"
        }
    }, token=token)
    check("Update user returns 200", status == 200, f"got {status}")
    if "user" in body:
        user = body["user"]
        check("Updated user has bio", user.get("bio") == "I am a smoke test user")


def test_get_articles_empty():
    """Test getting articles when empty."""
    print("\n--- Test: Get Articles (empty) ---")
    status, body = request("GET", "/articles")
    check("Get articles returns 200", status == 200, f"got {status}")
    check("Articles is array", isinstance(body.get("articles"), list))
    check("Articles count is number", isinstance(body.get("articlesCount"), (int, float)))


def test_create_article(token):
    """Test creating an article."""
    print("\n--- Test: Create Article ---")
    status, body = request("POST", "/articles", {
        "article": {
            "title": "How to train your dragon",
            "description": "Ever wonder how?",
            "body": "Very carefully.",
            "tagList": ["dragons", "training"]
        }
    }, token=token)
    check("Create article returns 201", status == 201, f"got {status}")
    check("Create article returns article object", "article" in body)
    if "article" in body:
        article = body["article"]
        check("Article has title", article.get("title") == "How to train your dragon")
        check("Article has slug", article.get("slug") is not None)
        check("Article has body", article.get("body") == "Very carefully.")
        check("Article has description", article.get("description") == "Ever wonder how?")
        check("Article has tagList", isinstance(article.get("tagList"), list))
        check("Article has createdAt", article.get("createdAt") is not None)
        check("Article has updatedAt", article.get("updatedAt") is not None)
        check("Article has author", isinstance(article.get("author"), dict))
        check("Article favoritesCount is 0", article.get("favoritesCount") == 0)
        return article.get("slug")
    return None


def test_get_article_by_slug(slug):
    """Test getting a single article by slug."""
    print("\n--- Test: Get Article by Slug ---")
    status, body = request("GET", f"/articles/{slug}")
    check("Get article returns 200", status == 200, f"got {status}")
    if "article" in body:
        article = body["article"]
        check("Article slug matches", article.get("slug") == slug)
        check("Article has title", article.get("title") is not None)


def test_update_article(token, slug):
    """Test updating an article."""
    print("\n--- Test: Update Article ---")
    status, body = request("PUT", f"/articles/{slug}", {
        "article": {
            "body": "With two hands"
        }
    }, token=token)
    check("Update article returns 200", status == 200, f"got {status}")
    if "article" in body:
        check("Updated article body", body["article"].get("body") == "With two hands")


def test_favorite_article(token, slug):
    """Test favoriting an article."""
    print("\n--- Test: Favorite Article ---")
    status, body = request("POST", f"/articles/{slug}/favorite", token=token)
    check("Favorite article returns 200", status == 200, f"got {status}")
    if "article" in body:
        check("Article is favorited", body["article"].get("favorited") is True)
        check("Favorites count is 1", body["article"].get("favoritesCount") == 1)


def test_unfavorite_article(token, slug):
    """Test unfavoriting an article."""
    print("\n--- Test: Unfavorite Article ---")
    status, body = request("DELETE", f"/articles/{slug}/favorite", token=token)
    check("Unfavorite article returns 200", status == 200, f"got {status}")
    if "article" in body:
        check("Article is not favorited", body["article"].get("favorited") is False)
        check("Favorites count is 0", body["article"].get("favoritesCount") == 0)


def test_create_comment(token, slug):
    """Test creating a comment on an article."""
    print("\n--- Test: Create Comment ---")
    status, body = request("POST", f"/articles/{slug}/comments", {
        "comment": {
            "body": "Thank you so much!"
        }
    }, token=token)
    check("Create comment returns 200", status == 200, f"got {status}")
    if "comment" in body:
        comment = body["comment"]
        check("Comment has body", comment.get("body") == "Thank you so much!")
        check("Comment has id", comment.get("id") is not None)
        check("Comment has author", isinstance(comment.get("author"), dict))
        return comment.get("id")
    return None


def test_get_comments(slug):
    """Test getting comments for an article."""
    print("\n--- Test: Get Comments ---")
    status, body = request("GET", f"/articles/{slug}/comments")
    check("Get comments returns 200", status == 200, f"got {status}")
    check("Comments is array", isinstance(body.get("comments"), list))
    if body.get("comments"):
        check("First comment has body", body["comments"][0].get("body") == "Thank you so much!")


def test_delete_comment(token, slug, comment_id):
    """Test deleting a comment."""
    print("\n--- Test: Delete Comment ---")
    status, body = request("DELETE", f"/articles/{slug}/comments/{comment_id}", token=token)
    check("Delete comment returns 200", status == 200, f"got {status}")


def test_profiles(token):
    """Test profile endpoints."""
    print("\n--- Test: Create Second User for Profiles ---")
    status, body = request("POST", "/users", {
        "user": {
            "username": "celeb_smokeuser",
            "email": "celeb_smoke@example.com",
            "password": "password123"
        }
    })
    check("Register celeb returns 201", status == 201, f"got {status}")

    print("\n--- Test: Get Profile ---")
    status, body = request("GET", "/profiles/celeb_smokeuser", token=token)
    check("Get profile returns 200", status == 200, f"got {status}")
    if "profile" in body:
        profile = body["profile"]
        check("Profile has username", profile.get("username") == "celeb_smokeuser")
        check("Profile not following", profile.get("following") is False)

    print("\n--- Test: Follow Profile ---")
    status, body = request("POST", "/profiles/celeb_smokeuser/follow", token=token)
    check("Follow returns 200", status == 200, f"got {status}")
    if "profile" in body:
        check("Now following", body["profile"].get("following") is True)

    print("\n--- Test: Unfollow Profile ---")
    status, body = request("DELETE", "/profiles/celeb_smokeuser/follow", token=token)
    check("Unfollow returns 200", status == 200, f"got {status}")
    if "profile" in body:
        check("No longer following", body["profile"].get("following") is False)


def test_tags():
    """Test tags endpoint."""
    print("\n--- Test: Get Tags ---")
    status, body = request("GET", "/tags")
    check("Get tags returns 200", status == 200, f"got {status}")
    check("Tags is array", isinstance(body.get("tags"), list))


def test_feed(token):
    """Test feed endpoint."""
    print("\n--- Test: Get Feed ---")
    status, body = request("GET", "/articles/feed", token=token)
    check("Feed returns 200", status == 200, f"got {status}")
    check("Feed articles is array", isinstance(body.get("articles"), list))
    check("Feed articlesCount is number", isinstance(body.get("articlesCount"), (int, float)))


def test_articles_by_filter(token, slug):
    """Test filtering articles."""
    print("\n--- Test: Articles by Author ---")
    status, body = request("GET", "/articles?author=smokeuser")
    check("Articles by author returns 200", status == 200, f"got {status}")
    check("Articles by author count >= 1", body.get("articlesCount", 0) >= 1)

    print("\n--- Test: Articles by Tag ---")
    status, body = request("GET", "/articles?tag=dragons")
    check("Articles by tag returns 200", status == 200, f"got {status}")
    check("Articles by tag count >= 1", body.get("articlesCount", 0) >= 1)


def test_delete_article(token, slug):
    """Test deleting an article."""
    print("\n--- Test: Delete Article ---")
    status, body = request("DELETE", f"/articles/{slug}", token=token)
    check("Delete article returns 200", status == 200, f"got {status}")


def main():
    global passed, failed

    if not wait_for_server():
        print("FATAL: Server not reachable")
        sys.exit(1)

    # Auth tests
    token = test_register()
    if not token:
        token = test_login()
    else:
        test_login()

    if not token:
        print("FATAL: Could not obtain auth token")
        sys.exit(1)

    test_get_current_user(token)
    test_update_user(token)

    # Article tests
    test_get_articles_empty()
    slug = test_create_article(token)

    if slug:
        test_get_article_by_slug(slug)
        test_update_article(token, slug)
        test_articles_by_filter(token, slug)
        test_favorite_article(token, slug)
        test_unfavorite_article(token, slug)

        # Comment tests
        comment_id = test_create_comment(token, slug)
        test_get_comments(slug)
        if comment_id:
            test_delete_comment(token, slug, comment_id)

        # Feed test
        test_feed(token)

        # Profile tests
        test_profiles(token)

        # Tags test
        test_tags()

        # Cleanup
        test_delete_article(token, slug)

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed")
    if errors:
        print("Failures:")
        for e in errors:
            print(f"  {e}")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
