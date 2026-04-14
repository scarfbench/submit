#!/usr/bin/env python3
"""
Smoke tests for the RealWorld API (Conduit).
Tests the core functionality: users, articles, comments, tags, profiles.
"""

import json
import sys
import time
import urllib.request
import urllib.error
import uuid
import traceback

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
API = f"{BASE_URL}/api"

passed = 0
failed = 0
errors = []

def req(method, path, data=None, token=None, expected_status=None):
    """Make an HTTP request and return (status_code, parsed_json_or_None)."""
    url = f"{API}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    body = json.dumps(data).encode("utf-8") if data else None
    request = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(request)
        status = resp.getcode()
        raw = resp.read().decode("utf-8")
        parsed = json.loads(raw) if raw else None
        return status, parsed
    except urllib.error.HTTPError as e:
        status = e.code
        raw = e.read().decode("utf-8")
        try:
            parsed = json.loads(raw) if raw else None
        except Exception:
            parsed = raw
        return status, parsed


def check(name, condition, detail=""):
    global passed, failed, errors
    if condition:
        passed += 1
        print(f"  PASS: {name}")
    else:
        failed += 1
        msg = f"  FAIL: {name}"
        if detail:
            msg += f" -- {detail}"
        print(msg)
        errors.append(msg)


def test_health():
    """Test that the API is reachable."""
    print("\n--- Health Check ---")
    try:
        status, body = req("GET", "/tags")
        check("API is reachable (GET /api/tags)", status == 200, f"status={status}")
    except Exception as e:
        check("API is reachable", False, str(e))


def test_register_user():
    """Test user registration and return (user_data, token)."""
    print("\n--- User Registration ---")
    unique = uuid.uuid4().hex[:8]
    payload = {
        "user": {
            "username": f"testuser_{unique}",
            "email": f"test_{unique}@example.com",
            "password": "password123"
        }
    }
    status, body = req("POST", "/users", data=payload)
    check("Register user returns 201", status == 201, f"status={status}")
    check("Response contains user object", body is not None and "user" in body, f"body={body}")

    if body and "user" in body:
        user = body["user"]
        check("User has username", user.get("username") == payload["user"]["username"],
              f"got={user.get('username')}")
        check("User has email", user.get("email") == payload["user"]["email"],
              f"got={user.get('email')}")
        check("User has token", "token" in user and user["token"] is not None,
              f"token={'present' if user.get('token') else 'missing'}")
        return user, user.get("token"), payload["user"]["password"]

    return None, None, None


def test_login(email, password):
    """Test user login."""
    print("\n--- User Login ---")
    payload = {"user": {"email": email, "password": password}}
    status, body = req("POST", "/users/login", data=payload)
    check("Login returns 200", status == 200, f"status={status}")
    check("Login response has user", body is not None and "user" in body, f"body={body}")
    if body and "user" in body:
        check("Login returns token", body["user"].get("token") is not None)
        return body["user"].get("token")
    return None


def test_get_current_user(token):
    """Test get current user."""
    print("\n--- Get Current User ---")
    status, body = req("GET", "/user", token=token)
    check("Get current user returns 200", status == 200, f"status={status}")
    check("Response has user", body is not None and "user" in body, f"body={body}")
    if body and "user" in body:
        check("User has username", "username" in body["user"])
        check("User has email", "email" in body["user"])


def test_update_user(token):
    """Test update user."""
    print("\n--- Update User ---")
    payload = {"user": {"bio": "I am a test user"}}
    status, body = req("PUT", "/user", data=payload, token=token)
    check("Update user returns 200", status == 200, f"status={status}")
    if body and "user" in body:
        check("Bio was updated", body["user"].get("bio") == "I am a test user",
              f"got={body['user'].get('bio')}")


def test_create_article(token):
    """Test article creation and return article data."""
    print("\n--- Create Article ---")
    unique = uuid.uuid4().hex[:8]
    payload = {
        "article": {
            "title": f"Test Article {unique}",
            "description": "This is a test article",
            "body": "The body of the test article",
            "tagList": ["test", "smoke"]
        }
    }
    status, body = req("POST", "/articles", data=payload, token=token)
    check("Create article returns 201", status == 201, f"status={status}")
    check("Response has article", body is not None and "article" in body, f"body={body}")
    if body and "article" in body:
        article = body["article"]
        check("Article has slug", "slug" in article and article["slug"] is not None)
        check("Article has title", article.get("title") == payload["article"]["title"],
              f"got={article.get('title')}")
        check("Article has description", article.get("description") == payload["article"]["description"])
        check("Article has body", article.get("body") == payload["article"]["body"])
        check("Article has author", "author" in article and article["author"] is not None)
        check("Article has tagList", "tagList" in article)
        return article
    return None


