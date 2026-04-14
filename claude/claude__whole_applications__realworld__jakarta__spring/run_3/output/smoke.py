#!/usr/bin/env python3
"""Smoke tests for the RealWorld API (Conduit) application."""

import json
import os
import sys
import time
import urllib.request
import urllib.error
import uuid

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
API_URL = f"{BASE_URL}/api"

PASS = 0
FAIL = 0


def api(method, path, body=None, token=None):
    """Make an API request and return (status_code, response_body_dict)."""
    url = f"{API_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        resp_body = resp.read().decode()
        return resp.status, json.loads(resp_body) if resp_body else {}
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode()
        try:
            return e.code, json.loads(resp_body) if resp_body else {}
        except json.JSONDecodeError:
            return e.code, {"raw": resp_body}
    except Exception as e:
        return 0, {"error": str(e)}


def check(name, condition):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  PASS: {name}")
    else:
        FAIL += 1
        print(f"  FAIL: {name}")


def test_health_and_tags():
    """Test that the API is responsive and tags endpoint works."""
    print("\n--- Test: Health & Tags ---")
    status, body = api("GET", "/tags")
    check("GET /tags returns 200", status == 200)
    check("Response has 'tags' key", "tags" in body)
    check("Tags is a list", isinstance(body.get("tags"), list))
    return status == 200


def test_user_registration():
    """Test user registration flow."""
    print("\n--- Test: User Registration ---")
    unique = uuid.uuid4().hex[:8]
    username = f"testuser_{unique}"
    email = f"test_{unique}@example.com"
    password = "password123"

    status, body = api("POST", "/users", {
        "user": {
            "username": username,
            "email": email,
            "password": password
        }
    })
    check("POST /users returns 201 or 200", status in (200, 201))
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        user = body["user"]
        check("User has username", user.get("username") == username)
        check("User has email", user.get("email") == email)
        check("User has token", "token" in user and user["token"] is not None)
        return user.get("token"), username, email, password
    return None, username, email, password


def test_user_login(email, password):
    """Test user login flow."""
    print("\n--- Test: User Login ---")
    status, body = api("POST", "/users/login", {
        "user": {
            "email": email,
            "password": password
        }
    })
    check("POST /users/login returns 200", status == 200)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        user = body["user"]
        check("User has token", "token" in user and user["token"] is not None)
        check("User has email", user.get("email") == email)
        return user.get("token")
    return None


def test_current_user(token):
    """Test getting the current user."""
    print("\n--- Test: Current User ---")
    status, body = api("GET", "/user", token=token)
    check("GET /user returns 200", status == 200)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        check("User has username", "username" in body["user"])
        check("User has email", "email" in body["user"])
        check("User has token", "token" in body["user"])


def test_update_user(token):
    """Test updating the current user."""
    print("\n--- Test: Update User ---")
    new_bio = "I am a test user with an updated bio"
    status, body = api("PUT", "/user", {
        "user": {
            "bio": new_bio
        }
    }, token=token)
    check("PUT /user returns 200", status == 200)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        check("Bio is updated", body["user"].get("bio") == new_bio)


def test_create_article(token):
    """Test creating an article."""
    print("\n--- Test: Create Article ---")
    unique = uuid.uuid4().hex[:8]
    title = f"Test Article {unique}"
    status, body = api("POST", "/articles", {
        "article": {
            "title": title,
            "description": "Test description",
            "body": "Test article body content",
            "tagList": ["test", "smoke"]
        }
    }, token=token)
    check("POST /articles returns 201 or 200", status in (200, 201))
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        article = body["article"]
        check("Article has slug", "slug" in article and article["slug"] is not None)
        check("Article has title", article.get("title") == title)
        check("Article has description", article.get("description") == "Test description")
        check("Article has body", article.get("body") == "Test article body content")
        check("Article has author", "author" in article)
        check("Article has tagList", "tagList" in article)
        check("Article has createdAt", "createdAt" in article)
        return article.get("slug")
    return None


