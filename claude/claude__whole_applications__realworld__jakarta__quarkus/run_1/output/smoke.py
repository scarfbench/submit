#!/usr/bin/env python3
"""Smoke tests for the RealWorld API migrated to Quarkus."""

import json
import sys
import urllib.request
import urllib.error
import time

BASE_URL = "http://localhost:8080/api"
PASSED = 0
FAILED = 0

def request(method, path, data=None, headers=None):
    """Make an HTTP request and return (status_code, response_body_dict)."""
    url = BASE_URL + path
    if headers is None:
        headers = {}
    headers["Content-Type"] = "application/json"

    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)

    try:
        with urllib.request.urlopen(req) as resp:
            resp_body = resp.read().decode("utf-8")
            return resp.status, json.loads(resp_body) if resp_body else {}
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8")
        try:
            return e.code, json.loads(resp_body) if resp_body else {}
        except json.JSONDecodeError:
            return e.code, {"raw": resp_body}
    except Exception as e:
        return 0, {"error": str(e)}

def check(test_name, condition):
    global PASSED, FAILED
    if condition:
        PASSED += 1
        print(f"  PASS: {test_name}")
    else:
        FAILED += 1
        print(f"  FAIL: {test_name}")

def test_tags_endpoint():
    print("\n--- Test: GET /tags ---")
    status, body = request("GET", "/tags")
    check("Status is 200", status == 200)
    check("Response has 'tags' key", "tags" in body)
    check("Tags is a list", isinstance(body.get("tags"), list))

def test_articles_endpoint():
    print("\n--- Test: GET /articles ---")
    status, body = request("GET", "/articles")
    check("Status is 200", status == 200)
    check("Response has 'articles' key", "articles" in body)
    check("Response has 'articlesCount' key", "articlesCount" in body)
    check("Articles is a list", isinstance(body.get("articles"), list))
    check("articlesCount is a number", isinstance(body.get("articlesCount"), (int, float)))

def test_register_user():
    print("\n--- Test: POST /users (Register) ---")
    data = {"user": {"username": "smokeuser", "email": "smoke@test.com", "password": "password123"}}
    status, body = request("POST", "/users", data)
    check("Status is 201", status == 201)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        user = body["user"]
        check("User has email", "email" in user)
        check("User has username", "username" in user)
        check("User has token", "token" in user)
        check("Email matches", user.get("email") == "smoke@test.com")
        check("Username matches", user.get("username") == "smokeuser")
        return user.get("token")
    return None

def test_login_user():
    print("\n--- Test: POST /users/login (Login) ---")
    data = {"user": {"email": "smoke@test.com", "password": "password123"}}
    status, body = request("POST", "/users/login", data)
    check("Status is 200", status == 200)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        user = body["user"]
        check("User has token", "token" in user)
        check("Email matches", user.get("email") == "smoke@test.com")
        return user.get("token")
    return None

