#!/usr/bin/env python3
"""
Smoke tests for DayTrader Spring Boot application.
Tests core REST API endpoints to verify migration from Quarkus to Spring Boot.
"""

import json
import sys
import time
import urllib.request
import urllib.parse
import urllib.error

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

passed = 0
failed = 0
errors = []


def test(name, method, path, data=None, expected_status=200, check_body=None):
    global passed, failed, errors
    url = BASE_URL + path
    try:
        if data and method == "POST":
            encoded = urllib.parse.urlencode(data).encode("utf-8")
            req = urllib.request.Request(url, data=encoded, method="POST")
            req.add_header("Content-Type", "application/x-www-form-urlencoded")
        else:
            req = urllib.request.Request(url, method=method)

        try:
            resp = urllib.request.urlopen(req, timeout=15)
            status = resp.getcode()
            body = resp.read().decode("utf-8")
        except urllib.error.HTTPError as e:
            status = e.code
            body = e.read().decode("utf-8") if e.fp else ""

        if status != expected_status:
            msg = f"FAIL {name}: expected status {expected_status}, got {status}"
            print(msg)
            errors.append(msg)
            failed += 1
            return None

        if check_body and not check_body(body):
            msg = f"FAIL {name}: body check failed. Body: {body[:200]}"
            print(msg)
            errors.append(msg)
            failed += 1
            return None

        print(f"PASS {name}")
        passed += 1
        return body

    except Exception as e:
        msg = f"FAIL {name}: exception {e}"
        print(msg)
        errors.append(msg)
        failed += 1
        return None


# Wait for application to be ready
print(f"Testing against {BASE_URL}")
print("Waiting for application to be ready...")
for i in range(30):
    try:
        resp = urllib.request.urlopen(BASE_URL + "/actuator/health", timeout=5)
        if resp.getcode() == 200:
            print("Application is ready!")
            break
    except Exception:
        pass
    time.sleep(2)
else:
    print("FAIL: Application did not become ready within 60 seconds")
    sys.exit(1)

print("\n=== Health Check ===")
test("Health endpoint", "GET", "/actuator/health",
     check_body=lambda b: "UP" in b)

print("\n=== Market Summary ===")
test("Get market summary", "GET", "/rest/trade/market",
     check_body=lambda b: "TSIA" in b or "tsia" in b.lower() or "topGainers" in b or "topLosers" in b)

print("\n=== User Login ===")
body = test("Login uid:0", "POST", "/rest/trade/login",
     data={"userID": "uid:0", "password": "xxx"},
     check_body=lambda b: "uid:0" in b or "accountID" in b)

print("\n=== Account Data ===")
test("Get account data", "GET", "/rest/trade/account/uid:0",
     check_body=lambda b: "accountID" in b or "balance" in b)

test("Get account profile", "GET", "/rest/trade/account/uid:0/profile",
     check_body=lambda b: "uid:0" in b)

print("\n=== Holdings ===")
test("Get holdings", "GET", "/rest/trade/account/uid:0/holdings")

print("\n=== Orders ===")
test("Get orders", "GET", "/rest/trade/account/uid:0/orders")

print("\n=== Quote Operations ===")
test("Get quote s:0", "GET", "/rest/quotes/s:0",
     check_body=lambda b: "s:0" in b)

print("\n=== Buy Stock ===")
test("Buy stock", "POST", "/rest/trade/buy",
     data={"userID": "uid:0", "symbol": "s:1", "quantity": "10.0"},
     check_body=lambda b: "buy" in b.lower() or "orderID" in b)

print("\n=== Messaging ===")
test("Ping broker", "POST", "/rest/messaging/ping/broker",
     check_body=lambda b: "sent" in b)

test("Ping streamer", "POST", "/rest/messaging/ping/streamer",
     check_body=lambda b: "sent" in b)

test("Get messaging stats", "GET", "/rest/messaging/stats",
     check_body=lambda b: "statistics" in b or "timestamp" in b)

print("\n=== Logout ===")
test("Logout uid:0", "POST", "/rest/trade/logout/uid:0")

print("\n=== Registration ===")
test("Register new user", "POST", "/rest/trade/register",
     data={
         "userID": "smoketest1",
         "password": "pass123",
         "fullname": "Smoke Test User",
         "address": "123 Test St",
         "email": "smoke@test.com",
         "creditcard": "1234567890",
         "openBalance": "50000.00"
     },
     check_body=lambda b: "smoketest1" in b or "accountID" in b)

print(f"\n{'='*50}")
print(f"Results: {passed} passed, {failed} failed")
if errors:
    print("\nFailures:")
    for e in errors:
        print(f"  - {e}")

sys.exit(0 if failed == 0 else 1)
