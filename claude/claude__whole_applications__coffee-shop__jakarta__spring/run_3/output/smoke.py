"""
Smoke tests for the Coffee Shop application after migration from Jakarta EE to Spring Boot.
Tests verify that the orders-service REST API and health endpoints work correctly.
"""

import json
import os
import time
import urllib.request
import urllib.error
import sys


BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def make_request(method, path, data=None, headers=None):
    """Make an HTTP request and return (status_code, response_body)."""
    url = f"{BASE_URL}{path}"
    if headers is None:
        headers = {}
    if data is not None:
        data = json.dumps(data).encode("utf-8")
        headers.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8") if e.fp else ""
        return e.code, body
    except urllib.error.URLError as e:
        return 0, str(e)


def wait_for_ready(timeout=120):
    """Wait for the application to be ready by polling the actuator health endpoint."""
    start = time.time()
    while time.time() - start < timeout:
        try:
            status, body = make_request("GET", "/actuator/health")
            if status == 200:
                print(f"[PASS] Application ready after {time.time() - start:.1f}s")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"[FAIL] Application not ready after {timeout}s")
    return False


def test_actuator_health():
    """Test that the Spring Boot Actuator health endpoint responds."""
    status, body = make_request("GET", "/actuator/health")
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert data.get("status") == "UP", f"Expected UP status, got: {data}"
    print("[PASS] test_actuator_health")


def test_actuator_info():
    """Test that the actuator info endpoint responds."""
    status, body = make_request("GET", "/actuator/info")
    assert status == 200, f"Expected 200, got {status}: {body}"
    print("[PASS] test_actuator_info")


def test_create_order():
    """Test creating a new order via POST /api/orders."""
    order = {"customer": "SmokeTestUser", "item": "coffee latte", "quantity": 2}
    status, body = make_request("POST", "/api/orders", data=order)
    assert status == 202, f"Expected 202 Accepted, got {status}: {body}"
    data = json.loads(body)
    assert "id" in data, f"Expected 'id' in response, got: {data}"
    order_id = data["id"]
    print(f"[PASS] test_create_order (id={order_id})")
    return order_id


def test_get_order(order_id):
    """Test retrieving an order by ID via GET /api/orders/{id}."""
    status, body = make_request("GET", f"/api/orders/{order_id}")
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert str(data.get("id")) == str(order_id), f"Expected id={order_id}, got: {data}"
    assert data.get("customer") == "SmokeTestUser", f"Expected customer SmokeTestUser, got: {data}"
    assert data.get("item") == "coffee latte", f"Expected item coffee latte, got: {data}"
    assert data.get("quantity") == 2, f"Expected quantity 2, got: {data}"
    assert data.get("status") == "PLACED", f"Expected status PLACED, got: {data}"
    print(f"[PASS] test_get_order (id={order_id})")


def test_get_nonexistent_order():
    """Test retrieving a non-existent order returns 404."""
    status, body = make_request("GET", "/api/orders/999999")
    assert status == 404, f"Expected 404, got {status}: {body}"
    print("[PASS] test_get_nonexistent_order")


def test_create_kitchen_order():
    """Test creating an order that routes to kitchen (non-drink item)."""
    order = {"customer": "KitchenUser", "item": "CROISSANT", "quantity": 1}
    status, body = make_request("POST", "/api/orders", data=order)
    assert status == 202, f"Expected 202 Accepted, got {status}: {body}"
    data = json.loads(body)
    assert "id" in data, f"Expected 'id' in response, got: {data}"
    print(f"[PASS] test_create_kitchen_order (id={data['id']})")


def test_static_content():
    """Test that the static HTML page is served."""
    status, body = make_request("GET", "/index.html")
    assert status == 200, f"Expected 200, got {status}: {body}"
    assert "Coffee Shop" in body or "coffee" in body.lower(), f"Expected coffee shop content in HTML"
    print("[PASS] test_static_content")


def test_validation_error():
    """Test that invalid order returns validation error."""
    order = {"customer": "", "item": "", "quantity": 0}
    status, body = make_request("POST", "/api/orders", data=order)
    assert status == 400, f"Expected 400, got {status}: {body}"
    print("[PASS] test_validation_error")


def main():
    print(f"Running smoke tests against {BASE_URL}")
    print("=" * 60)

    # Wait for application readiness
    if not wait_for_ready():
        sys.exit(1)

    passed = 0
    failed = 0
    errors = []

    tests = [
        ("test_actuator_health", lambda: test_actuator_health()),
        ("test_actuator_info", lambda: test_actuator_info()),
        ("test_create_order", None),  # handled specially
        ("test_get_nonexistent_order", lambda: test_get_nonexistent_order()),
        ("test_create_kitchen_order", lambda: test_create_kitchen_order()),
        ("test_static_content", lambda: test_static_content()),
        ("test_validation_error", lambda: test_validation_error()),
    ]

    for name, fn in tests:
        if name == "test_create_order":
            try:
                order_id = test_create_order()
                passed += 1
                try:
                    test_get_order(order_id)
                    passed += 1
                except AssertionError as e:
                    failed += 1
                    errors.append(f"test_get_order: {e}")
                    print(f"[FAIL] test_get_order: {e}")
                except Exception as e:
                    failed += 1
                    errors.append(f"test_get_order: {e}")
                    print(f"[FAIL] test_get_order: {e}")
            except AssertionError as e:
                failed += 1
                errors.append(f"{name}: {e}")
                print(f"[FAIL] {name}: {e}")
            except Exception as e:
                failed += 1
                errors.append(f"{name}: {e}")
                print(f"[FAIL] {name}: {e}")
        else:
            try:
                fn()
                passed += 1
            except AssertionError as e:
                failed += 1
                errors.append(f"{name}: {e}")
                print(f"[FAIL] {name}: {e}")
            except Exception as e:
                failed += 1
                errors.append(f"{name}: {e}")
                print(f"[FAIL] {name}: {e}")

    print("=" * 60)
    print(f"Results: {passed} passed, {failed} failed out of {passed + failed} tests")

    if errors:
        print("\nErrors:")
        for e in errors:
            print(f"  - {e}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
