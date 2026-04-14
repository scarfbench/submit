#!/usr/bin/env python3
"""Smoke tests for the PetClinic application after Quarkus -> Spring Boot migration."""

import sys
import time
import requests

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"


def test_welcome_page():
    """Test that the welcome/home page loads."""
    r = requests.get(f"{BASE_URL}/", timeout=10)
    assert r.status_code == 200, f"Welcome page returned {r.status_code}"
    assert "Welcome" in r.text or "welcome" in r.text.lower(), "Welcome page missing expected content"
    print("PASS: Welcome page loads successfully")


def test_find_owners_page():
    """Test that find owners page loads."""
    r = requests.get(f"{BASE_URL}/owners/find", timeout=10)
    assert r.status_code == 200, f"Find owners page returned {r.status_code}"
    assert "Find Owners" in r.text or "find" in r.text.lower(), "Find owners page missing expected content"
    print("PASS: Find owners page loads successfully")


def test_owners_list():
    """Test that owners list loads (all owners)."""
    r = requests.get(f"{BASE_URL}/owners?lastName=", timeout=10)
    assert r.status_code == 200, f"Owners list returned {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, "Owners list missing seed data"
    print("PASS: Owners list loads with seed data")


def test_owner_details():
    """Test that owner details page loads for a seeded owner."""
    r = requests.get(f"{BASE_URL}/owners/1001", timeout=10)
    assert r.status_code == 200, f"Owner details returned {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, "Owner details missing expected data"
    print("PASS: Owner details page loads successfully")


def test_new_owner_form():
    """Test that new owner form loads."""
    r = requests.get(f"{BASE_URL}/owners/new", timeout=10)
    assert r.status_code == 200, f"New owner form returned {r.status_code}"
    print("PASS: New owner form loads successfully")


def test_vets_html_page():
    """Test that vets HTML page loads."""
    r = requests.get(f"{BASE_URL}/vets.html", timeout=10)
    assert r.status_code == 200, f"Vets HTML page returned {r.status_code}"
    assert "James" in r.text or "Carter" in r.text, "Vets page missing seed data"
    print("PASS: Vets HTML page loads with seed data")


def test_vets_json_api():
    """Test that vets JSON API works."""
    r = requests.get(f"{BASE_URL}/vets", timeout=10, headers={"Accept": "application/json"})
    assert r.status_code == 200, f"Vets JSON API returned {r.status_code}"
    data = r.json()
    assert "vetList" in data or isinstance(data, list), "Vets API returned unexpected format"
    print("PASS: Vets JSON API works")


def test_owners_api_list():
    """Test that owners API list works."""
    r = requests.get(f"{BASE_URL}/owners/api/list", timeout=10)
    assert r.status_code == 200, f"Owners API returned {r.status_code}"
    data = r.json()
    assert len(data) > 0, "Owners API returned empty list"
    print("PASS: Owners API list works")


def test_create_owner():
    """Test creating a new owner via POST."""
    data = {
        "firstName": "Test",
        "lastName": "Owner",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }
    r = requests.post(f"{BASE_URL}/owners/new", data=data, timeout=10)
    assert r.status_code == 200, f"Create owner returned {r.status_code}"
    assert "Test" in r.text or "Owner" in r.text, "Created owner not shown in response"
    print("PASS: Create owner works")


def test_edit_owner_form():
    """Test that edit owner form loads."""
    r = requests.get(f"{BASE_URL}/owners/1001/edit", timeout=10)
    assert r.status_code == 200, f"Edit owner form returned {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, "Edit form missing owner data"
    print("PASS: Edit owner form loads successfully")


def test_new_pet_form():
    """Test that new pet form loads."""
    r = requests.get(f"{BASE_URL}/owners/1001/pets/new", timeout=10)
    assert r.status_code == 200, f"New pet form returned {r.status_code}"
    print("PASS: New pet form loads successfully")


def test_edit_pet_form():
    """Test that edit pet form loads."""
    r = requests.get(f"{BASE_URL}/owners/1001/pets/1001/edit", timeout=10)
    assert r.status_code == 200, f"Edit pet form returned {r.status_code}"
    print("PASS: Edit pet form loads successfully")


def test_new_visit_form():
    """Test that new visit form loads."""
    r = requests.get(f"{BASE_URL}/owners/1001/pets/1001/visits/new", timeout=10)
    assert r.status_code == 200, f"New visit form returned {r.status_code}"
    print("PASS: New visit form loads successfully")


def test_error_page():
    """Test that the error trigger works."""
    r = requests.get(f"{BASE_URL}/oups", timeout=10)
    # Should still return 200 with error template or 500
    assert r.status_code in [200, 500], f"Error page returned {r.status_code}"
    print("PASS: Error page handled correctly")


def wait_for_app(max_wait=60):
    """Wait for app to be ready."""
    for i in range(max_wait):
        try:
            r = requests.get(f"{BASE_URL}/", timeout=5)
            if r.status_code == 200:
                print(f"App is ready after {i+1} seconds")
                return True
        except Exception:
            pass
        time.sleep(1)
    print(f"App did not become ready within {max_wait} seconds")
    return False


if __name__ == "__main__":
    print(f"Running smoke tests against {BASE_URL}")

    if not wait_for_app():
        print("FAIL: Application did not start")
        sys.exit(1)

    tests = [
        test_welcome_page,
        test_find_owners_page,
        test_owners_list,
        test_owner_details,
        test_new_owner_form,
        test_vets_html_page,
        test_vets_json_api,
        test_owners_api_list,
        test_create_owner,
        test_edit_owner_form,
        test_new_pet_form,
        test_edit_pet_form,
        test_new_visit_form,
        test_error_page,
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
        print("Failures:")
        for err in errors:
            print(f"  - {err}")
    print(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)
