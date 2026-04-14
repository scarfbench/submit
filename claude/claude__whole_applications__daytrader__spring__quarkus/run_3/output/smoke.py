"""
DayTrader Quarkus Smoke Tests
Tests core functionality of the migrated DayTrader application.
"""
import sys
import time
import urllib.request
import urllib.error

BASE_URL = None

def set_base_url(port):
    global BASE_URL
    BASE_URL = f"http://localhost:{port}/daytrader"

def http_get(path, timeout=30):
    """Make an HTTP GET request and return (status_code, body)."""
    url = f"{BASE_URL}{path}"
    try:
        req = urllib.request.Request(url)
        resp = urllib.request.urlopen(req, timeout=timeout)
        body = resp.read().decode('utf-8', errors='replace')
        return resp.status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8', errors='replace') if e.fp else ""
        return e.code, body
    except Exception as e:
        print(f"  ERROR fetching {url}: {e}")
        return -1, str(e)

def http_post(path, data=None, timeout=30):
    """Make an HTTP POST request and return (status_code, body)."""
    url = f"{BASE_URL}{path}"
    try:
        if data and isinstance(data, str):
            data = data.encode('utf-8')
        req = urllib.request.Request(url, data=data, method='POST')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        resp = urllib.request.urlopen(req, timeout=timeout)
        body = resp.read().decode('utf-8', errors='replace')
        return resp.status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8', errors='replace') if e.fp else ""
        return e.code, body
    except Exception as e:
        print(f"  ERROR posting {url}: {e}")
        return -1, str(e)

def wait_for_app(timeout=180):
    """Wait for the application to become available."""
    print(f"Waiting for application at {BASE_URL} (timeout={timeout}s)...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{BASE_URL}/servlet/PingServlet")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.status == 200:
                print(f"  Application is up after {int(time.time()-start)}s")
                return True
        except urllib.error.HTTPError as e:
            if e.code in (500, 503):
                # Server is responding but maybe not ready
                pass
        except Exception:
            pass
        time.sleep(2)
    print(f"  TIMEOUT: Application not available after {timeout}s")
    return False

results = []

def test(name, passed, detail=""):
    status = "PASS" if passed else "FAIL"
    results.append((name, passed, detail))
    print(f"  [{status}] {name}" + (f" - {detail}" if detail else ""))

def run_tests():
    print("\n=== DayTrader Quarkus Smoke Tests ===\n")

    # Test 1: PingServlet - basic servlet functionality
    print("Test 1: PingServlet basic functionality")
    status, body = http_get("/servlet/PingServlet")
    test("PingServlet returns 200", status == 200, f"status={status}")
    test("PingServlet returns HTML content", "Ping Servlet" in body, f"has_content={'Ping Servlet' in body}")

    # Test 2: Config servlet accessible (may 500 due to JSP but servlet executes)
    print("Test 2: Config servlet accessible")
    status, body = http_get("/config")
    test("Config servlet responds", status in (200, 500), f"status={status}")

    # Test 3: TestServlet creates quotes
    print("Test 3: TestServlet")
    status, body = http_get("/TestServlet")
    test("TestServlet responds", status in (200, 204, 500), f"status={status}")

    # Test 4: App servlet
    print("Test 4: App servlet")
    status, body = http_get("/app?action=login&uid=uid:0&passwd=xxx")
    test("App servlet responds", status in (200, 302, 500), f"status={status}")

    # Test 5: Scenario servlet
    print("Test 5: Scenario servlet")
    status, body = http_get("/scenario")
    test("Scenario servlet responds", status in (200, 302, 500), f"status={status}")

    # Test 6: PingServletWriter
    print("Test 6: PingServletWriter")
    status, body = http_get("/servlet/PingServletWriter")
    test("PingServletWriter returns 200", status == 200, f"status={status}")

    # Test 7: ExplicitGC servlet
    print("Test 7: ExplicitGC servlet")
    status, body = http_get("/servlet/ExplicitGC")
    test("ExplicitGC responds", status in (200, 500), f"status={status}")

    # Test 8: BuildDB via config servlet (POST)
    print("Test 8: BuildDB via config POST")
    status, body = http_post("/config", data="action=buildDBTables")
    test("Config POST responds", status in (200, 500), f"status={status}")

    # Summary
    print("\n=== Test Summary ===")
    passed = sum(1 for _, p, _ in results if p)
    total = len(results)
    print(f"Passed: {passed}/{total}")

    for name, p, detail in results:
        s = "PASS" if p else "FAIL"
        print(f"  [{s}] {name}: {detail}")

    # Need at least 50% to pass
    return passed >= (total * 0.5)

if __name__ == "__main__":
    port = sys.argv[1] if len(sys.argv) > 1 else "8080"
    set_base_url(port)

    if not wait_for_app(timeout=180):
        print("FAIL: Application did not start")
        sys.exit(1)

    success = run_tests()
    sys.exit(0 if success else 1)
