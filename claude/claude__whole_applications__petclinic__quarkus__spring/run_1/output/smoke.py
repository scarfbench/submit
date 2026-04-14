#!/usr/bin/env python3
"""
Smoke tests for PetClinic application (Spring Boot migration).
Tests basic HTTP endpoints to verify the application is working correctly.
"""

import sys
import requests
import time

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"

def test(name, method, path, expected_status=200, data=None, check_text=None, allow_redirect=True):
    """Run a single smoke test."""
    url = f"{BASE_URL}{path}"
    try:
        if method == "GET":
            resp = requests.get(url, timeout=10, allow_redirects=allow_redirect)
        elif method == "POST":
            resp = requests.post(url, data=data, timeout=10, allow_redirects=allow_redirect)
        else:
            print(f"FAIL [{name}]: Unknown method {method}")
            return False

        if resp.status_code != expected_status:
            print(f"FAIL [{name}]: Expected status {expected_status}, got {resp.status_code} for {url}")
            return False

        if check_text and check_text not in resp.text:
            print(f"FAIL [{name}]: Expected text '{check_text}' not found in response for {url}")
            return False

        print(f"PASS [{name}]")
        return True
    except Exception as e:
        print(f"FAIL [{name}]: {e}")
        return False

def wait_for_app(max_retries=30, delay=2):
    """Wait for the application to start."""
    for i in range(max_retries):
        try:
            resp = requests.get(f"{BASE_URL}/", timeout=5)
            if resp.status_code == 200:
                print(f"Application is ready after {i * delay} seconds")
                return True
        except Exception:
            pass
        time.sleep(delay)
    print("Application failed to start within timeout")
    return False

def main():
    print(f"Running smoke tests against {BASE_URL}")
    print("=" * 60)

    if not wait_for_app():
        sys.exit(1)

    results = []

    # Test 1: Welcome page
    results.append(test("Welcome Page", "GET", "/", check_text="Welcome"))

    # Test 2: Find Owners page
    results.append(test("Find Owners Page", "GET", "/owners/find", check_text="Find Owners"))

    # Test 3: List all owners
    results.append(test("List All Owners", "GET", "/owners?lastName=", check_text="Owners"))

    # Test 4: View specific owner (George Franklin - id 1001)
    results.append(test("View Owner 1001", "GET", "/owners/1001", check_text="George"))

    # Test 5: New owner form
    results.append(test("New Owner Form", "GET", "/owners/new", check_text="Owner"))

    # Test 6: Vets page (HTML)
    results.append(test("Vets HTML Page", "GET", "/vets.html", check_text="Veterinarians"))

    # Test 7: Vets JSON API
    results.append(test("Vets JSON API", "GET", "/vets", check_text="Carter"))

    # Test 8: Owner API list
    results.append(test("Owners API List", "GET", "/owners/api/list", check_text="George"))

    # Test 9: Add new pet form
    results.append(test("New Pet Form", "GET", "/owners/1001/pets/new", check_text="Pet"))

    # Test 10: Add new visit form
    results.append(test("New Visit Form", "GET", "/owners/1001/pets/1001/visits/new", check_text="Visit"))

    # Test 11: Error page
    results.append(test("Error Page", "GET", "/oups", check_text="Something"))

    # Test 12: Create a new owner via POST
    results.append(test("Create Owner POST", "POST", "/owners/new", data={
        "firstName": "TestFirst",
        "lastName": "TestLast",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }, check_text="TestFirst"))

    # Test 13: Find owner by last name
    results.append(test("Find Owner By LastName", "GET", "/owners?lastName=Davis", check_text="Davis"))

    print("=" * 60)
    passed = sum(1 for r in results if r)
    total = len(results)
    print(f"Results: {passed}/{total} tests passed")

    if passed < total:
        print("SOME TESTS FAILED")
        sys.exit(1)
    else:
        print("ALL TESTS PASSED")
        sys.exit(0)

if __name__ == "__main__":
    main()
