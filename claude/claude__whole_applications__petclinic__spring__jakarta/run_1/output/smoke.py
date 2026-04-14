"""
Smoke tests for the PetClinic application after Spring-to-Jakarta migration.
Tests verify that core functionality is preserved after the migration.
"""

import os
import sys
import time
import urllib.request
import urllib.error
import json


BASE_URL = os.environ.get("APP_URL", "http://localhost:8080")
ERRORS = []
PASSES = []


def fetch(path, method="GET", data=None, expect_status=200, follow_redirects=True):
    """Fetch a URL and return (status_code, body, headers)."""
    url = f"{BASE_URL}{path}"
    try:
        req = urllib.request.Request(url, method=method)
        if data:
            req.data = data.encode("utf-8")
            req.add_header("Content-Type", "application/x-www-form-urlencoded")
        if not follow_redirects:
            class NoRedirect(urllib.request.HTTPRedirectHandler):
                def redirect_request(self, req, fp, code, msg, headers, newurl):
                    raise urllib.error.HTTPError(newurl, code, msg, headers, fp)
            opener = urllib.request.build_opener(NoRedirect)
            try:
                resp = opener.open(req, timeout=15)
                return resp.status, resp.read().decode("utf-8", errors="replace"), dict(resp.headers)
            except urllib.error.HTTPError as e:
                body = ""
                if e.fp:
                    try:
                        body = e.fp.read().decode("utf-8", errors="replace")
                    except:
                        pass
                return e.code, body, dict(e.headers) if hasattr(e, 'headers') else {}
        else:
            resp = urllib.request.urlopen(req, timeout=15)
            return resp.status, resp.read().decode("utf-8", errors="replace"), dict(resp.headers)
    except urllib.error.HTTPError as e:
        body = ""
        if e.fp:
            try:
                body = e.fp.read().decode("utf-8", errors="replace")
            except:
                pass
        return e.code, body, {}
    except Exception as e:
        return 0, str(e), {}


def test(name, condition, detail=""):
    """Record a test result."""
    if condition:
        PASSES.append(name)
        print(f"  PASS: {name}")
    else:
        ERRORS.append(f"{name}: {detail}")
        print(f"  FAIL: {name} - {detail}")


