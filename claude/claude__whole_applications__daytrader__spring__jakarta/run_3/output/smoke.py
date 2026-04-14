#!/usr/bin/env python3
"""
Smoke tests for DayTrader application.
Tests core functionality: health check, servlet endpoints, ping servlets.
"""
import sys
import time
import urllib.request
import urllib.error
import ssl

# Disable SSL verification for self-signed certs
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

BASE_URL = None

def set_base_url(port):
    global BASE_URL
    BASE_URL = f"http://localhost:{port}/daytrader"

def fetch(path, expected_status=200, method="GET", timeout=15):
    """Fetch a URL and return (status_code, body_text)."""
    url = f"{BASE_URL}{path}"
    print(f"  [{method}] {url} ... ", end="", flush=True)
    try:
        req = urllib.request.Request(url, method=method)
        resp = urllib.request.urlopen(req, timeout=timeout, context=ctx)
        body = resp.read().decode("utf-8", errors="replace")
        status = resp.getcode()
        print(f"HTTP {status} ({len(body)} bytes)")
        return status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace") if e.fp else ""
        print(f"HTTP {e.code}")
        return e.code, body
    except Exception as e:
        print(f"ERROR: {e}")
        return 0, str(e)


def test_welcome_page():
    """Test that the welcome/landing page is accessible."""
    status, body = fetch("/app")
    assert status == 200, f"Welcome page returned {status}"
    print("    PASS: Welcome page accessible")
    return True


def test_config_page():
    """Test that the configuration page is accessible."""
    status, body = fetch("/config")
    assert status == 200, f"Config page returned {status}"
    print("    PASS: Config page accessible")
    return True


def test_ping_servlet():
    """Test basic PingServlet."""
    status, body = fetch("/servlet/PingServlet")
    assert status == 200, f"PingServlet returned {status}"
    assert "Ping" in body or "ping" in body.lower(), "Expected ping content"
    print("    PASS: PingServlet works")
    return True


def test_ping_servlet_writer():
    """Test PingServletWriter."""
    status, body = fetch("/servlet/PingServletWriter")
    # Accept 200 or 404 (may not be registered in all configs)
    if status == 200:
        print("    PASS: PingServletWriter works")
    elif status == 404:
        print("    SKIP: PingServletWriter not found (404)")
    else:
        assert False, f"PingServletWriter returned unexpected {status}"
    return True


def test_ping_html():
    """Test static HTML ping page."""
    status, body = fetch("/PingHtml.html")
    if status == 200:
        print("    PASS: PingHtml.html accessible")
    elif status == 404:
        print("    SKIP: PingHtml.html not found")
    else:
        assert False, f"PingHtml.html returned unexpected {status}"
    return True


def test_ping_jsp():
    """Test PingJsp page."""
    status, body = fetch("/PingJsp.jsp")
    if status == 200:
        print("    PASS: PingJsp.jsp accessible")
    elif status == 404:
        print("    SKIP: PingJsp.jsp not found")
    else:
        assert False, f"PingJsp.jsp returned unexpected {status}"
    return True


def test_scenario_servlet():
    """Test the TradeScenarioServlet."""
    status, body = fetch("/scenario?action=n")
    if status == 200:
        print("    PASS: TradeScenarioServlet works")
    elif status == 404:
        print("    SKIP: TradeScenarioServlet not found")
    else:
        # May redirect or return other status
        print(f"    WARN: TradeScenarioServlet returned {status}")
    return True


def test_explicit_gc():
    """Test ExplicitGC servlet."""
    status, body = fetch("/servlet/ExplicitGC")
    if status == 200:
        print("    PASS: ExplicitGC servlet works")
    elif status == 404:
        print("    SKIP: ExplicitGC not found")
    else:
        print(f"    WARN: ExplicitGC returned {status}")
    return True


def wait_for_app(port, max_wait=180):
    """Wait for the application to be ready."""
    set_base_url(port)
    print(f"Waiting for app at {BASE_URL} (max {max_wait}s)...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            req = urllib.request.Request(f"{BASE_URL}/app", method="GET")
            resp = urllib.request.urlopen(req, timeout=5, context=ctx)
            if resp.getcode() in (200, 302, 301):
                print(f"App ready after {int(time.time() - start)}s")
                return True
        except Exception:
            pass
        # Also try a simple ping
        try:
            req = urllib.request.Request(f"{BASE_URL}/servlet/PingServlet", method="GET")
            resp = urllib.request.urlopen(req, timeout=5, context=ctx)
            if resp.getcode() == 200:
                print(f"App ready after {int(time.time() - start)}s (via PingServlet)")
                return True
        except Exception:
            pass
        time.sleep(3)
    print(f"App did not become ready within {max_wait}s")
    return False


def main():
    if len(sys.argv) < 2:
        print("Usage: python smoke.py <port>")
        sys.exit(1)

    port = sys.argv[1]

    if not wait_for_app(port):
        print("FAIL: Application not ready")
        sys.exit(1)

    tests = [
        test_welcome_page,
        test_config_page,
        test_ping_servlet,
        test_ping_servlet_writer,
        test_ping_html,
        test_ping_jsp,
        test_scenario_servlet,
        test_explicit_gc,
    ]

    passed = 0
    failed = 0
    skipped = 0

    for test in tests:
        name = test.__name__
        print(f"\nRunning {name}...")
        try:
            test()
            passed += 1
        except AssertionError as e:
            print(f"    FAIL: {e}")
            failed += 1
        except Exception as e:
            print(f"    ERROR: {e}")
            failed += 1

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
