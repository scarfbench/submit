#!/usr/bin/env python3
"""Smoke tests for the RealWorld Quarkus application."""

import json
import subprocess
import sys
import time

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

def curl(method, path, data=None, token=None, expected_status=None):
    """Make an HTTP request using curl and return (status_code, response_body)."""
    cmd = ["curl", "-s", "-w", "\n%{http_code}", "-X", method]
    cmd.extend(["-H", "Content-Type: application/json"])
    if token:
        cmd.extend(["-H", f"Authorization: Token {token}"])
    if data:
        cmd.extend(["-d", json.dumps(data)])
    cmd.append(f"{BASE_URL}{path}")

    result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
    lines = result.stdout.strip().rsplit("\n", 1)
    body = lines[0] if len(lines) > 1 else ""
    status = int(lines[-1]) if lines[-1].isdigit() else 0

    if expected_status and status != expected_status:
        print(f"  FAIL: Expected {expected_status}, got {status}")
        print(f"  Body: {body[:500]}")
        return status, body, False

    return status, body, True


def test_health():
    """Test that the app is running."""
    print("Test: Health check...")
    status, body, ok = curl("GET", "/tags")
    if status == 200:
        print("  PASS: Application is running")
        return True
    print(f"  FAIL: Got status {status}")
    return False


def test_register_user():
    """Test user registration."""
    print("Test: Register user...")
    data = {
        "user": {
            "email": "smoketest@example.com",
            "username": "smoketestuser",
            "password": "password123"
        }
    }
    status, body, ok = curl("POST", "/users", data=data, expected_status=200)
    if ok:
        resp = json.loads(body)
        token = resp.get("user", {}).get("token")
        if token:
            print("  PASS: User registered successfully")
            return token
    print("  FAIL: User registration failed")
    return None


def test_login(token):
    """Test user login."""
    print("Test: Login user...")
    data = {
        "user": {
            "email": "smoketest@example.com",
            "password": "password123"
        }
    }
    status, body, ok = curl("POST", "/users/login", data=data, expected_status=200)
    if ok:
        resp = json.loads(body)
        login_token = resp.get("user", {}).get("token")
        if login_token:
            print("  PASS: User logged in successfully")
            return login_token
    print("  FAIL: Login failed")
    return None


def test_get_current_user(token):
    """Test getting current user."""
    print("Test: Get current user...")
    status, body, ok = curl("GET", "/user", token=token, expected_status=200)
    if ok:
        resp = json.loads(body)
        if resp.get("user", {}).get("email") == "smoketest@example.com":
            print("  PASS: Got current user")
            return True
    print("  FAIL: Get current user failed")
    return False


def test_update_user(token):
    """Test updating user."""
    print("Test: Update user...")
    data = {
        "user": {
            "bio": "I am a smoke test user"
        }
    }
    status, body, ok = curl("PUT", "/user", data=data, token=token, expected_status=200)
    if ok:
        resp = json.loads(body)
        if resp.get("user", {}).get("bio") == "I am a smoke test user":
            print("  PASS: User updated successfully")
            return True
    print("  FAIL: User update failed")
    return False


def test_get_profile(token):
    """Test getting a profile."""
    print("Test: Get profile...")
    status, body, ok = curl("GET", "/profiles/smoketestuser", token=token, expected_status=200)
    if ok:
        resp = json.loads(body)
        if resp.get("profile", {}).get("username") == "smoketestuser":
            print("  PASS: Got profile")
            return True
    print("  FAIL: Get profile failed")
    return False


def test_create_article(token):
    """Test creating an article."""
    print("Test: Create article...")
    data = {
        "article": {
            "title": "Smoke Test Article",
            "description": "A test article",
            "body": "This is a test article body",
            "tagList": ["test", "smoke"]
        }
    }
    status, body, ok = curl("POST", "/articles", data=data, token=token, expected_status=200)
    if ok:
        resp = json.loads(body)
        slug = resp.get("article", {}).get("slug")
        if slug:
            print(f"  PASS: Article created with slug: {slug}")
            return slug
    print("  FAIL: Article creation failed")
    return None


