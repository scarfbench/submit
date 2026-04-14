#!/usr/bin/env python3
"""
Smoke tests for DayTrader Spring Boot Application
Tests the core REST endpoints to verify the application is working correctly.
"""

import os
import sys
import requests

# Get base URL from environment or use default
BASE_URL = os.environ.get('BASE_URL', 'http://localhost:8080')

def test_endpoint(method, path, data=None, expected_status=200, test_name=None):
    """Test a single endpoint and return True if successful."""
    url = f"{BASE_URL}{path}"
    test_name = test_name or f"{method} {path}"

    try:
        if method == 'GET':
            response = requests.get(url, timeout=10)
        elif method == 'POST':
            response = requests.post(url, data=data, timeout=10)
        else:
            print(f"FAIL: {test_name} - Unsupported method {method}")
            return False

        if response.status_code == expected_status:
            print(f"PASS: {test_name} (status={response.status_code})")
            return True
        else:
            print(f"FAIL: {test_name} - Expected {expected_status}, got {response.status_code}")
            print(f"      Response: {response.text[:200]}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"FAIL: {test_name} - Request error: {str(e)}")
        return False
    except Exception as e:
        print(f"FAIL: {test_name} - Unexpected error: {str(e)}")
        return False

def run_smoke_tests():
    """Run all smoke tests and return success status."""
    print(f"Running smoke tests against {BASE_URL}\n")

    tests = []

    # Test 1: GET /rest/trade/market - Get market summary
    tests.append(test_endpoint('GET', '/rest/trade/market',
                                test_name="Get market summary"))

    # Test 2: GET /rest/quotes/s:0 - Get quote for symbol s:0
    tests.append(test_endpoint('GET', '/rest/quotes/s:0',
                                test_name="Get quote for s:0"))

    # Test 3: POST /rest/trade/login - Login user uid:0
    tests.append(test_endpoint('POST', '/rest/trade/login',
                                data={'userID': 'uid:0', 'password': 'xxx'},
                                test_name="Login user uid:0"))

    # Test 4: GET /rest/trade/account/uid:0 - Get account data
    tests.append(test_endpoint('GET', '/rest/trade/account/uid:0',
                                test_name="Get account data for uid:0"))

    # Test 5: GET /rest/trade/account/uid:0/holdings - Get holdings
    tests.append(test_endpoint('GET', '/rest/trade/account/uid:0/holdings',
                                test_name="Get holdings for uid:0"))

    # Test 6: POST /rest/trade/buy - Buy stock
    tests.append(test_endpoint('POST', '/rest/trade/buy',
                                data={'userID': 'uid:0', 'symbol': 's:1', 'quantity': '10'},
                                test_name="Buy 10 shares of s:1"))

    # Test 7: POST /rest/messaging/ping/broker - Ping broker
    tests.append(test_endpoint('POST', '/rest/messaging/ping/broker',
                                data={'message': 'smoke test'},
                                test_name="Ping broker queue"))

    # Test 8: POST /rest/messaging/ping/streamer - Ping streamer
    tests.append(test_endpoint('POST', '/rest/messaging/ping/streamer',
                                data={'message': 'smoke test'},
                                test_name="Ping streamer topic"))

    # Test 9: GET /rest/messaging/stats - Get messaging stats
    tests.append(test_endpoint('GET', '/rest/messaging/stats',
                                test_name="Get messaging stats"))

    # Test 10: GET /rest/app - Get welcome page
    tests.append(test_endpoint('GET', '/rest/app',
                                test_name="Get welcome page"))

    # Summary
    print(f"\n{'='*60}")
    passed = sum(tests)
    total = len(tests)
    print(f"Results: {passed}/{total} tests passed")
    print(f"{'='*60}\n")

    return all(tests)

if __name__ == '__main__':
    success = run_smoke_tests()
    sys.exit(0 if success else 1)
