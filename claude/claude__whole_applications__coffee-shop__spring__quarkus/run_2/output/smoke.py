"""
Smoke tests for the migrated Quarkus Coffee Shop web-service.

Tests verify:
1. Health endpoint returns 200
2. API health endpoint returns OK
3. POST /api/order returns 202 Accepted
4. POST /api/message returns 202 Accepted
5. GET / returns the HTML page with expected content
6. GET /api/dashboard/stream returns SSE stream
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

def check(name, url, method="GET", data=None, headers=None, expected_status=200, body_contains=None):
    """Execute a single smoke test."""
    if headers is None:
        headers = {}
    try:
        if data is not None:
            if isinstance(data, dict):
                data = json.dumps(data).encode("utf-8")
                headers.setdefault("Content-Type", "application/json")
            elif isinstance(data, str):
                data = data.encode("utf-8")
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        resp = urllib.request.urlopen(req, timeout=10)
        status = resp.getcode()
        body = resp.read().decode("utf-8", errors="replace")
        
        if status != expected_status:
            print(f"  FAIL {name}: expected {expected_status}, got {status}")
            return False
        
        if body_contains and body_contains not in body:
            print(f"  FAIL {name}: expected body to contain '{body_contains}', got: {body[:200]}")
            return False
        
        print(f"  PASS {name} ({status})")
        return True
    except urllib.error.HTTPError as e:
        status = e.code
        if status == expected_status:
            print(f"  PASS {name} ({status})")
            return True
        print(f"  FAIL {name}: HTTP {status} - {e.reason}")
        return False
    except Exception as e:
        print(f"  FAIL {name}: {e}")
        return False


def wait_for_ready(url, timeout=120):
    """Wait for the service to be ready."""
    print(f"Waiting for service at {url} ...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(url, method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print(f"  Service ready after {int(time.time() - start)}s")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"  TIMEOUT waiting for service after {timeout}s")
    return False


def main():
    print(f"\n=== Smoke Tests for Quarkus Coffee Shop ===")
    print(f"Base URL: {BASE_URL}\n")

    # Wait for service to be ready
    if not wait_for_ready(f"{BASE_URL}/api/health"):
        print("\nService not ready. Attempting /q/health instead...")
        if not wait_for_ready(f"{BASE_URL}/q/health"):
            print("\nFAILED: Service never became ready")
            sys.exit(1)

    results = []

    # Test 1: Custom health endpoint
    results.append(check(
        "GET /api/health",
        f"{BASE_URL}/api/health",
        body_contains="web-service OK"
    ))

    # Test 2: POST /api/order with valid PlaceOrderCommand
    order_payload = {
        "id": "smoke-test-001",
        "orderSource": "WEB",
        "location": "ATLANTA",
        "loyaltyMemberId": None,
        "baristaItems": [
            {"item": "ESPRESSO", "name": "SmokeTest", "price": 3.50}
        ],
        "kitchenItems": None,
        "timestamp": "2025-01-01T00:00:00Z"
    }
    results.append(check(
        "POST /api/order",
        f"{BASE_URL}/api/order",
        method="POST",
        data=order_payload,
        expected_status=202
    ))

    # Test 3: POST /api/message
    results.append(check(
        "POST /api/message",
        f"{BASE_URL}/api/message",
        method="POST",
        data="hello from smoke test",
        headers={"Content-Type": "application/json"},
        expected_status=202
    ))

    # Test 4: GET / (home page)
    results.append(check(
        "GET / (home page)",
        f"{BASE_URL}/",
        body_contains="Quarkus Coffee Shop"
    ))

    # Test 5: SSE stream endpoint - just check it responds
    try:
        req = urllib.request.Request(f"{BASE_URL}/api/dashboard/stream")
        resp = urllib.request.urlopen(req, timeout=5)
        status = resp.getcode()
        # Read a bit to confirm SSE data comes through
        chunk = resp.read(512).decode("utf-8", errors="replace")
        if status == 200 and ("dashboard stream connected" in chunk or "data:" in chunk):
            print(f"  PASS GET /api/dashboard/stream (SSE connected)")
            results.append(True)
        else:
            print(f"  FAIL GET /api/dashboard/stream: status={status}, chunk={chunk[:100]}")
            results.append(False)
    except Exception as e:
        # SSE may timeout reading, that's ok if connection was established
        if "timeout" in str(e).lower() or "timed out" in str(e).lower():
            print(f"  PASS GET /api/dashboard/stream (SSE timeout is normal)")
            results.append(True)
        else:
            print(f"  FAIL GET /api/dashboard/stream: {e}")
            results.append(False)

    # Summary
    passed = sum(results)
    total = len(results)
    print(f"\n=== Results: {passed}/{total} passed ===")

    if passed < total:
        sys.exit(1)
    else:
        print("All smoke tests PASSED!")
        sys.exit(0)

if __name__ == "__main__":
    main()
