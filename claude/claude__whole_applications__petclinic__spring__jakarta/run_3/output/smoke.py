"""
Smoke tests for PetClinic application.
Tests core functionality: welcome page, owner CRUD, vet listing, etc.
"""
import os
import sys
import time
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def test_welcome_page():
    """Test that the welcome/home page loads."""
    resp = requests.get(f"{BASE_URL}/", timeout=10)
    assert resp.status_code == 200, f"Welcome page returned {resp.status_code}"
    body = resp.text.lower()
    assert "welcome" in body or "petclinic" in body, "Welcome page missing expected content"
    print("PASS: Welcome page loads successfully")


def test_find_owners_page():
    """Test that the find owners page loads."""
    resp = requests.get(f"{BASE_URL}/owners/find", timeout=10)
    assert resp.status_code == 200, f"Find owners page returned {resp.status_code}"
    print("PASS: Find owners page loads successfully")


def test_owners_list():
    """Test listing all owners (empty search)."""
    resp = requests.get(f"{BASE_URL}/owners?lastName=", timeout=10)
    assert resp.status_code == 200, f"Owners list returned {resp.status_code}"
    assert "Franklin" in resp.text or "Davis" in resp.text or "owner" in resp.text.lower(), \
        "Owners list missing expected owner data"
    print("PASS: Owners list loads with data")


def test_owner_details():
    """Test viewing a specific owner's details."""
    resp = requests.get(f"{BASE_URL}/owners/1", timeout=10)
    assert resp.status_code == 200, f"Owner details returned {resp.status_code}"
    assert "George" in resp.text or "Franklin" in resp.text, "Owner details missing expected data"
    print("PASS: Owner details page loads successfully")


def test_create_owner():
    """Test creating a new owner."""
    data = {
        "firstName": "TestFirst",
        "lastName": "TestLast",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }
    resp = requests.post(f"{BASE_URL}/owners/new", data=data, timeout=10, allow_redirects=True)
    assert resp.status_code == 200, f"Create owner returned {resp.status_code}"
    assert "TestFirst" in resp.text or "TestLast" in resp.text, "Created owner not found in response"
    print("PASS: Create owner works successfully")


def test_vets_html_page():
    """Test the veterinarians HTML page."""
    resp = requests.get(f"{BASE_URL}/vets.html", timeout=10)
    assert resp.status_code == 200, f"Vets HTML page returned {resp.status_code}"
    assert "James" in resp.text or "Carter" in resp.text or "vet" in resp.text.lower(), \
        "Vets page missing expected vet data"
    print("PASS: Vets HTML page loads successfully")


def test_vets_json_api():
    """Test the veterinarians JSON API endpoint."""
    resp = requests.get(f"{BASE_URL}/vets", timeout=10, headers={"Accept": "application/json"})
    assert resp.status_code == 200, f"Vets JSON API returned {resp.status_code}"
    print("PASS: Vets JSON/XML API works successfully")


def test_owner_search():
    """Test searching for owners by last name."""
    resp = requests.get(f"{BASE_URL}/owners?lastName=Davis", timeout=10)
    assert resp.status_code == 200, f"Owner search returned {resp.status_code}"
    assert "Davis" in resp.text, "Owner search results missing expected data"
    print("PASS: Owner search works successfully")


def test_error_page():
    """Test that the error trigger page works (returns error page)."""
    resp = requests.get(f"{BASE_URL}/oups", timeout=10)
    assert resp.status_code in [200, 500], f"Error page returned unexpected {resp.status_code}"
    print("PASS: Error page works as expected")


def wait_for_app(max_retries=60, delay=3):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL}...")
    for i in range(max_retries):
        try:
            resp = requests.get(f"{BASE_URL}/", timeout=5)
            if resp.status_code == 200:
                print(f"Application is ready after {(i+1)*delay} seconds")
                return True
        except requests.exceptions.ConnectionError:
            pass
        except requests.exceptions.Timeout:
            pass
        time.sleep(delay)
    print(f"Application did not become ready after {max_retries * delay} seconds")
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
        test_create_owner,
        test_vets_html_page,
        test_vets_json_api,
        test_owner_search,
        test_error_page,
    ]

    passed = 0
    failed = 0
    errors = []

    for test in tests:
        try:
            test()
            passed += 1
        except AssertionError as e:
            failed += 1
            errors.append(f"FAIL: {test.__name__}: {e}")
            print(f"FAIL: {test.__name__}: {e}")
        except Exception as e:
            failed += 1
            errors.append(f"ERROR: {test.__name__}: {e}")
            print(f"ERROR: {test.__name__}: {e}")

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {len(tests)} tests")

    if errors:
        print("\nFailures:")
        for err in errors:
            print(f"  - {err}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
