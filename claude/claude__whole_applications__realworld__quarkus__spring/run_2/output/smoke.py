#!/usr/bin/env python3
"""
RealWorld API Smoke Test Script

Tests the key endpoints of the RealWorld Conduit API.
Accepts BASE_URL from environment variable or defaults to http://localhost:8080.
"""

import os
import sys
import time
import random
import string
import requests
from typing import Optional, Dict, Any


def generate_random_string(length: int = 8) -> str:
    """Generate a random string for unique usernames/emails."""
    return ''.join(random.choices(string.ascii_lowercase + string.digits, k=length))


def wait_for_server(base_url: str, timeout: int = 60) -> bool:
    """Wait for the server to be ready."""
    print(f"Waiting for server at {base_url} to be ready...")
    start_time = time.time()

    while time.time() - start_time < timeout:
        try:
            # Try to hit the tags endpoint as a health check
            response = requests.get(f"{base_url}/api/tags", timeout=5)
            if response.status_code in [200, 404]:  # 404 is OK, means server is up
                print(f"Server is ready!")
                return True
        except requests.exceptions.RequestException:
            pass

        time.sleep(2)

    print(f"Server did not become ready within {timeout} seconds")
    return False


class SmokeTestRunner:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip('/')
        self.api_url = f"{self.base_url}/api"
        self.test_results = []
        self.auth_token: Optional[str] = None
        self.test_user = {
            "username": f"testuser_{generate_random_string()}",
            "email": f"test_{generate_random_string()}@example.com",
            "password": "Test123456!"
        }
        self.article_slug: Optional[str] = None
        self.comment_id: Optional[int] = None

    def log_test(self, test_name: str, passed: bool, message: str = ""):
        """Log test result."""
        status = "PASS" if passed else "FAIL"
        result = f"[{status}] {test_name}"
        if message:
            result += f": {message}"
        print(result)
        self.test_results.append(passed)

    def make_request(self, method: str, endpoint: str, data: Optional[Dict[Any, Any]] = None,
                     requires_auth: bool = False) -> Optional[requests.Response]:
        """Make an HTTP request to the API."""
        url = f"{self.api_url}{endpoint}"
        headers = {"Content-Type": "application/json"}

        if requires_auth and self.auth_token:
            headers["Authorization"] = f"Token {self.auth_token}"

        try:
            if method == "GET":
                response = requests.get(url, headers=headers, timeout=10)
            elif method == "POST":
                response = requests.post(url, json=data, headers=headers, timeout=10)
            elif method == "PUT":
                response = requests.put(url, json=data, headers=headers, timeout=10)
            elif method == "DELETE":
                response = requests.delete(url, headers=headers, timeout=10)
            else:
                return None

            return response
        except requests.exceptions.RequestException as e:
            print(f"Request error: {e}")
            return None

    def test_register_user(self):
        """Test user registration."""
        test_name = "Register User"

        data = {
            "user": {
                "username": self.test_user["username"],
                "email": self.test_user["email"],
                "password": self.test_user["password"]
            }
        }

        response = self.make_request("POST", "/users", data)

        if response and response.status_code in [200, 201]:
            try:
                json_data = response.json()
                if "user" in json_data and "token" in json_data["user"]:
                    self.auth_token = json_data["user"]["token"]
                    self.log_test(test_name, True, f"User '{self.test_user['username']}' registered")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing user/token")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_login_user(self):
        """Test user login."""
        test_name = "Login User"

        data = {
            "user": {
                "email": self.test_user["email"],
                "password": self.test_user["password"]
            }
        }

        response = self.make_request("POST", "/users/login", data)

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "user" in json_data and "token" in json_data["user"]:
                    self.auth_token = json_data["user"]["token"]
                    self.log_test(test_name, True, "Login successful")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing user/token")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_get_current_user(self):
        """Test getting current user."""
        test_name = "Get Current User"

        response = self.make_request("GET", "/user", requires_auth=True)

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "user" in json_data and json_data["user"]["email"] == self.test_user["email"]:
                    self.log_test(test_name, True, "Retrieved current user")
                    return True
                else:
                    self.log_test(test_name, False, "User data mismatch")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_create_article(self):
        """Test creating an article."""
        test_name = "Create Article"

        data = {
            "article": {
                "title": f"Test Article {generate_random_string()}",
                "description": "This is a test article description",
                "body": "This is the body of the test article with some content.",
                "tagList": ["test", "smoke"]
            }
        }

        response = self.make_request("POST", "/articles", data, requires_auth=True)

        if response and response.status_code in [200, 201]:
            try:
                json_data = response.json()
                if "article" in json_data and "slug" in json_data["article"]:
                    self.article_slug = json_data["article"]["slug"]
                    self.log_test(test_name, True, f"Article created with slug: {self.article_slug}")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing article/slug")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_get_articles(self):
        """Test getting list of articles."""
        test_name = "Get Articles List"

        response = self.make_request("GET", "/articles")

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "articles" in json_data and isinstance(json_data["articles"], list):
                    count = len(json_data["articles"])
                    self.log_test(test_name, True, f"Retrieved {count} articles")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing articles array")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_get_article_by_slug(self):
        """Test getting a specific article by slug."""
        test_name = "Get Article by Slug"

        if not self.article_slug:
            self.log_test(test_name, False, "No article slug available")
            return False

        response = self.make_request("GET", f"/articles/{self.article_slug}")

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "article" in json_data and json_data["article"]["slug"] == self.article_slug:
                    self.log_test(test_name, True, f"Retrieved article: {self.article_slug}")
                    return True
                else:
                    self.log_test(test_name, False, "Article data mismatch")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_create_comment(self):
        """Test creating a comment on an article."""
        test_name = "Create Comment"

        if not self.article_slug:
            self.log_test(test_name, False, "No article slug available")
            return False

        data = {
            "comment": {
                "body": "This is a test comment on the article."
            }
        }

        response = self.make_request("POST", f"/articles/{self.article_slug}/comments", data, requires_auth=True)

        if response and response.status_code in [200, 201]:
            try:
                json_data = response.json()
                if "comment" in json_data and "id" in json_data["comment"]:
                    self.comment_id = json_data["comment"]["id"]
                    self.log_test(test_name, True, f"Comment created with ID: {self.comment_id}")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing comment/id")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_get_comments(self):
        """Test getting comments for an article."""
        test_name = "Get Comments"

        if not self.article_slug:
            self.log_test(test_name, False, "No article slug available")
            return False

        response = self.make_request("GET", f"/articles/{self.article_slug}/comments")

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "comments" in json_data and isinstance(json_data["comments"], list):
                    count = len(json_data["comments"])
                    self.log_test(test_name, True, f"Retrieved {count} comments")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing comments array")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_get_tags(self):
        """Test getting tags."""
        test_name = "Get Tags"

        response = self.make_request("GET", "/tags")

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "tags" in json_data and isinstance(json_data["tags"], list):
                    count = len(json_data["tags"])
                    self.log_test(test_name, True, f"Retrieved {count} tags")
                    return True
                else:
                    self.log_test(test_name, False, "Response missing tags array")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_get_profile(self):
        """Test getting a user profile."""
        test_name = "Get Profile"

        response = self.make_request("GET", f"/profiles/{self.test_user['username']}")

        if response and response.status_code == 200:
            try:
                json_data = response.json()
                if "profile" in json_data and json_data["profile"]["username"] == self.test_user["username"]:
                    self.log_test(test_name, True, f"Retrieved profile: {self.test_user['username']}")
                    return True
                else:
                    self.log_test(test_name, False, "Profile data mismatch")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def test_favorite_article(self):
        """Test favoriting an article."""
        test_name = "Favorite Article"

        if not self.article_slug:
            self.log_test(test_name, False, "No article slug available")
            return False

        response = self.make_request("POST", f"/articles/{self.article_slug}/favorite", requires_auth=True)

        if response and response.status_code in [200, 201]:
            try:
                json_data = response.json()
                if "article" in json_data and json_data["article"].get("favorited") == True:
                    self.log_test(test_name, True, "Article favorited")
                    return True
                else:
                    self.log_test(test_name, False, "Article not marked as favorited")
                    return False
            except Exception as e:
                self.log_test(test_name, False, f"JSON parse error: {e}")
                return False
        else:
            status = response.status_code if response else "No response"
            self.log_test(test_name, False, f"Status: {status}")
            return False

    def run_all_tests(self):
        """Run all smoke tests."""
        print("=" * 60)
        print("RealWorld API Smoke Tests")
        print("=" * 60)
        print(f"Base URL: {self.base_url}")
        print(f"API URL: {self.api_url}")
        print("=" * 60)
        print()

        # Run tests in order
        self.test_register_user()
        self.test_login_user()
        self.test_get_current_user()
        self.test_create_article()
        self.test_get_articles()
        self.test_get_article_by_slug()
        self.test_create_comment()
        self.test_get_comments()
        self.test_get_tags()
        self.test_get_profile()
        self.test_favorite_article()

        # Print summary
        print()
        print("=" * 60)
        total_tests = len(self.test_results)
        passed_tests = sum(self.test_results)
        failed_tests = total_tests - passed_tests

        print(f"Total Tests: {total_tests}")
        print(f"Passed: {passed_tests}")
        print(f"Failed: {failed_tests}")
        print("=" * 60)

        # Return exit code
        return 0 if all(self.test_results) else 1


def main():
    """Main entry point."""
    # Get BASE_URL from environment or command line argument
    base_url = os.environ.get('BASE_URL', 'http://localhost:8080')

    # Override with command line argument if provided
    if len(sys.argv) > 1:
        base_url = sys.argv[1]

    # Wait for server to be ready
    if not wait_for_server(base_url):
        print("FAIL: Server is not ready")
        sys.exit(1)

    # Run tests
    runner = SmokeTestRunner(base_url)
    exit_code = runner.run_all_tests()

    sys.exit(exit_code)


if __name__ == "__main__":
    main()
