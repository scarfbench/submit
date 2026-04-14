#!/usr/bin/env python3
"""Smoke tests for DayTrader Jakarta EE migration."""
import sys
import time
import urllib.request
import urllib.parse
import json

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
PASS = 0
FAIL = 0

def test(name, url, method="GET", data=None, expected_status=200, check_body=None):
    global PASS, FAIL
    try:
        if data and method == "POST":
            encoded = urllib.parse.urlencode(data).encode('utf-8')
            req = urllib.request.Request(url, data=encoded, method=method)
            req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        else:
            req = urllib.request.Request(url, method=method)

        with urllib.request.urlopen(req, timeout=30) as resp:
            status = resp.status
            body = resp.read().decode('utf-8', errors='replace')

            if status != expected_status:
                print(f"  FAIL {name}: expected status {expected_status}, got {status}")
                FAIL += 1
                return None

            if check_body and check_body not in body:
                print(f"  FAIL {name}: expected body to contain '{check_body}', body={body[:200]}")
                FAIL += 1
                return None

            print(f"  PASS {name}")
            PASS += 1
            return body
    except Exception as e:
        print(f"  FAIL {name}: {e}")
        FAIL += 1
        return None

print(f"Running smoke tests against {BASE_URL}")
print()

# Wait for app to be ready
print("Waiting for application to start...")
for i in range(60):
    try:
        req = urllib.request.Request(f"{BASE_URL}/rest/trade/market", method="GET")
        with urllib.request.urlopen(req, timeout=5) as resp:
            if resp.status == 200:
                print("Application is ready!")
                break
    except:
        pass
    time.sleep(2)
else:
    print("FAIL: Application did not start within 120 seconds")
    sys.exit(1)

print()
print("=== REST API Tests ===")

# Test market summary
test("GET market summary", f"{BASE_URL}/rest/trade/market")

# Test get quote
test("GET quote s:0", f"{BASE_URL}/rest/quotes/s:0")

# Test get multiple quotes
test("GET quotes s:0,s:1", f"{BASE_URL}/rest/quotes/s:0,s:1")

# Test login
body = test("POST login uid:0", f"{BASE_URL}/rest/trade/login",
    method="POST", data={"userID": "uid:0", "password": "xxx"})

# Test get account
test("GET account uid:0", f"{BASE_URL}/rest/trade/account/uid:0")

# Test get account profile
test("GET account profile uid:0", f"{BASE_URL}/rest/trade/account/uid:0/profile")

# Test get holdings
test("GET holdings uid:0", f"{BASE_URL}/rest/trade/account/uid:0/holdings")

# Test get orders
test("GET orders uid:0", f"{BASE_URL}/rest/trade/account/uid:0/orders")

# Test buy
test("POST buy", f"{BASE_URL}/rest/trade/buy",
    method="POST", data={"userID": "uid:0", "symbol": "s:0", "quantity": "10.0"})

# Test register
test("POST register", f"{BASE_URL}/rest/trade/register",
    method="POST", data={
        "userID": "testuser1",
        "password": "testpass",
        "fullname": "Test User",
        "address": "123 Test St",
        "email": "test@test.com",
        "creditcard": "1234-5678-9012-3456",
        "openBalance": "10000.00"
    })

# Test logout
test("POST logout uid:0", f"{BASE_URL}/rest/trade/logout/uid:0", method="POST")

print()
print("=== Web Interface Tests ===")

# Test web welcome page
test("GET web welcome", f"{BASE_URL}/rest/app", check_body="DayTrader")

# Test web quotes
test("POST web quotes", f"{BASE_URL}/rest/app",
    method="POST", data={"action": "quotes", "symbols": "s:0,s:1"})

print()
print("=== Messaging Tests ===")

# Test messaging stats
test("GET messaging stats", f"{BASE_URL}/rest/messaging/stats")

# Test broker ping
test("POST broker ping", f"{BASE_URL}/rest/messaging/ping/broker", method="POST")

# Test streamer ping
test("POST streamer ping", f"{BASE_URL}/rest/messaging/ping/streamer", method="POST")

# Test stats reset
test("POST stats reset", f"{BASE_URL}/rest/messaging/stats/reset", method="POST")

print()
print(f"=== Results: {PASS} passed, {FAIL} failed ===")
sys.exit(0 if FAIL == 0 else 1)
