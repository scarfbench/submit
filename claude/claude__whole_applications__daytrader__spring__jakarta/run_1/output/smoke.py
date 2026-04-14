"""
Smoke tests for DayTrader Jakarta EE application on Open Liberty.

Tests basic functionality: server startup, config page, DB build,
user registration, login, trading operations, and ping servlets.

Environment Variables:
    BASE_URL: Base URL for the application (default: http://localhost:9080)

Requirements:
    - requests
    - pytest
"""

import os
import sys
import time

import requests

# Configuration
BASE_URL = os.environ.get("BASE_URL", "http://localhost:9080")
CONTEXT = "/daytrader"
APP_URL = f"{BASE_URL}{CONTEXT}"
TIMEOUT = 30


def log(msg):
    print(f"  [smoke] {msg}")


def wait_for_server(url, max_wait=180):
    """Wait for the server to become available."""
    log(f"Waiting for server at {url} (max {max_wait}s)...")
    start = time.time()
    last_err = None
    while time.time() - start < max_wait:
        try:
            r = requests.get(url, timeout=5, allow_redirects=True)
            if r.status_code < 500:
                elapsed = time.time() - start
                log(f"Server ready after {elapsed:.1f}s (status={r.status_code})")
                return True
        except Exception as e:
            last_err = e
        time.sleep(3)
    log(f"Server not ready after {max_wait}s. Last error: {last_err}")
    return False


def test_welcome_page():
    """Test that the welcome/index page loads."""
    log("Testing welcome page...")
    r = requests.get(f"{APP_URL}/", timeout=TIMEOUT, allow_redirects=True)
    assert r.status_code == 200, f"Welcome page returned {r.status_code}"
    assert "DayTrader" in r.text or "Trade" in r.text or "FRAME" in r.text, \
        "Welcome page missing expected content"
    log("  PASS - Welcome page loaded")


def test_config_page():
    """Test that the configuration page loads."""
    log("Testing config page...")
    r = requests.get(f"{APP_URL}/config", timeout=TIMEOUT)
    assert r.status_code == 200, f"Config page returned {r.status_code}"
    log("  PASS - Config page loaded")


def test_build_db():
    """Test database population via the config servlet."""
    log("Testing DB build (populate)...")
    r = requests.get(f"{APP_URL}/config?action=buildDB", timeout=120)
    assert r.status_code == 200, f"buildDB returned {r.status_code}"
    log(f"  PASS - buildDB completed (status={r.status_code}, len={len(r.text)})")


def test_ping_servlet():
    """Test basic ping servlet."""
    log("Testing PingServlet...")
    r = requests.get(f"{APP_URL}/servlet/PingServlet", timeout=TIMEOUT)
    assert r.status_code == 200, f"PingServlet returned {r.status_code}"
    assert "Ping" in r.text or "hit" in r.text, "PingServlet missing expected content"
    log("  PASS - PingServlet responded")


def test_ping_servlet_writer():
    """Test PingServletWriter."""
    log("Testing PingServletWriter...")
    r = requests.get(f"{APP_URL}/servlet/PingServletWriter", timeout=TIMEOUT)
    assert r.status_code == 200, f"PingServletWriter returned {r.status_code}"
    log("  PASS - PingServletWriter responded")


def test_ping_servlet_jdbc():
    """Test JDBC ping servlet (requires DB to be built)."""
    log("Testing PingJDBCRead...")
    r = requests.get(f"{APP_URL}/servlet/PingJDBCRead", timeout=TIMEOUT)
    assert r.status_code == 200, f"PingJDBCRead returned {r.status_code}"
    log(f"  PASS - PingJDBCRead responded (status={r.status_code})")


def test_ping_cdi():
    """Test CDI ping servlet."""
    log("Testing PingServletCDI...")
    r = requests.get(f"{APP_URL}/servlet/PingServletCDI", timeout=TIMEOUT)
    assert r.status_code == 200, f"PingServletCDI returned {r.status_code}"
    log("  PASS - PingServletCDI responded")


def test_ping_jsonp():
    """Test JSON-P ping servlet."""
    log("Testing PingJSONPObject...")
    r = requests.get(f"{APP_URL}/servlet/PingJSONPObject", timeout=TIMEOUT)
    assert r.status_code == 200, f"PingJSONPObject returned {r.status_code}"
    log("  PASS - PingJSONPObject responded")


def test_register():
    """Test user registration."""
    log("Testing registration...")
    session = requests.Session()
    r = session.post(f"{APP_URL}/app", data={
        "action": "register",
        "user id": "smokeTestUser",
        "passwd": "smokepass",
        "confirm passwd": "smokepass",
        "Full Name": "Smoke Test User",
        "Credit Card Number": "1234567890",
        "money": "10000.00",
        "email": "smoke@test.com",
        "snail mail": "123 Smoke St"
    }, timeout=TIMEOUT, allow_redirects=True)
    assert r.status_code == 200, f"Register returned {r.status_code}"
    log(f"  PASS - Registration completed (status={r.status_code})")


