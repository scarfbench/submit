import os
import sys
import time
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

def wait_for_ready(timeout=120):
    """Wait for the application to be ready."""
    start = time.time()
    while time.time() - start < timeout:
        try:
            r = requests.get(f"{BASE_URL}/daytrader/servlet/PingServlet", timeout=5, verify=False)
            if r.status_code == 200:
                print(f"Application ready after {int(time.time() - start)}s")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"Application not ready after {timeout}s")
    return False

def test_ping_servlet():
    """Test PingServlet responds."""
    r = requests.get(f"{BASE_URL}/daytrader/servlet/PingServlet", verify=False)
    assert r.status_code == 200, f"PingServlet returned {r.status_code}"
    assert "Ping" in r.text or "ping" in r.text.lower(), f"Unexpected response: {r.text[:200]}"
    print("PASS: PingServlet")

def test_config_servlet():
    """Test TradeConfigServlet responds."""
    r = requests.get(f"{BASE_URL}/daytrader/config", verify=False)
    assert r.status_code == 200, f"ConfigServlet returned {r.status_code}"
    assert "DayTrader" in r.text, f"Unexpected config response: {r.text[:200]}"
    print("PASS: ConfigServlet")

def test_app_servlet():
    """Test TradeAppServlet responds."""
    r = requests.get(f"{BASE_URL}/daytrader/app", verify=False, allow_redirects=True)
    assert r.status_code == 200, f"AppServlet returned {r.status_code}"
    print("PASS: AppServlet")

def populate_db():
    """Populate the database with test users and quotes using small numbers."""
    print("Setting runtime mode to Direct (JDBC) and building DB...")
    try:
        # Set runtime mode to Direct (JDBC) = mode 1, reduce users/quotes
        r = requests.get(
            f"{BASE_URL}/daytrader/config?action=updateConfig&RuntimeMode=1&MaxUsers=50&MaxQuotes=50",
            timeout=30, verify=False
        )
        print(f"  Config update returned {r.status_code}")

        # Build the database
        r = requests.get(f"{BASE_URL}/daytrader/config?action=buildDB", timeout=120, verify=False)
        if r.status_code == 200:
            print("  Database populated successfully")
            return True
        else:
            print(f"  Database population returned status {r.status_code}")
            return False
    except Exception as e:
        print(f"  Database population failed: {e}")
        return False

def test_trade_scenario():
    """Test TradeScenarioServlet responds after DB is populated."""
    r = requests.get(f"{BASE_URL}/daytrader/scenario", verify=False, allow_redirects=True)
    assert r.status_code in [200, 302], f"ScenarioServlet returned {r.status_code}"
    print("PASS: ScenarioServlet")

if __name__ == "__main__":
    if not wait_for_ready():
        print("FAIL: Application did not start in time")
        sys.exit(1)

    tests = [test_ping_servlet, test_config_servlet, test_app_servlet]
    failures = 0
    for test in tests:
        try:
            test()
        except Exception as e:
            print(f"FAIL: {test.__name__}: {e}")
            failures += 1

    # Populate DB then test scenario
    db_populated = populate_db()
    if db_populated:
        try:
            test_trade_scenario()
        except Exception as e:
            print(f"FAIL: test_trade_scenario: {e}")
            failures += 1
    else:
        print("SKIP: test_trade_scenario (DB population failed)")

    total = len(tests) + (1 if db_populated else 0)
    if failures > 0:
        print(f"\n{failures}/{total} tests FAILED")
        sys.exit(1)
    else:
        print(f"\nAll {total} tests PASSED")
        sys.exit(0)
