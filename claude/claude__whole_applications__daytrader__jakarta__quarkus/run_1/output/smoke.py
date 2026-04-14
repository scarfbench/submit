"""
DayTrader Quarkus Smoke Tests
Tests core functionality of the migrated DayTrader application.
"""
import os
import sys
import json
import time
import urllib.request
import urllib.error
import urllib.parse

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

def make_request(method, path, data=None, content_type="application/json"):
    """Make HTTP request and return response."""
    url = f"{BASE_URL}{path}"
    headers = {}
    body = None

    if data and content_type == "application/x-www-form-urlencoded":
        body = urllib.parse.urlencode(data).encode('utf-8')
        headers['Content-Type'] = content_type
    elif data:
        body = json.dumps(data).encode('utf-8')
        headers['Content-Type'] = content_type

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        response = urllib.request.urlopen(req, timeout=30)
        resp_body = response.read().decode('utf-8')
        return response.status, resp_body
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode('utf-8') if e.fp else ""
        return e.code, resp_body
    except Exception as e:
        return 0, str(e)

def test_health():
    """Test health endpoint."""
    status, body = make_request("GET", "/q/health")
    assert status == 200, f"Health check failed: status={status}, body={body}"
    data = json.loads(body)
    assert data["status"] == "UP", f"Health check not UP: {body}"
    print("PASS: Health check")

def test_ping():
    """Test ping endpoint."""
    status, body = make_request("GET", "/api/ping")
    assert status == 200, f"Ping failed: status={status}, body={body}"
    data = json.loads(body)
    assert data["status"] == "alive", f"Ping not alive: {body}"
    assert data["framework"] == "Quarkus", f"Wrong framework: {body}"
    print("PASS: Ping endpoint")

def test_get_quotes():
    """Test getting stock quotes."""
    status, body = make_request("GET", "/api/quotes/s:0")
    assert status == 200, f"Get quotes failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got: {type(data)}"
    assert len(data) > 0, f"Expected at least one quote, got empty list"
    quote = data[0]
    assert "symbol" in quote, f"Quote missing symbol: {quote}"
    assert quote["symbol"] == "s:0", f"Wrong symbol: {quote['symbol']}"
    print("PASS: Get quotes")

def test_get_all_quotes():
    """Test getting all quotes."""
    status, body = make_request("GET", "/api/quotes")
    assert status == 200, f"Get all quotes failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got: {type(data)}"
    assert len(data) >= 10, f"Expected at least 10 quotes, got {len(data)}"
    print(f"PASS: Get all quotes ({len(data)} quotes)")

def test_get_account():
    """Test getting account data."""
    status, body = make_request("GET", "/api/account/uid:0")
    assert status == 200, f"Get account failed: status={status}, body={body}"
    data = json.loads(body)
    assert "accountID" in data, f"Account missing accountID: {data}"
    assert "balance" in data, f"Account missing balance: {data}"
    print("PASS: Get account data")

def test_get_account_profile():
    """Test getting account profile."""
    status, body = make_request("GET", "/api/account/uid:0/profile")
    assert status == 200, f"Get profile failed: status={status}, body={body}"
    data = json.loads(body)
    assert "userID" in data, f"Profile missing userID: {data}"
    assert data["userID"] == "uid:0", f"Wrong userID: {data['userID']}"
    print("PASS: Get account profile")

def test_login():
    """Test login."""
    status, body = make_request("POST", "/api/login",
                                data={"userID": "uid:0", "password": "xxx"},
                                content_type="application/x-www-form-urlencoded")
    assert status == 200, f"Login failed: status={status}, body={body}"
    data = json.loads(body)
    assert "accountID" in data, f"Login response missing accountID: {data}"
    print("PASS: Login")

def test_login_failure():
    """Test login with wrong password."""
    status, body = make_request("POST", "/api/login",
                                data={"userID": "uid:0", "password": "wrongpassword"},
                                content_type="application/x-www-form-urlencoded")
    assert status == 401, f"Expected 401 for bad login, got: status={status}"
    print("PASS: Login failure handled correctly")

def test_register():
    """Test user registration."""
    status, body = make_request("POST", "/api/register",
                                data={
                                    "userID": "testuser1",
                                    "password": "testpass",
                                    "fullname": "Test User",
                                    "address": "123 Test St",
                                    "email": "test@example.com",
                                    "creditcard": "1234-5678-9012-3456",
                                    "openBalance": "50000"
                                },
                                content_type="application/x-www-form-urlencoded")
    assert status == 200, f"Register failed: status={status}, body={body}"
    data = json.loads(body)
    assert "accountID" in data, f"Register response missing accountID: {data}"
    print("PASS: Register user")

def test_buy():
    """Test buying stock."""
    status, body = make_request("POST", "/api/buy",
                                data={
                                    "userID": "uid:0",
                                    "symbol": "s:0",
                                    "quantity": "10.0"
                                },
                                content_type="application/x-www-form-urlencoded")
    assert status == 200, f"Buy failed: status={status}, body={body}"
    data = json.loads(body)
    assert "orderID" in data, f"Buy response missing orderID: {data}"
    print("PASS: Buy stock")

def test_get_holdings():
    """Test getting holdings after buy."""
    status, body = make_request("GET", "/api/holdings/uid:0")
    assert status == 200, f"Get holdings failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got: {type(data)}"
    print(f"PASS: Get holdings ({len(data)} holdings)")

def test_get_orders():
    """Test getting orders."""
    status, body = make_request("GET", "/api/orders/uid:0")
    assert status == 200, f"Get orders failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got: {type(data)}"
    print(f"PASS: Get orders ({len(data)} orders)")

def test_market_summary():
    """Test market summary."""
    status, body = make_request("GET", "/api/marketSummary")
    assert status == 200, f"Market summary failed: status={status}, body={body}"
    data = json.loads(body)
    assert "TSIA" in data or "tsia" in data or "tSIA" in data, f"Market summary missing TSIA: {data}"
    print("PASS: Market summary")

def test_logout():
    """Test logout."""
    status, body = make_request("POST", "/api/logout/uid:0")
    assert status == 200, f"Logout failed: status={status}, body={body}"
    print("PASS: Logout")

def test_rest_quotes_endpoint():
    """Test the /quotes endpoint (from original JAX-RS QuoteResource)."""
    status, body = make_request("GET", "/quotes/s:0")
    assert status == 200, f"REST quotes failed: status={status}, body={body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got: {type(data)}"
    assert len(data) > 0, f"Expected at least one quote"
    print("PASS: REST quotes endpoint")

def wait_for_app(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            status, body = make_request("GET", "/q/health")
            if status == 200:
                print(f"Application ready after {int(time.time() - start)} seconds")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"Application not ready after {max_wait} seconds")
    return False

def main():
    if not wait_for_app():
        print("FAIL: Application did not start in time")
        sys.exit(1)

    # Give a bit more time for database initialization
    time.sleep(3)

    tests = [
        test_health,
        test_ping,
        test_get_all_quotes,
        test_get_quotes,
        test_get_account,
        test_get_account_profile,
        test_login,
        test_login_failure,
        test_register,
        test_buy,
        test_get_holdings,
        test_get_orders,
        test_market_summary,
        test_logout,
        test_rest_quotes_endpoint,
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
        for error in errors:
            print(f"  - {error}")

    if failed > 0:
        sys.exit(1)
    else:
        print("\nAll smoke tests PASSED!")
        sys.exit(0)

if __name__ == "__main__":
    main()
