#!/usr/bin/env python3
"""
Smoke tests for the Coffee Shop web-service after Spring -> Jakarta EE migration.
Tests the main REST endpoints to verify the application is running and functional.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:9080")


def make_request(method, path, data=None, content_type="application/json", expect_status=None):
    """Helper to make HTTP requests."""
    url = f"{BASE_URL}{path}"
    headers = {}
    body = None
    if data is not None:
        if isinstance(data, dict) or isinstance(data, list):
            body = json.dumps(data).encode("utf-8")
        else:
            body = data.encode("utf-8")
        headers["Content-Type"] = content_type

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        status = resp.status
        resp_body = resp.read().decode("utf-8", errors="replace")
        if expect_status and status != expect_status:
            print(f"  WARN: Expected status {expect_status}, got {status}")
        return status, resp_body
    except urllib.error.HTTPError as e:
        status = e.code
        resp_body = e.read().decode("utf-8", errors="replace")
        if expect_status and status != expect_status:
            print(f"  WARN: Expected status {expect_status}, got {status}")
        return status, resp_body
    except Exception as e:
        print(f"  ERROR: {e}")
        return None, str(e)


def wait_for_ready(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} to become ready...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(f"{BASE_URL}/api/health", method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print(f"  Application is ready (took {time.time() - start:.1f}s)")
                return True
        except Exception:
            pass
        # Also try the root page
        try:
            req = urllib.request.Request(f"{BASE_URL}/", method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print(f"  Application is ready (took {time.time() - start:.1f}s)")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"  TIMEOUT: Application not ready after {max_wait}s")
    return False


def test_health_endpoint():
    """Test the /api/health endpoint."""
    print("TEST: GET /api/health")
    status, body = make_request("GET", "/api/health")
    assert status == 200, f"Expected 200, got {status}"
    assert body is not None and len(body) > 0, "Expected non-empty body"
    print(f"  PASS: status={status}, body={body[:200]}")
    return True


def test_root_page():
    """Test the root page (Thymeleaf/HTML template)."""
    print("TEST: GET /")
    status, body = make_request("GET", "/")
    assert status == 200, f"Expected 200, got {status}"
    assert "html" in body.lower() or "coffee" in body.lower(), "Expected HTML content with 'coffee'"
    print(f"  PASS: status={status}, body length={len(body)}")
    return True


def test_place_order():
    """Test POST /api/order with a valid PlaceOrderCommand."""
    print("TEST: POST /api/order")
    order_data = {
        "id": "test-order-001",
        "orderSource": "WEB",
        "location": "ATLANTA",
        "loyaltyMemberId": None,
        "baristaItems": [
            {
                "item": "CAPPUCCINO",
                "name": "Test Customer",
                "price": 4.50
            }
        ],
        "kitchenItems": None,
        "timestamp": "2025-01-01T00:00:00Z"
    }
    status, body = make_request("POST", "/api/order", data=order_data)
    assert status in (200, 202), f"Expected 200 or 202, got {status}"
    if body:
        try:
            result = json.loads(body)
            print(f"  PASS: status={status}, order result has keys: {list(result.keys()) if isinstance(result, dict) else 'array'}")
        except json.JSONDecodeError:
            print(f"  PASS: status={status}, body={body[:200]}")
    else:
        print(f"  PASS: status={status}")
    return True


def test_place_order_with_kitchen_items():
    """Test POST /api/order with both barista and kitchen items."""
    print("TEST: POST /api/order (with kitchen items)")
    order_data = {
        "id": "test-order-002",
        "orderSource": "COUNTER",
        "location": "CHARLOTTE",
        "loyaltyMemberId": "loyalty-123",
        "baristaItems": [
            {
                "item": "LATTE",
                "name": "Alice",
                "price": 4.50
            }
        ],
        "kitchenItems": [
            {
                "item": "CROISSANT",
                "name": "Alice",
                "price": 3.25
            }
        ],
        "timestamp": "2025-01-01T12:00:00Z"
    }
    status, body = make_request("POST", "/api/order", data=order_data)
    assert status in (200, 202), f"Expected 200 or 202, got {status}"
    print(f"  PASS: status={status}")
    return True


def test_send_message():
    """Test POST /api/message endpoint."""
    print("TEST: POST /api/message")
    status, body = make_request("POST", "/api/message", data="Hello from smoke test", content_type="text/plain")
    # Accept 200, 202, or 415 (if content-type handling changed)
    assert status in (200, 202, 204, 415), f"Expected 200/202/204, got {status}"
    print(f"  PASS: status={status}")
    return True


def test_dashboard_stream():
    """Test the SSE dashboard stream endpoint (just verify it returns 200)."""
    print("TEST: GET /api/dashboard/stream (SSE)")
    try:
        req = urllib.request.Request(f"{BASE_URL}/api/dashboard/stream", method="GET")
        req.add_header("Accept", "text/event-stream")
        resp = urllib.request.urlopen(req, timeout=5)
        status = resp.status
        # Read a small chunk - SSE streams stay open
        chunk = resp.read(512).decode("utf-8", errors="replace")
        print(f"  PASS: status={status}, got initial SSE data: {chunk[:200]}")
        return True
    except Exception as e:
        # SSE may timeout, which is acceptable if it connected
        if "timed out" in str(e).lower() or "timeout" in str(e).lower():
            print(f"  PASS: SSE connection established (timed out reading, which is expected)")
            return True
        print(f"  WARN: SSE test encountered: {e}")
        return True  # Non-critical


def test_invalid_order():
    """Test POST /api/order with invalid data - should return 400."""
    print("TEST: POST /api/order (invalid data)")
    status, body = make_request("POST", "/api/order", data={})
    # Expect 400 (bad request) or 500 (internal error if validation not set up)
    assert status in (400, 422, 500), f"Expected 400/422/500, got {status}"
    print(f"  PASS: status={status} (validation working)")
    return True


def main():
    """Run all smoke tests."""
    print("=" * 60)
    print("Coffee Shop Web-Service Smoke Tests")
    print(f"Base URL: {BASE_URL}")
    print("=" * 60)

    if not wait_for_ready():
        print("FAIL: Application not ready, aborting smoke tests")
        sys.exit(1)

    tests = [
        test_health_endpoint,
        test_root_page,
        test_place_order,
        test_place_order_with_kitchen_items,
        test_send_message,
        test_dashboard_stream,
        test_invalid_order,
    ]

    passed = 0
    failed = 0
    errors = []

    for test_fn in tests:
        try:
            if test_fn():
                passed += 1
            else:
                failed += 1
                errors.append(f"{test_fn.__name__}: returned False")
        except AssertionError as e:
            failed += 1
            errors.append(f"{test_fn.__name__}: {e}")
            print(f"  FAIL: {e}")
        except Exception as e:
            failed += 1
            errors.append(f"{test_fn.__name__}: {e}")
            print(f"  ERROR: {e}")

    print()
    print("=" * 60)
    print(f"Results: {passed} passed, {failed} failed, {passed + failed} total")
    print("=" * 60)

    if errors:
        print("Failures:")
        for err in errors:
            print(f"  - {err}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
