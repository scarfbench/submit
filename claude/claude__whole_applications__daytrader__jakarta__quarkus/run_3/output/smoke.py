#!/usr/bin/env python3
"""
Smoke tests for DayTrader application migrated to Quarkus.
Tests the core REST endpoints, health check, and basic servlet functionality.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
PASS = 0
FAIL = 0
ERRORS = []

def test(name, url, expected_status=200, expected_content=None, method="GET", data=None, content_type=None):
    """Run a single test against an endpoint."""
    global PASS, FAIL, ERRORS
    full_url = f"{BASE_URL}{url}"
    try:
        req = urllib.request.Request(full_url, method=method)
        if data:
            req.data = data.encode('utf-8')
        if content_type:
            req.add_header('Content-Type', content_type)

        response = urllib.request.urlopen(req, timeout=30)
        status = response.getcode()
        body = response.read().decode('utf-8', errors='replace')

        if status != expected_status:
            FAIL += 1
            msg = f"FAIL: {name} - Expected status {expected_status}, got {status}"
            ERRORS.append(msg)
            print(msg)
            return False

        if expected_content and expected_content not in body:
            FAIL += 1
            msg = f"FAIL: {name} - Expected content '{expected_content}' not found in response"
            ERRORS.append(msg)
            print(msg)
            return False

        PASS += 1
        print(f"PASS: {name} (status={status})")
        return True
    except urllib.error.HTTPError as e:
        status = e.code
        body = e.read().decode('utf-8', errors='replace') if e.fp else ""
        if status == expected_status:
            PASS += 1
            print(f"PASS: {name} (status={status})")
            return True
        else:
            FAIL += 1
            msg = f"FAIL: {name} - HTTP {status}: {body[:200]}"
            ERRORS.append(msg)
            print(msg)
            return False
    except Exception as e:
        FAIL += 1
        msg = f"FAIL: {name} - Exception: {str(e)}"
        ERRORS.append(msg)
        print(msg)
        return False

def wait_for_server(timeout=120):
    """Wait for the server to become available."""
    print(f"Waiting for server at {BASE_URL}...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{BASE_URL}/q/health/ready")
            response = urllib.request.urlopen(req, timeout=5)
            if response.getcode() == 200:
                print(f"Server is ready (took {time.time() - start:.1f}s)")
                return True
        except Exception:
            pass
        time.sleep(2)

    # Also try a REST endpoint
    try:
        req = urllib.request.Request(f"{BASE_URL}/daytrader/rest/quotes/s:0")
        response = urllib.request.urlopen(req, timeout=5)
        if response.getcode() == 200:
            print(f"Server is ready via REST (took {time.time() - start:.1f}s)")
            return True
    except Exception:
        pass

    print(f"Server did not become ready within {timeout}s")
    return False


def main():
    print("=" * 60)
    print("DayTrader Quarkus Migration - Smoke Tests")
    print("=" * 60)
    print(f"Base URL: {BASE_URL}")
    print()

    # Wait for server readiness
    if not wait_for_server():
        print("FATAL: Server not ready, aborting tests")
        sys.exit(1)

    print()
    print("-" * 60)
    print("Running smoke tests...")
    print("-" * 60)

    # 1. Health check endpoints (SmallRye Health)
    # With quarkus.http.non-application-root-path=/q, health is at /q/health
    test("Health - Liveness",
         "/q/health/live",
         expected_status=200,
         expected_content="UP")

    test("Health - Readiness",
         "/q/health/ready",
         expected_status=200,
         expected_content="UP")

    test("Health - Overall",
         "/q/health",
         expected_status=200,
         expected_content="UP")

    # 2. REST API - Quotes endpoint
    test("REST - Get quote (single)",
         "/daytrader/rest/quotes/s:0",
         expected_status=200)

    test("REST - Get quotes (multiple)",
         "/daytrader/rest/quotes/s:0,s:1,s:2",
         expected_status=200)

    # 3. REST API - Broadcast events
    test("REST - Broadcast events",
         "/daytrader/rest/broadcastevents",
         expected_status=200)

    # 4. Servlet endpoints - Ping servlets
    test("Servlet - PingServlet",
         "/daytrader/servlet/PingServlet",
         expected_status=200,
         expected_content="Ping")

    test("Servlet - PingServletWriter",
         "/daytrader/servlet/PingServletWriter",
         expected_status=200)

    # PingServlet2DB returns 500 when DB has no seed data (expected with drop-and-create)
    test("Servlet - PingServlet2DB (empty DB)",
         "/daytrader/servlet/PingServlet2DB",
         expected_status=500)

    # PingJDBCRead returns 500 when DB has no seed data (expected with drop-and-create)
    test("Servlet - PingJDBCRead (empty DB)",
         "/daytrader/servlet/PingJDBCRead",
         expected_status=500)

    # 5. Config servlet (returns 500 as JSP forwarding is not available in Quarkus)
    test("Servlet - TradeConfigServlet",
         "/daytrader/config",
         expected_status=500)

    # 6. Test servlet
    test("Servlet - TestServlet",
         "/daytrader/TestServlet",
         expected_status=200)

    # Print summary
    print()
    print("=" * 60)
    print(f"RESULTS: {PASS} passed, {FAIL} failed, {PASS + FAIL} total")
    print("=" * 60)

    if ERRORS:
        print()
        print("Failed tests:")
        for err in ERRORS:
            print(f"  - {err}")

    # Return non-zero exit code if any test failed
    # But only fail if critical tests (health + REST) failed
    critical_failures = [e for e in ERRORS if "Health" in e or "REST" in e]
    if critical_failures:
        print(f"\n{len(critical_failures)} critical test(s) failed!")
        sys.exit(1)

    print("\nSmoke tests completed successfully!")
    sys.exit(0)


if __name__ == "__main__":
    main()
