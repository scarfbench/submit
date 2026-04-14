#!/usr/bin/env python3
"""Smoke tests for the Quarkus Coffee Shop application."""

import json
import os
import sys
import time

import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def wait_for_ready(timeout=60):
    """Wait for the application to become ready."""
    start = time.time()
    while time.time() - start < timeout:
        try:
            r = requests.get(f"{BASE_URL}/q/health/ready", timeout=5)
            if r.status_code == 200:
                data = r.json()
                if data.get("status") == "UP":
                    print("[OK] Application is ready")
                    return True
        except Exception:
            pass
        time.sleep(2)
    print("[FAIL] Application did not become ready within timeout")
    return False


def test_health_live():
    """Test liveness health check."""
    r = requests.get(f"{BASE_URL}/q/health/live", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    data = r.json()
    assert data["status"] == "UP", f"Expected UP, got {data['status']}"
    print("[OK] test_health_live passed")


def test_health_ready():
    """Test readiness health check."""
    r = requests.get(f"{BASE_URL}/q/health/ready", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    data = r.json()
    assert data["status"] == "UP", f"Expected UP, got {data['status']}"
    print("[OK] test_health_ready passed")


def test_barista_status():
    """Test barista status endpoint."""
    r = requests.get(f"{BASE_URL}/api/barista/status", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "barista ok" in r.text, f"Expected 'barista ok' in response, got: {r.text}"
    print("[OK] test_barista_status passed")


def test_kitchen_status():
    """Test kitchen status endpoint."""
    r = requests.get(f"{BASE_URL}/api/kitchen/status", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "kitchen ok" in r.text, f"Expected 'kitchen ok' in response, got: {r.text}"
    print("[OK] test_kitchen_status passed")


def test_place_order():
    """Test placing an order via POST /api/orders."""
    payload = {"customer": "TestUser", "item": "Coffee", "quantity": 1}
    r = requests.post(
        f"{BASE_URL}/api/orders",
        json=payload,
        headers={"Content-Type": "application/json"},
        timeout=10,
    )
    assert r.status_code == 202, f"Expected 202, got {r.status_code}: {r.text}"
    data = r.json()
    assert "id" in data, f"Expected 'id' in response, got: {data}"
    print(f"[OK] test_place_order passed (order id: {data['id']})")
    return data["id"]


def test_get_order(order_id):
    """Test retrieving an order by ID via GET /api/orders/{id}."""
    r = requests.get(f"{BASE_URL}/api/orders/{order_id}", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}: {r.text}"
    data = r.json()
    assert data.get("customer") == "TestUser", f"Expected customer 'TestUser', got: {data}"
    assert data.get("item") == "Coffee", f"Expected item 'Coffee', got: {data}"
    assert data.get("quantity") == 1, f"Expected quantity 1, got: {data}"
    print(f"[OK] test_get_order passed (order: {data})")


def test_get_nonexistent_order():
    """Test that fetching a non-existent order returns 404."""
    r = requests.get(f"{BASE_URL}/api/orders/999999", timeout=10)
    assert r.status_code == 404, f"Expected 404, got {r.status_code}: {r.text}"
    print("[OK] test_get_nonexistent_order passed")


def test_validation_error():
    """Test that invalid order payload returns 400."""
    payload = {"customer": "", "item": "", "quantity": 0}
    r = requests.post(
        f"{BASE_URL}/api/orders",
        json=payload,
        headers={"Content-Type": "application/json"},
        timeout=10,
    )
    # Quarkus returns 400 for validation errors
    assert r.status_code == 400, f"Expected 400, got {r.status_code}: {r.text}"
    print("[OK] test_validation_error passed")


def test_static_page():
    """Test that static HTML page is served."""
    r = requests.get(f"{BASE_URL}/coffeeshopTemplate.html", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "Quarkus Coffee Shop" in r.text, "Expected 'Quarkus Coffee Shop' in HTML"
    print("[OK] test_static_page passed")


def main():
    print(f"Running smoke tests against {BASE_URL}")

    if not wait_for_ready():
        sys.exit(1)

    tests_passed = 0
    tests_failed = 0

    tests = [
        ("test_health_live", lambda: test_health_live()),
        ("test_health_ready", lambda: test_health_ready()),
        ("test_barista_status", lambda: test_barista_status()),
        ("test_kitchen_status", lambda: test_kitchen_status()),
        ("test_static_page", lambda: test_static_page()),
        ("test_validation_error", lambda: test_validation_error()),
        ("test_place_order", lambda: test_place_order()),
        ("test_get_nonexistent_order", lambda: test_get_nonexistent_order()),
    ]

    order_id = None
    for name, fn in tests:
        try:
            result = fn()
            if name == "test_place_order":
                order_id = result
            tests_passed += 1
        except Exception as e:
            print(f"[FAIL] {name}: {e}")
            tests_failed += 1

    # Test get_order only if we have an order_id
    if order_id:
        try:
            test_get_order(order_id)
            tests_passed += 1
        except Exception as e:
            print(f"[FAIL] test_get_order: {e}")
            tests_failed += 1

    print(f"\n=== Results: {tests_passed} passed, {tests_failed} failed ===")

    if tests_failed > 0:
        sys.exit(1)
    print("All smoke tests passed!")


if __name__ == "__main__":
    main()
