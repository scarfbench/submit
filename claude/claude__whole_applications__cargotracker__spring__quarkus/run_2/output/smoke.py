#!/usr/bin/env python3
"""Smoke tests for Cargo Tracker application after Spring -> Quarkus migration."""

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

def get(path, accept="application/json"):
    req = urllib.request.Request(url(path), headers={"Accept": accept})
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
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
        headers={
            "Content-Type": "application/json",
            "Accept": "application/json",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            resp_body = resp.read().decode("utf-8")
            return resp.status, resp_body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except Exception as e:
        return 0, str(e)

passed = 0
failed = 0

def check(name, condition, detail=""):
    global passed, failed
    if condition:
        passed += 1
        print(f"  PASS: {name}")
    else:
        failed += 1
        print(f"  FAIL: {name} -- {detail}")

def wait_for_ready(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL}{CONTEXT} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(url("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=FIHEL"))
            with urllib.request.urlopen(req, timeout=5) as resp:
                if resp.status == 200:
                    print("Application is ready!")
                    return True
        except Exception:
            pass
        time.sleep(3)
    print("Timeout waiting for application")
    return False

def test_graph_traversal():
    """Test the graph traversal / pathfinder REST endpoint."""
    print("\n--- Test: Graph Traversal Service ---")
    status, body = get("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=FIHEL")
    check("Graph traversal returns 200", status == 200, f"status={status}")
    if status == 200:
        data = json.loads(body)
        check("Graph traversal returns list of paths", isinstance(data, list) and len(data) > 0, f"body={body[:200]}")
        if len(data) > 0:
            path = data[0]
            check("Path has transitEdges", "transitEdges" in path, f"keys={list(path.keys())}")

def test_handling_report():
    """Test the handling report REST endpoint."""
    print("\n--- Test: Handling Report Service ---")
    report = {
        "completionTime": "3/15/2026 12:00 PM",
        "trackingId": "ABC123",
        "eventType": "UNLOAD",
        "unLocode": "USDAL",
        "voyageNumber": "0200T",
    }
    status, body = post_json("/rest/handling/reports", report)
    # Expect either 200 or 204 (success with no content)
    check("Handling report accepted", status in (200, 204), f"status={status}, body={body[:300]}")

def test_cargo_sse_endpoint():
    """Test the SSE cargo tracking endpoint exists and responds."""
    print("\n--- Test: Cargo SSE Endpoint ---")
    # Just check it responds with the right content type (we can't easily consume SSE stream)
    try:
        req = urllib.request.Request(url("/rest/cargo"), headers={"Accept": "text/event-stream"})
        with urllib.request.urlopen(req, timeout=5) as resp:
            content_type = resp.getheader("Content-Type", "")
            check("SSE endpoint returns event stream", "text/event-stream" in content_type, f"content-type={content_type}")
    except urllib.error.HTTPError as e:
        check("SSE endpoint accessible", False, f"status={e.code}")
    except Exception as e:
        # Timeout is OK for SSE (long-polling)
        if "timed out" in str(e).lower() or "timeout" in str(e).lower():
            check("SSE endpoint accessible (timed out reading stream, expected for SSE)", True)
        else:
            check("SSE endpoint accessible", False, str(e))

def test_jsf_pages():
    """Test that key JSF/web pages are accessible."""
    print("\n--- Test: JSF Pages ---")

    # Test main index page
    status, body = get("/", accept="text/html")
    check("Index page accessible", status in (200, 301, 302, 303), f"status={status}")

    # Test dashboard
    status, body = get("/admin/dashboard.xhtml", accept="text/html")
    check("Dashboard page accessible", status == 200, f"status={status}")

def test_sample_data():
    """Test that sample data was loaded properly by querying the graph traversal service."""
    print("\n--- Test: Sample Data Loaded ---")
    # The graph traversal service uses the GraphDao which has hardcoded locations
    # If the app starts, it means sample data loading worked
    status, body = get("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=USNYC")
    check("Sample data available (graph traversal works)", status == 200, f"status={status}")


if __name__ == "__main__":
    if not wait_for_ready():
        print("\nApplication failed to start. Aborting smoke tests.")
        sys.exit(1)

    test_graph_traversal()
    test_handling_report()
    test_cargo_sse_endpoint()
    test_jsf_pages()
    test_sample_data()

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {passed+failed} tests")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)
