#!/usr/bin/env python3
"""
Smoke tests for the Coffee Shop orders-service (Spring Boot).
Run with: pytest smoke.py -v
Expects BASE_URL env var (default http://localhost:8080).
"""
import os
import time
import json
import pytest
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080").rstrip("/")


def http_request(method, path, data=None, headers=None):
    """Simple HTTP helper using urllib."""
    url = BASE_URL + path
    if headers is None:
        headers = {}
    if data is not None:
        data = json.dumps(data).encode("utf-8")
        headers.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req, timeout=30)
        body = resp.read().decode("utf-8")
        return resp.status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8") if e.fp else ""
        return e.code, body


def wait_for_ready(max_wait=120):
    """Wait for the application to be ready."""
    start = time.time()
    while time.time() - start < max_wait:
        try:
            status, body = http_request("GET", "/actuator/health")
            if status == 200:
                data = json.loads(body)
                if data.get("status") == "UP":
                    return True
        except Exception:
            pass
        time.sleep(2)
    return False


@pytest.fixture(scope="session", autouse=True)
def ensure_app_ready():
    """Ensure the application is ready before running tests."""
    assert wait_for_ready(120), f"Application at {BASE_URL} did not become ready within 120s"


class TestHealthEndpoints:
    def test_actuator_health(self):
        """Test Spring Boot Actuator health endpoint."""
        status, body = http_request("GET", "/actuator/health")
        assert status == 200
        data = json.loads(body)
        assert data["status"] == "UP"

    def test_actuator_info(self):
        """Test Spring Boot Actuator info endpoint."""
        status, body = http_request("GET", "/actuator/info")
        assert status == 200


class TestOrdersAPI:
    def test_create_order_coffee(self):
        """Test creating a coffee order (should route to barista)."""
        order = {"customer": "Alice", "item": "Latte", "quantity": 1}
        status, body = http_request("POST", "/api/orders", data=order)
        assert status == 202, f"Expected 202, got {status}: {body}"
        data = json.loads(body)
        assert "id" in data
        assert data["id"] is not None

    def test_create_order_food(self):
        """Test creating a food order (should route to kitchen)."""
        order = {"customer": "Bob", "item": "Croissant", "quantity": 2}
        status, body = http_request("POST", "/api/orders", data=order)
        assert status == 202, f"Expected 202, got {status}: {body}"
        data = json.loads(body)
        assert "id" in data

    def test_get_order(self):
        """Test retrieving an order by ID."""
        # First create an order
        order = {"customer": "Charlie", "item": "Espresso", "quantity": 1}
        status, body = http_request("POST", "/api/orders", data=order)
        assert status == 202
        data = json.loads(body)
        order_id = data["id"]

        # Then retrieve it
        status, body = http_request("GET", f"/api/orders/{order_id}")
        assert status == 200, f"Expected 200, got {status}: {body}"
        data = json.loads(body)
        assert data["customer"] == "Charlie"
        assert data["item"] == "Espresso"
        assert data["quantity"] == 1

    def test_get_nonexistent_order(self):
        """Test retrieving a non-existent order."""
        status, body = http_request("GET", "/api/orders/999999")
        assert status == 404

    def test_create_order_validation_missing_customer(self):
        """Test that validation rejects missing customer."""
        order = {"customer": "", "item": "Coffee", "quantity": 1}
        status, body = http_request("POST", "/api/orders", data=order)
        assert status == 400, f"Expected 400, got {status}: {body}"

    def test_create_order_validation_missing_item(self):
        """Test that validation rejects missing item."""
        order = {"customer": "Dan", "item": "", "quantity": 1}
        status, body = http_request("POST", "/api/orders", data=order)
        assert status == 400, f"Expected 400, got {status}: {body}"

    def test_create_order_validation_zero_quantity(self):
        """Test that validation rejects zero quantity."""
        order = {"customer": "Eve", "item": "Mocha", "quantity": 0}
        status, body = http_request("POST", "/api/orders", data=order)
        assert status == 400, f"Expected 400, got {status}: {body}"


class TestStaticContent:
    def test_index_html(self):
        """Test that the static index page is served."""
        status, body = http_request("GET", "/index.html")
        assert status == 200
        assert "Coffee Shop" in body or "coffee" in body.lower()


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