def test_get_current_user(token):
    print("\n--- Test: GET /user (Current User) ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("GET", "/user", headers=headers)
    check("Status is 200", status == 200)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        check("User has email", "email" in body["user"])
        check("User has username", "username" in body["user"])

def test_update_user(token):
    print("\n--- Test: PUT /user (Update User) ---")
    headers = {"Authorization": f"Token {token}"}
    data = {"user": {"bio": "my-new-bio"}}
    status, body = request("PUT", "/user", data, headers)
    check("Status is 200", status == 200)
    check("Response has 'user' key", "user" in body)
    if "user" in body:
        check("Bio updated", body["user"].get("bio") == "my-new-bio")

def test_create_article(token):
    print("\n--- Test: POST /articles (Create Article) ---")
    headers = {"Authorization": f"Token {token}"}
    data = {"article": {
        "title": "How to train your dragon",
        "description": "Ever wonder how?",
        "body": "Very carefully.",
        "tagList": ["dragons", "training"]
    }}
    status, body = request("POST", "/articles", data, headers)
    check("Status is 201", status == 201)
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        article = body["article"]
        check("Article has slug", "slug" in article)
        check("Title matches", article.get("title") == "How to train your dragon")
        check("Article has tagList", "tagList" in article)
        check("Article has author", "author" in article)
        check("Article has createdAt", "createdAt" in article)
        return article.get("slug")
    return None

def test_get_article_by_slug(slug):
    print("\n--- Test: GET /articles/{slug} ---")
    status, body = request("GET", f"/articles/{slug}")
    check("Status is 200", status == 200)
    check("Response has 'article' key", "article" in body)
    if "article" in body:
        check("Slug matches", body["article"].get("slug") == slug)

def test_get_articles_by_author():
    print("\n--- Test: GET /articles?author=smokeuser ---")
    status, body = request("GET", "/articles?author=smokeuser")
    check("Status is 200", status == 200)
    check("articlesCount is 1", body.get("articlesCount") == 1)

def test_get_articles_by_tag():
    print("\n--- Test: GET /articles?tag=dragons ---")
    status, body = request("GET", "/articles?tag=dragons")
    check("Status is 200", status == 200)
    check("articlesCount is 1", body.get("articlesCount") == 1)

def test_update_article(token, slug):
    print("\n--- Test: PUT /articles/{slug} (Update) ---")
    headers = {"Authorization": f"Token {token}"}
    data = {"article": {"body": "With two hands"}}
    status, body = request("PUT", f"/articles/{slug}", data, headers)
    check("Status is 200", status == 200)
    if "article" in body:
        check("Body updated", body["article"].get("body") == "With two hands")

def test_favorite_article(token, slug):
    print("\n--- Test: POST /articles/{slug}/favorite ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("POST", f"/articles/{slug}/favorite", headers=headers)
    check("Status is 200", status == 200)
    if "article" in body:
        check("Favorited is true", body["article"].get("favorited") == True)
        check("favoritesCount is 1", body["article"].get("favoritesCount") == 1)

def test_unfavorite_article(token, slug):
    print("\n--- Test: DELETE /articles/{slug}/favorite ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("DELETE", f"/articles/{slug}/favorite", headers=headers)
    check("Status is 200", status == 200)
    if "article" in body:
        check("Favorited is false", body["article"].get("favorited") == False)
        check("favoritesCount is 0", body["article"].get("favoritesCount") == 0)

def test_create_comment(token, slug):
    print("\n--- Test: POST /articles/{slug}/comments ---")
    headers = {"Authorization": f"Token {token}"}
    data = {"comment": {"body": "Thank you so much!"}}
    status, body = request("POST", f"/articles/{slug}/comments", data, headers)
    check("Status is 200", status == 200)
    check("Response has 'comment' key", "comment" in body)
    if "comment" in body:
        check("Comment has id", "id" in body["comment"])
        check("Comment body matches", body["comment"].get("body") == "Thank you so much!")
        return body["comment"].get("id")
    return None

def test_get_comments(slug):
    print("\n--- Test: GET /articles/{slug}/comments ---")
    status, body = request("GET", f"/articles/{slug}/comments")
    check("Status is 200", status == 200)
    check("Response has 'comments' key", "comments" in body)
    if "comments" in body:
        check("Has at least 1 comment", len(body["comments"]) >= 1)

def test_delete_comment(token, slug, comment_id):
    print("\n--- Test: DELETE /articles/{slug}/comments/{id} ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("DELETE", f"/articles/{slug}/comments/{comment_id}", headers=headers)
    check("Status is 200", status == 200)

def test_register_celeb():
    print("\n--- Test: Register Celeb User ---")
    data = {"user": {"username": "celeb_smokeuser", "email": "celeb_smoke@test.com", "password": "password123"}}
    status, body = request("POST", "/users", data)
    check("Status is 201", status == 201)

def test_get_profile(token):
    print("\n--- Test: GET /profiles/celeb_smokeuser ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("GET", "/profiles/celeb_smokeuser", headers=headers)
    check("Status is 200", status == 200)
    check("Response has 'profile' key", "profile" in body)
    if "profile" in body:
        check("Username matches", body["profile"].get("username") == "celeb_smokeuser")
        check("Following is false", body["profile"].get("following") == False)

def test_follow_profile(token):
    print("\n--- Test: POST /profiles/celeb_smokeuser/follow ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("POST", "/profiles/celeb_smokeuser/follow", headers=headers)
    check("Status is 200", status == 200)
    if "profile" in body:
        check("Following is true", body["profile"].get("following") == True)

def test_unfollow_profile(token):
    print("\n--- Test: DELETE /profiles/celeb_smokeuser/follow ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("DELETE", "/profiles/celeb_smokeuser/follow", headers=headers)
    check("Status is 200", status == 200)
    if "profile" in body:
        check("Following is false", body["profile"].get("following") == False)

def test_get_tags_after_article():
    print("\n--- Test: GET /tags (after article creation) ---")
    status, body = request("GET", "/tags")
    check("Status is 200", status == 200)
    if "tags" in body:
        tags = body["tags"]
        check("Has dragons tag", "dragons" in tags)
        check("Has training tag", "training" in tags)

def test_delete_article(token, slug):
    print("\n--- Test: DELETE /articles/{slug} ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("DELETE", f"/articles/{slug}", headers=headers)
    check("Status is 200", status == 200)

def test_feed(token):
    print("\n--- Test: GET /articles/feed ---")
    headers = {"Authorization": f"Token {token}"}
    status, body = request("GET", "/articles/feed", headers=headers)
    check("Status is 200", status == 200)
    check("Response has 'articles' key", "articles" in body)
    check("Response has 'articlesCount' key", "articlesCount" in body)

def main():
    print("=" * 60)
    print("RealWorld API Smoke Tests")
    print("=" * 60)

    # Wait for app to be ready
    print("\nWaiting for application to be ready...")
    for i in range(30):
        try:
            status, _ = request("GET", "/tags")
            if status == 200:
                print("Application is ready!")
                break
        except:
            pass
        time.sleep(2)
    else:
        print("Application did not start in time!")
        sys.exit(1)

    # Run tests
    test_tags_endpoint()
    test_articles_endpoint()

    token = test_register_user()
    if not token:
        print("FATAL: Could not register user, aborting remaining tests")
        sys.exit(1)

    token = test_login_user()
    if not token:
        print("FATAL: Could not login, aborting remaining tests")
        sys.exit(1)

    test_get_current_user(token)
    test_update_user(token)
    test_feed(token)

    slug = test_create_article(token)
    if slug:
        test_get_article_by_slug(slug)
        test_get_articles_by_author()
        test_get_articles_by_tag()
        test_update_article(token, slug)
        test_favorite_article(token, slug)
        test_unfavorite_article(token, slug)

        comment_id = test_create_comment(token, slug)
        test_get_comments(slug)
        if comment_id:
            test_delete_comment(token, slug, comment_id)

        test_register_celeb()
        test_get_profile(token)
        test_follow_profile(token)
        test_unfollow_profile(token)

        test_get_tags_after_article()
        test_delete_article(token, slug)

    # Summary
    print("\n" + "=" * 60)
    print(f"RESULTS: {PASSED} passed, {FAILED} failed, {PASSED + FAILED} total")
    print("=" * 60)

    if FAILED > 0:
        sys.exit(1)
    else:
        print("All smoke tests passed!")
        sys.exit(0)

if __name__ == "__main__":
    main()
