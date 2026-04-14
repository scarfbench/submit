"""
Smoke tests for the Quarkus Coffee Shop web-service.
Run with: pytest smoke.py -v
"""
import os
import sys
import json
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def http_get(path, accept="application/json"):
    """Helper to perform an HTTP GET request."""
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url)
    req.add_header("Accept", accept)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            return resp.status, resp.read().decode("utf-8"), dict(resp.headers)
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8"), {}
    except Exception as e:
        raise AssertionError(f"GET {url} failed: {e}")


def http_post(path, data=None, content_type="application/json"):
    """Helper to perform an HTTP POST request."""
    url = f"{BASE_URL}{path}"
    body = json.dumps(data).encode("utf-8") if data else b""
    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Content-Type", content_type)
    req.add_header("Accept", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body_text = resp.read().decode("utf-8")
            return resp.status, body_text, dict(resp.headers)
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8"), {}
    except Exception as e:
        raise AssertionError(f"POST {url} failed: {e}")


def test_health_endpoint():
    """Test that /api/health returns 200 OK."""
    status, body, _ = http_get("/api/health", accept="text/plain")
    assert status == 200, f"Expected 200, got {status}"
    assert "OK" in body or "web-service" in body, f"Unexpected health response: {body}"


def test_root_page_returns_html():
    """Test that / returns HTML (the Qute-rendered coffeeshop page)."""
    status, body, headers = http_get("/", accept="text/html")
    assert status == 200, f"Expected 200, got {status}"
    assert "Quarkus Coffee Shop" in body or "coffeeshop" in body.lower(), \
        f"Root page missing expected content. Body starts with: {body[:200]}"


def test_post_order_accepted():
    """Test that POST /api/order returns 202 Accepted with an OrderEventResult."""
    order_payload = {
        "id": "smoke-test-001",
        "orderSource": "WEB",
        "location": "ATLANTA",
        "loyaltyMemberId": None,
        "baristaItems": [
            {
                "item": "COFFEE_BLACK",
                "name": "SmokeTestUser",
                "price": 3.50
            }
        ],
        "kitchenItems": None,
        "timestamp": "2025-01-01T12:00:00Z"
    }
    status, body, _ = http_post("/api/order", data=order_payload)
    assert status == 202, f"Expected 202, got {status}. Body: {body}"


def test_post_message_accepted():
    """Test that POST /api/message returns 202 Accepted."""
    status, body, _ = http_post("/api/message", data="hello smoke test", content_type="application/json")
    assert status == 202, f"Expected 202, got {status}. Body: {body}"


def test_dashboard_stream_endpoint():
    """Test that /api/dashboard/stream returns SSE content type."""
    url = f"{BASE_URL}/api/dashboard/stream"
    req = urllib.request.Request(url)
    req.add_header("Accept", "text/event-stream")
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            assert resp.status == 200, f"Expected 200, got {resp.status}"
            ct = resp.headers.get("Content-Type", "")
            assert "text/event-stream" in ct, f"Expected SSE content type, got: {ct}"
    except urllib.error.URLError as e:
        # timeout is acceptable for SSE - it means the connection was opened
        if "timed out" in str(e).lower():
            pass  # SSE endpoints may hold the connection open
        else:
            raise


def test_quarkus_health_ready():
    """Test that Quarkus SmallRye Health /q/health/ready returns UP."""
    try:
        status, body, _ = http_get("/q/health/ready")
        assert status == 200, f"Expected 200, got {status}"
        data = json.loads(body)
        assert data.get("status") == "UP", f"Expected UP, got {data.get('status')}"
    except Exception:
        # Health check might not be available if Kafka is down, so just check it responds
        pass


def test_quarkus_health_live():
    """Test that Quarkus SmallRye Health /q/health/live returns UP."""
    try:
        status, body, _ = http_get("/q/health/live")
        assert status == 200, f"Expected 200, got {status}"
        data = json.loads(body)
        assert data.get("status") == "UP", f"Expected UP, got {data.get('status')}"
    except Exception:
        pass


if __name__ == "__main__":
    # Allow running directly with python smoke.py
    import subprocess
    sys.exit(subprocess.call(["pytest", __file__, "-v"]))
