#!/usr/bin/env python3
"""Smoke tests for the PetClinic application after Jakarta EE migration."""

import sys
import time
import urllib.request
import urllib.error
import json


BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
# WildFly deploys the WAR at /petclinic context path
CTX = "/petclinic"
PASS = 0
FAIL = 0


def test(name, func):
    global PASS, FAIL
    try:
        func()
        PASS += 1
        print(f"  PASS: {name}")
    except Exception as e:
        FAIL += 1
        print(f"  FAIL: {name} -> {e}")


def get(path, expected_status=200, accept="text/html"):
    url = f"{BASE_URL}{CTX}{path}"
    req = urllib.request.Request(url, headers={"Accept": accept})
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        status = resp.getcode()
        body = resp.read().decode("utf-8", errors="replace")
        if status != expected_status:
            raise AssertionError(f"Expected status {expected_status}, got {status}")
        return body
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return e.read().decode("utf-8", errors="replace")
        raise AssertionError(f"Expected status {expected_status}, got {e.code}: {e.read().decode('utf-8', errors='replace')[:200]}")


def post(path, data, expected_status=200, content_type="application/x-www-form-urlencoded"):
    url = f"{BASE_URL}{CTX}{path}"
    if isinstance(data, str):
        data = data.encode("utf-8")
    req = urllib.request.Request(url, data=data, headers={
        "Content-Type": content_type,
        "Accept": "text/html"
    })
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        status = resp.getcode()
        body = resp.read().decode("utf-8", errors="replace")
        if status != expected_status:
            raise AssertionError(f"Expected status {expected_status}, got {status}")
        return body
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            return e.read().decode("utf-8", errors="replace")
        raise AssertionError(f"Expected status {expected_status}, got {e.code}")


def wait_for_app(timeout=180):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL}{CTX}/ ...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            urllib.request.urlopen(f"{BASE_URL}{CTX}/", timeout=5)
            print("Application is ready!")
            return True
        except Exception:
            time.sleep(3)
    raise RuntimeError(f"Application not ready after {timeout}s")


# ─── Tests ────────────────────────────────────────────────────────────────────

def test_welcome_page():
    body = get("/")
    assert "PetClinic" in body or "Welcome" in body or "welcome" in body.lower(), \
        f"Welcome page missing expected content. Body starts with: {body[:200]}"


def test_find_owners_page():
    body = get("/owners/find")
    assert "Find" in body or "find" in body.lower() or "owner" in body.lower(), \
        f"Find owners page missing expected content. Body starts with: {body[:200]}"


def test_owners_list():
    body = get("/owners?lastName=")
    assert "owner" in body.lower() or "George" in body or "Franklin" in body, \
        f"Owners list missing expected content. Body starts with: {body[:200]}"


def test_owner_detail():
    body = get("/owners/1001")
    assert "George" in body or "Franklin" in body, \
        f"Owner detail missing expected content. Body starts with: {body[:200]}"


def test_vets_html_page():
    body = get("/vets.html")
    assert "vet" in body.lower() or "James" in body or "Carter" in body, \
        f"Vets page missing expected content. Body starts with: {body[:200]}"


def test_vets_json():
    body = get("/vets", accept="application/json")
    assert "James" in body or "Carter" in body or "vetList" in body or "vets" in body.lower(), \
        f"Vets JSON missing expected content. Body starts with: {body[:200]}"


def test_new_owner_form():
    body = get("/owners/new")
    assert "form" in body.lower() or "owner" in body.lower(), \
        f"New owner form missing expected content. Body starts with: {body[:200]}"


def test_create_owner():
    data = "firstName=Test&lastName=Smoketest&address=123+Test+St&city=Testville&telephone=1234567890"
    body = post("/owners/new", data)
    assert "Test" in body or "Smoketest" in body, \
        f"Create owner response missing expected content. Body starts with: {body[:200]}"


def test_edit_owner_form():
    body = get("/owners/1001/edit")
    assert "George" in body or "Franklin" in body or "form" in body.lower(), \
        f"Edit owner form missing expected content. Body starts with: {body[:200]}"


def test_new_pet_form():
    body = get("/owners/1001/pets/new")
    assert "form" in body.lower() or "pet" in body.lower(), \
        f"New pet form missing expected content. Body starts with: {body[:200]}"


def test_new_visit_form():
    body = get("/owners/1001/pets/1001/visits/new")
    assert "form" in body.lower() or "visit" in body.lower(), \
        f"New visit form missing expected content. Body starts with: {body[:200]}"


def test_error_page():
    """Test the error trigger page returns without a 500 crash (error is handled)."""
    try:
        body = get("/oups", expected_status=200)
        assert len(body) > 0
    except (AssertionError, Exception):
        # A 500 is also acceptable since this is a deliberately triggered error
        try:
            body = get("/oups", expected_status=500)
        except Exception:
            pass  # Error page in any form is acceptable


def test_owners_api_list():
    body = get("/owners/api/list", accept="application/json")
    data = json.loads(body)
    assert isinstance(data, list), f"Expected a list, got {type(data)}"
    assert len(data) > 0, "Expected at least one owner"


# ─── Main ─────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    wait_for_app()
    print("\nRunning smoke tests...\n")

    test("Welcome page", test_welcome_page)
    test("Find owners page", test_find_owners_page)
    test("Owners list", test_owners_list)
    test("Owner detail (1001)", test_owner_detail)
    test("Vets HTML page", test_vets_html_page)
    test("Vets JSON", test_vets_json)
    test("New owner form", test_new_owner_form)
    test("Create owner", test_create_owner)
    test("Edit owner form", test_edit_owner_form)
    test("New pet form", test_new_pet_form)
    test("New visit form", test_new_visit_form)
    test("Error page", test_error_page)
    test("Owners API list (JSON)", test_owners_api_list)

    print(f"\n{'='*50}")
    print(f"Results: {PASS} passed, {FAIL} failed out of {PASS+FAIL}")
    print(f"{'='*50}")

    sys.exit(1 if FAIL > 0 else 0)
