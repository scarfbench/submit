#!/usr/bin/env python3
"""
Smoke tests for the Coffee Shop orders-service (Quarkus migration).
These tests verify the REST API endpoints work correctly after migration.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
PASS = 0
FAIL = 0


def log(status, message):
    global PASS, FAIL
    if status == "PASS":
        PASS += 1
        print(f"  [PASS] {message}")
    else:
        FAIL += 1
        print(f"  [FAIL] {message}")


def request(method, path, body=None, expect_status=None):
    """Helper to make HTTP requests."""
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    data = json.dumps(body).encode("utf-8") if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        resp_body = resp.read().decode("utf-8")
        status = resp.getcode()
        return status, resp_body
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8") if e.fp else ""
        return e.code, resp_body
    except Exception as e:
        return None, str(e)


def wait_for_ready(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} to be ready...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            status, body = request("GET", "/health")
            if status and status < 500:
                print(f"  Application ready after {int(time.time() - start)}s")
                return True
        except Exception:
            pass
        # Also try the q/health endpoint (Quarkus default)
        try:
            status, body = request("GET", "/q/health")
            if status and status < 500:
                print(f"  Application ready after {int(time.time() - start)}s (via /q/health)")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"  Application not ready after {max_wait}s")
    return False


def test_health():
    """Test health endpoint."""
    print("\n--- Test: Health Check ---")
    # Try Quarkus default health paths
    for path in ["/health", "/q/health", "/q/health/ready"]:
        status, body = request("GET", path)
        if status == 200:
            log("PASS", f"Health check {path} returned 200")
            return
    log("FAIL", "No health endpoint returned 200")


def test_create_order():
    """Test creating a new order via POST /api/orders."""
    print("\n--- Test: Create Order ---")
    order = {"customer": "TestUser", "item": "Latte", "quantity": 1}
    status, body = request("POST", "/api/orders", body=order)
    if status in (200, 201, 202):
        log("PASS", f"POST /api/orders returned {status}")
        try:
            data = json.loads(body)
            if "id" in data:
                log("PASS", f"Response contains order id: {data['id']}")
                return data["id"]
            else:
                log("FAIL", f"Response missing 'id' field: {body}")
        except json.JSONDecodeError:
            log("FAIL", f"Response is not valid JSON: {body}")
    else:
        log("FAIL", f"POST /api/orders returned {status}: {body}")
    return None


def test_get_order(order_id):
    """Test getting an order by ID via GET /api/orders/{id}."""
    print("\n--- Test: Get Order ---")
    if order_id is None:
        log("FAIL", "Skipped - no order ID from creation test")
        return
    status, body = request("GET", f"/api/orders/{order_id}")
    if status == 200:
        log("PASS", f"GET /api/orders/{order_id} returned 200")
        try:
            data = json.loads(body)
            if data.get("customer") == "TestUser":
                log("PASS", "Order customer matches")
            if data.get("item") == "Latte":
                log("PASS", "Order item matches")
            if data.get("status") == "PLACED":
                log("PASS", "Order status is PLACED")
        except json.JSONDecodeError:
            log("FAIL", f"Response is not valid JSON: {body}")
    else:
        log("FAIL", f"GET /api/orders/{order_id} returned {status}: {body}")


def test_create_food_order():
    """Test creating a food (kitchen) order."""
    print("\n--- Test: Create Food Order (Kitchen) ---")
    order = {"customer": "FoodTester", "item": "Sandwich", "quantity": 2}
    status, body = request("POST", "/api/orders", body=order)
    if status in (200, 201, 202):
        log("PASS", f"POST /api/orders (food) returned {status}")
        try:
            data = json.loads(body)
            if "id" in data:
                log("PASS", f"Food order id: {data['id']}")
        except json.JSONDecodeError:
            log("FAIL", f"Response is not valid JSON: {body}")
    else:
        log("FAIL", f"POST /api/orders (food) returned {status}: {body}")


def test_validation():
    """Test that validation works (missing required fields)."""
    print("\n--- Test: Validation ---")
    # Missing 'customer' field
    order = {"item": "Latte", "quantity": 1}
    status, body = request("POST", "/api/orders", body=order)
    if status == 400:
        log("PASS", "Validation correctly rejected request with missing customer (400)")
    elif status in (200, 201, 202):
        # Some frameworks might accept this - check if customer is empty
        log("FAIL", f"Validation did not reject missing customer field, got {status}")
    else:
        log("PASS", f"Request rejected with status {status} (acceptable)")


def test_static_content():
    """Test that static content is served."""
    print("\n--- Test: Static Content ---")
    status, body = request("GET", "/coffeeshopTemplate.html")
    if status == 200:
        log("PASS", "Static content served at /coffeeshopTemplate.html")
    else:
        # Not a critical failure for the migration
        log("FAIL", f"Static content not available: status {status}")


def main():
    global PASS, FAIL

    if not wait_for_ready():
        print("\nFATAL: Application did not start in time")
        sys.exit(1)

    test_health()
    order_id = test_create_order()
    test_get_order(order_id)
    test_create_food_order()
    test_validation()
    test_static_content()

    print(f"\n{'='*50}")
    print(f"Results: {PASS} passed, {FAIL} failed out of {PASS + FAIL} checks")
    print(f"{'='*50}")

    if FAIL > 0:
        sys.exit(1)
    sys.exit(0)


if __name__ == "__main__":
    main()
