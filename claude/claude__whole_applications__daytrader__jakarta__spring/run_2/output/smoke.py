#!/usr/bin/env python3
"""
Smoke tests for the DayTrader Spring Boot application.
Tests basic functionality: health, REST API, trading operations.
"""

import os
import sys
import time
import json
import urllib.request
import urllib.error
import urllib.parse

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
PASSED = 0
FAILED = 0


def test(name, func):
    global PASSED, FAILED
    try:
        func()
        PASSED += 1
        print(f"  PASS: {name}")
    except Exception as e:
        FAILED += 1
        print(f"  FAIL: {name} - {e}")


def http_get(path, expect_status=200):
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url)
    try:
        resp = urllib.request.urlopen(req, timeout=30)
        body = resp.read().decode("utf-8", errors="replace")
        if resp.status != expect_status:
            raise AssertionError(f"Expected status {expect_status}, got {resp.status}")
        return body
    except urllib.error.HTTPError as e:
        if e.code == expect_status:
            return e.read().decode("utf-8", errors="replace")
        raise AssertionError(f"Expected status {expect_status}, got {e.code}: {e.read().decode('utf-8', errors='replace')[:200]}")


def http_post(path, data=None, content_type="application/x-www-form-urlencoded", expect_status=200):
    url = f"{BASE_URL}{path}"
    if data and isinstance(data, dict):
        data = urllib.parse.urlencode(data).encode("utf-8")
    elif data and isinstance(data, str):
        data = data.encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    if content_type:
        req.add_header("Content-Type", content_type)
    try:
        resp = urllib.request.urlopen(req, timeout=30)
        body = resp.read().decode("utf-8", errors="replace")
        if resp.status != expect_status:
            raise AssertionError(f"Expected status {expect_status}, got {resp.status}")
        return body
    except urllib.error.HTTPError as e:
        if e.code == expect_status:
            return e.read().decode("utf-8", errors="replace")
        raise AssertionError(f"Expected status {expect_status}, got {e.code}: {e.read().decode('utf-8', errors='replace')[:200]}")


def wait_for_server(timeout=120):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            urllib.request.urlopen(f"{BASE_URL}/api/health", timeout=5)
            print("Server is ready!")
            return True
        except Exception:
            time.sleep(2)
    raise RuntimeError(f"Server did not start within {timeout}s")


# ---- Tests ----

def test_health_endpoint():
    body = http_get("/api/health")
    data = json.loads(body)
    assert data.get("status") == "UP", f"Health status is not UP: {data}"


def test_config_endpoint():
    body = http_get("/api/config")
    data = json.loads(body)
    assert "runtimeMode" in data, f"Config missing runtimeMode: {data}"


def test_build_db():
    """Initialize the database with test data."""
    body = http_post("/api/config/buildDB")
    data = json.loads(body)
    assert "success" in data.get("status", "").lower() or "built" in body.lower() or "result" in data, f"BuildDB failed: {body[:300]}"


def test_create_quote():
    body = http_post("/api/quotes", data=json.dumps({
        "symbol": "TESTQ",
        "companyName": "Test Quote Inc",
        "price": 100.0
    }), content_type="application/json", expect_status=201)
    data = json.loads(body)
    assert data.get("symbol") == "TESTQ", f"Quote creation failed: {data}"


def test_get_quote():
    body = http_get("/api/quotes/TESTQ")
    data = json.loads(body)
    assert data.get("symbol") == "TESTQ", f"Quote retrieval failed: {data}"


def test_get_all_quotes():
    body = http_get("/api/quotes")
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list of quotes: {data}"
    assert len(data) > 0, "No quotes returned"


def test_register_user():
    body = http_post("/api/account/register", data=json.dumps({
        "userID": "smoketest_user",
        "password": "password123",
        "fullname": "Smoke Test User",
        "address": "123 Test St",
        "email": "smoke@test.com",
        "creditcard": "1234567890",
        "openBalance": 100000.00
    }), content_type="application/json", expect_status=201)
    data = json.loads(body)
    assert data.get("profileID") == "smoketest_user" or data.get("profile", {}).get("userID") == "smoketest_user", f"Registration failed: {data}"


def test_login():
    body = http_post("/api/account/login", data=json.dumps({
        "userID": "smoketest_user",
        "password": "password123"
    }), content_type="application/json")
    data = json.loads(body)
    assert data.get("profileID") == "smoketest_user" or "accountID" in data, f"Login failed: {data}"


def test_get_account():
    body = http_get("/api/account/smoketest_user")
    data = json.loads(body)
    assert "accountID" in data, f"Account retrieval failed: {data}"


def test_get_account_profile():
    body = http_get("/api/account/smoketest_user/profile")
    data = json.loads(body)
    assert data.get("userID") == "smoketest_user", f"Profile retrieval failed: {data}"


def test_buy_stock():
    body = http_post("/api/trade/buy", data=json.dumps({
        "userID": "smoketest_user",
        "symbol": "TESTQ",
        "quantity": 10.0
    }), content_type="application/json")
    data = json.loads(body)
    assert "orderID" in data, f"Buy order failed: {data}"
    assert data.get("orderType") == "buy", f"Expected buy order: {data}"


def test_get_holdings():
    body = http_get("/api/account/smoketest_user/holdings")
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list of holdings: {data}"


def test_get_orders():
    body = http_get("/api/account/smoketest_user/orders")
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list of orders: {data}"
    assert len(data) > 0, "No orders returned after buy"


def test_market_summary():
    body = http_get("/api/market/summary")
    data = json.loads(body)
    assert "TSIA" in data or "tsia" in str(data).lower(), f"Market summary missing TSIA: {data}"


def test_logout():
    body = http_post("/api/account/logout", data=json.dumps({
        "userID": "smoketest_user"
    }), content_type="application/json")
    # Just verify no error
    assert body is not None


# ---- Main ----

if __name__ == "__main__":
    print("=" * 60)
    print("DayTrader Spring Boot Smoke Tests")
    print("=" * 60)

    wait_for_server()
    print()

    # Phase 1: Health and config
    print("Phase 1: Health and Configuration")
    test("Health endpoint", test_health_endpoint)
    test("Config endpoint", test_config_endpoint)
    print()

    # Phase 2: Database setup
    print("Phase 2: Database Setup")
    test("Build database", test_build_db)
    print()

    # Phase 3: Quote operations
    print("Phase 3: Quote Operations")
    test("Create quote", test_create_quote)
    test("Get quote", test_get_quote)
    test("Get all quotes", test_get_all_quotes)
    print()

    # Phase 4: User operations
    print("Phase 4: User Operations")
    test("Register user", test_register_user)
    test("Login", test_login)
    test("Get account", test_get_account)
    test("Get account profile", test_get_account_profile)
    print()

    # Phase 5: Trading operations
    print("Phase 5: Trading Operations")
    test("Buy stock", test_buy_stock)
    test("Get holdings", test_get_holdings)
    test("Get orders", test_get_orders)
    print()

    # Phase 6: Market operations
    print("Phase 6: Market Operations")
    test("Market summary", test_market_summary)
    print()

    # Phase 7: Cleanup
    print("Phase 7: Session Management")
    test("Logout", test_logout)
    print()

    # Summary
    print("=" * 60)
    total = PASSED + FAILED
    print(f"Results: {PASSED}/{total} passed, {FAILED}/{total} failed")
    print("=" * 60)

    sys.exit(0 if FAILED == 0 else 1)
