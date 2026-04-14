#!/usr/bin/env python3
"""
Smoke tests for DayTrader Quarkus Migration.
Tests core REST endpoints to verify the migration is functional.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

def make_request(method, path, data=None, content_type="application/json"):
    """Make an HTTP request and return (status_code, response_body)."""
    url = BASE_URL + path
    headers = {}
    body = None

    if data is not None:
        if content_type == "application/json":
            body = json.dumps(data).encode("utf-8")
            headers["Content-Type"] = "application/json"
        else:
            body = data.encode("utf-8") if isinstance(data, str) else data
            headers["Content-Type"] = content_type

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            return resp.status, resp.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8")
    except Exception as e:
        return -1, str(e)


def test_health():
    """Test health endpoint."""
    status, body = make_request("GET", "/health")
    assert status == 200, f"Health check failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("status") == "UP", f"Health status not UP: {data}"
    print("PASS: Health endpoint")


def test_config():
    """Test config endpoint."""
    status, body = make_request("GET", "/rest/config")
    assert status == 200, f"Config failed: status={status}, body={body}"
    data = json.loads(body)
    assert "maxUsers" in data, f"Config missing maxUsers: {data}"
    assert "maxQuotes" in data, f"Config missing maxQuotes: {data}"
    assert data.get("status") == "running", f"Config status not running: {data}"
    print("PASS: Config endpoint")


def test_populate_database():
    """Test database population."""
    status, body = make_request("POST", "/rest/config/populate",
                                {"maxUsers": 10, "maxQuotes": 20})
    assert status == 200, f"Populate failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("status") == "populated", f"Populate status not populated: {data}"
    print("PASS: Database population")


def test_get_quotes():
    """Test getting quotes."""
    status, body = make_request("GET", "/rest/quotes/s:0")
    assert status == 200, f"Get quotes failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    assert len(data) > 0, "No quotes returned"
    assert data[0].get("symbol") == "s:0", f"Wrong symbol: {data[0]}"
    print("PASS: Get quotes")


def test_get_multiple_quotes():
    """Test getting multiple quotes."""
    status, body = make_request("GET", "/rest/quotes/s:0,s:1,s:2")
    assert status == 200, f"Get multiple quotes failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    assert len(data) >= 1, f"Expected at least 1 quote, got {len(data)}"
    print("PASS: Get multiple quotes")


def test_register_user():
    """Test user registration."""
    status, body = make_request("POST", "/rest/trade/register", {
        "userID": "testuser1",
        "password": "testpass",
        "fullname": "Test User",
        "address": "123 Test St",
        "email": "test@test.com",
        "creditcard": "1234-5678-9012-3456",
        "openBalance": "100000"
    })
    assert status == 200, f"Register failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("balance") is not None, f"No balance in response: {data}"
    print("PASS: User registration")


def test_login():
    """Test user login."""
    status, body = make_request("POST", "/rest/trade/login", {
        "userID": "testuser1",
        "password": "testpass"
    })
    assert status == 200, f"Login failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("loginCount") is not None, f"No loginCount in response: {data}"
    print("PASS: User login")


def test_get_account():
    """Test getting account data."""
    status, body = make_request("GET", "/rest/trade/account/testuser1")
    assert status == 200, f"Get account failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("accountID") is not None, f"No accountID in response: {data}"
    print("PASS: Get account")


def test_get_profile():
    """Test getting profile data."""
    status, body = make_request("GET", "/rest/trade/profile/testuser1")
    assert status == 200, f"Get profile failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("userID") == "testuser1", f"Wrong userID: {data}"
    print("PASS: Get profile")


def test_buy():
    """Test buying stock."""
    status, body = make_request("POST", "/rest/trade/buy", {
        "userID": "testuser1",
        "symbol": "s:0",
        "quantity": 10.0
    })
    assert status == 200, f"Buy failed: status={status}, body={body}"
    data = json.loads(body)
    assert data.get("orderType") == "buy", f"Wrong order type: {data}"
    assert data.get("orderStatus") is not None, f"No order status: {data}"
    print("PASS: Buy stock")


def test_get_holdings():
    """Test getting holdings."""
    status, body = make_request("GET", "/rest/trade/holdings/testuser1")
    assert status == 200, f"Get holdings failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    print("PASS: Get holdings")


def test_sell():
    """Test selling stock - first get a holding, then sell it."""
    # Get holdings
    status, body = make_request("GET", "/rest/trade/holdings/testuser1")
    assert status == 200, f"Get holdings for sell failed: status={status}"
    data = json.loads(body)
    if len(data) == 0:
        print("SKIP: Sell stock (no holdings)")
        return

    holding_id = data[0].get("holdingID")
    assert holding_id is not None, f"No holdingID in holding: {data[0]}"

    status, body = make_request("POST", "/rest/trade/sell", {
        "userID": "testuser1",
        "holdingID": holding_id
    })
    assert status == 200, f"Sell failed: status={status}, body={body}"
    result = json.loads(body)
    assert result.get("orderType") == "sell", f"Wrong order type: {result}"
    print("PASS: Sell stock")


def test_get_orders():
    """Test getting orders."""
    status, body = make_request("GET", "/rest/trade/orders/testuser1")
    assert status == 200, f"Get orders failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    assert len(data) > 0, "No orders returned"
    print("PASS: Get orders")


def test_logout():
    """Test user logout."""
    status, body = make_request("POST", "/rest/trade/logout/testuser1")
    assert status == 200, f"Logout failed: status={status}, body={body}"
    print("PASS: User logout")


def test_market_summary():
    """Test market summary."""
    status, body = make_request("GET", "/rest/trade/marketSummary")
    assert status == 200, f"Market summary failed: status={status}, body={body}"
    data = json.loads(body)
    assert "tsia" in data or "TSIA" in data, f"No TSIA in market summary: {data}"
    print("PASS: Market summary")


def test_stats():
    """Test stats endpoint."""
    status, body = make_request("GET", "/rest/config/stats")
    assert status == 200, f"Stats failed: status={status}, body={body}"
    data = json.loads(body)
    assert "tradeUserCount" in data, f"No tradeUserCount in stats: {data}"
    print("PASS: Stats endpoint")


def wait_for_server(max_wait=120):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {BASE_URL} to be ready...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            status, body = make_request("GET", "/health")
            if status == 200:
                print(f"Server ready after {int(time.time() - start)} seconds")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"Server did not become ready within {max_wait} seconds")
    return False


def main():
    if not wait_for_server():
        print("FAIL: Server not ready")
        sys.exit(1)

    tests = [
        test_health,
        test_config,
        test_populate_database,
        test_get_quotes,
        test_get_multiple_quotes,
        test_register_user,
        test_login,
        test_get_account,
        test_get_profile,
        test_market_summary,
        test_buy,
        test_get_holdings,
        test_sell,
        test_get_orders,
        test_logout,
        test_stats,
    ]

    passed = 0
    failed = 0
    errors = []

    for test in tests:
        try:
            test()
            passed += 1
        except AssertionError as e:
            failed += 1
            errors.append(f"FAIL: {test.__name__}: {e}")
            print(f"FAIL: {test.__name__}: {e}")
        except Exception as e:
            failed += 1
            errors.append(f"ERROR: {test.__name__}: {e}")
            print(f"ERROR: {test.__name__}: {e}")

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")
    if errors:
        print("\nFailures:")
        for e in errors:
            print(f"  {e}")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
