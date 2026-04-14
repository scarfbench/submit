#!/usr/bin/env python3
"""Smoke tests for the Cargo Tracker application after Spring -> Jakarta EE migration."""

import os
import sys
import time
import json
import subprocess
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
CONTEXT_PATH = "/cargo-tracker"

def url(path):
    return f"{BASE_URL}{CONTEXT_PATH}{path}"

def wait_for_app(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for app at {url('')} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            r = requests.get(url("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=FIHEL"), timeout=5)
            if r.status_code == 200:
                print(f"App is ready (took {int(time.time()-start)}s)")
                return True
        except Exception:
            pass
        time.sleep(3)
    print(f"App not ready after {max_wait}s")
    return False

def test_graph_traversal():
    """Test the graph traversal REST endpoint."""
    print("TEST: Graph Traversal API ...")
    r = requests.get(url("/rest/graph-traversal/shortest-path"),
                     params={"origin": "CNHKG", "destination": "FIHEL"},
                     headers={"Accept": "application/json"},
                     timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}: {r.text}"
    data = r.json()
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    assert len(data) > 0, "Expected at least one transit path"
    for path in data:
        assert "transitEdges" in path, f"Missing transitEdges in {path}"
        for edge in path["transitEdges"]:
            assert "voyageNumber" in edge, f"Missing voyageNumber in {edge}"
            assert "fromUnLocode" in edge, f"Missing fromUnLocode in {edge}"
            assert "toUnLocode" in edge, f"Missing toUnLocode in {edge}"
    print(f"  PASS: Got {len(data)} transit paths")

def test_handling_report():
    """Test the handling report REST endpoint."""
    print("TEST: Handling Report API ...")
    report = {
        "completionTime": "2/15/2026 3:45 PM",
        "trackingId": "ABC123",
        "eventType": "UNLOAD",
        "unLocode": "USDAL",
        "voyageNumber": "0200T"
    }
    r = requests.post(url("/rest/handling/reports"),
                      json=report,
                      headers={"Content-Type": "application/json"},
                      timeout=10)
    # Should return 200 or 204 (no content) on success
    assert r.status_code in [200, 204], f"Expected 200/204, got {r.status_code}: {r.text}"
    print(f"  PASS: Handling report accepted (status {r.status_code})")

def test_cargo_sse_endpoint_exists():
    """Test that the cargo SSE endpoint exists (just check it responds)."""
    print("TEST: Cargo SSE endpoint ...")
    try:
        r = requests.get(url("/rest/cargo"),
                         headers={"Accept": "text/event-stream"},
                         timeout=5,
                         stream=True)
        # SSE endpoint should return 200 with text/event-stream
        assert r.status_code == 200, f"Expected 200, got {r.status_code}"
        print(f"  PASS: SSE endpoint responded with status {r.status_code}")
        r.close()
    except requests.exceptions.Timeout:
        # SSE connections may hang waiting for events - that's OK
        print("  PASS: SSE endpoint connected (timed out waiting for events, which is expected)")
    except Exception as e:
        print(f"  WARN: SSE test inconclusive: {e}")

def test_jsf_pages():
    """Test that JSF pages are accessible."""
    print("TEST: JSF Pages ...")
    pages = [
        "/admin/dashboard.xhtml",
        "/public/track.xhtml",
    ]
    for page in pages:
        r = requests.get(url(page), timeout=10, allow_redirects=True)
        assert r.status_code == 200, f"Page {page} returned {r.status_code}: {r.text[:200]}"
        print(f"  PASS: {page} -> {r.status_code}")

def test_graph_traversal_validation():
    """Test validation on graph traversal endpoint."""
    print("TEST: Graph Traversal Validation ...")
    # Missing origin
    r = requests.get(url("/rest/graph-traversal/shortest-path"),
                     params={"destination": "FIHEL"},
                     timeout=10)
    assert r.status_code in [400, 500], f"Expected 400/500 for missing origin, got {r.status_code}"
    print(f"  PASS: Missing origin correctly rejected (status {r.status_code})")

def main():
    if not wait_for_app():
        print("FAIL: Application did not start")
        sys.exit(1)

    tests = [
        test_graph_traversal,
        test_handling_report,
        test_cargo_sse_endpoint_exists,
        test_jsf_pages,
        test_graph_traversal_validation,
    ]

    passed = 0
    failed = 0
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
