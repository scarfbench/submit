#!/usr/bin/env python3
"""Smoke tests for the Coffee Shop orders-service (Spring Boot)."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def http_get(path, expected_status=200):
    """Perform an HTTP GET and return (status_code, body_string)."""
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url, method="GET")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except Exception as e:
        return 0, str(e)


def http_post_json(path, payload, expected_status=202):
    """Perform an HTTP POST with JSON body and return (status_code, body_string)."""
    url = f"{BASE_URL}{path}"
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url, data=data, method="POST",
        headers={"Content-Type": "application/json", "Accept": "application/json"}
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except Exception as e:
        return 0, str(e)


def wait_for_service(max_wait=60):
    """Wait until the service is up."""
    print(f"Waiting for service at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            status, body = http_get("/actuator/health")
            if status == 200:
                print(f"  Service is UP (took {time.time()-start:.1f}s)")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"  Service did not start within {max_wait}s")
    return False


def test_health_endpoint():
    """Test that the health actuator endpoint returns UP."""
    status, body = http_get("/actuator/health")
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert data.get("status") == "UP", f"Expected status UP, got: {data}"
    print("  PASS: /actuator/health returns UP")


def test_static_html():
    """Test that the static HTML page is served."""
    status, body = http_get("/index.html")
    assert status == 200, f"Expected 200, got {status}"
    assert "Coffee Shop" in body or "coffee" in body.lower(), \
        f"Expected 'Coffee Shop' in HTML body"
    print("  PASS: /index.html serves static content")


def test_create_order_drink():
    """Test creating a drink order via POST /api/orders."""
    payload = {"customer": "Alice", "item": "Latte", "quantity": 1}
    status, body = http_post_json("/api/orders", payload)
    assert status == 202, f"Expected 202, got {status}: {body}"
    data = json.loads(body)
    assert "id" in data, f"Expected 'id' in response: {data}"
    print(f"  PASS: POST /api/orders (drink) returned 202, id={data['id']}")
    return data["id"]


def test_create_order_food():
    """Test creating a food order via POST /api/orders."""
    payload = {"customer": "Bob", "item": "Croissant", "quantity": 2}
    status, body = http_post_json("/api/orders", payload)
    assert status == 202, f"Expected 202, got {status}: {body}"
    data = json.loads(body)
    assert "id" in data, f"Expected 'id' in response: {data}"
    print(f"  PASS: POST /api/orders (food) returned 202, id={data['id']}")
    return data["id"]


def test_get_order(order_id):
    """Test retrieving an order by ID."""
    status, body = http_get(f"/api/orders/{order_id}")
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert data.get("id") == int(order_id), f"Expected id={order_id}, got: {data}"
    assert data.get("status") == "PLACED", f"Expected status PLACED, got: {data}"
    print(f"  PASS: GET /api/orders/{order_id} returns correct order")


def test_get_nonexistent_order():
    """Test that getting a non-existent order returns 404."""
    status, body = http_get("/api/orders/99999")
    assert status == 404, f"Expected 404, got {status}: {body}"
    print("  PASS: GET /api/orders/99999 returns 404")


def test_invalid_order():
    """Test that an invalid order returns 400."""
    payload = {"customer": "", "item": "", "quantity": 0}
    status, body = http_post_json("/api/orders", payload)
    assert status == 400, f"Expected 400, got {status}: {body}"
    print("  PASS: POST /api/orders with invalid data returns 400")


def main():
    """Run all smoke tests."""
    print(f"\n{'='*60}")
    print(f"Coffee Shop Spring Boot Smoke Tests")
    print(f"Base URL: {BASE_URL}")
    print(f"{'='*60}\n")

    if not wait_for_service():
        print("FAIL: Service did not start")
        sys.exit(1)

    tests = [
        ("Health endpoint", test_health_endpoint),
        ("Static HTML", test_static_html),
        ("Create drink order", test_create_order_drink),
        ("Create food order", test_create_order_food),
        ("Get nonexistent order", test_get_nonexistent_order),
        ("Invalid order validation", test_invalid_order),
    ]

    passed = 0
    failed = 0
    errors = []

    for name, test_fn in tests:
        try:
            print(f"\nTest: {name}")
            result = test_fn()
            # If the test returns an order_id, test retrieval
            if name == "Create drink order" and result:
                print(f"\nTest: Get drink order")
                test_get_order(result)
                passed += 1
            if name == "Create food order" and result:
                print(f"\nTest: Get food order")
                test_get_order(result)
                passed += 1
            passed += 1
        except AssertionError as e:
            failed += 1
            errors.append(f"{name}: {e}")
            print(f"  FAIL: {e}")
        except Exception as e:
            failed += 1
            errors.append(f"{name}: {e}")
            print(f"  ERROR: {e}")

    print(f"\n{'='*60}")
    print(f"Results: {passed} passed, {failed} failed")
    if errors:
        print("\nFailures:")
        for err in errors:
            print(f"  - {err}")
    print(f"{'='*60}\n")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