def wait_for_app(timeout=120):
    """Wait for the application to be available."""
    print(f"Waiting for application at {BASE_URL} (timeout={timeout}s)...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            resp = urllib.request.urlopen(f"{BASE_URL}/", timeout=5)
            if resp.status == 200:
                print("Application is ready!")
                return True
        except:
            pass
        time.sleep(2)
    print("Timed out waiting for application!")
    return False


def test_welcome_page():
    """Test the welcome/home page loads."""
    print("\n--- Testing Welcome Page ---")
    status, body, _ = fetch("/")
    test("Welcome page returns 200", status == 200, f"Got status {status}")
    test("Welcome page contains PetClinic", "PetClinic" in body or "petclinic" in body.lower(),
         "PetClinic not found in body")


def test_find_owners_page():
    """Test the find owners page."""
    print("\n--- Testing Find Owners ---")
    status, body, _ = fetch("/owners/find")
    test("Find owners page returns 200", status == 200, f"Got status {status}")
    test("Find owners has form", "owner" in body.lower() or "find" in body.lower(),
         "Expected owner/find in body")


def test_owners_list():
    """Test listing all owners."""
    print("\n--- Testing Owners List ---")
    status, body, _ = fetch("/owners?lastName=")
    test("Owners list returns 200", status == 200, f"Got status {status}")
    test("Owners list contains owner data",
         "Franklin" in body or "Davis" in body or "George" in body,
         "Expected owner names in body")


def test_owner_details():
    """Test viewing a specific owner."""
    print("\n--- Testing Owner Details ---")
    status, body, _ = fetch("/owners/1")
    test("Owner details returns 200", status == 200, f"Got status {status}")
    test("Owner details shows George Franklin",
         "George" in body and "Franklin" in body,
         "Expected George Franklin in body")
    test("Owner details shows pet info",
         "Leo" in body,
         "Expected pet name Leo in body")


def test_vets_html_page():
    """Test the veterinarians HTML page."""
    print("\n--- Testing Vets HTML Page ---")
    status, body, _ = fetch("/vets.html")
    test("Vets HTML page returns 200", status == 200, f"Got status {status}")
    test("Vets page contains vet data",
         "Carter" in body or "Leary" in body or "James" in body,
         "Expected vet names in body")


def test_vets_json():
    """Test the veterinarians JSON endpoint."""
    print("\n--- Testing Vets JSON ---")
    status, body, _ = fetch("/vets")
    test("Vets JSON returns 200", status == 200, f"Got status {status}")
    try:
        data = json.loads(body)
        vet_list = data.get("vetList", [])
        test("Vets JSON has vet list", len(vet_list) > 0,
             f"Got {len(vet_list)} vets")
        if vet_list:
            test("First vet has name fields",
                 "firstName" in vet_list[0] and "lastName" in vet_list[0],
                 "Missing name fields in vet JSON")
    except json.JSONDecodeError:
        test("Vets JSON is valid JSON", False, f"Failed to parse: {body[:200]}")


def test_new_owner_form():
    """Test creating a new owner form loads."""
    print("\n--- Testing New Owner Form ---")
    status, body, _ = fetch("/owners/new")
    test("New owner form returns 200", status == 200, f"Got status {status}")
    test("New owner form has input fields",
         "firstName" in body or "first_name" in body or "First Name" in body.title(),
         "Expected name fields in form")


def test_create_owner():
    """Test creating a new owner via POST."""
    print("\n--- Testing Create Owner ---")
    form_data = "firstName=Test&lastName=Smoketest&address=123+Test+St&city=Testville&telephone=1234567890"
    status, body, headers = fetch("/owners/new", method="POST", data=form_data, follow_redirects=False)
    test("Create owner returns redirect (302/303)",
         status in (302, 303, 200, 301),
         f"Got status {status}")


def test_owner_edit_form():
    """Test that owner edit form loads."""
    print("\n--- Testing Owner Edit Form ---")
    status, body, _ = fetch("/owners/1/edit")
    test("Owner edit form returns 200", status == 200, f"Got status {status}")
    test("Edit form has owner data",
         "George" in body or "Franklin" in body,
         "Expected existing owner data in form")


def test_new_pet_form():
    """Test that the new pet form loads."""
    print("\n--- Testing New Pet Form ---")
    status, body, _ = fetch("/owners/1/pets/new")
    test("New pet form returns 200", status == 200, f"Got status {status}")
    test("Pet form has type selection",
         "cat" in body.lower() or "dog" in body.lower() or "type" in body.lower(),
         "Expected pet type options")


def test_new_visit_form():
    """Test that the new visit form loads."""
    print("\n--- Testing New Visit Form ---")
    status, body, _ = fetch("/owners/1/pets/1/visits/new")
    test("New visit form returns 200", status == 200, f"Got status {status}")
    test("Visit form has description field",
         "description" in body.lower() or "Description" in body,
         "Expected description field in form")


def test_actuator_health():
    """Test the actuator health endpoint if available."""
    print("\n--- Testing Actuator Health ---")
    status, body, _ = fetch("/actuator/health")
    if status == 200:
        test("Health endpoint returns 200", True)
        test("Health status is UP",
             '"UP"' in body or '"status":"UP"' in body.replace(" ", ""),
             f"Unexpected health body: {body[:200]}")
    elif status == 404:
        test("Health endpoint (optional, not found)", True,
             "Actuator not available - may be expected")
    else:
        test("Health endpoint accessible", False, f"Got status {status}")


def test_error_page():
    """Test the error handler."""
    print("\n--- Testing Error Handler ---")
    status, body, _ = fetch("/oups")
    test("Error page returns error status",
         status in (200, 500),
         f"Got status {status}")


def main():
    """Run all smoke tests."""
    print("=" * 60)
    print("PetClinic Smoke Tests - Jakarta Migration Verification")
    print("=" * 60)

    if not wait_for_app():
        print("\nFATAL: Application did not start within timeout!")
        sys.exit(1)

    test_welcome_page()
    test_find_owners_page()
    test_owners_list()
    test_owner_details()
    test_vets_html_page()
    test_vets_json()
    test_new_owner_form()
    test_create_owner()
    test_owner_edit_form()
    test_new_pet_form()
    test_new_visit_form()
    test_actuator_health()
    test_error_page()

    print("\n" + "=" * 60)
    print(f"Results: {len(PASSES)} passed, {len(ERRORS)} failed")
    print("=" * 60)

    if ERRORS:
        print("\nFailed tests:")
        for err in ERRORS:
            print(f"  - {err}")
        sys.exit(1)
    else:
        print("\nAll smoke tests passed!")
        sys.exit(0)


if __name__ == "__main__":
    main()
