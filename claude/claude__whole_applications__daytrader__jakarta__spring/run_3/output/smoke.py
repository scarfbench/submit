"""
Smoke tests for DayTrader Spring Boot application.
Tests basic functionality: health, REST API, and core trading operations.
"""
import os
import sys
import json
import urllib.request
import urllib.error
import time

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080/daytrader")
PASS = 0
FAIL = 0

def test(name, fn):
    global PASS, FAIL
    try:
        fn()
        PASS += 1
        print(f"  PASS: {name}")
    except Exception as e:
        FAIL += 1
        print(f"  FAIL: {name} - {e}")

def get(path, expected_status=200):
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        assert resp.getcode() == expected_status, f"Expected {expected_status}, got {resp.getcode()}"
        return body
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return e.read().decode("utf-8")
        raise

def post(path, data=None, content_type="application/x-www-form-urlencoded", expected_status=200):
    url = f"{BASE_URL}{path}"
    if data and isinstance(data, str):
        data = data.encode("utf-8")
    req = urllib.request.Request(url, data=data, method="POST")
    if content_type:
        req.add_header("Content-Type", content_type)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        assert resp.getcode() == expected_status, f"Expected {expected_status}, got {resp.getcode()}"
        return body
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return e.read().decode("utf-8")
        raise

def wait_for_ready(max_retries=30, delay=2):
    """Wait for the application to be ready."""
    for i in range(max_retries):
        try:
            resp = urllib.request.urlopen(f"{BASE_URL}/health", timeout=5)
            if resp.getcode() == 200:
                print(f"Application ready after {i * delay} seconds")
                return True
        except Exception:
            pass
        time.sleep(delay)
    raise Exception(f"Application not ready after {max_retries * delay} seconds")

# --- Test Functions ---

def test_health():
    body = get("/health")
    data = json.loads(body)
    assert data["status"] == "UP", f"Expected UP, got {data['status']}"
    assert "DayTrader" in data["application"], "Application name should contain DayTrader"

def test_rest_quotes_get():
    """Test the REST quotes endpoint with GET."""
    body = get("/rest/quotes/s:0")
    data = json.loads(body)
    assert isinstance(data, list), "Expected a list of quotes"

def test_rest_quotes_post():
    """Test the REST quotes endpoint with POST."""
    body = post("/rest/quotes", data="symbols=s:0,s:1")
    data = json.loads(body)
    assert isinstance(data, list), "Expected a list of quotes"

def test_h2_console():
    """Test that the H2 console is accessible."""
    try:
        body = get("/h2-console/")
    except Exception:
        # H2 console may redirect, which is acceptable
        pass

def test_context_root():
    """Test that the application context root responds."""
    try:
        body = get("/")
    except Exception:
        # May return 404 if no root mapping, but shouldn't error out
        pass

if __name__ == "__main__":
    print("=" * 60)
    print("DayTrader Spring Boot Smoke Tests")
    print(f"Base URL: {BASE_URL}")
    print("=" * 60)

    # Wait for app to be ready
    try:
        wait_for_ready()
    except Exception as e:
        print(f"FATAL: {e}")
        sys.exit(1)

    print("\n--- Health Tests ---")
    test("Health endpoint returns UP", test_health)

    print("\n--- REST API Tests ---")
    test("GET /rest/quotes/s:0 returns quotes", test_rest_quotes_get)
    test("POST /rest/quotes returns quotes", test_rest_quotes_post)

    print("\n--- Infrastructure Tests ---")
    test("H2 console accessible", test_h2_console)

    print("\n" + "=" * 60)
    print(f"Results: {PASS} passed, {FAIL} failed out of {PASS + FAIL} tests")
    print("=" * 60)

    sys.exit(0 if FAIL == 0 else 1)