def test_get_articles():
    """Test getting articles."""
    print("Test: Get articles...")
    status, body, ok = curl("GET", "/articles", expected_status=200)
    if ok:
        resp = json.loads(body)
        if "articles" in resp:
            print(f"  PASS: Got {resp.get('articlesCount', 0)} articles")
            return True
    print("  FAIL: Get articles failed")
    return False


def test_get_article_by_slug(slug):
    """Test getting article by slug."""
    print(f"Test: Get article by slug ({slug})...")
    status, body, ok = curl("GET", f"/articles/{slug}", expected_status=200)
    if ok:
        resp = json.loads(body)
        if resp.get("article", {}).get("slug") == slug:
            print("  PASS: Got article by slug")
            return True
    print("  FAIL: Get article by slug failed")
    return False


def test_get_tags():
    """Test getting tags."""
    print("Test: Get tags...")
    status, body, ok = curl("GET", "/tags", expected_status=200)
    if ok:
        resp = json.loads(body)
        if "tags" in resp:
            print(f"  PASS: Got tags: {resp['tags']}")
            return True
    print("  FAIL: Get tags failed")
    return False


def test_add_comment(token, slug):
    """Test adding a comment."""
    print("Test: Add comment...")
    data = {
        "comment": {
            "body": "This is a smoke test comment"
        }
    }
    status, body, ok = curl("POST", f"/articles/{slug}/comments", data=data, token=token, expected_status=200)
    if ok:
        resp = json.loads(body)
        comment_id = resp.get("comment", {}).get("id")
        if comment_id:
            print(f"  PASS: Comment added with id: {comment_id}")
            return comment_id
    print("  FAIL: Add comment failed")
    return None


def test_get_comments(slug):
    """Test getting comments."""
    print("Test: Get comments...")
    status, body, ok = curl("GET", f"/articles/{slug}/comments", expected_status=200)
    if ok:
        resp = json.loads(body)
        if "comments" in resp:
            print(f"  PASS: Got {len(resp['comments'])} comments")
            return True
    print("  FAIL: Get comments failed")
    return False


def main():
    print(f"\n=== Smoke Tests for RealWorld Quarkus App ===")
    print(f"Base URL: {BASE_URL}\n")

    passed = 0
    failed = 0
    total = 0

    # Wait for app to be ready
    print("Waiting for application to start...")
    for i in range(30):
        try:
            status, _, _ = curl("GET", "/tags")
            if status == 200:
                print("Application is ready!\n")
                break
        except:
            pass
        time.sleep(2)
    else:
        print("FATAL: Application did not start in time")
        sys.exit(1)

    # Run tests
    tests_results = []

    # Health
    total += 1
    if test_health():
        passed += 1
    else:
        failed += 1

    # Tags
    total += 1
    if test_get_tags():
        passed += 1
    else:
        failed += 1

    # Register
    total += 1
    token = test_register_user()
    if token:
        passed += 1
    else:
        failed += 1

    if token:
        # Login
        total += 1
        login_token = test_login(token)
        if login_token:
            passed += 1
            token = login_token  # Use fresh token
        else:
            failed += 1

        # Get current user
        total += 1
        if test_get_current_user(token):
            passed += 1
        else:
            failed += 1

        # Update user
        total += 1
        if test_update_user(token):
            passed += 1
        else:
            failed += 1

        # Get profile
        total += 1
        if test_get_profile(token):
            passed += 1
        else:
            failed += 1

        # Create article
        total += 1
        slug = test_create_article(token)
        if slug:
            passed += 1
        else:
            failed += 1

        # Get articles
        total += 1
        if test_get_articles():
            passed += 1
        else:
            failed += 1

        if slug:
            # Get article by slug
            total += 1
            if test_get_article_by_slug(slug):
                passed += 1
            else:
                failed += 1

            # Add comment
            total += 1
            comment_id = test_add_comment(token, slug)
            if comment_id:
                passed += 1
            else:
                failed += 1

            # Get comments
            total += 1
            if test_get_comments(slug):
                passed += 1
            else:
                failed += 1

    print(f"\n=== Results ===")
    print(f"Total: {total}, Passed: {passed}, Failed: {failed}")

    if failed > 0:
        sys.exit(1)
    else:
        print("All smoke tests passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
