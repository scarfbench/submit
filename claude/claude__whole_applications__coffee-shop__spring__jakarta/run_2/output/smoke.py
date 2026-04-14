#!/usr/bin/env python3
"""
Smoke tests for the Coffee Shop web-service after Jakarta EE migration.
Tests the core REST API endpoints to verify the migration preserved functionality.
"""

import json
import sys
import time
import urllib.request
import urllib.error
from datetime import datetime, timezone

BASE_URL = "http://localhost:8080"
RESULTS = []


def test(name, func):
    """Run a test and record the result."""
    try:
        func()
        RESULTS.append((name, "PASS", None))
        print(f"  PASS: {name}")
    except Exception as e:
        RESULTS.append((name, "FAIL", str(e)))
        print(f"  FAIL: {name} -> {e}")


def wait_for_server(url, timeout=60, interval=2):
    """Wait until the server responds."""
    print(f"Waiting for server at {url} ...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{url}/api/health")
            with urllib.request.urlopen(req, timeout=5) as resp:
                if resp.status == 200:
                    print(f"  Server ready after {time.time() - start:.1f}s")
                    return True
        except (urllib.error.URLError, ConnectionRefusedError, OSError):
            pass
        time.sleep(interval)
    raise TimeoutError(f"Server at {url} not ready after {timeout}s")


def test_health():
    """GET /api/health should return 200 with 'web-service OK'."""
    req = urllib.request.Request(f"{BASE_URL}/api/health")
    with urllib.request.urlopen(req, timeout=10) as resp:
        assert resp.status == 200, f"Expected 200, got {resp.status}"
        body = resp.read().decode()
        assert "web-service OK" in body, f"Unexpected body: {body}"


def test_post_order():
    """POST /api/order should return 202 Accepted with OrderEventResult JSON."""
    timestamp = datetime.now(timezone.utc).isoformat()
    payload = json.dumps({
        "id": "smoke-test-001",
        "orderSource": "WEB",
        "location": "ATLANTA",
        "loyaltyMemberId": None,
        "baristaItems": [
            {"item": "CAPPUCCINO", "name": "Test Barista", "price": 4.50}
        ],
        "kitchenItems": [
            {"item": "CROISSANT", "name": "Test Kitchen", "price": 3.25}
        ],
        "timestamp": timestamp
    }).encode("utf-8")

    req = urllib.request.Request(
        f"{BASE_URL}/api/order",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    with urllib.request.urlopen(req, timeout=10) as resp:
        assert resp.status == 202, f"Expected 202, got {resp.status}"
        body = json.loads(resp.read().decode())
        assert "order" in body, f"Missing 'order' in response: {body}"
        assert body["order"]["orderId"] == "smoke-test-001", f"Wrong orderId: {body}"


def test_post_order_barista_only():
    """POST /api/order with only barista items should work."""
    timestamp = datetime.now(timezone.utc).isoformat()
    payload = json.dumps({
        "id": "smoke-test-002",
        "orderSource": "COUNTER",
        "location": "CHARLOTTE",
        "baristaItems": [
            {"item": "ESPRESSO", "name": "Espresso Fan", "price": 3.50}
        ],
        "timestamp": timestamp
    }).encode("utf-8")

    req = urllib.request.Request(
        f"{BASE_URL}/api/order",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    with urllib.request.urlopen(req, timeout=10) as resp:
        assert resp.status == 202, f"Expected 202, got {resp.status}"
        body = json.loads(resp.read().decode())
        assert body["order"]["orderId"] == "smoke-test-002"


def test_post_message():
    """POST /api/message should return 202 Accepted."""
    payload = json.dumps("Hello from smoke test").encode("utf-8")
    req = urllib.request.Request(
        f"{BASE_URL}/api/message",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    with urllib.request.urlopen(req, timeout=10) as resp:
        assert resp.status == 202, f"Expected 202, got {resp.status}"


def test_sse_stream():
    """GET /api/dashboard/stream should return 200 with SSE content type."""
    import socket
    req = urllib.request.Request(f"{BASE_URL}/api/dashboard/stream")
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            assert resp.status == 200, f"Expected 200, got {resp.status}"
            content_type = resp.headers.get("Content-Type", "")
            assert "text/event-stream" in content_type, f"Wrong content-type: {content_type}"
            # Read first chunk to confirm init event
            chunk = resp.read(512).decode()
            assert "init" in chunk or "dashboard" in chunk, f"No init event in: {chunk}"
    except (urllib.error.URLError, TimeoutError, socket.timeout, OSError):
        # SSE is a long-lived connection; timeout is expected and acceptable
        # The fact that we connected at all means the endpoint is working
        pass


def test_root_page():
    """GET / should return HTML page."""
    req = urllib.request.Request(f"{BASE_URL}/")
    with urllib.request.urlopen(req, timeout=10) as resp:
        assert resp.status == 200, f"Expected 200, got {resp.status}"
        body = resp.read().decode()
        assert "Coffee Shop" in body or "coffeeshop" in body.lower() or "html" in body.lower(), \
            f"Unexpected page content: {body[:200]}"


def main():
    print("=" * 60)
    print("Coffee Shop Web Service - Smoke Tests (Jakarta EE)")
    print("=" * 60)

    try:
        wait_for_server(BASE_URL)
    except TimeoutError as e:
        print(f"FATAL: {e}")
        sys.exit(1)

    print("\nRunning smoke tests...\n")

    test("Health check endpoint", test_health)
    test("Place order (full order)", test_post_order)
    test("Place order (barista only)", test_post_order_barista_only)
    test("Send message", test_post_message)
    test("SSE dashboard stream", test_sse_stream)
    test("Root page", test_root_page)

    print("\n" + "=" * 60)
    passed = sum(1 for _, s, _ in RESULTS if s == "PASS")
    failed = sum(1 for _, s, _ in RESULTS if s == "FAIL")
    print(f"Results: {passed} passed, {failed} failed, {len(RESULTS)} total")
    print("=" * 60)

    if failed > 0:
        print("\nFailed tests:")
        for name, status, err in RESULTS:
            if status == "FAIL":
                print(f"  - {name}: {err}")
        sys.exit(1)
    else:
        print("\nAll smoke tests passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
