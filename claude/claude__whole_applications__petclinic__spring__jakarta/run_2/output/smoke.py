#!/usr/bin/env python3
"""
Smoke tests for the PetClinic application after Spring-to-Jakarta migration.
Tests cover all major endpoints and functionality.
"""

import os
import sys
import time
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def log(msg):
    print(f"[SMOKE] {msg}")


def test_welcome_page():
    """Test the welcome/home page loads."""
    log("Testing welcome page (GET /)...")
    resp = requests.get(f"{BASE_URL}/", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    assert "Welcome" in resp.text or "welcome" in resp.text.lower(), "Welcome page content missing"
    log("  PASS: Welcome page loads successfully")


def test_find_owners_page():
    """Test the find owners page."""
    log("Testing find owners page (GET /owners/find)...")
    resp = requests.get(f"{BASE_URL}/owners/find", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: Find owners page loads successfully")


def test_owners_list():
    """Test listing all owners."""
    log("Testing owners list (GET /owners)...")
    resp = requests.get(f"{BASE_URL}/owners?lastName=", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: Owners list page loads successfully")


def test_owner_details():
    """Test viewing a specific owner."""
    log("Testing owner details (GET /owners/1)...")
    resp = requests.get(f"{BASE_URL}/owners/1", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    assert "George" in resp.text or "Franklin" in resp.text, "Owner details missing expected data"
    log("  PASS: Owner details page loads successfully")


def test_new_owner_form():
    """Test the new owner creation form."""
    log("Testing new owner form (GET /owners/new)...")
    resp = requests.get(f"{BASE_URL}/owners/new", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: New owner form loads successfully")


def test_create_owner():
    """Test creating a new owner."""
    log("Testing create owner (POST /owners/new)...")
    data = {
        "firstName": "SmokeTest",
        "lastName": "Owner",
        "address": "123 Test Street",
        "city": "TestCity",
        "telephone": "1234567890"
    }
    resp = requests.post(f"{BASE_URL}/owners/new", data=data, timeout=10, allow_redirects=True)
    assert resp.status_code == 200, f"Expected 200 (after redirect), got {resp.status_code}"
    assert "SmokeTest" in resp.text, "Created owner not found in response"
    log("  PASS: Owner creation works successfully")


def test_edit_owner_form():
    """Test the edit owner form."""
    log("Testing edit owner form (GET /owners/1/edit)...")
    resp = requests.get(f"{BASE_URL}/owners/1/edit", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: Edit owner form loads successfully")


def test_vets_html_page():
    """Test the vets HTML page."""
    log("Testing vets HTML page (GET /vets.html)...")
    resp = requests.get(f"{BASE_URL}/vets.html", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: Vets HTML page loads successfully")


def test_vets_json():
    """Test the vets JSON endpoint."""
    log("Testing vets JSON (GET /vets)...")
    resp = requests.get(f"{BASE_URL}/vets", timeout=10, headers={"Accept": "application/json"})
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    data = resp.json()
    assert "vetList" in data, "Expected 'vetList' key in JSON response"
    assert len(data["vetList"]) > 0, "Expected at least one vet"
    log(f"  PASS: Vets JSON endpoint returns {len(data['vetList'])} vets")


def test_new_pet_form():
    """Test the new pet form for an owner."""
    log("Testing new pet form (GET /owners/1/pets/new)...")
    resp = requests.get(f"{BASE_URL}/owners/1/pets/new", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: New pet form loads successfully")


def test_edit_pet_form():
    """Test the edit pet form."""
    log("Testing edit pet form (GET /owners/1/pets/1/edit)...")
    resp = requests.get(f"{BASE_URL}/owners/1/pets/1/edit", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: Edit pet form loads successfully")


def test_new_visit_form():
    """Test the new visit form."""
    log("Testing new visit form (GET /owners/1/pets/1/visits/new)...")
    resp = requests.get(f"{BASE_URL}/owners/1/pets/1/visits/new", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    log("  PASS: New visit form loads successfully")


def test_create_visit():
    """Test creating a new visit."""
    log("Testing create visit (POST /owners/1/pets/1/visits/new)...")
    data = {
        "date": "2025-01-15",
        "description": "Smoke test visit"
    }
    resp = requests.post(f"{BASE_URL}/owners/1/pets/1/visits/new", data=data, timeout=10, allow_redirects=True)
    assert resp.status_code == 200, f"Expected 200 (after redirect), got {resp.status_code}"
    log("  PASS: Visit creation works successfully")


def test_crash_page():
    """Test the oups (crash) page returns error page."""
    log("Testing crash page (GET /oups)...")
    resp = requests.get(f"{BASE_URL}/oups", timeout=10)
    assert resp.status_code == 500, f"Expected 500, got {resp.status_code}"
    log("  PASS: Crash page returns 500 as expected")


def test_actuator_health():
    """Test actuator health endpoint."""
    log("Testing actuator health (GET /actuator/health)...")
    resp = requests.get(f"{BASE_URL}/actuator/health", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    data = resp.json()
    assert data.get("status") == "UP", f"Expected status UP, got {data.get('status')}"
    log("  PASS: Actuator health endpoint returns UP")


def test_owner_search():
    """Test searching for owners by last name."""
    log("Testing owner search (GET /owners?lastName=Davis)...")
    resp = requests.get(f"{BASE_URL}/owners?lastName=Davis", timeout=10)
    assert resp.status_code == 200, f"Expected 200, got {resp.status_code}"
    assert "Davis" in resp.text, "Search results should contain 'Davis'"
    log("  PASS: Owner search works correctly")


def wait_for_app():
    """Wait for the application to be ready."""
    log(f"Waiting for application at {BASE_URL} to be ready...")
    max_retries = 60
    for i in range(max_retries):
        try:
            resp = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
            if resp.status_code == 200:
                log(f"Application is ready after {i + 1} attempts")
                return True
        except requests.exceptions.ConnectionError:
            pass
        except requests.exceptions.Timeout:
            pass
        time.sleep(2)
    log(f"ERROR: Application not ready after {max_retries * 2} seconds")
    return False


def main():
    if not wait_for_app():
        log("FATAL: Application did not start in time")
        sys.exit(1)

    tests = [
        test_welcome_page,
        test_find_owners_page,
        test_owners_list,
        test_owner_details,
        test_new_owner_form,
        test_create_owner,
        test_edit_owner_form,
        test_vets_html_page,
        test_vets_json,
        test_new_pet_form,
        test_edit_pet_form,
        test_new_visit_form,
        test_create_visit,
        test_crash_page,
        test_actuator_health,
        test_owner_search,
    ]

    passed = 0
    failed = 0
    errors = []

    for test_fn in tests:
        try:
            test_fn()
            passed += 1
        except AssertionError as e:
            failed += 1
            errors.append(f"FAIL: {test_fn.__name__}: {e}")
            log(f"  FAIL: {test_fn.__name__}: {e}")
        except Exception as e:
            failed += 1
            errors.append(f"ERROR: {test_fn.__name__}: {e}")
            log(f"  ERROR: {test_fn.__name__}: {e}")

    log(f"\n{'='*50}")
    log(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")
    if errors:
        log("Failures:")
        for err in errors:
            log(f"  - {err}")
    log(f"{'='*50}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
