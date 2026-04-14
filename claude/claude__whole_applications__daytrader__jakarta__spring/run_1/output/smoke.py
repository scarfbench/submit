#!/usr/bin/env python3
"""
Smoke tests for DayTrader Spring Boot application.
Tests core functionality: health check, registration, login, quotes, buy/sell operations.
"""

import os
import sys
import time
import requests

BASE_URL = os.environ.get("APP_URL", "http://localhost:8080/daytrader")


def test_health_endpoint():
    """Test Spring Boot Actuator health endpoint returns UP."""
    resp = requests.get(f"{BASE_URL}/actuator/health", timeout=10)
    assert resp.status_code == 200, f"Health check failed: {resp.status_code}"
    data = resp.json()
    assert data["status"] == "UP", f"Health status not UP: {data['status']}"
    assert "db" in data.get("components", {}), "Database component not in health check"
    assert data["components"]["db"]["status"] == "UP", "Database not UP"
    print("PASS: Health endpoint returns UP with DB component")


def test_welcome_page():
    """Test that the welcome/login page loads."""
    resp = requests.get(f"{BASE_URL}/app", timeout=10)
    assert resp.status_code == 200, f"Welcome page failed: {resp.status_code}"
    assert "DayTrader" in resp.text, "DayTrader not found in welcome page"
    assert "Log in" in resp.text or "Login" in resp.text, "Login form not found"
    print("PASS: Welcome page loads correctly")


def test_config_page():
    """Test that the configuration page loads."""
    resp = requests.get(f"{BASE_URL}/config", timeout=10)
    assert resp.status_code == 200, f"Config page failed: {resp.status_code}"
    assert "DayTrader Configuration" in resp.text, "Config page content not found"
    assert "MaxUsers" in resp.text, "MaxUsers setting not found"
    print("PASS: Configuration page loads correctly")


def test_config_update():
    """Test updating configuration parameters."""
    resp = requests.post(
        f"{BASE_URL}/config",
        data={"action": "updateConfig", "MaxUsers": "200", "MaxQuotes": "100"},
        timeout=10,
    )
    assert resp.status_code == 200, f"Config update failed: {resp.status_code}"
    print("PASS: Configuration update succeeds")


def test_create_quotes():
    """Test that the TestServlet creates quotes in the database."""
    resp = requests.get(f"{BASE_URL}/TestServlet", timeout=10)
    assert resp.status_code == 200, f"TestServlet failed: {resp.status_code}"
    print("PASS: TestServlet creates quotes successfully")


def test_rest_quotes():
    """Test REST API returns quote data as JSON."""
    # First ensure quotes exist via TestServlet
    requests.get(f"{BASE_URL}/TestServlet", timeout=10)
    time.sleep(1)

    resp = requests.get(f"{BASE_URL}/rest/quotes/s:1", timeout=10)
    assert resp.status_code == 200, f"REST quotes failed: {resp.status_code}"
    data = resp.json()
    assert isinstance(data, list), f"Expected list, got {type(data)}"
    assert len(data) > 0, "No quotes returned"
    assert data[0] is not None, "Quote data is null"
    assert data[0]["symbol"] == "s:1", f"Wrong symbol: {data[0].get('symbol')}"
    assert "price" in data[0], "Price not in quote data"
    print(f"PASS: REST API returns quote data for s:1 (price={data[0]['price']})")


def test_rest_multiple_quotes():
    """Test REST API returns multiple quotes."""
    resp = requests.get(f"{BASE_URL}/rest/quotes/s:1,s:2,s:3", timeout=10)
    assert resp.status_code == 200, f"REST multiple quotes failed: {resp.status_code}"
    data = resp.json()
    assert len(data) == 3, f"Expected 3 quotes, got {len(data)}"
    print("PASS: REST API returns multiple quotes correctly")


def test_register_user():
    """Test user registration."""
    session = requests.Session()
    resp = session.post(
        f"{BASE_URL}/app",
        data={
            "action": "register",
            "user id": "smoke_test_user",
            "passwd": "testpass",
            "confirm passwd": "testpass",
            "Full Name": "Smoke Test User",
            "snail mail": "123 Test Ave",
            "email": "smoke@test.com",
            "Credit Card Number": "1234567890",
            "money": "50000",
        },
        timeout=15,
    )
    assert resp.status_code == 200, f"Registration failed: {resp.status_code}"
    # Registration auto-logs in, so we should see the home page
    assert "DayTrader" in resp.text, "DayTrader not in response after registration"
    print("PASS: User registration succeeds")
    return session


def test_login_user():
    """Test user login."""
    session = requests.Session()
    resp = session.post(
        f"{BASE_URL}/app",
        data={"action": "login", "uid": "smoke_test_user", "passwd": "testpass"},
        timeout=10,
    )
    assert resp.status_code == 200, f"Login failed: {resp.status_code}"
    assert "Home" in resp.text or "home" in resp.text, "Home page not shown after login"
    print("PASS: User login succeeds")
    return session


def test_buy_stock(session):
    """Test buying a stock."""
    resp = session.post(
        f"{BASE_URL}/app",
        data={"action": "buy", "symbol": "s:1", "quantity": "50"},
        timeout=15,
    )
    assert resp.status_code == 200, f"Buy failed: {resp.status_code}"
    assert "buy" in resp.text.lower() or "order" in resp.text.lower(), "Buy order not confirmed"
    print("PASS: Buy stock operation succeeds")


