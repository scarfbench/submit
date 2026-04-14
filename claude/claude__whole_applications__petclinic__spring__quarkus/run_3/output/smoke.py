"""
Smoke tests for PetClinic application migrated to Quarkus.
Tests core functionality: welcome page, owner CRUD, vet listing, and JSON API.
"""
import os
import sys
import requests
import time

BASE_URL = os.environ.get("APP_URL", "http://localhost:8080")

def wait_for_app(url, timeout=120):
    """Wait for the application to be ready."""
    start = time.time()
    while time.time() - start < timeout:
        try:
            r = requests.get(url, timeout=5)
            if r.status_code < 500:
                print(f"Application is ready (status {r.status_code})")
                return True
        except requests.exceptions.ConnectionError:
            pass
        except requests.exceptions.Timeout:
            pass
        time.sleep(2)
    print("Timed out waiting for application")
    return False


def test_welcome_page():
    """Test that the welcome/home page loads."""
    r = requests.get(f"{BASE_URL}/")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "Welcome" in r.text or "welcome" in r.text.lower(), "Welcome page content missing"
    print("PASS: Welcome page loads correctly")


def test_find_owners_page():
    """Test that the find owners page loads."""
    r = requests.get(f"{BASE_URL}/owners/find")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    print("PASS: Find owners page loads correctly")


def test_owners_list():
    """Test that the owners list page loads with search."""
    r = requests.get(f"{BASE_URL}/owners?lastName=")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    print("PASS: Owners list page loads correctly")


def test_owner_details():
    """Test that an individual owner page loads."""
    r = requests.get(f"{BASE_URL}/owners/1")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, "Owner details not found"
    print("PASS: Owner details page loads correctly")


def test_vets_html_page():
    """Test that the vets HTML page loads."""
    r = requests.get(f"{BASE_URL}/vets.html")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    print("PASS: Vets HTML page loads correctly")


def test_vets_json_api():
    """Test that the vets JSON API returns data."""
    r = requests.get(f"{BASE_URL}/vets")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    data = r.json()
    assert "vetList" in data, "Expected vetList in JSON response"
    assert len(data["vetList"]) > 0, "Expected at least one vet"
    print("PASS: Vets JSON API works correctly")


def test_create_owner():
    """Test creating a new owner via form POST."""
    owner_data = {
        "firstName": "TestFirst",
        "lastName": "TestLast",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }
    r = requests.post(f"{BASE_URL}/owners/new", data=owner_data, allow_redirects=True)
    assert r.status_code == 200, f"Expected 200 after redirect, got {r.status_code}"
    assert "TestFirst" in r.text or "TestLast" in r.text, "Created owner not found in response"
    print("PASS: Owner creation works correctly")


def test_static_resources():
    """Test that static CSS resources are accessible."""
    r = requests.get(f"{BASE_URL}/resources/css/petclinic.css")
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    print("PASS: Static resources accessible")


def main():
    if not wait_for_app(BASE_URL):
        print("FAIL: Application did not start in time")
        sys.exit(1)

    tests = [
        test_welcome_page,
        test_find_owners_page,
        test_owners_list,
        test_owner_details,
        test_vets_html_page,
        test_vets_json_api,
        test_create_owner,
        test_static_resources,
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
            print(f"  - {err}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
