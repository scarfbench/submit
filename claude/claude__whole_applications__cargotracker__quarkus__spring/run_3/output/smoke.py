#!/usr/bin/env python3
"""
Smoke tests for the CargoTracker application migrated from Quarkus to Spring Boot.
Tests the REST API endpoints to verify the application is functional.
"""
import json
import sys
import time
import urllib.request
import urllib.error
import urllib.parse


BASE_URL = None


def set_base_url(port):
    global BASE_URL
    BASE_URL = f"http://localhost:{port}/cargo-tracker"


def make_request(path, method="GET", data=None, content_type="application/json", expect_status=200):
    """Make an HTTP request and return (status_code, body)."""
    url = f"{BASE_URL}{path}"
    headers = {}
    if content_type:
        headers["Content-Type"] = content_type
    if data and isinstance(data, (dict, list)):
        data = json.dumps(data).encode("utf-8")
    elif data and isinstance(data, str):
        data = data.encode("utf-8")

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8") if e.fp else ""
        return e.code, body
    except urllib.error.URLError as e:
        print(f"  FAIL: Could not connect to {url}: {e}")
        return -1, str(e)


def wait_for_app(port, timeout=120):
    """Wait for the application to be ready."""
    set_base_url(port)
    print(f"Waiting for application at {BASE_URL} (timeout={timeout}s)...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{BASE_URL}/rest/graph-traversal/shortest-path?origin=CNHKG&destination=AUMEL",
                                        method="GET")
            with urllib.request.urlopen(req, timeout=5) as resp:
                if resp.status == 200:
                    print("  Application is ready!")
                    return True
        except Exception:
            pass
        time.sleep(3)
    print("  FAIL: Application did not start within timeout")
    return False


def test_graph_traversal_endpoint():
    """Test the graph traversal shortest-path endpoint."""
    print("\n--- Test: Graph Traversal Shortest Path ---")
    status, body = make_request(
        "/rest/graph-traversal/shortest-path?origin=CNHKG&destination=AUMEL"
    )
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert "transitPaths" in data, f"Expected 'transitPaths' key in response: {data}"
    assert len(data["transitPaths"]) > 0, "Expected at least one transit path"
    path = data["transitPaths"][0]
    assert "transitEdges" in path, f"Expected 'transitEdges' in path: {path}"
    assert len(path["transitEdges"]) > 0, "Expected at least one transit edge"
    edge = path["transitEdges"][0]
    assert "voyageNumber" in edge, f"Expected 'voyageNumber' in edge: {edge}"
    assert "fromUnLocode" in edge, f"Expected 'fromUnLocode' in edge: {edge}"
    assert "toUnLocode" in edge, f"Expected 'toUnLocode' in edge: {edge}"
    print("  PASS: Graph traversal returns valid paths with edges")


def test_graph_traversal_different_routes():
    """Test graph traversal with different origin/destination pairs."""
    print("\n--- Test: Graph Traversal Different Routes ---")
    pairs = [
        ("USNYC", "USDAL"),
        ("SESTO", "FIHEL"),
        ("DEHAM", "CNSHA"),
    ]
    for origin, dest in pairs:
        status, body = make_request(
            f"/rest/graph-traversal/shortest-path?origin={origin}&destination={dest}"
        )
        assert status == 200, f"Expected 200 for {origin}->{dest}, got {status}: {body}"
        data = json.loads(body)
        assert len(data["transitPaths"]) > 0, f"No paths for {origin}->{dest}"
    print("  PASS: Multiple route queries return valid results")


def test_handling_report_submission():
    """Test submitting a handling event report."""
    print("\n--- Test: Handling Report Submission ---")
    report = {
        "completionTime": "3/14/2026 1:00 PM",
        "trackingId": "ABC123",
        "eventType": "UNLOAD",
        "unLocode": "USDAL",
        "voyageNumber": "0200T"
    }
    status, body = make_request(
        "/rest/handling/reports",
        method="POST",
        data=report,
        content_type="application/json"
    )
    # Accept 200, 204 (no content) as valid responses
    assert status in (200, 204), f"Expected 200/204, got {status}: {body}"
    print("  PASS: Handling report submitted successfully")


def test_handling_report_validation():
    """Test that invalid handling reports are rejected."""
    print("\n--- Test: Handling Report Validation ---")
    # Missing required fields
    report = {
        "completionTime": "",
        "trackingId": "",
        "eventType": "",
        "unLocode": ""
    }
    status, body = make_request(
        "/rest/handling/reports",
        method="POST",
        data=report,
        content_type="application/json"
    )
    # Should get a 400 Bad Request for validation errors
    assert status in (400, 500), f"Expected 400/500 for invalid report, got {status}: {body}"
    print("  PASS: Invalid handling report rejected correctly")


def test_sse_cargo_endpoint():
    """Test that the SSE cargo tracking endpoint is available."""
    print("\n--- Test: SSE Cargo Tracking Endpoint ---")
    # Just check the endpoint exists and starts responding
    try:
        url = f"{BASE_URL}/rest/cargo"
        req = urllib.request.Request(url, headers={"Accept": "text/event-stream"})
        with urllib.request.urlopen(req, timeout=5) as resp:
            # If we get here, the endpoint is responsive
            status = resp.status
            assert status == 200, f"Expected 200, got {status}"
            print("  PASS: SSE endpoint is available")
    except urllib.error.URLError as e:
        # Timeout is acceptable - SSE keeps the connection open
        if "timed out" in str(e).lower():
            print("  PASS: SSE endpoint is available (connection held open as expected)")
        else:
            print(f"  FAIL: SSE endpoint error: {e}")
            raise
    except Exception as e:
        if "timed out" in str(e).lower():
            print("  PASS: SSE endpoint is available (connection held open as expected)")
        else:
            raise


def test_application_loads_sample_data():
    """Test that sample data is loaded by verifying graph traversal works (requires locations in DB)."""
    print("\n--- Test: Sample Data Loaded ---")
    status, body = make_request(
        "/rest/graph-traversal/shortest-path?origin=CNHKG&destination=FIHEL"
    )
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert len(data["transitPaths"]) > 0, "Expected transit paths (requires sample data loaded)"
    print("  PASS: Sample data appears to be loaded")


def main():
    if len(sys.argv) < 2:
        print("Usage: python smoke.py <port>")
        sys.exit(1)

    port = sys.argv[1]

    if not wait_for_app(port):
        sys.exit(1)

    # Allow some extra time for sample data loading
    time.sleep(5)

    passed = 0
    failed = 0
    errors = []

    tests = [
        test_graph_traversal_endpoint,
        test_graph_traversal_different_routes,
        test_handling_report_submission,
        test_handling_report_validation,
        test_sse_cargo_endpoint,
        test_application_loads_sample_data,
    ]

    for test in tests:
        try:
            test()
            passed += 1
        except Exception as e:
            failed += 1
            errors.append((test.__name__, str(e)))
            print(f"  FAIL: {e}")

    print(f"\n{'='*60}")
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")
    if errors:
        print("\nFailed tests:")
        for name, err in errors:
            print(f"  - {name}: {err}")
    print(f"{'='*60}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
