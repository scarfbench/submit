#!/usr/bin/env python3
"""Smoke tests for the CargoTracker Spring Boot application."""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
CONTEXT = "/cargo-tracker"


def url(path):
    return f"{BASE_URL}{CONTEXT}{path}"


def get_json(path, expected_status=200):
    """Make a GET request and return parsed JSON."""
    req = urllib.request.Request(url(path))
    req.add_header("Accept", "application/json")
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        assert resp.status == expected_status, f"Expected {expected_status}, got {resp.status}"
        body = resp.read().decode("utf-8")
        return json.loads(body) if body else None
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return None
        raise


def post_json(path, data, expected_status=200):
    """Make a POST request with JSON body."""
    body = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(url(path), data=body, method="POST")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        assert resp.status == expected_status, f"Expected {expected_status}, got {resp.status}"
        content = resp.read().decode("utf-8")
        return json.loads(content) if content else None
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return None
        raise


def test_health():
    """Test that the app health/actuator endpoint is reachable."""
    try:
        req = urllib.request.Request(url("/actuator/health"))
        resp = urllib.request.urlopen(req, timeout=15)
        assert resp.status == 200, f"Health check failed with status {resp.status}"
        data = json.loads(resp.read().decode("utf-8"))
        assert data.get("status") == "UP", f"Health status is not UP: {data}"
        print("PASS: Health endpoint returns UP")
    except Exception as e:
        print(f"FAIL: Health endpoint - {e}")
        return False
    return True


def test_list_locations():
    """Test listing all shipping locations."""
    try:
        data = get_json("/rest/booking/locations")
        assert isinstance(data, list), f"Expected list, got {type(data)}"
        assert len(data) > 0, "Expected at least one location"
        print(f"PASS: List locations returned {len(data)} locations")
    except Exception as e:
        print(f"FAIL: List locations - {e}")
        return False
    return True


def test_list_cargos():
    """Test listing all cargos."""
    try:
        data = get_json("/rest/booking/cargos")
        assert isinstance(data, list), f"Expected list, got {type(data)}"
        assert len(data) >= 4, f"Expected at least 4 sample cargos, got {len(data)}"
        print(f"PASS: List cargos returned {len(data)} cargos")
    except Exception as e:
        print(f"FAIL: List cargos - {e}")
        return False
    return True


def test_get_cargo_details():
    """Test getting details of a known sample cargo."""
    try:
        data = get_json("/rest/booking/cargo/ABC123")
        assert data is not None, "Expected cargo details"
        assert data.get("trackingId") == "ABC123", f"Expected trackingId ABC123, got {data.get('trackingId')}"
        print("PASS: Get cargo details for ABC123")
    except Exception as e:
        print(f"FAIL: Get cargo details - {e}")
        return False
    return True


def test_graph_traversal():
    """Test the graph traversal / pathfinder service."""
    try:
        data = get_json("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=FIHEL")
        assert data is not None, "Expected transit paths response"
        paths = data.get("transitPaths", [])
        assert len(paths) > 0, "Expected at least one transit path"
        print(f"PASS: Graph traversal returned {len(paths)} paths")
    except Exception as e:
        print(f"FAIL: Graph traversal - {e}")
        return False
    return True


def test_handling_report():
    """Test submitting a handling report."""
    try:
        report = {
            "completionTime": "3/15/2026 12:00 PM",
            "trackingId": "ABC123",
            "eventType": "UNLOAD",
            "unLocode": "USDAL",
            "voyageNumber": "0200T"
        }
        post_json("/rest/handling/reports", report, expected_status=200)
        print("PASS: Handling report submitted successfully")
    except urllib.error.HTTPError as e:
        # 204 No Content is also acceptable for POST
        if e.code == 204:
            print("PASS: Handling report submitted successfully (204)")
            return True
        print(f"FAIL: Handling report - HTTP {e.code}: {e.read().decode('utf-8', errors='replace')}")
        return False
    except Exception as e:
        print(f"FAIL: Handling report - {e}")
        return False
    return True


def test_tracking_ids():
    """Test listing all tracking IDs."""
    try:
        data = get_json("/rest/booking/tracking-ids")
        assert isinstance(data, list), f"Expected list, got {type(data)}"
        assert "ABC123" in data, f"Expected ABC123 in tracking IDs: {data}"
        print(f"PASS: Tracking IDs returned {len(data)} IDs including ABC123")
    except Exception as e:
        print(f"FAIL: Tracking IDs - {e}")
        return False
    return True


def wait_for_app(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for app at {BASE_URL}...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(url("/actuator/health"))
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print(f"App is ready (took {int(time.time() - start)}s)")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"App did not become ready within {max_wait}s")
    return False


def main():
    if not wait_for_app():
        print("FAIL: Application did not start in time")
        sys.exit(1)

    # Give the app a moment to finish loading sample data
    time.sleep(5)

    tests = [
        test_health,
        test_list_locations,
        test_list_cargos,
        test_get_cargo_details,
        test_tracking_ids,
        test_graph_traversal,
        test_handling_report,
    ]

    passed = 0
    failed = 0
    for test in tests:
        try:
            if test():
                passed += 1
            else:
                failed += 1
        except Exception as e:
            print(f"FAIL: {test.__name__} raised exception: {e}")
            failed += 1

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
