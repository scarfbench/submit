#!/usr/bin/env python3
"""
Smoke tests for DayTrader Jakarta EE application.
Tests core functionality: health, quotes, trading, accounts.
"""

import sys
import time
import requests
import json

BASE_URL = None

def get_base_url():
    """Get base URL from command line or environment."""
    global BASE_URL
    if len(sys.argv) > 1:
        BASE_URL = sys.argv[1].rstrip('/')
    else:
        import os
        port = os.environ.get('APP_PORT', '8080')
        BASE_URL = f"http://localhost:{port}"
    return BASE_URL


def wait_for_app(timeout=120):
    """Wait for the application to become available."""
    print(f"Waiting for application at {BASE_URL} (timeout={timeout}s)...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            r = requests.get(f"{BASE_URL}/rest/trade/market", timeout=5)
            if r.status_code in (200, 500):
                print(f"Application is responding (status={r.status_code})")
                # Give it a moment for DB population to complete
                time.sleep(5)
                return True
        except requests.exceptions.ConnectionError:
            pass
        except requests.exceptions.Timeout:
            pass
        time.sleep(2)
    print("ERROR: Application did not start within timeout")
    return False


def test_market_summary():
    """Test GET /rest/trade/market - market summary endpoint."""
    print("\n--- Test: Market Summary ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/trade/market", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  TSIA: {data.get('tsia', 'N/A')}")
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_get_quote():
    """Test GET /rest/quotes/s:0 - get a stock quote."""
    print("\n--- Test: Get Quote ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/quotes/s:0", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        assert len(data) > 0, "Expected at least one quote"
        quote = data[0]
        print(f"  Symbol: {quote.get('symbol', 'N/A')}")
        print(f"  Price: {quote.get('price', 'N/A')}")
        assert quote.get('symbol') == 's:0', f"Expected symbol s:0, got {quote.get('symbol')}"
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_get_multiple_quotes():
    """Test GET /rest/quotes/s:0,s:1,s:2 - get multiple quotes."""
    print("\n--- Test: Get Multiple Quotes ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/quotes/s:0,s:1,s:2", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Quotes returned: {len(data)}")
        assert len(data) == 3, f"Expected 3 quotes, got {len(data)}"
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_login():
    """Test POST /rest/trade/login - user login."""
    print("\n--- Test: Login ---")
    try:
        r = requests.post(f"{BASE_URL}/rest/trade/login",
                         data={"userID": "uid:0", "password": "xxx"},
                         timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Account ID: {data.get('accountID', 'N/A')}")
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_get_account():
    """Test GET /rest/trade/account/uid:0 - get account data."""
    print("\n--- Test: Get Account ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/trade/account/uid:0", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Balance: {data.get('balance', 'N/A')}")
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_get_account_profile():
    """Test GET /rest/trade/account/uid:0/profile - get profile data."""
    print("\n--- Test: Get Account Profile ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/trade/account/uid:0/profile", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  UserID: {data.get('userID', 'N/A')}")
        assert data.get('userID') == 'uid:0', f"Expected uid:0, got {data.get('userID')}"
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_get_holdings():
    """Test GET /rest/trade/account/uid:0/holdings - get holdings."""
    print("\n--- Test: Get Holdings ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/trade/account/uid:0/holdings", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Holdings count: {len(data)}")
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_buy_stock():
    """Test POST /rest/trade/buy - buy stock."""
    print("\n--- Test: Buy Stock ---")
    try:
        r = requests.post(f"{BASE_URL}/rest/trade/buy",
                         data={"userID": "uid:0", "symbol": "s:1", "quantity": "10.0"},
                         timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Order ID: {data.get('orderID', 'N/A')}")
        print(f"  Order Type: {data.get('orderType', 'N/A')}")
        print(f"  Order Status: {data.get('orderStatus', 'N/A')}")
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_get_orders():
    """Test GET /rest/trade/account/uid:0/orders - get orders."""
    print("\n--- Test: Get Orders ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/trade/account/uid:0/orders", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Orders count: {len(data)}")
        assert len(data) > 0, "Expected at least one order after buy"
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_register():
    """Test POST /rest/trade/register - register new user."""
    print("\n--- Test: Register New User ---")
    try:
        r = requests.post(f"{BASE_URL}/rest/trade/register",
                         data={
                             "userID": "testuser1",
                             "password": "testpass",
                             "fullname": "Test User",
                             "address": "123 Test St",
                             "email": "test@test.com",
                             "creditcard": "1111-2222-3333-4444",
                             "openBalance": "50000.00"
                         },
                         timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        data = r.json()
        print(f"  Account ID: {data.get('accountID', 'N/A')}")
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_logout():
    """Test POST /rest/trade/logout/uid:0 - logout."""
    print("\n--- Test: Logout ---")
    try:
        r = requests.post(f"{BASE_URL}/rest/trade/logout/uid:0", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def test_web_app():
    """Test GET /rest/app - web UI endpoint."""
    print("\n--- Test: Web App ---")
    try:
        r = requests.get(f"{BASE_URL}/rest/app", timeout=10)
        print(f"  Status: {r.status_code}")
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        assert "DayTrader" in r.text, "Expected 'DayTrader' in response"
        print(f"  PASS")
        return True
    except Exception as e:
        print(f"  FAIL: {e}")
        return False


def main():
    get_base_url()
    print(f"=== DayTrader Smoke Tests ===")
    print(f"Base URL: {BASE_URL}")

    if not wait_for_app():
        print("\nFATAL: Application not available")
        sys.exit(1)

    tests = [
        test_market_summary,
        test_get_quote,
        test_get_multiple_quotes,
        test_login,
        test_get_account,
        test_get_account_profile,
        test_get_holdings,
        test_buy_stock,
        test_get_orders,
        test_register,
        test_logout,
        test_web_app,
    ]

    passed = 0
    failed = 0
    for test in tests:
        try:
            if test():
                passed += 1
            else:
                failed += 1
        except Exception as e:
            print(f"  UNEXPECTED ERROR: {e}")
            failed += 1

    print(f"\n=== Results ===")
    print(f"Passed: {passed}/{len(tests)}")
    print(f"Failed: {failed}/{len(tests)}")

    if failed > 0:
        sys.exit(1)
    else:
        print("All smoke tests passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
