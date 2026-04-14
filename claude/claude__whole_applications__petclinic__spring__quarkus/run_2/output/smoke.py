"""
Smoke tests for PetClinic application migrated to Quarkus.
Tests core functionality: home page, owners CRUD, vets listing, REST API.
"""
import os
import sys
import requests
import time

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")

passed = 0
failed = 0

def test(name, fn):
    global passed, failed
    try:
        fn()
        print(f"  PASS: {name}")
        passed += 1
    except Exception as e:
        print(f"  FAIL: {name} - {e}")
        failed += 1

def test_home_page():
    r = requests.get(f"{BASE_URL}/", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "Welcome" in r.text or "welcome" in r.text.lower(), "Home page should contain 'Welcome'"

def test_find_owners_page():
    r = requests.get(f"{BASE_URL}/owners/find", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"

def test_owners_list():
    """Test listing all owners (no filter)"""
    r = requests.get(f"{BASE_URL}/owners?lastName=", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    # Should contain owner data from seed
    assert "Franklin" in r.text or "Davis" in r.text or "George" in r.text, \
        "Owners list should contain seeded owner data"

def test_owner_details():
    """Test viewing owner details for owner ID 1 (George Franklin)"""
    r = requests.get(f"{BASE_URL}/owners/1", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, \
        "Owner details should contain George Franklin's data"

def test_vets_html_page():
    """Test the vets HTML page"""
    r = requests.get(f"{BASE_URL}/vets.html", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "Carter" in r.text or "Leary" in r.text, \
        "Vets page should contain seeded vet data"

def test_vets_json_api():
    """Test the vets JSON REST endpoint"""
    r = requests.get(f"{BASE_URL}/vets", timeout=10, headers={"Accept": "application/json"})
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    data = r.json()
    # Should return vet data
    assert "vetList" in data or isinstance(data, list), \
        f"Expected vetList in response, got: {list(data.keys()) if isinstance(data, dict) else type(data)}"

def test_create_owner():
    """Test creating a new owner via POST"""
    r = requests.post(f"{BASE_URL}/owners/new", data={
        "firstName": "TestFirst",
        "lastName": "TestLast",
        "address": "123 Test St",
        "city": "TestCity",
        "telephone": "1234567890"
    }, timeout=10, allow_redirects=True)
    assert r.status_code == 200, f"Expected 200 (after redirect), got {r.status_code}"
    assert "TestFirst" in r.text or "TestLast" in r.text, \
        "Should show new owner after creation"

def test_owner_search_by_lastname():
    """Test searching owners by last name"""
    r = requests.get(f"{BASE_URL}/owners?lastName=Davis", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "Davis" in r.text, "Search results should contain 'Davis'"

def test_oups_error_page():
    """Test the error trigger page"""
    r = requests.get(f"{BASE_URL}/oups", timeout=10)
    # Should return 500 or error page
    assert r.status_code in [200, 500], f"Expected 200 or 500, got {r.status_code}"

def test_static_css():
    """Test that static CSS resources are served"""
    r = requests.get(f"{BASE_URL}/resources/css/petclinic.css", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"

def test_new_pet_form():
    """Test accessing the new pet form for owner 1"""
    r = requests.get(f"{BASE_URL}/owners/1/pets/new", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"

def test_new_visit_form():
    """Test accessing the new visit form for owner 1, pet 1"""
    r = requests.get(f"{BASE_URL}/owners/1/pets/1/visits/new", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"

def test_edit_owner_form():
    """Test accessing the edit owner form"""
    r = requests.get(f"{BASE_URL}/owners/1/edit", timeout=10)
    assert r.status_code == 200, f"Expected 200, got {r.status_code}"
    assert "George" in r.text or "Franklin" in r.text, \
        "Edit form should contain owner's current data"

if __name__ == "__main__":
    print(f"\nRunning smoke tests against {BASE_URL}\n")

    # Wait for app to be ready
    for i in range(30):
        try:
            r = requests.get(f"{BASE_URL}/", timeout=5)
            if r.status_code == 200:
                print("Application is ready!\n")
                break
        except:
            pass
        print(f"Waiting for application to start... ({i+1}/30)")
        time.sleep(2)
    else:
        print("ERROR: Application did not start within 60 seconds")
        sys.exit(1)

    test("Home page loads", test_home_page)
    test("Find owners page loads", test_find_owners_page)
    test("Owners list with data", test_owners_list)
    test("Owner details (ID=1)", test_owner_details)
    test("Vets HTML page", test_vets_html_page)
    test("Vets JSON API", test_vets_json_api)
    test("Create new owner", test_create_owner)
    test("Search owners by last name", test_owner_search_by_lastname)
    test("Error page (oups)", test_oups_error_page)
    test("Static CSS resource", test_static_css)
    test("New pet form", test_new_pet_form)
    test("New visit form", test_new_visit_form)
    test("Edit owner form", test_edit_owner_form)

    print(f"\n{'='*50}")
    print(f"Results: {passed} passed, {failed} failed out of {passed + failed}")
    print(f"{'='*50}\n")

    sys.exit(0 if failed == 0 else 1)