def test_get_article(slug):
    """Test getting a single article by slug."""
    print("\n--- Test: Get Article ---")
    status, body = api("GET", f"/articles/{slug}")
    check("GET /articles/:slug returns 200", status == 200)
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        check("Article slug matches", body["article"].get("slug") == slug)


def test_list_articles():
    """Test listing articles."""
    print("\n--- Test: List Articles ---")
    status, body = api("GET", "/articles")
    check("GET /articles returns 200", status == 200)
    check("Response has 'articles' key", "articles" in body)
    check("Response has 'articlesCount' key", "articlesCount" in body)
    if "articles" in body:
        check("Articles is a list", isinstance(body["articles"], list))


def test_update_article(slug, token):
    """Test updating an article."""
    print("\n--- Test: Update Article ---")
    status, body = api("PUT", f"/articles/{slug}", {
        "article": {
            "description": "Updated description"
        }
    }, token=token)
    check("PUT /articles/:slug returns 200", status == 200)
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        check("Description is updated", body["article"].get("description") == "Updated description")
    return body.get("article", {}).get("slug", slug)


def test_favorite_article(slug, token):
    """Test favoriting an article."""
    print("\n--- Test: Favorite Article ---")
    status, body = api("POST", f"/articles/{slug}/favorite", token=token)
    check("POST /articles/:slug/favorite returns 200", status == 200)
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        check("Article is favorited", body["article"].get("favorited") is True)
        check("Favorites count >= 1", body["article"].get("favoritesCount", 0) >= 1)


def test_unfavorite_article(slug, token):
    """Test unfavoriting an article."""
    print("\n--- Test: Unfavorite Article ---")
    status, body = api("DELETE", f"/articles/{slug}/favorite", token=token)
    check("DELETE /articles/:slug/favorite returns 200", status == 200)
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        check("Article is not favorited", body["article"].get("favorited") is False)


def test_create_comment(slug, token):
    """Test creating a comment on an article."""
    print("\n--- Test: Create Comment ---")
    status, body = api("POST", f"/articles/{slug}/comments", {
        "comment": {
            "body": "This is a test comment"
        }
    }, token=token)
    check("POST /articles/:slug/comments returns 200", status == 200)
    check("Response has 'comment' key", "comment" in body)
    if "comment" in body:
        comment = body["comment"]
        check("Comment has id", "id" in comment)
        check("Comment has body", comment.get("body") == "This is a test comment")
        check("Comment has author", "author" in comment)
        check("Comment has createdAt", "createdAt" in comment)
        return comment.get("id")
    return None


def test_get_comments(slug):
    """Test getting comments for an article."""
    print("\n--- Test: Get Comments ---")
    status, body = api("GET", f"/articles/{slug}/comments")
    check("GET /articles/:slug/comments returns 200", status == 200)
    check("Response has 'comments' key", "comments" in body)
    if "comments" in body:
        check("Comments is a list", isinstance(body["comments"], list))
        check("At least one comment", len(body["comments"]) > 0)


def test_delete_comment(slug, comment_id, token):
    """Test deleting a comment."""
    print("\n--- Test: Delete Comment ---")
    status, body = api("DELETE", f"/articles/{slug}/comments/{comment_id}", token=token)
    check("DELETE /articles/:slug/comments/:id returns 200", status == 200)


def test_profile(username, token):
    """Test getting a user profile."""
    print("\n--- Test: Get Profile ---")
    status, body = api("GET", f"/profiles/{username}", token=token)
    check("GET /profiles/:username returns 200", status == 200)
    check("Response has 'profile' key", "profile" in body)
    if "profile" in body:
        profile = body["profile"]
        check("Profile has username", profile.get("username") == username)
        check("Profile has following field", "following" in profile)