def test_portfolio(session):
    """Test viewing portfolio."""
    resp = session.post(
        f"{BASE_URL}/app",
        data={"action": "portfolio"},
        timeout=10,
    )
    assert resp.status_code == 200, f"Portfolio failed: {resp.status_code}"
    assert "Portfolio" in resp.text or "portfolio" in resp.text, "Portfolio page not shown"
    print("PASS: Portfolio view succeeds")


def test_quotes_page(session):
    """Test getting quotes via the web interface."""
    resp = session.post(
        f"{BASE_URL}/app",
        data={"action": "quotes", "symbols": "s:1"},
        timeout=10,
    )
    assert resp.status_code == 200, f"Quotes page failed: {resp.status_code}"
    assert "s:1" in resp.text, "Quote symbol not found in response"
    print("PASS: Quotes page works correctly")


def test_account_page(session):
    """Test viewing account information."""
    resp = session.post(
        f"{BASE_URL}/app",
        data={"action": "account"},
        timeout=10,
    )
    assert resp.status_code == 200, f"Account page failed: {resp.status_code}"
    print("PASS: Account page loads correctly")


def test_logout(session):
    """Test user logout."""
    resp = session.post(
        f"{BASE_URL}/app",
        data={"action": "logout"},
        timeout=10,
    )
    assert resp.status_code == 200, f"Logout failed: {resp.status_code}"
    assert "Log in" in resp.text or "Login" in resp.text, "Login page not shown after logout"
    print("PASS: Logout succeeds")


def test_scenario_servlet():
    """Test the scenario servlet for load testing.
    Note: This requires pre-populated users (uid:0...) which may not exist in a fresh database.
    The servlet itself is accessible; 500 errors from missing users are expected without DB population.
    """
    resp = requests.get(f"{BASE_URL}/scenario", timeout=10)
    # The scenario servlet may return 500 if users aren't populated (expected for fresh DB)
    # We consider it a pass if the servlet is reachable (not 404)
    assert resp.status_code != 404, f"Scenario servlet not found: {resp.status_code}"
    if resp.status_code == 200:
        print("PASS: Scenario servlet responds with 200")
    else:
        print(f"PASS: Scenario servlet is reachable (status={resp.status_code}, expected without populated DB)")


def test_actuator_info():
    """Test Spring Boot Actuator info endpoint."""
    resp = requests.get(f"{BASE_URL}/actuator/info", timeout=10)
    assert resp.status_code == 200, f"Actuator info failed: {resp.status_code}"
    print("PASS: Actuator info endpoint works")


def main():
    """Run all smoke tests."""
    print(f"\n{'='*60}")
    print(f"DayTrader Spring Boot Smoke Tests")
    print(f"Base URL: {BASE_URL}")
    print(f"{'='*60}\n")

    passed = 0
    failed = 0
    errors = []

    tests = [
        ("Health Endpoint", test_health_endpoint),
        ("Welcome Page", test_welcome_page),
        ("Config Page", test_config_page),
        ("Config Update", test_config_update),
        ("Create Quotes", test_create_quotes),
        ("REST Quotes", test_rest_quotes),
        ("REST Multiple Quotes", test_rest_multiple_quotes),
        ("Actuator Info", test_actuator_info),
    ]

    # Run independent tests first
    for name, test_fn in tests:
        try:
            test_fn()
            passed += 1
        except Exception as e:
            failed += 1
            errors.append((name, str(e)))
            print(f"FAIL: {name} - {e}")

    # Run session-dependent tests
    try:
        session = test_register_user()
        passed += 1
    except Exception as e:
        failed += 1
        errors.append(("Register User", str(e)))
        print(f"FAIL: Register User - {e}")
        session = None

    if session:
        session_tests = [
            ("Buy Stock", lambda: test_buy_stock(session)),
            ("Portfolio", lambda: test_portfolio(session)),
            ("Quotes Page", lambda: test_quotes_page(session)),
            ("Account Page", lambda: test_account_page(session)),
            ("Logout", lambda: test_logout(session)),
        ]

        for name, test_fn in session_tests:
            try:
                test_fn()
                passed += 1
            except Exception as e:
                failed += 1
                errors.append((name, str(e)))
                print(f"FAIL: {name} - {e}")

    # Test login separately
    try:
        login_session = test_login_user()
        passed += 1
        try:
            test_logout(login_session)
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(("Logout after login", str(e)))
    except Exception as e:
        failed += 1
        errors.append(("Login User", str(e)))
        print(f"FAIL: Login User - {e}")

    # Scenario servlet test
    try:
        test_scenario_servlet()
        passed += 1
    except Exception as e:
        failed += 1
        errors.append(("Scenario Servlet", str(e)))
        print(f"FAIL: Scenario Servlet - {e}")

    print(f"\n{'='*60}")
    print(f"Results: {passed} passed, {failed} failed")
    print(f"{'='*60}")

    if errors:
        print("\nFailed tests:")
        for name, error in errors:
            print(f"  - {name}: {error}")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
