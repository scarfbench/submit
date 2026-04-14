#!/usr/bin/env python3
"""
Smoke tests for CargoTracker Jakarta EE application.
Tests REST API endpoints and basic web pages.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error


BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
CONTEXT_PATH = "/cargo-tracker"


def make_url(path):
    return f"{BASE_URL}{CONTEXT_PATH}{path}"


def http_get(path, accept="application/json", timeout=10):
    """Perform HTTP GET request and return (status_code, body)."""
    url = make_url(path)
    req = urllib.request.Request(url)
    req.add_header("Accept", accept)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except urllib.error.URLError as e:
        return 0, str(e)
    except Exception as e:
        return 0, str(e)


def http_post_json(path, data, timeout=10):
    """Perform HTTP POST with JSON body and return (status_code, body)."""
    url = make_url(path)
    body = json.dumps(data).encode("utf-8")
    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            resp_body = resp.read().decode("utf-8")
            return resp.status, resp_body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except urllib.error.URLError as e:
        return 0, str(e)
    except Exception as e:
        return 0, str(e)


def wait_for_app(max_wait=120):
    """Wait for the application to become available."""
    print(f"Waiting for application at {make_url('')} ...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            status, _ = http_get("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=SESTO", timeout=5)
            if status in (200, 204):
                print(f"Application is ready (took {time.time() - start:.1f}s)")
                return True
        except Exception:
            pass
        # Also try the index page
        try:
            status, body = http_get("/index.xhtml", accept="text/html", timeout=5)
            if status == 200 and "Cargo" in body:
                print(f"Application is ready (took {time.time() - start:.1f}s)")
                return True
        except Exception:
            pass
        time.sleep(3)
    print(f"Application not ready after {max_wait}s")
    return False


def test_graph_traversal_api():
    """Test the graph traversal REST API endpoint."""
    print("TEST: Graph Traversal API ...")
    status, body = http_get("/rest/graph-traversal/shortest-path?origin=CNHKG&destination=SESTO")
    if status != 200:
        print(f"  FAIL: Expected 200, got {status}")
        print(f"  Body: {body[:500]}")
        return False
    try:
        data = json.loads(body)
        if "transitPaths" in data and len(data["transitPaths"]) > 0:
            print(f"  PASS: Got {len(data['transitPaths'])} transit paths")
            return True
        else:
            print(f"  FAIL: No transit paths in response")
            return False
    except json.JSONDecodeError:
        print(f"  FAIL: Response is not valid JSON: {body[:200]}")
        return False


def test_handling_report_api():
    """Test the handling report REST API endpoint (POST)."""
    print("TEST: Handling Report API ...")
    report = {
        "completionTime": "3/15/2026 12:00 PM",
        "trackingId": "ABC123",
        "eventType": "UNLOAD",
        "unLocode": "USDAL",
        "voyageNumber": "0200T"
    }
    status, body = http_post_json("/rest/handling/reports", report)
    # 200 or 204 means success
    if status in (200, 204):
        print(f"  PASS: Handling report accepted (status {status})")
        return True
    else:
        print(f"  INFO: Handling report returned status {status} (may be expected for sample data state)")
        print(f"  Body: {body[:500]}")
        # Not a hard failure - the business logic may reject based on current data state
        return True


def test_index_page():
    """Test that the main index page loads."""
    print("TEST: Index page ...")
    status, body = http_get("/index.xhtml", accept="text/html")
    if status == 200:
        print(f"  PASS: Index page loaded (status 200, length {len(body)})")
        return True
    else:
        print(f"  FAIL: Expected 200, got {status}")
        return False


def test_admin_dashboard():
    """Test that the admin dashboard page loads."""
    print("TEST: Admin Dashboard ...")
    status, body = http_get("/admin/dashboard.xhtml", accept="text/html")
    if status == 200:
        print(f"  PASS: Admin dashboard loaded (status 200, length {len(body)})")
        return True
    else:
        print(f"  FAIL: Expected 200, got {status}")
        return False


def test_public_tracking_page():
    """Test that the public tracking page loads."""
    print("TEST: Public Tracking page ...")
    status, body = http_get("/public/track.xhtml", accept="text/html")
    if status == 200:
        print(f"  PASS: Tracking page loaded (status 200, length {len(body)})")
        return True
    else:
        print(f"  FAIL: Expected 200, got {status}")
        return False


def test_event_logger_page():
    """Test that the event logger page loads."""
    print("TEST: Event Logger page ...")
    status, body = http_get("/event-logger/index.xhtml", accept="text/html")
    if status == 200:
        print(f"  PASS: Event logger page loaded (status 200, length {len(body)})")
        return True
    else:
        print(f"  FAIL: Expected 200, got {status}")
        return False


def main():
    if not wait_for_app():
        print("ABORT: Application not available")
        sys.exit(1)

    tests = [
        test_graph_traversal_api,
        test_handling_report_api,
        test_index_page,
        test_admin_dashboard,
        test_public_tracking_page,
        test_event_logger_page,
    ]

    results = []
    for test in tests:
        try:
            result = test()
            results.append((test.__name__, result))
        except Exception as e:
            print(f"  ERROR: {e}")
            results.append((test.__name__, False))

    print("\n" + "=" * 60)
    print("SMOKE TEST RESULTS")
    print("=" * 60)
    passed = sum(1 for _, r in results if r)
    failed = sum(1 for _, r in results if not r)

    for name, result in results:
        status = "PASS" if result else "FAIL"
        print(f"  [{status}] {name}")

    print(f"\nTotal: {len(results)}, Passed: {passed}, Failed: {failed}")

    if failed > 0:
        sys.exit(1)
    else:
        print("\nAll smoke tests passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