def test_follow_unfollow(username_to_follow, token):
    """Test following and unfollowing a user."""
    print("\n--- Test: Follow/Unfollow ---")
    # Follow
    status, body = api("POST", f"/profiles/{username_to_follow}/follow", token=token)
    check("POST /profiles/:username/follow returns 200", status == 200)
    check("Response has 'profile' key", "profile" in body)
    if "profile" in body:
        check("Following is true", body["profile"].get("following") is True)

    # Unfollow
    status, body = api("DELETE", f"/profiles/{username_to_follow}/follow", token=token)
    check("DELETE /profiles/:username/follow returns 200", status == 200)
    if "profile" in body:
        check("Following is false after unfollow", body["profile"].get("following") is False)


def test_filter_articles_by_author(username, token):
    """Test filtering articles by author."""
    print("\n--- Test: Filter Articles by Author ---")
    status, body = api("GET", f"/articles?author={username}", token=token)
    check("GET /articles?author=... returns 200", status == 200)
    check("Response has 'articles' key", "articles" in body)


def test_filter_articles_by_tag():
    """Test filtering articles by tag."""
    print("\n--- Test: Filter Articles by Tag ---")
    status, body = api("GET", "/articles?tag=test")
    check("GET /articles?tag=... returns 200", status == 200)
    check("Response has 'articles' key", "articles" in body)


def test_article_feed(token):
    """Test the article feed endpoint."""
    print("\n--- Test: Article Feed ---")
    status, body = api("GET", "/articles/feed", token=token)
    check("GET /articles/feed returns 200", status == 200)
    check("Response has 'articles' key", "articles" in body)
    check("Response has 'articlesCount' key", "articlesCount" in body)


def test_delete_article(slug, token):
    """Test deleting an article."""
    print("\n--- Test: Delete Article ---")
    status, body = api("DELETE", f"/articles/{slug}", token=token)
    check("DELETE /articles/:slug returns 200", status == 200)


def wait_for_server(max_retries=30, delay=2):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {API_URL}...")
    for i in range(max_retries):
        try:
            req = urllib.request.Request(f"{API_URL}/tags", method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print(f"Server is ready after {i * delay} seconds")
                return True
        except Exception:
            pass
        time.sleep(delay)
    print(f"Server not ready after {max_retries * delay} seconds")
    return False


def main():
    global PASS, FAIL

    if not wait_for_server():
        print("FATAL: Server did not become ready")
        sys.exit(1)

    # Test basic health
    if not test_health_and_tags():
        print("FATAL: API not responding, aborting tests")
        sys.exit(1)

    # Register user 1
    token1, username1, email1, password1 = test_user_registration()
    if not token1:
        print("FATAL: Could not register user, aborting tests")
        sys.exit(1)

    # Login
    token1 = test_user_login(email1, password1)
    if not token1:
        print("FATAL: Could not login, aborting tests")
        sys.exit(1)

    # Current user
    test_current_user(token1)

    # Update user
    test_update_user(token1)

    # Register user 2 for follow/unfollow tests
    token2, username2, email2, password2 = test_user_registration()

    # Create article
    slug = test_create_article(token1)
    if not slug:
        print("FATAL: Could not create article, aborting tests")
        sys.exit(1)

    # Get article
    test_get_article(slug)

    # List articles
    test_list_articles()

    # Update article
    slug = test_update_article(slug, token1)

    # Favorite / unfavorite
    test_favorite_article(slug, token1)
    test_unfavorite_article(slug, token1)

    # Comments
    comment_id = test_create_comment(slug, token1)
    test_get_comments(slug)
    if comment_id:
        test_delete_comment(slug, comment_id, token1)

    # Profiles
    test_profile(username1, token1)

    # Follow / unfollow (user1 follows user2)
    if token2 and username2:
        test_follow_unfollow(username2, token1)

    # Filter articles
    test_filter_articles_by_author(username1, token1)
    test_filter_articles_by_tag()

    # Feed
    test_article_feed(token1)

    # Delete article
    test_delete_article(slug, token1)

    # Summary
    total = PASS + FAIL
    print(f"\n{'='*60}")
    print(f"SMOKE TEST RESULTS: {PASS}/{total} passed, {FAIL}/{total} failed")
    print(f"{'='*60}")

    if FAIL > 0:
        sys.exit(1)
    sys.exit(0)


if __name__ == "__main__":
    main()
