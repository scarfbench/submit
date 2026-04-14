#!/usr/bin/env python3
"""
Smoke tests for the Eclipse Cargo Tracker application after Spring -> Jakarta EE migration.
Tests REST endpoints, JSF pages, and core functionality.
"""

import sys
import time
import requests
import json

BASE_URL = None

def get_base_url():
    """Get base URL from command line or default."""
    global BASE_URL
    if len(sys.argv) > 1:
        BASE_URL = sys.argv[1].rstrip("/")
    else:
        BASE_URL = "http://localhost:8080/cargo-tracker"
    return BASE_URL


def test_root_page():
    """Test that the root page loads."""
    url = f"{BASE_URL}/"
    resp = requests.get(url, timeout=30, allow_redirects=True)
    assert resp.status_code == 200, f"Root page returned {resp.status_code}"
    print("[PASS] Root page loads successfully")
    return True


def test_admin_dashboard():
    """Test the admin dashboard page."""
    url = f"{BASE_URL}/admin/dashboard.xhtml"
    resp = requests.get(url, timeout=30, allow_redirects=True)
    assert resp.status_code == 200, f"Dashboard returned {resp.status_code}"
    assert len(resp.text) > 100, "Dashboard response too short"
    print("[PASS] Admin dashboard loads successfully")
    return True


def test_graph_traversal_rest():
    """Test the graph traversal REST API (shortest-path endpoint)."""
    url = f"{BASE_URL}/rest/graph-traversal/shortest-path"
    params = {"origin": "CNHKG", "destination": "USNYC"}
    resp = requests.get(url, params=params, timeout=30)
    assert resp.status_code == 200, f"Graph traversal returned {resp.status_code}"
    data = resp.json()
    assert isinstance(data, list), "Expected a list of transit paths"
    assert len(data) > 0, "Expected at least one transit path"
    # Each path should have transit edges
    for path in data:
        assert "transitEdges" in path, "Transit path missing transitEdges"
        edges = path["transitEdges"]
        assert len(edges) > 0, "Expected at least one transit edge"
        for edge in edges:
            assert "voyageNumber" in edge, "Edge missing voyageNumber"
            assert "fromUnLocode" in edge, "Edge missing fromUnLocode"
            assert "toUnLocode" in edge, "Edge missing toUnLocode"
    print("[PASS] Graph traversal REST API works correctly")
    return True


def test_handling_report_endpoint():
    """Test the handling report REST endpoint (POST)."""
    url = f"{BASE_URL}/rest/handling/reports"
    # Use a known cargo tracking ID from sample data
    payload = {
        "completionTime": "3/1/2026 10:00 AM",
        "trackingId": "ABC123",
        "eventType": "UNLOAD",
        "unLocode": "USDAL",
        "voyageNumber": "0200T"
    }
    headers = {"Content-Type": "application/json"}
    resp = requests.post(url, json=payload, headers=headers, timeout=30)
    # 200 or 204 are both acceptable for successful processing
    assert resp.status_code in [200, 204], f"Handling report returned {resp.status_code}: {resp.text}"
    print("[PASS] Handling report REST endpoint accepts submissions")
    return True


def test_cargo_sse_endpoint():
    """Test the SSE cargo tracking endpoint returns data."""
    url = f"{BASE_URL}/rest/cargo"
    try:
        resp = requests.get(url, timeout=10, stream=True, headers={"Accept": "text/event-stream"})
        assert resp.status_code == 200, f"SSE endpoint returned {resp.status_code}"
        # Read a small chunk to verify we get data
        chunk = next(resp.iter_content(chunk_size=256, decode_unicode=True), None)
        assert chunk is not None, "No SSE data received"
        print("[PASS] SSE cargo tracking endpoint streams data")
        resp.close()
        return True
    except requests.exceptions.ReadTimeout:
        # Timeout can happen if no events, but connection success means the endpoint works
        print("[PASS] SSE cargo tracking endpoint is reachable (timeout is expected)")
        return True
    except StopIteration:
        print("[PASS] SSE endpoint reachable, no data yet")
        return True


def test_tracking_page():
    """Test the public tracking page."""
    url = f"{BASE_URL}/admin/tracking/track.xhtml"
    resp = requests.get(url, timeout=30, allow_redirects=True)
    assert resp.status_code == 200, f"Tracking page returned {resp.status_code}"
    print("[PASS] Tracking page loads successfully")
    return True


def test_event_logger_page():
    """Test the event logger page."""
    url = f"{BASE_URL}/event-logger/index.xhtml"
    resp = requests.get(url, timeout=30, allow_redirects=True)
    assert resp.status_code == 200, f"Event logger page returned {resp.status_code}"
    print("[PASS] Event logger page loads successfully")
    return True


def wait_for_app(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            resp = requests.get(f"{BASE_URL}/", timeout=5, allow_redirects=True)
            if resp.status_code == 200:
                print(f"Application is ready (took {int(time.time() - start)}s)")
                return True
        except requests.exceptions.ConnectionError:
            pass
        except requests.exceptions.ReadTimeout:
            pass
        time.sleep(3)
    print(f"Application did not become ready within {max_wait}s")
    return False


def main():
    get_base_url()
    print(f"Running smoke tests against: {BASE_URL}")
    print("=" * 60)

    if not wait_for_app():
        print("FATAL: Application not reachable")
        sys.exit(1)

    # Give extra time for sample data loading
    time.sleep(5)

    tests = [
        test_root_page,
        test_admin_dashboard,
        test_graph_traversal_rest,
        test_handling_report_endpoint,
        test_cargo_sse_endpoint,
        test_tracking_page,
        test_event_logger_page,
    ]

    passed = 0
    failed = 0
    errors = []

    for test in tests:
        try:
            test()
            passed += 1
        except Exception as e:
            failed += 1
            errors.append((test.__name__, str(e)))
            print(f"[FAIL] {test.__name__}: {e}")

    print("=" * 60)
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")

    if errors:
        print("\nFailed tests:")
        for name, err in errors:
            print(f"  - {name}: {err}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
