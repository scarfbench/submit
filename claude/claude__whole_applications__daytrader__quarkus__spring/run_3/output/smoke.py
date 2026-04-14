#!/usr/bin/env python3
"""
Smoke tests for DayTrader Spring Boot application.
Tests core REST API endpoints to verify the migration works correctly.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error
import urllib.parse

BASE_URL = os.environ.get("APP_URL", "http://localhost:8080")
REST_BASE = f"{BASE_URL}/rest"

passed = 0
failed = 0
errors = []


def test(name, fn):
    global passed, failed, errors
    try:
        fn()
        passed += 1
        print(f"  PASS: {name}")
    except Exception as e:
        failed += 1
        errors.append((name, str(e)))
        print(f"  FAIL: {name} - {e}")


def get_json(url):
    """GET request expecting JSON response."""
    req = urllib.request.Request(url)
    req.add_header("Accept", "application/json")
    with urllib.request.urlopen(req, timeout=15) as resp:
        body = resp.read().decode("utf-8")
        return json.loads(body), resp.status


def post_form(url, data):
    """POST form-encoded data expecting JSON response."""
    encoded = urllib.parse.urlencode(data).encode("utf-8")
    req = urllib.request.Request(url, data=encoded, method="POST")
    req.add_header("Content-Type", "application/x-www-form-urlencoded")
    req.add_header("Accept", "application/json")
    with urllib.request.urlopen(req, timeout=15) as resp:
        body = resp.read().decode("utf-8")
        try:
            return json.loads(body), resp.status
        except json.JSONDecodeError:
            return body, resp.status


def get_text(url):
    """GET request expecting text/html response."""
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req, timeout=15) as resp:
        return resp.read().decode("utf-8"), resp.status


def wait_for_ready(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} to be ready...")
    # Actuator endpoints go through DispatcherServlet at /rest path
    health_url = f"{REST_BASE}/actuator/health"
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(health_url)
            with urllib.request.urlopen(req, timeout=5) as resp:
                body = json.loads(resp.read().decode("utf-8"))
                if body.get("status") == "UP":
                    print(f"Application is ready! (took {time.time() - start:.1f}s)")
                    return True
        except Exception:
            pass
        time.sleep(2)
    print(f"Application did not become ready within {max_wait}s")
    return False


# ============================================================
# Test functions
# ============================================================

def test_health_endpoint():
    """Test the actuator health endpoint."""
    body, status = get_json(f"{REST_BASE}/actuator/health")
    assert status == 200, f"Expected 200, got {status}"
    assert body.get("status") == "UP", f"Expected UP, got {body.get('status')}"


def test_market_summary():
    """Test the market summary endpoint."""
    body, status = get_json(f"{REST_BASE}/trade/market")
    assert status == 200, f"Expected 200, got {status}"
    assert body is not None, "Market summary should not be null"


def test_get_quote():
    """Test getting a stock quote."""
    body, status = get_json(f"{REST_BASE}/quotes/s:0")
    assert status == 200, f"Expected 200, got {status}"
    # Response is a list of quotes
    assert isinstance(body, list), f"Expected list, got {type(body)}"
    assert len(body) > 0, "Expected at least one quote"
    quote = body[0]
    assert quote.get("symbol") == "s:0", f"Expected symbol s:0, got {quote.get('symbol')}"


def test_get_multiple_quotes():
    """Test getting multiple stock quotes."""
    body, status = get_json(f"{REST_BASE}/quotes/s:0,s:1,s:2")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, list), f"Expected list, got {type(body)}"
    assert len(body) >= 3, f"Expected at least 3 quotes, got {len(body)}"


def test_login():
    """Test user login."""
    body, status = post_form(f"{REST_BASE}/trade/login", {
        "userID": "uid:0",
        "password": "xxx"
    })
    assert status == 200, f"Expected 200, got {status}"
    assert body is not None, "Login response should not be null"


def test_get_account():
    """Test getting account data."""
    body, status = get_json(f"{REST_BASE}/trade/account/uid:0")
    assert status == 200, f"Expected 200, got {status}"
    assert body is not None, "Account data should not be null"


def test_get_holdings():
    """Test getting user holdings."""
    body, status = get_json(f"{REST_BASE}/trade/account/uid:0/holdings")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, list), f"Expected list, got {type(body)}"


def test_get_orders():
    """Test getting user orders."""
    body, status = get_json(f"{REST_BASE}/trade/account/uid:0/orders")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, list), f"Expected list, got {type(body)}"


def test_buy_stock():
    """Test buying stock."""
    body, status = post_form(f"{REST_BASE}/trade/buy", {
        "userID": "uid:0",
        "symbol": "s:1",
        "quantity": "100.0"
    })
    assert status == 200, f"Expected 200, got {status}"


def test_register_user():
    """Test registering a new user."""
    body, status = post_form(f"{REST_BASE}/trade/register", {
        "userID": "testuser1",
        "password": "testpass",
        "fullname": "Test User",
        "address": "123 Test St",
        "email": "test@test.com",
        "creditcard": "1234-5678-9012-3456",
        "openBalance": "100000"
    })
    assert status == 200, f"Expected 200, got {status}"


def test_web_welcome_page():
    """Test the web welcome page."""
    body, status = get_text(f"{REST_BASE}/app")
    assert status == 200, f"Expected 200, got {status}"
    assert "DayTrader" in body, "Welcome page should contain 'DayTrader'"


def test_messaging_ping_broker():
    """Test messaging ping to broker."""
    body, status = post_form(f"{REST_BASE}/messaging/ping/broker", {
        "message": "smoketest"
    })
    assert status == 200, f"Expected 200, got {status}"


def test_messaging_ping_streamer():
    """Test messaging ping to streamer."""
    body, status = post_form(f"{REST_BASE}/messaging/ping/streamer", {
        "message": "smoketest"
    })
    assert status == 200, f"Expected 200, got {status}"


def test_messaging_stats():
    """Test getting MDB stats."""
    body, status = get_json(f"{REST_BASE}/messaging/stats")
    assert status == 200, f"Expected 200, got {status}"


# ============================================================
# Main
# ============================================================

if __name__ == "__main__":
    if not wait_for_ready():
        print("FATAL: Application not ready. Exiting.")
        sys.exit(1)

    # Give the app a moment after health check passes for DB population
    time.sleep(5)

    print("\n--- Running Smoke Tests ---\n")

    test("Health endpoint", test_health_endpoint)
    test("Market summary", test_market_summary)
    test("Get single quote", test_get_quote)
    test("Get multiple quotes", test_get_multiple_quotes)
    test("User login", test_login)
    test("Get account data", test_get_account)
    test("Get holdings", test_get_holdings)
    test("Get orders", test_get_orders)
    test("Buy stock", test_buy_stock)
    test("Register user", test_register_user)
    test("Web welcome page", test_web_welcome_page)
    test("Messaging ping broker", test_messaging_ping_broker)
    test("Messaging ping streamer", test_messaging_ping_streamer)
    test("Messaging stats", test_messaging_stats)

    print(f"\n--- Results: {passed} passed, {failed} failed ---\n")

    if errors:
        print("Failures:")
        for name, err in errors:
            print(f"  - {name}: {err}")

    sys.exit(0 if failed == 0 else 1)
