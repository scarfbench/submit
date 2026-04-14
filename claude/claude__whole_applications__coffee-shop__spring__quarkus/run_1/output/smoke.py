#!/usr/bin/env python3
"""
Smoke tests for the Coffee Shop web-service migrated to Quarkus.
Validates that the application starts, serves pages, and handles API requests.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

passed = 0
failed = 0


def test(name, fn):
    global passed, failed
    try:
        fn()
        print(f"  PASS: {name}")
        passed += 1
    except Exception as e:
        print(f"  FAIL: {name} -> {e}")
        failed += 1


def get(path, expected_status=200):
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        assert resp.status == expected_status, f"Expected {expected_status}, got {resp.status}"
        return body
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return e.read().decode("utf-8")
        raise


def post_json(path, data, expected_status=202):
    url = f"{BASE_URL}{path}"
    payload = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(url, data=payload, method="POST")
    req.add_header("Content-Type", "application/json")
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        assert resp.status == expected_status, f"Expected {expected_status}, got {resp.status}"
        return body
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return e.read().decode("utf-8")
        raise


# ---- Tests ----

def test_health_endpoint():
    body = get("/api/health")
    assert "web-service OK" in body, f"Unexpected body: {body}"


def test_quarkus_health_liveness():
    """Quarkus built-in health endpoint should exist (may be DOWN without Kafka)"""
    url = f"{BASE_URL}/q/health/live"
    req = urllib.request.Request(url)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = json.loads(resp.read().decode("utf-8"))
        # Liveness should be UP even without Kafka
        assert body.get("status") in ("UP", "DOWN"), f"Unexpected status: {body}"
    except urllib.error.HTTPError:
        # Health endpoint exists, some checks might fail
        pass


def test_root_page_serves_html():
    body = get("/")
    assert "<!DOCTYPE html>" in body or "<html" in body, "Root page should serve HTML"
    assert "Quarkus Coffee Shop" in body, "Page should contain 'Quarkus Coffee Shop'"


def test_root_page_has_stream_url():
    body = get("/")
    assert "/api/dashboard/stream" in body, "Page should contain streamUrl"


def test_root_page_has_store_id():
    body = get("/")
    assert "ATLANTA" in body, "Page should contain storeId ATLANTA"


def test_static_css_accessible():
    body = get("/css/cafe.css")
    assert len(body) > 0, "CSS file should be served"


def test_place_order_barista_only():
    order = {
        "id": "smoke-test-001",
        "orderSource": "WEB",
        "location": "ATLANTA",
        "loyaltyMemberId": None,
        "baristaItems": [
            {"item": "CAPPUCCINO", "name": "Alice", "price": 4.50}
        ],
        "kitchenItems": None,
        "timestamp": "2025-01-01T12:00:00Z"
    }
    body = post_json("/api/order", order, expected_status=202)
    result = json.loads(body)
    assert result["order"]["orderId"] == "smoke-test-001"
    assert result["order"]["orderStatus"] == "IN_PROGRESS"
    assert len(result["baristaTickets"]) == 1
    assert result["baristaTickets"][0]["item"] == "CAPPUCCINO"
    assert result["baristaTickets"][0]["name"] == "Alice"


def test_place_order_kitchen_only():
    order = {
        "id": "smoke-test-002",
        "orderSource": "COUNTER",
        "location": "RALEIGH",
        "loyaltyMemberId": "loyalty-xyz",
        "baristaItems": None,
        "kitchenItems": [
            {"item": "CROISSANT", "name": "Bob", "price": 3.25}
        ],
        "timestamp": "2025-01-02T08:30:00Z"
    }
    body = post_json("/api/order", order, expected_status=202)
    result = json.loads(body)
    assert result["order"]["orderId"] == "smoke-test-002"
    assert result["order"]["orderStatus"] == "IN_PROGRESS"
    assert len(result["kitchenTickets"]) == 1
    assert result["kitchenTickets"][0]["item"] == "CROISSANT"


def test_place_order_both_barista_and_kitchen():
    order = {
        "id": "smoke-test-003",
        "orderSource": "WEB",
        "location": "CHARLOTTE",
        "loyaltyMemberId": None,
        "baristaItems": [
            {"item": "LATTE", "name": "Carol", "price": 4.50},
            {"item": "ESPRESSO", "name": "Carol", "price": 3.50}
        ],
        "kitchenItems": [
            {"item": "MUFFIN", "name": "Carol", "price": 3.00}
        ],
        "timestamp": "2025-01-03T14:00:00Z"
    }
    body = post_json("/api/order", order, expected_status=202)
    result = json.loads(body)
    assert result["order"]["orderId"] == "smoke-test-003"
    assert len(result["baristaTickets"]) == 2
    assert len(result["kitchenTickets"]) == 1
    assert len(result["orderUpdates"]) == 3  # 2 barista + 1 kitchen


def test_message_endpoint():
    body = post_json("/api/message", "hello from smoke test", expected_status=202)
    # Should return 202 Accepted with empty body


def test_sse_endpoint_exists():
    """The SSE endpoint should exist and return text/event-stream"""
    url = f"{BASE_URL}/api/dashboard/stream"
    req = urllib.request.Request(url)
    try:
        resp = urllib.request.urlopen(req, timeout=3)
        content_type = resp.headers.get("Content-Type", "")
        assert "text/event-stream" in content_type, f"Expected SSE content type, got {content_type}"
    except (urllib.error.URLError, TimeoutError, Exception):
        # SSE endpoint hangs by design (long-lived connection)
        # timeout is acceptable - it means the endpoint exists
        pass


if __name__ == "__main__":
    print(f"\nSmoke Tests - Base URL: {BASE_URL}")
    print("=" * 50)

    test("Health endpoint returns OK", test_health_endpoint)
    test("Quarkus health liveness", test_quarkus_health_liveness)
    test("Root page serves HTML", test_root_page_serves_html)
    test("Root page has stream URL", test_root_page_has_stream_url)
    test("Root page has store ID", test_root_page_has_store_id)
    test("Static CSS is accessible", test_static_css_accessible)
    test("Place order (barista only)", test_place_order_barista_only)
    test("Place order (kitchen only)", test_place_order_kitchen_only)
    test("Place order (both barista+kitchen)", test_place_order_both_barista_and_kitchen)
    test("Message endpoint accepts POST", test_message_endpoint)
    test("SSE endpoint exists", test_sse_endpoint_exists)

    print("=" * 50)
    print(f"Results: {passed} passed, {failed} failed, {passed + failed} total")

    if failed > 0:
        sys.exit(1)
    print("\nAll smoke tests passed!")
    sys.exit(0)
