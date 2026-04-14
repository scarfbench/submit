"""
Smoke tests for PetClinic application.
These tests verify the core functionality of the migrated Spring Boot PetClinic app.
Run with: pytest smoke.py -v
Requires: APP_URL environment variable (e.g., http://localhost:8080)
"""
import os
import pytest
import urllib.request
import urllib.parse
import urllib.error
import json


BASE_URL = os.environ.get("APP_URL", "http://localhost:8080")


def http_get(path, accept="text/html"):
    """Helper to perform HTTP GET requests."""
    url = BASE_URL + path
    req = urllib.request.Request(url, headers={"Accept": accept})
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        return resp.getcode(), resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")


def http_post(path, data, content_type="application/x-www-form-urlencoded"):
    """Helper to perform HTTP POST requests that follow redirects manually."""
    url = BASE_URL + path
    if isinstance(data, dict):
        data = urllib.parse.urlencode(data).encode("utf-8")
    elif isinstance(data, str):
        data = data.encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={
        "Content-Type": content_type,
        "Accept": "text/html",
    })
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        return resp.getcode(), resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")


# ============================================================
# Welcome / Home Page
# ============================================================
class TestWelcomePage:
    def test_home_page_returns_200(self):
        code, body = http_get("/")
        assert code == 200, f"Expected 200, got {code}"

    def test_home_page_contains_welcome(self):
        code, body = http_get("/")
        assert "Welcome" in body, "Home page should contain 'Welcome'"

    def test_home_page_has_navigation(self):
        code, body = http_get("/")
        assert "Find Owners" in body or "find" in body.lower(), "Home page should have navigation"


# ============================================================
# Veterinarians
# ============================================================
class TestVeterinarians:
    def test_vets_html_returns_200(self):
        code, body = http_get("/vets.html")
        assert code == 200, f"Expected 200, got {code}"

    def test_vets_html_contains_vets(self):
        code, body = http_get("/vets.html")
        assert "James" in body and "Carter" in body, "Vets page should contain James Carter"

    def test_vets_html_contains_specialties(self):
        code, body = http_get("/vets.html")
        assert "radiology" in body.lower() or "Radiology" in body, "Vets page should show specialties"

    def test_vets_json_returns_200(self):
        code, body = http_get("/vets", accept="application/json")
        assert code == 200, f"Expected 200, got {code}"

    def test_vets_json_contains_data(self):
        code, body = http_get("/vets", accept="application/json")
        data = json.loads(body)
        # Could be a list or an object with vetList
        if isinstance(data, dict) and "vetList" in data:
            vets = data["vetList"]
        elif isinstance(data, list):
            vets = data
        else:
            vets = data.get("vetList", [])
        assert len(vets) > 0, "Should have at least one vet"


# ============================================================
# Find Owners
# ============================================================
class TestFindOwners:
    def test_find_owners_page_returns_200(self):
        code, body = http_get("/owners/find")
        assert code == 200, f"Expected 200, got {code}"

    def test_find_owners_page_has_form(self):
        code, body = http_get("/owners/find")
        assert "lastName" in body, "Find owners page should have lastName field"

    def test_list_all_owners(self):
        code, body = http_get("/owners?lastName=")
        assert code == 200, f"Expected 200, got {code}"
        assert "Franklin" in body or "George" in body, "Should list owners when no filter"

    def test_find_owners_by_last_name(self):
        code, body = http_get("/owners?lastName=Davis")
        assert code == 200, f"Expected 200, got {code}"
        assert "Davis" in body, "Should find owners with last name Davis"

    def test_find_single_owner_redirects_to_details(self):
        code, body = http_get("/owners?lastName=Franklin")
        assert code == 200, f"Expected 200, got {code}"
        # Single owner match should show owner details
        assert "George" in body and "Franklin" in body, "Should show George Franklin"


# ============================================================
# Owner Details
# ============================================================
class TestOwnerDetails:
    def test_owner_details_returns_200(self):
        code, body = http_get("/owners/1001")
        assert code == 200, f"Expected 200, got {code}"

    def test_owner_details_shows_info(self):
        code, body = http_get("/owners/1001")
        assert "George" in body, "Owner details should show first name"
        assert "Franklin" in body, "Owner details should show last name"
        assert "110 W. Liberty St." in body, "Owner details should show address"


# ============================================================
# New Owner
# ============================================================
class TestNewOwner:
    def test_new_owner_form_returns_200(self):
        code, body = http_get("/owners/new")
        assert code == 200, f"Expected 200, got {code}"

    def test_new_owner_form_has_fields(self):
        code, body = http_get("/owners/new")
        assert "firstName" in body, "Form should have firstName field"
        assert "lastName" in body, "Form should have lastName field"
        assert "address" in body, "Form should have address field"

    def test_create_new_owner(self):
        data = {
            "firstName": "TestFirst",
            "lastName": "TestLast",
            "address": "123 Test St",
            "city": "TestCity",
            "telephone": "1234567890"
        }
        code, body = http_post("/owners/new", data)
        assert code == 200, f"Expected 200, got {code}"
        assert "TestFirst" in body, "Should show created owner"


# ============================================================
# Edit Owner
# ============================================================
class TestEditOwner:
    def test_edit_owner_form_returns_200(self):
        code, body = http_get("/owners/1001/edit")
        assert code == 200, f"Expected 200, got {code}"

    def test_edit_owner_form_has_data(self):
        code, body = http_get("/owners/1001/edit")
        assert "George" in body, "Edit form should show first name"


# ============================================================
# Pets
# ============================================================
class TestPets:
    def test_new_pet_form_returns_200(self):
        code, body = http_get("/owners/1001/pets/new")
        assert code == 200, f"Expected 200, got {code}"

    def test_new_pet_form_has_type_select(self):
        code, body = http_get("/owners/1001/pets/new")
        assert "type" in body.lower(), "Pet form should have type field"

    def test_edit_pet_form_returns_200(self):
        code, body = http_get("/owners/1001/pets/1001/edit")
        assert code == 200, f"Expected 200, got {code}"


# ============================================================
# Visits
# ============================================================
class TestVisits:
    def test_new_visit_form_returns_200(self):
        code, body = http_get("/owners/1001/pets/1001/visits/new")
        assert code == 200, f"Expected 200, got {code}"

    def test_new_visit_form_has_fields(self):
        code, body = http_get("/owners/1001/pets/1001/visits/new")
        assert "description" in body.lower(), "Visit form should have description field"


# ============================================================
# Owner API (JSON)
# ============================================================
class TestOwnerApi:
    def test_owners_api_list(self):
        code, body = http_get("/owners/api/list", accept="application/json")
        assert code == 200, f"Expected 200, got {code}"
        data = json.loads(body)
        assert isinstance(data, list), "API should return a list"
        assert len(data) > 0, "Should have at least one owner"


# ============================================================
# Error Page
# ============================================================
class TestErrorPage:
    def test_oups_page(self):
        code, body = http_get("/oups")
        # The error page might return 200 (handled error) or 500
        assert code in [200, 500], f"Expected 200 or 500, got {code}"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
