#!/usr/bin/env python3
"""
Smoke tests for the PetClinic application.
Tests the core functionality after migration from Quarkus to Jakarta EE.
"""

import os
import sys
import time
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
APP = "/app"
API = "/api"


def app_url(path):
    """Build a full URL for the given path under the Servlet application root."""
    return f"{BASE_URL}{APP}{path}"


def api_url(path):
    """Build a full URL for the given path under the JAX-RS API root."""
    return f"{BASE_URL}{API}{path}"


def test_welcome_page():
    """Test the welcome/home page loads."""
    r = requests.get(app_url("/"), timeout=10)
    assert r.status_code == 200, f"Welcome page returned {r.status_code}"
    assert "PetClinic" in r.text or "Welcome" in r.text or "welcome" in r.text, \
        "Welcome page missing expected content"
    print("[PASS] Welcome page loads correctly")


def test_find_owners_page():
    """Test the find owners page loads."""
    r = requests.get(app_url("/owners/find"), timeout=10)
    assert r.status_code == 200, f"Find owners page returned {r.status_code}"
    print("[PASS] Find owners page loads correctly")


def test_list_all_owners():
    """Test listing all owners."""
    r = requests.get(app_url("/owners?lastName="), timeout=10)
    assert r.status_code == 200, f"List owners returned {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, \
        "Owner list missing expected owner data"
    print("[PASS] List all owners works correctly")


def test_owner_details():
    """Test viewing owner details."""
    r = requests.get(app_url("/owners/1001"), timeout=10)
    assert r.status_code == 200, f"Owner details returned {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, \
        "Owner details missing expected data"
    print("[PASS] Owner details page loads correctly")


def test_vets_html_page():
    """Test the veterinarians HTML page."""
    r = requests.get(app_url("/vets.html"), timeout=10)
    assert r.status_code == 200, f"Vets HTML page returned {r.status_code}"
    assert "James" in r.text or "Carter" in r.text, \
        "Vets page missing expected vet data"
    print("[PASS] Vets HTML page loads correctly")


def test_vets_json_api():
    """Test the vets JSON API."""
    r = requests.get(api_url("/vets"), timeout=10, headers={"Accept": "application/json"})
    assert r.status_code == 200, f"Vets API returned {r.status_code}"
    data = r.json()
    assert "vetList" in data, "Vets JSON missing vetList field"
    assert len(data["vetList"]) > 0, "Vets list is empty"
    print("[PASS] Vets JSON API works correctly")


def test_owners_api():
    """Test the owners JSON API."""
    r = requests.get(api_url("/owners"), timeout=10,
                     headers={"Accept": "application/json"})
    assert r.status_code == 200, f"Owners API returned {r.status_code}"
    data = r.json()
    assert len(data) > 0, "Owners list is empty"
    print("[PASS] Owners JSON API works correctly")


def test_new_owner_form():
    """Test the new owner form loads."""
    r = requests.get(app_url("/owners/new"), timeout=10)
    assert r.status_code == 200, f"New owner form returned {r.status_code}"
    print("[PASS] New owner form loads correctly")


def test_error_page():
    """Test the error page works."""
    r = requests.get(app_url("/oups"), timeout=10)
    # The oups endpoint throws an exception; the error page should render
    assert r.status_code in (200, 500), f"Error page returned {r.status_code}"
    assert "Error" in r.text or "error" in r.text or "Something" in r.text, \
        "Error page missing expected error content"
    print("[PASS] Error page works correctly")


def test_create_owner():
    """Test creating a new owner via POST."""
    data = {
        "firstName": "TestFirst",
        "lastName": "TestLast",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }
    r = requests.post(app_url("/owners/new"), data=data, timeout=10)
    assert r.status_code == 200, f"Create owner returned {r.status_code}"
    assert "TestFirst" in r.text or "TestLast" in r.text, \
        "Created owner not shown in response"
    print("[PASS] Create new owner works correctly")


def test_new_pet_form():
    """Test the new pet form loads for an existing owner."""
    r = requests.get(app_url("/owners/1001/pets/new"), timeout=10)
    assert r.status_code == 200, f"New pet form returned {r.status_code}"
    print("[PASS] New pet form loads correctly")


def test_new_visit_form():
    """Test the new visit form loads for an existing pet."""
    r = requests.get(app_url("/owners/1001/pets/1001/visits/new"), timeout=10)
    assert r.status_code == 200, f"New visit form returned {r.status_code}"
    print("[PASS] New visit form loads correctly")


def wait_for_server(base_url, timeout=120):
    """Wait for the server to become available."""
    start = time.time()
    while time.time() - start < timeout:
        try:
            r = requests.get(f"{base_url}{APP}/", timeout=5)
            if r.status_code < 500:
                return True
        except (requests.ConnectionError, requests.Timeout):
            pass
        time.sleep(2)
    return False


def main():
    print(f"Running smoke tests against {BASE_URL}")
    print("=" * 60)

    # Wait for server to be ready
    print("Waiting for server to be ready...")
    if not wait_for_server(BASE_URL):
        print("[FAIL] Server did not become available within timeout")
        sys.exit(1)
    print("Server is ready!")
    print("=" * 60)

    tests = [
        test_welcome_page,
        test_find_owners_page,
        test_list_all_owners,
        test_owner_details,
        test_vets_html_page,
        test_vets_json_api,
        test_owners_api,
        test_new_owner_form,
        test_error_page,
        test_create_owner,
        test_new_pet_form,
        test_new_visit_form,
    ]

    passed = 0
    failed = 0
    errors = []

    for test in tests:
        try:
            test()
            passed += 1
        except Exception as e:
            failed += 1
            errors.append(f"[FAIL] {test.__name__}: {e}")
            print(f"[FAIL] {test.__name__}: {e}")

    print("=" * 60)
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")

    if errors:
        print("\nFailed tests:")
        for err in errors:
            print(f"  {err}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