def test_get_article(slug):
    """Test get article by slug."""
    print("\n--- Get Article ---")
    status, body = req("GET", f"/articles/{slug}")
    check("Get article returns 200", status == 200, f"status={status}")
    if body and "article" in body:
        check("Article slug matches", body["article"].get("slug") == slug)


def test_list_articles():
    """Test listing articles."""
    print("\n--- List Articles ---")
    status, body = req("GET", "/articles")
    check("List articles returns 200", status == 200, f"status={status}")
    check("Response has articles array", body is not None and "articles" in body, f"body={body}")
    if body and "articles" in body:
        check("Response has articlesCount", "articlesCount" in body)


def test_update_article(slug, token):
    """Test updating an article."""
    print("\n--- Update Article ---")
    payload = {"article": {"description": "Updated description"}}
    status, body = req("PUT", f"/articles/{slug}", data=payload, token=token)
    check("Update article returns 200", status == 200, f"status={status}")
    if body and "article" in body:
        check("Description was updated", body["article"].get("description") == "Updated description",
              f"got={body['article'].get('description')}")


def test_add_comment(slug, token):
    """Test adding a comment to an article."""
    print("\n--- Add Comment ---")
    payload = {"comment": {"body": "This is a test comment"}}
    status, body = req("POST", f"/articles/{slug}/comments", data=payload, token=token)
    check("Add comment returns 200", status == 200, f"status={status}")
    check("Response has comment", body is not None and "comment" in body, f"body={body}")
    if body and "comment" in body:
        comment = body["comment"]
        check("Comment has body", comment.get("body") == "This is a test comment")
        check("Comment has id", "id" in comment)
        check("Comment has author", "author" in comment)
        return comment
    return None


def test_get_comments(slug):
    """Test getting comments for an article."""
    print("\n--- Get Comments ---")
    status, body = req("GET", f"/articles/{slug}/comments")
    check("Get comments returns 200", status == 200, f"status={status}")
    check("Response has comments array", body is not None and "comments" in body, f"body={body}")


def test_favorite_article(slug, token):
    """Test favoriting an article."""
    print("\n--- Favorite Article ---")
    status, body = req("POST", f"/articles/{slug}/favorite", token=token)
    check("Favorite article returns 200", status == 200, f"status={status}")
    if body and "article" in body:
        check("Article is favorited", body["article"].get("favorited") == True,
              f"got={body['article'].get('favorited')}")
        check("Favorites count >= 1", body["article"].get("favoritesCount", 0) >= 1)


def test_unfavorite_article(slug, token):
    """Test unfavoriting an article."""
    print("\n--- Unfavorite Article ---")
    status, body = req("DELETE", f"/articles/{slug}/favorite", token=token)
    check("Unfavorite article returns 200", status == 200, f"status={status}")
    if body and "article" in body:
        check("Article is not favorited", body["article"].get("favorited") == False,
              f"got={body['article'].get('favorited')}")


def test_tags():
    """Test getting tags."""
    print("\n--- Get Tags ---")
    status, body = req("GET", "/tags")
    check("Get tags returns 200", status == 200, f"status={status}")
    check("Response has tags array", body is not None and "tags" in body, f"body={body}")


def test_profile(username, token):
    """Test getting a user profile."""
    print("\n--- Get Profile ---")
    status, body = req("GET", f"/profiles/{username}", token=token)
    check("Get profile returns 200", status == 200, f"status={status}")
    check("Response has profile", body is not None and "profile" in body, f"body={body}")
    if body and "profile" in body:
        check("Profile has username", body["profile"].get("username") == username)


