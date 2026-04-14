#!/usr/bin/env python3
"""Smoke tests for PetClinic application migrated to Quarkus."""

import os
import sys
import time
import requests

BASE_URL = os.environ.get("PETCLINIC_URL", "http://localhost:8080")


def test_welcome_page():
    """Test the welcome/home page loads."""
    r = requests.get(f"{BASE_URL}/", timeout=10)
    assert r.status_code == 200, f"Welcome page returned {r.status_code}"
    assert "Welcome" in r.text or "welcome" in r.text.lower(), "Welcome page missing expected content"
    print("PASS: Welcome page loads successfully")


def test_find_owners_page():
    """Test the find owners page."""
    r = requests.get(f"{BASE_URL}/owners/find", timeout=10)
    assert r.status_code == 200, f"Find owners page returned {r.status_code}"
    print("PASS: Find owners page loads successfully")


def test_owners_list():
    """Test listing all owners."""
    r = requests.get(f"{BASE_URL}/owners?lastName=", timeout=10)
    assert r.status_code == 200, f"Owners list returned {r.status_code}"
    assert any(name in r.text for name in ["Franklin", "Davis", "Black", "Coleman", "Escobito"]), "Owners list missing expected data"
    print("PASS: Owners list loads successfully with data")


def test_owner_details():
    """Test viewing a specific owner."""
    r = requests.get(f"{BASE_URL}/owners/1", timeout=10)
    assert r.status_code == 200, f"Owner details returned {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, "Owner details missing expected data"
    print("PASS: Owner details page loads successfully")


def test_vets_json():
    """Test the vets JSON endpoint."""
    r = requests.get(f"{BASE_URL}/vets", timeout=10)
    assert r.status_code == 200, f"Vets JSON returned {r.status_code}"
    data = r.json()
    assert "vetList" in data, "Vets JSON missing vetList key"
    assert len(data["vetList"]) > 0, "Vets list is empty"
    print(f"PASS: Vets JSON endpoint returns {len(data['vetList'])} vets")


def test_vets_html():
    """Test the vets HTML page."""
    r = requests.get(f"{BASE_URL}/vets.html", timeout=10)
    assert r.status_code == 200, f"Vets HTML returned {r.status_code}"
    assert "Carter" in r.text or "James" in r.text, "Vets HTML missing expected data"
    print("PASS: Vets HTML page loads successfully")


def test_new_owner_form():
    """Test the new owner creation form loads."""
    r = requests.get(f"{BASE_URL}/owners/new", timeout=10)
    assert r.status_code == 200, f"New owner form returned {r.status_code}"
    print("PASS: New owner form loads successfully")


def test_create_owner():
    """Test creating a new owner."""
    data = {
        "firstName": "TestFirst",
        "lastName": "TestLast",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }
    r = requests.post(f"{BASE_URL}/owners/new", data=data, timeout=10, allow_redirects=True)
    assert r.status_code == 200, f"Create owner returned {r.status_code}"
    assert "TestFirst" in r.text or "TestLast" in r.text, "Created owner not found in response"
    print("PASS: Owner creation works successfully")


def test_search_owner():
    """Test searching for an owner by last name."""
    r = requests.get(f"{BASE_URL}/owners?lastName=Davis", timeout=10)
    assert r.status_code == 200, f"Owner search returned {r.status_code}"
    assert "Davis" in r.text, "Search results missing expected owner"
    print("PASS: Owner search works successfully")


def test_owner_edit_form():
    """Test owner edit form loads."""
    r = requests.get(f"{BASE_URL}/owners/1/edit", timeout=10)
    assert r.status_code == 200, f"Owner edit form returned {r.status_code}"
    print("PASS: Owner edit form loads successfully")


def test_new_pet_form():
    """Test new pet form loads."""
    r = requests.get(f"{BASE_URL}/owners/1/pets/new", timeout=10)
    assert r.status_code == 200, f"New pet form returned {r.status_code}"
    print("PASS: New pet form loads successfully")


def test_new_visit_form():
    """Test new visit form loads."""
    r = requests.get(f"{BASE_URL}/owners/1/pets/1/visits/new", timeout=10)
    assert r.status_code == 200, f"New visit form returned {r.status_code}"
    print("PASS: New visit form loads successfully")


def wait_for_app(max_wait=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL}...")
    start = time.time()
    while time.time() - start < max_wait:
        try:
            r = requests.get(f"{BASE_URL}/", timeout=5)
            if r.status_code == 200:
                print(f"Application ready after {int(time.time() - start)}s")
                return True
        except requests.exceptions.ConnectionError:
            pass
        except requests.exceptions.Timeout:
            pass
        time.sleep(2)
    print(f"Application not ready after {max_wait}s")
    return False


def main():
    if not wait_for_app():
        print("FAIL: Application did not start in time")
        sys.exit(1)

    tests = [
        test_welcome_page,
        test_find_owners_page,
        test_owners_list,
        test_owner_details,
        test_vets_json,
        test_vets_html,
        test_new_owner_form,
        test_create_owner,
        test_search_owner,
        test_owner_edit_form,
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
            errors.append(f"FAIL: {test.__name__}: {e}")
            print(f"FAIL: {test.__name__}: {e}")

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")
    if errors:
        print("\nFailed tests:")
        for err in errors:
            print(f"  {err}")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
