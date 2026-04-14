"""
Smoke tests for Cargo Tracker application after Spring -> Quarkus migration.
Tests core REST endpoints and basic application health.
"""

import os
import sys
import time
import json
import subprocess
import urllib.request
import urllib.error

BASE_URL = os.environ.get("SMOKE_BASE_URL", "http://localhost:8080")
CONTEXT = "/cargo-tracker"


def url(path):
    return f"{BASE_URL}{CONTEXT}{path}"


def get(path, accept="application/json"):
    req = urllib.request.Request(url(path), headers={"Accept": accept})
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except Exception as e:
        return 0, str(e)


def post_json(path, data):
    body = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(
        url(path),
        data=body,
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            resp_body = resp.read().decode("utf-8")
            return resp.status, resp_body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except Exception as e:
        return 0, str(e)


def wait_for_ready(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} to be ready...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(url("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=USNYC"))
            with urllib.request.urlopen(req, timeout=5) as resp:
                if resp.status == 200:
                    print(f"Application ready after {int(time.time() - start)}s")
                    return True
        except Exception:
            pass
        time.sleep(3)
    print(f"Application not ready after {max_wait}s")
    return False


def test_graph_traversal():
    """Test the graph traversal REST API (pathfinder)."""
    print("TEST: Graph Traversal API...")
    status, body = get("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=USNYC")
    assert status == 200, f"Expected 200, got {status}: {body}"
    data = json.loads(body)
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    assert len(data) > 0, "Expected at least one transit path"
    for path in data:
        assert "transitEdges" in path, f"Missing transitEdges in path: {path}"
        assert len(path["transitEdges"]) > 0, "Expected at least one transit edge"
        for edge in path["transitEdges"]:
            assert "voyageNumber" in edge, f"Missing voyageNumber in edge: {edge}"
            assert "fromUnLocode" in edge, f"Missing fromUnLocode in edge: {edge}"
            assert "toUnLocode" in edge, f"Missing toUnLocode in edge: {edge}"
    print(f"  PASS: Got {len(data)} transit paths")


def test_handling_report_endpoint():
    """Test submitting a handling report via REST."""
    print("TEST: Handling Report REST API...")
    report = {
        "completionTime": "3/15/2026 12:00 AM",
        "trackingId": "ABC123",
        "eventType": "UNLOAD",
        "unLocode": "USDAL",
        "voyageNumber": "0200T",
    }
    status, body = post_json("/rest/handling/reports", report)
    # Accept 200, 204 (no content) or even 400 if validation fails - just test it's reachable
    assert status in (200, 204, 400), f"Expected 200/204/400, got {status}: {body}"
    print(f"  PASS: Handling report endpoint returned {status}")


def test_graph_traversal_validation():
    """Test that graph traversal validates input parameters."""
    print("TEST: Graph Traversal Input Validation...")
    # Missing origin
    status, body = get("/rest/graph-traversal/shortest-path?destination=USNYC")
    assert status in (400, 500), f"Expected 400/500 for missing origin, got {status}"
    print(f"  PASS: Missing origin returns {status}")


def test_graph_traversal_different_routes():
    """Test graph traversal with different origin/destination pairs."""
    print("TEST: Graph Traversal Different Routes...")
    pairs = [
        ("SESTO", "FIHEL"),
        ("USNYC", "USDAL"),
        ("DEHAM", "NLRTM"),
    ]
    for origin, dest in pairs:
        status, body = get(f"/rest/graph-traversal/shortest-path?origin={origin}&destination={dest}")
        assert status == 200, f"Expected 200 for {origin}->{dest}, got {status}: {body}"
        data = json.loads(body)
        assert len(data) > 0, f"Expected paths for {origin}->{dest}"
    print(f"  PASS: Multiple route queries successful")


def main():
    if not wait_for_ready():
        print("FAIL: Application did not start in time")
        sys.exit(1)

    # Give extra time for sample data loading
    time.sleep(5)

    passed = 0
    failed = 0
    tests = [
        test_graph_traversal,
        test_handling_report_endpoint,
        test_graph_traversal_validation,
        test_graph_traversal_different_routes,
    ]

    for test in tests:
        try:
            test()
            passed += 1
        except AssertionError as e:
            print(f"  FAIL: {e}")
            failed += 1
        except Exception as e:
            print(f"  ERROR: {e}")
            failed += 1

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