def test_follow_user(token):
    """Test following another user."""
    print("\n--- Follow/Unfollow User ---")
    # First create another user to follow
    unique = uuid.uuid4().hex[:8]
    payload = {
        "user": {
            "username": f"followme_{unique}",
            "email": f"followme_{unique}@example.com",
            "password": "password123"
        }
    }
    status, body = req("POST", "/users", data=payload)
    if status != 201 or not body or "user" not in body:
        check("Created user to follow", False, f"status={status}")
        return

    target_username = body["user"]["username"]

    # Follow
    status, body = req("POST", f"/profiles/{target_username}/follow", token=token)
    check("Follow user returns 200", status == 200, f"status={status}")
    if body and "profile" in body:
        check("User is now followed", body["profile"].get("following") == True,
              f"got={body['profile'].get('following')}")

    # Unfollow
    status, body = req("DELETE", f"/profiles/{target_username}/follow", token=token)
    check("Unfollow user returns 200", status == 200, f"status={status}")
    if body and "profile" in body:
        check("User is now unfollowed", body["profile"].get("following") == False,
              f"got={body['profile'].get('following')}")


def test_delete_comment(slug, comment_id, token):
    """Test deleting a comment."""
    print("\n--- Delete Comment ---")
    status, body = req("DELETE", f"/articles/{slug}/comments/{comment_id}", token=token)
    check("Delete comment returns 200", status == 200, f"status={status}")


def test_delete_article(slug, token):
    """Test deleting an article."""
    print("\n--- Delete Article ---")
    status, body = req("DELETE", f"/articles/{slug}", token=token)
    check("Delete article returns 200", status == 200, f"status={status}")

    # Verify it's gone
    status2, _ = req("GET", f"/articles/{slug}")
    check("Deleted article is not found", status2 == 404, f"status={status2}")


def test_unauthorized_access():
    """Test that protected endpoints reject unauthorized requests."""
    print("\n--- Unauthorized Access ---")
    status, body = req("GET", "/user")
    check("Get user without token returns 401", status == 401, f"status={status}")

    status, body = req("POST", "/articles", data={"article": {"title": "x", "description": "x", "body": "x"}})
    check("Create article without token returns 401", status == 401, f"status={status}")


def test_feed(token):
    """Test the article feed endpoint."""
    print("\n--- Article Feed ---")
    status, body = req("GET", "/articles/feed", token=token)
    check("Feed returns 200", status == 200, f"status={status}")
    check("Feed has articles array", body is not None and "articles" in body, f"body={body}")


def main():
    global passed, failed

    print(f"RealWorld API Smoke Tests")
    print(f"Target: {API}")
    print("=" * 60)

    # Wait for API to be ready
    print("\nWaiting for API to be ready...")
    for i in range(30):
        try:
            status, _ = req("GET", "/tags")
            if status == 200:
                print(f"API ready after {i+1} attempts")
                break
        except Exception:
            pass
        time.sleep(2)
    else:
        print("ERROR: API did not become ready in time")
        sys.exit(1)

    try:
        # Test health
        test_health()

        # Test unauthorized access
        test_unauthorized_access()

        # Test user registration
        user_data, token, password = test_register_user()
        if not token:
            print("\nFATAL: Could not register user, aborting remaining tests")
            sys.exit(1)

        # Test login
        login_token = test_login(user_data["email"], password)
        if login_token:
            token = login_token

        # Test get current user
        test_get_current_user(token)

        # Test update user
        test_update_user(token)

        # Test tags
        test_tags()

        # Test profile
        test_profile(user_data["username"], token)

        # Test follow/unfollow
        test_follow_user(token)

        # Test article CRUD
        article = test_create_article(token)
        if article:
            slug = article["slug"]

            # Test get article
            test_get_article(slug)

            # Test list articles
            test_list_articles()

            # Test update article
            test_update_article(slug, token)

            # Test favorite/unfavorite
            test_favorite_article(slug, token)
            test_unfavorite_article(slug, token)

            # Test comments
            comment = test_add_comment(slug, token)
            test_get_comments(slug)

            if comment:
                test_delete_comment(slug, comment["id"], token)

            # Test feed
            test_feed(token)

            # Test delete article
            test_delete_article(slug, token)
        else:
            print("\nWARNING: Could not create article, skipping article-related tests")

    except Exception as e:
        print(f"\nUNEXPECTED ERROR: {e}")
        traceback.print_exc()
        failed += 1

    # Summary
    print("\n" + "=" * 60)
    print(f"RESULTS: {passed} passed, {failed} failed, {passed + failed} total")
    if errors:
        print("\nFailed tests:")
        for e in errors:
            print(f"  {e}")
    print("=" * 60)

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