def test_login():
    """Test user login flow. Returns session for subsequent tests."""
    log("Testing login...")
    session = requests.Session()
    r = session.post(f"{APP_URL}/app", data={
        "action": "login",
        "uid": "uid:0",
        "passwd": "xxx"
    }, timeout=TIMEOUT, allow_redirects=True)
    assert r.status_code == 200, f"Login returned {r.status_code}"
    log(f"  PASS - Login completed (status={r.status_code})")
    return session


def test_home_page(session):
    """Test home page after login."""
    log("Testing home page...")
    r = session.get(f"{APP_URL}/app?action=home", timeout=TIMEOUT)
    assert r.status_code == 200, f"Home page returned {r.status_code}"
    log(f"  PASS - Home page loaded")


def test_portfolio(session):
    """Test portfolio page."""
    log("Testing portfolio...")
    r = session.get(f"{APP_URL}/app?action=portfolio", timeout=TIMEOUT)
    assert r.status_code == 200, f"Portfolio returned {r.status_code}"
    log(f"  PASS - Portfolio loaded")


def test_quotes(session):
    """Test quotes page."""
    log("Testing quotes...")
    r = session.get(f"{APP_URL}/app?action=quotes&symbols=s:0", timeout=TIMEOUT)
    assert r.status_code == 200, f"Quotes returned {r.status_code}"
    log(f"  PASS - Quotes loaded")


def test_account(session):
    """Test account page."""
    log("Testing account page...")
    r = session.get(f"{APP_URL}/app?action=account", timeout=TIMEOUT)
    assert r.status_code == 200, f"Account returned {r.status_code}"
    log(f"  PASS - Account page loaded")


def test_market_summary(session):
    """Test market summary."""
    log("Testing market summary...")
    r = session.get(f"{APP_URL}/app?action=mksummary", timeout=TIMEOUT)
    assert r.status_code == 200, f"Market summary returned {r.status_code}"
    log(f"  PASS - Market summary loaded")


def test_buy(session):
    """Test buying a stock."""
    log("Testing buy...")
    r = session.post(f"{APP_URL}/app", data={
        "action": "buy",
        "symbol": "s:0",
        "quantity": "10"
    }, timeout=TIMEOUT, allow_redirects=True)
    assert r.status_code == 200, f"Buy returned {r.status_code}"
    log(f"  PASS - Buy completed")


def test_logout(session):
    """Test logout."""
    log("Testing logout...")
    r = session.get(f"{APP_URL}/app?action=logout", timeout=TIMEOUT, allow_redirects=True)
    assert r.status_code == 200, f"Logout returned {r.status_code}"
    log("  PASS - Logout completed")


def main():
    passed = 0
    failed = 0
    errors = []

    # Wait for server to start
    if not wait_for_server(f"{APP_URL}/config"):
        print("FATAL: Server did not start in time")
        sys.exit(1)

    # Phase 1: Basic connectivity tests
    basic_tests = [
        ("welcome_page", test_welcome_page),
        ("config_page", test_config_page),
        ("build_db", test_build_db),
        ("ping_servlet", test_ping_servlet),
        ("ping_servlet_writer", test_ping_servlet_writer),
        ("ping_cdi", test_ping_cdi),
        ("ping_jsonp", test_ping_jsonp),
        ("ping_jdbc", test_ping_servlet_jdbc),
        ("register", test_register),
    ]

    for name, test_fn in basic_tests:
        try:
            test_fn()
            passed += 1
        except Exception as e:
            failed += 1
            errors.append((name, str(e)))
            log(f"  FAIL - {name}: {e}")

    # Phase 2: Session-dependent tests (login flow)
    session = None
    try:
        session = test_login()
        passed += 1
    except Exception as e:
        failed += 1
        errors.append(("login", str(e)))
        log(f"  FAIL - login: {e}")

    if session:
        session_tests = [
            ("home_page", lambda: test_home_page(session)),
            ("portfolio", lambda: test_portfolio(session)),
            ("quotes", lambda: test_quotes(session)),
            ("account", lambda: test_account(session)),
            ("market_summary", lambda: test_market_summary(session)),
            ("buy", lambda: test_buy(session)),
            ("logout", lambda: test_logout(session)),
        ]
        for name, test_fn in session_tests:
            try:
                test_fn()
                passed += 1
            except Exception as e:
                failed += 1
                errors.append((name, str(e)))
                log(f"  FAIL - {name}: {e}")

    # Summary
    total = passed + failed
    print(f"\n{'='*60}")
    print(f"Smoke Test Results: {passed}/{total} passed, {failed} failed")
    if errors:
        print(f"\nFailures:")
        for name, err in errors:
            print(f"  - {name}: {err}")
    print(f"{'='*60}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
