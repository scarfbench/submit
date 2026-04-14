#!/usr/bin/env python3
"""
Smoke tests for DayTrader Jakarta EE application.
Tests core REST API endpoints to verify the migrated application is functional.
"""

import requests
import sys
import time
import json

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

def log(msg):
    print(f"[SMOKE] {msg}")

def test_endpoint(name, method, url, expected_status=200, data=None, headers=None, allow_statuses=None):
    """Test a single endpoint and return True if it passes."""
    try:
        if method == "GET":
            resp = requests.get(url, headers=headers, timeout=15)
        elif method == "POST":
            resp = requests.post(url, data=data, headers=headers, timeout=15)
        else:
            log(f"  FAIL [{name}]: Unknown method {method}")
            return False

        if allow_statuses:
            if resp.status_code in allow_statuses:
                log(f"  PASS [{name}]: status={resp.status_code}")
                return True
            else:
                log(f"  FAIL [{name}]: status={resp.status_code}, expected one of {allow_statuses}")
                log(f"        Response: {resp.text[:500]}")
                return False

        if resp.status_code == expected_status:
            log(f"  PASS [{name}]: status={resp.status_code}")
            return True
        else:
            log(f"  FAIL [{name}]: status={resp.status_code}, expected {expected_status}")
            log(f"        Response: {resp.text[:500]}")
            return False
    except Exception as e:
        log(f"  FAIL [{name}]: {e}")
        return False

def wait_for_app(url, max_wait=120):
    """Wait for the application to be ready."""
    log(f"Waiting for app at {url} (max {max_wait}s)...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            resp = requests.get(url, timeout=5)
            if resp.status_code < 500:
                log(f"App is ready (status={resp.status_code}, took {int(time.time()-start)}s)")
                return True
        except:
            pass
        time.sleep(3)
    log(f"App did not become ready within {max_wait}s")
    return False

def main():
    log(f"Starting smoke tests against {BASE_URL}")

    # Wait for app to be ready - try multiple endpoints
    ready = False
    for path in ["/rest/trade/market", "/rest/app", "/health", "/"]:
        try:
            resp = requests.get(f"{BASE_URL}{path}", timeout=5)
            if resp.status_code < 500:
                ready = True
                break
        except:
            pass

    if not ready:
        if not wait_for_app(f"{BASE_URL}/rest/trade/market"):
            # Try alternate health paths
            if not wait_for_app(f"{BASE_URL}/rest/app"):
                log("FATAL: Application not reachable")
                sys.exit(1)

    passed = 0
    failed = 0
    total = 0

    # Test 1: Market Summary
    log("\n--- Test: Market Summary ---")
    total += 1
    if test_endpoint("GET /rest/trade/market", "GET", f"{BASE_URL}/rest/trade/market",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 2: Get quotes
    log("\n--- Test: Get Quotes ---")
    total += 1
    if test_endpoint("GET /rest/quotes/s:0", "GET", f"{BASE_URL}/rest/quotes/s:0",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 3: Get multiple quotes
    log("\n--- Test: Get Multiple Quotes ---")
    total += 1
    if test_endpoint("GET /rest/quotes/s:0,s:1,s:2", "GET", f"{BASE_URL}/rest/quotes/s:0,s:1,s:2",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 4: Login
    log("\n--- Test: Login ---")
    total += 1
    if test_endpoint("POST /rest/trade/login", "POST", f"{BASE_URL}/rest/trade/login",
                     data={"userID": "uid:0", "password": "xxx"},
                     headers={"Content-Type": "application/x-www-form-urlencoded"},
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 5: Get Account
    log("\n--- Test: Get Account ---")
    total += 1
    if test_endpoint("GET /rest/trade/account/uid:0", "GET", f"{BASE_URL}/rest/trade/account/uid:0",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 6: Get Account Profile
    log("\n--- Test: Get Account Profile ---")
    total += 1
    if test_endpoint("GET /rest/trade/account/uid:0/profile", "GET",
                     f"{BASE_URL}/rest/trade/account/uid:0/profile",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 7: Get Holdings
    log("\n--- Test: Get Holdings ---")
    total += 1
    if test_endpoint("GET /rest/trade/account/uid:0/holdings", "GET",
                     f"{BASE_URL}/rest/trade/account/uid:0/holdings",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 8: Get Orders
    log("\n--- Test: Get Orders ---")
    total += 1
    if test_endpoint("GET /rest/trade/account/uid:0/orders", "GET",
                     f"{BASE_URL}/rest/trade/account/uid:0/orders",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 9: Buy stock
    log("\n--- Test: Buy Stock ---")
    total += 1
    if test_endpoint("POST /rest/trade/buy", "POST", f"{BASE_URL}/rest/trade/buy",
                     data={"userID": "uid:0", "symbol": "s:0", "quantity": "10.0"},
                     headers={"Content-Type": "application/x-www-form-urlencoded"},
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 10: Register new user
    log("\n--- Test: Register ---")
    total += 1
    if test_endpoint("POST /rest/trade/register", "POST", f"{BASE_URL}/rest/trade/register",
                     data={"userID": "testuser1", "password": "testpass", "fullname": "Test User",
                           "address": "123 Test St", "email": "test@test.com",
                           "creditcard": "1234-5678-9012-3456", "openBalance": "100000.00"},
                     headers={"Content-Type": "application/x-www-form-urlencoded"},
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Test 11: Logout
    log("\n--- Test: Logout ---")
    total += 1
    if test_endpoint("POST /rest/trade/logout/uid:0", "POST", f"{BASE_URL}/rest/trade/logout/uid:0",
                     allow_statuses=[200, 204]):
        passed += 1
    else:
        failed += 1

    # Test 12: Web App page
    log("\n--- Test: Web App Welcome ---")
    total += 1
    if test_endpoint("GET /rest/app", "GET", f"{BASE_URL}/rest/app",
                     allow_statuses=[200]):
        passed += 1
    else:
        failed += 1

    # Summary
    log(f"\n{'='*50}")
    log(f"RESULTS: {passed}/{total} passed, {failed}/{total} failed")
    log(f"{'='*50}")

    if failed > 0:
        log("SMOKE TESTS FAILED")
        sys.exit(1)
    else:
        log("ALL SMOKE TESTS PASSED")
        sys.exit(0)

if __name__ == "__main__":
    main()
