#!/usr/bin/env python3
"""Smoke tests for the RealWorld Conduit API after Spring -> Jakarta migration."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
TIMEOUT = 10

def req(method, path, body=None, token=None):
    """Make an HTTP request and return (status_code, parsed_json_or_None)."""
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    data = json.dumps(body).encode() if body else None
    r = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(r, timeout=TIMEOUT)
        raw = resp.read().decode()
        return resp.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read().decode() if e.fp else ""
        try:
            return e.code, json.loads(raw) if raw else None
        except Exception:
            return e.code, raw
    except Exception as e:
        return None, str(e)

passed = 0
failed = 0

def check(name, condition, detail=""):
    global passed, failed
    if condition:
        passed += 1
        print(f"  PASS: {name}")
    else:
        failed += 1
        print(f"  FAIL: {name} -- {detail}")

def wait_for_server(max_wait=120):
    """Wait for the server to become available."""
    print(f"Waiting for server at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            urllib.request.urlopen(f"{BASE_URL}/tags", timeout=5)
            print("Server is up!")
            return True
        except Exception:
            time.sleep(2)
    print("Server did not start in time!")
    return False

def main():
    if not wait_for_server():
        sys.exit(1)

    # ---- 1. Tags endpoint (public) ----
    print("\n[1] GET /tags")
    status, data = req("GET", "/tags")
    check("GET /tags returns 200", status == 200, f"status={status}")
    check("GET /tags has 'tags' key", data and "tags" in data, f"data={data}")

    # ---- 2. Articles endpoint (public) ----
    print("\n[2] GET /articles")
    status, data = req("GET", "/articles")
    check("GET /articles returns 200", status == 200, f"status={status}")
    check("GET /articles has 'articles' key", data and "articles" in data, f"data={data}")

    # ---- 3. Register a user ----
    print("\n[3] POST /users (register)")
    user_body = {
        "user": {
            "username": "smokeuser",
            "email": "smokeuser@example.com",
            "password": "password123"
        }
    }
    status, data = req("POST", "/users", body=user_body)
    check("POST /users returns 200", status == 200, f"status={status}, data={data}")
    token = None
    if data and "user" in data:
        token = data["user"].get("token")
        check("Registration returns token", token is not None, f"data={data}")
        check("Registration returns email", data["user"].get("email") == "smokeuser@example.com")
        check("Registration returns username", data["user"].get("username") == "smokeuser")
    else:
        check("Registration returns user object", False, f"data={data}")

    # ---- 4. Login ----
    print("\n[4] POST /users/login")
    login_body = {
        "user": {
            "email": "smokeuser@example.com",
            "password": "password123"
        }
    }
    status, data = req("POST", "/users/login", body=login_body)
    check("POST /users/login returns 200", status == 200, f"status={status}, data={data}")
    if data and "user" in data:
        login_token = data["user"].get("token")
        check("Login returns token", login_token is not None)
        if login_token:
            token = login_token
    else:
        check("Login returns user object", False, f"data={data}")

    if not token:
        print("\nNo token obtained, skipping authenticated tests.")
        print(f"\nResults: {passed} passed, {failed} failed")
        sys.exit(1 if failed else 0)

    # ---- 5. Get current user ----
    print("\n[5] GET /user (authenticated)")
    status, data = req("GET", "/user", token=token)
    check("GET /user returns 200", status == 200, f"status={status}")
    check("GET /user has user data", data and "user" in data, f"data={data}")

    # ---- 6. Update user ----
    print("\n[6] PUT /user (update bio)")
    update_body = {"user": {"bio": "Smoke test bio"}}
    status, data = req("PUT", "/user", body=update_body, token=token)
    check("PUT /user returns 200", status == 200, f"status={status}")
    if data and "user" in data:
        check("PUT /user updates bio", data["user"].get("bio") == "Smoke test bio", f"bio={data['user'].get('bio')}")

    # ---- 7. Get profile ----
    print("\n[7] GET /profiles/smokeuser")
    status, data = req("GET", "/profiles/smokeuser", token=token)
    check("GET /profiles/smokeuser returns 200", status == 200, f"status={status}")
    check("GET /profiles returns profile", data and "profile" in data, f"data={data}")

    # ---- 8. Create article ----
    print("\n[8] POST /articles")
    article_body = {
        "article": {
            "title": "Smoke Test Article",
            "description": "A test article",
            "body": "This is the body of the smoke test article.",
            "tagList": ["smoke", "test"]
        }
    }
    status, data = req("POST", "/articles", body=article_body, token=token)
    check("POST /articles returns 200", status == 200, f"status={status}, data={data}")
    slug = None
    if data and "article" in data:
        slug = data["article"].get("slug")
        check("Article has slug", slug is not None, f"slug={slug}")
        check("Article has title", data["article"].get("title") == "Smoke Test Article")
        check("Article has tagList", "tagList" in data["article"])

    # ---- 9. Get article by slug ----
    if slug:
        print(f"\n[9] GET /articles/{slug}")
        status, data = req("GET", f"/articles/{slug}")
        check(f"GET /articles/{slug} returns 200", status == 200, f"status={status}")
        check("Article data matches", data and "article" in data and data["article"].get("slug") == slug)

    # ---- 10. Favorite article ----
    if slug:
        print(f"\n[10] POST /articles/{slug}/favorite")
        status, data = req("POST", f"/articles/{slug}/favorite", token=token)
        check("Favorite returns 200", status == 200, f"status={status}")
        if data and "article" in data:
            check("Article is favorited", data["article"].get("favorited") == True)
            check("Favorites count > 0", data["article"].get("favoritesCount", 0) > 0)

    # ---- 11. Create comment ----
    if slug:
        print(f"\n[11] POST /articles/{slug}/comments")
        comment_body = {"comment": {"body": "Nice smoke test!"}}
        status, data = req("POST", f"/articles/{slug}/comments", body=comment_body, token=token)
        check("Post comment returns 200", status == 200, f"status={status}, data={data}")
        if data and "comment" in data:
            check("Comment has body", data["comment"].get("body") == "Nice smoke test!")

    # ---- 12. Get comments ----
    if slug:
        print(f"\n[12] GET /articles/{slug}/comments")
        status, data = req("GET", f"/articles/{slug}/comments", token=token)
        check("Get comments returns 200", status == 200, f"status={status}")
        check("Comments array present", data and "comments" in data, f"data={data}")

    # ---- 13. Get articles with tag filter ----
    print("\n[13] GET /articles?tag=smoke")
    status, data = req("GET", "/articles?tag=smoke")
    check("GET /articles?tag=smoke returns 200", status == 200, f"status={status}")

    # ---- 14. Get articles with author filter ----
    print("\n[14] GET /articles?author=smokeuser")
    status, data = req("GET", "/articles?author=smokeuser")
    check("GET /articles?author=smokeuser returns 200", status == 200, f"status={status}")

    # ---- 15. Get feed (authenticated) ----
    print("\n[15] GET /articles/feed")
    status, data = req("GET", "/articles/feed", token=token)
    check("GET /articles/feed returns 200", status == 200, f"status={status}")

    # ---- 16. Unfavorite ----
    if slug:
        print(f"\n[16] DELETE /articles/{slug}/favorite")
        status, data = req("DELETE", f"/articles/{slug}/favorite", token=token)
        check("Unfavorite returns 200", status == 200, f"status={status}")

    # ---- 17. Register second user and test follow ----
    print("\n[17] Register second user and test follow")
    user2_body = {
        "user": {
            "username": "smokeuser2",
            "email": "smokeuser2@example.com",
            "password": "password456"
        }
    }
    status, data = req("POST", "/users", body=user2_body)
    token2 = None
    if data and "user" in data:
        token2 = data["user"].get("token")

    if token2:
        status, data = req("POST", "/profiles/smokeuser/follow", token=token2)
        check("Follow user returns 200", status == 200, f"status={status}")
        if data and "profile" in data:
            check("Following is true", data["profile"].get("following") == True)

        status, data = req("DELETE", "/profiles/smokeuser/follow", token=token2)
        check("Unfollow user returns 200", status == 200, f"status={status}")
        if data and "profile" in data:
            check("Following is false after unfollow", data["profile"].get("following") == False)

    # ---- 18. Update article ----
    if slug:
        print(f"\n[18] PUT /articles/{slug}")
        update_article_body = {"article": {"title": "Updated Smoke Test"}}
        status, data = req("PUT", f"/articles/{slug}", body=update_article_body, token=token)
        check("PUT article returns 200", status == 200, f"status={status}")

    # ---- 19. Delete article ----
    if slug:
        print(f"\n[19] DELETE /articles/{slug}")
        status, data = req("DELETE", f"/articles/{slug}", token=token)
        check("DELETE article returns 204", status == 204, f"status={status}")

    # ---- 20. Verify article deleted ----
    if slug:
        print(f"\n[20] Verify article deleted GET /articles/{slug}")
        status, data = req("GET", f"/articles/{slug}")
        check("Deleted article returns 404", status == 404, f"status={status}")

    # ---- Summary ----
    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {passed+failed} tests")
    print(f"{'='*50}")
    sys.exit(1 if failed else 0)

if __name__ == "__main__":
    main()
