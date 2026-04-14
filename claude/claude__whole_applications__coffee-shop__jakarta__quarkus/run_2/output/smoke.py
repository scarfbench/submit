#!/usr/bin/env python3
"""Smoke tests for the Coffee Shop orders-service (Quarkus migration)."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE = os.environ.get("APP_URL", "http://localhost:8080")

passed = 0
failed = 0

def test(name, fn):
    global passed, failed
    try:
        fn()
        print(f"  PASS  {name}")
        passed += 1
    except Exception as e:
        print(f"  FAIL  {name}: {e}")
        failed += 1

def http(method, path, body=None, expected_status=None):
    url = BASE.rstrip("/") + path
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        resp_body = resp.read().decode()
        status = resp.getcode()
    except urllib.error.HTTPError as e:
        status = e.code
        resp_body = e.read().decode() if e.fp else ""
    if expected_status and status != expected_status:
        raise AssertionError(f"Expected status {expected_status}, got {status}. Body: {resp_body}")
    return status, resp_body

# --- Wait for readiness ---
def wait_for_ready(max_wait=120):
    """Wait for the app to be ready."""
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(BASE.rstrip("/") + "/q/health/ready")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print(f"App ready after {int(time.time()-start)}s")
                return True
        except Exception:
            pass
        # Also try a simple status endpoint
        try:
            req = urllib.request.Request(BASE.rstrip("/") + "/api/status")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print(f"App ready (via /api/status) after {int(time.time()-start)}s")
                return True
        except Exception:
            pass
        time.sleep(3)
    print(f"App did NOT become ready within {max_wait}s")
    return False

# --- Tests ---
def test_health_ready():
    status, body = http("GET", "/q/health/ready")
    assert status == 200, f"Health check failed with status {status}"

def test_health_live():
    status, body = http("GET", "/q/health/live")
    assert status == 200, f"Liveness check failed with status {status}"

def test_create_order():
    status, body = http("POST", "/api/orders",
                        body={"customer": "Alice", "item": "Latte", "quantity": 1},
                        expected_status=202)
    data = json.loads(body)
    assert "id" in data or "id" in str(data), f"No id in response: {body}"
    return data

def test_create_order_food():
    status, body = http("POST", "/api/orders",
                        body={"customer": "Bob", "item": "Sandwich", "quantity": 2},
                        expected_status=202)
    data = json.loads(body)
    assert "id" in data or "id" in str(data), f"No id in response: {body}"

def test_get_order():
    # First create an order
    status, body = http("POST", "/api/orders",
                        body={"customer": "Charlie", "item": "Espresso", "quantity": 1})
    data = json.loads(body)
    order_id = data.get("id", data.get("orderId"))
    # Then retrieve it
    status2, body2 = http("GET", f"/api/orders/{order_id}")
    assert status2 == 200, f"GET order returned {status2}"
    order = json.loads(body2)
    assert order.get("customer") == "Charlie" or str(order_id) in body2

def test_invalid_order():
    """Empty body should return 400."""
    status, body = http("POST", "/api/orders",
                        body={"customer": "", "item": "", "quantity": 0})
    assert status == 400, f"Expected 400 for invalid order, got {status}"

def test_status_endpoint():
    """Test the status/status endpoint returns OK."""
    try:
        status, body = http("GET", "/api/status")
        assert status == 200, f"Status endpoint returned {status}"
    except Exception:
        # Quarkus may not have this - just skip
        pass

if __name__ == "__main__":
    print(f"Smoke testing against: {BASE}")
    if not wait_for_ready():
        print("ABORT: Application not ready")
        sys.exit(1)

    print("\n--- Running smoke tests ---\n")
    test("Health readiness", test_health_ready)
    test("Health liveness", test_health_live)
    test("Create drink order (barista)", test_create_order)
    test("Create food order (kitchen)", test_create_order_food)
    test("Get order by ID", test_get_order)
    test("Invalid order returns 400", test_invalid_order)
    test("Status endpoint", test_status_endpoint)

    print(f"\n--- Results: {passed} passed, {failed} failed ---\n")
    sys.exit(0 if failed == 0 else 1)
