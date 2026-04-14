"""
Smoke tests for the RealWorld Conduit API.
Tests user registration, login, profile, articles, comments, tags, and favorites.
"""
import json
import os
import subprocess
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

def req(method, path, data=None, token=None):
    """Make an HTTP request and return (status_code, parsed_json_or_None)."""
    url = BASE_URL + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Token {token}"
    body = json.dumps(data).encode() if data else None
    r = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(r)
        status = resp.status
        raw = resp.read().decode()
        return status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        raw = e.read().decode() if e.fp else ""
        try:
            return e.code, json.loads(raw) if raw else None
        except Exception:
            return e.code, raw

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

def wait_for_server(timeout=120):
    """Wait for the server to become available."""
    print(f"Waiting for server at {BASE_URL} ...")
    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            urllib.request.urlopen(BASE_URL + "/tags")
            print("Server is up!")
            return True
        except Exception:
            time.sleep(2)
    print("Server did not start in time!")
    return False

def main():
    global passed, failed

    if not wait_for_server():
        print("FATAL: server not reachable")
        sys.exit(1)

    # ---- Tags (public) ----
    print("\n== Tags ==")
    s, body = req("GET", "/tags")
    check("GET /tags returns 200", s == 200, f"status={s}")
    check("GET /tags has 'tags' key", body is not None and "tags" in body, f"body={body}")

    # ---- Register User ----
    print("\n== User Registration ==")
    user_data = {
        "user": {
            "email": "smoke@test.com",
            "username": "smokeuser",
            "password": "password123"
        }
    }
    s, body = req("POST", "/users", user_data)
    check("POST /users returns 200", s == 200, f"status={s} body={body}")
    token = None
    if body and "user" in body:
        token = body["user"].get("token")
        check("Registration returns token", token is not None, f"body={body}")
        check("Registration returns correct email", body["user"].get("email") == "smoke@test.com")
        check("Registration returns correct username", body["user"].get("username") == "smokeuser")
    else:
        check("Registration response has user key", False, f"body={body}")

    # ---- Login ----
    print("\n== User Login ==")
    login_data = {
        "user": {
            "email": "smoke@test.com",
            "password": "password123"
        }
    }
    s, body = req("POST", "/users/login", login_data)
    check("POST /users/login returns 200", s == 200, f"status={s} body={body}")
    if body and "user" in body:
        token = body["user"].get("token")
        check("Login returns token", token is not None)
    else:
        check("Login response has user key", False, f"body={body}")

    # ---- Login wrong password ----
    print("\n== Login Wrong Password ==")
    bad_login = {
        "user": {
            "email": "smoke@test.com",
            "password": "wrongpassword"
        }
    }
    s, _ = req("POST", "/users/login", bad_login)
    check("POST /users/login wrong password returns 401", s == 401, f"status={s}")

    # ---- Get Current User ----
    print("\n== Get Current User ==")
    s, body = req("GET", "/user", token=token)
    check("GET /user returns 200", s == 200, f"status={s} body={body}")
    if body and "user" in body:
        check("GET /user returns correct email", body["user"].get("email") == "smoke@test.com")
    else:
        check("GET /user has user key", False, f"body={body}")

    # ---- Update User ----
    print("\n== Update User ==")
    update_data = {"user": {"bio": "I am a smoke test user"}}
    s, body = req("PUT", "/user", update_data, token=token)
    check("PUT /user returns 200", s == 200, f"status={s} body={body}")
    if body and "user" in body:
        check("PUT /user updated bio", body["user"].get("bio") == "I am a smoke test user")

    # ---- Get Profile ----
    print("\n== Get Profile ==")
    s, body = req("GET", "/profiles/smokeuser", token=token)
    check("GET /profiles/smokeuser returns 200", s == 200, f"status={s} body={body}")
    if body and "profile" in body:
        check("Profile has correct username", body["profile"].get("username") == "smokeuser")
        check("Profile has following field", "following" in body["profile"])
    else:
        check("Profile response has profile key", False, f"body={body}")

    # ---- Create Article ----
    print("\n== Create Article ==")
    article_data = {
        "article": {
            "title": "Smoke Test Article",
            "description": "A test article",
            "body": "This is the body of the test article",
            "tagList": ["smoke", "test"]
        }
    }
    s, body = req("POST", "/articles", article_data, token=token)
    check("POST /articles returns 200", s == 200, f"status={s} body={body}")
    slug = None
    if body and "article" in body:
        slug = body["article"].get("slug")
        check("Article has slug", slug is not None, f"body={body}")
        check("Article has correct title", body["article"].get("title") == "Smoke Test Article")
        check("Article has correct description", body["article"].get("description") == "A test article")
        check("Article has author", body["article"].get("author") is not None)
    else:
        check("Create article has article key", False, f"body={body}")

    # ---- Get Article ----
    if slug:
        print("\n== Get Article ==")
        s, body = req("GET", f"/articles/{slug}")
        check(f"GET /articles/{slug} returns 200", s == 200, f"status={s}")
        if body and "article" in body:
            check("Article body correct", body["article"].get("body") == "This is the body of the test article")

    # ---- List Articles ----
    print("\n== List Articles ==")
    s, body = req("GET", "/articles")
    check("GET /articles returns 200", s == 200, f"status={s}")
    if body:
        check("Articles response has articles key", "articles" in body, f"body={body}")
        check("Articles response has articlesCount key", "articlesCount" in body, f"body={body}")

    # ---- Favorite Article ----
    if slug:
        print("\n== Favorite Article ==")
        s, body = req("POST", f"/articles/{slug}/favorite", token=token)
        check(f"POST /articles/{slug}/favorite returns 200", s == 200, f"status={s} body={body}")
        if body and "article" in body:
            check("Favorited article is favorited", body["article"].get("favorited") == True)
            check("Favorites count is 1", body["article"].get("favoritesCount") == 1)

        # Unfavorite
        s, body = req("DELETE", f"/articles/{slug}/favorite", token=token)
        check(f"DELETE /articles/{slug}/favorite returns 200", s == 200, f"status={s}")

    # ---- Add Comment ----
    if slug:
        print("\n== Comments ==")
        comment_data = {"comment": {"body": "This is a test comment"}}
        s, body = req("POST", f"/articles/{slug}/comments", comment_data, token=token)
        check(f"POST /articles/{slug}/comments returns 200", s == 200, f"status={s} body={body}")
        comment_id = None
        if body and "comment" in body:
            comment_id = body["comment"].get("id")
            check("Comment has id", comment_id is not None)
            check("Comment body correct", body["comment"].get("body") == "This is a test comment")

        # Get Comments
        s, body = req("GET", f"/articles/{slug}/comments", token=token)
        check(f"GET /articles/{slug}/comments returns 200", s == 200, f"status={s}")
        if body:
            check("Comments response has comments key", "comments" in body, f"body={body}")

    # ---- Follow User ----
    print("\n== Follow/Unfollow ==")
    # Register second user
    user2_data = {
        "user": {
            "email": "smoke2@test.com",
            "username": "smokeuser2",
            "password": "password123"
        }
    }
    s, body = req("POST", "/users", user2_data)
    token2 = body["user"]["token"] if body and "user" in body else None
    if token2:
        s, body = req("POST", "/profiles/smokeuser/follow", token=token2)
        check("POST /profiles/smokeuser/follow returns 200", s == 200, f"status={s} body={body}")
        if body and "profile" in body:
            check("Following is true", body["profile"].get("following") == True)

        s, body = req("DELETE", "/profiles/smokeuser/follow", token=token2)
        check("DELETE /profiles/smokeuser/follow returns 200", s == 200, f"status={s}")
        if body and "profile" in body:
            check("Following is false", body["profile"].get("following") == False)

    # ---- Tags after article creation ----
    print("\n== Tags after article ==")
    s, body = req("GET", "/tags")
    check("GET /tags returns 200", s == 200, f"status={s}")
    if body and "tags" in body:
        check("Tags include 'smoke'", "smoke" in body["tags"], f"tags={body['tags']}")
        check("Tags include 'test'", "test" in body["tags"], f"tags={body['tags']}")

    # ---- Update Article ----
    if slug:
        print("\n== Update Article ==")
        update_article = {"article": {"title": "Updated Smoke Title"}}
        s, body = req("PUT", f"/articles/{slug}", update_article, token=token)
        check(f"PUT /articles/{slug} returns 200", s == 200, f"status={s} body={body}")

    # ---- Delete Article ----
    if slug:
        print("\n== Delete Article ==")
        s, body = req("DELETE", f"/articles/{slug}", token=token)
        check(f"DELETE /articles/{slug} returns 204 or 200", s in (200, 204), f"status={s}")

    # ---- Unauthenticated access ----
    print("\n== Unauthenticated Access ==")
    s, _ = req("GET", "/user")
    check("GET /user without token returns 401", s == 401, f"status={s}")

    # ---- Summary ----
    total = passed + failed
    print(f"\n{'='*50}")
    print(f"Results: {passed}/{total} passed, {failed}/{total} failed")
    print(f"{'='*50}")
    sys.exit(0 if failed == 0 else 1)

if __name__ == "__main__":
    main()
