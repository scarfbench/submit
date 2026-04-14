#!/usr/bin/env python3
"""
Smoke tests for Petclinic Spring Boot application.
Tests REST API endpoints and web UI pages after migration from Jakarta EE.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error


BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080/petclinic")
MAX_RETRIES = int(os.environ.get("SMOKE_RETRIES", "30"))
RETRY_DELAY = int(os.environ.get("SMOKE_RETRY_DELAY", "2"))

passed = 0
failed = 0
errors = []


def log(msg):
    print(f"  {msg}")


def test(name, func):
    global passed, failed
    try:
        func()
        passed += 1
        print(f"  PASS: {name}")
    except Exception as e:
        failed += 1
        errors.append((name, str(e)))
        print(f"  FAIL: {name} - {e}")


def get(path, accept="text/html"):
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url, headers={"Accept": accept})
    with urllib.request.urlopen(req, timeout=10) as resp:
        body = resp.read().decode("utf-8")
        return resp.status, body


def get_json(path):
    url = f"{BASE_URL}{path}"
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=10) as resp:
        body = resp.read().decode("utf-8")
        return resp.status, json.loads(body)


def wait_for_app():
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} ...")
    for i in range(MAX_RETRIES):
        try:
            req = urllib.request.Request(
                f"{BASE_URL}/actuator/health",
                headers={"Accept": "application/json"},
            )
            with urllib.request.urlopen(req, timeout=5) as resp:
                body = json.loads(resp.read().decode("utf-8"))
                if body.get("status") == "UP":
                    print(f"Application is ready (attempt {i + 1})")
                    return True
        except Exception:
            pass
        time.sleep(RETRY_DELAY)
    print(f"Application not ready after {MAX_RETRIES * RETRY_DELAY}s")
    return False


# ---- Actuator / Health ----

def test_health_endpoint():
    status, body = get_json("/actuator/health")
    assert status == 200, f"Expected 200, got {status}"
    assert body.get("status") == "UP", f"Expected UP, got {body.get('status')}"


def test_info_endpoint():
    status, _ = get("/actuator/info", accept="application/json")
    assert status == 200, f"Expected 200, got {status}"


# ---- Web UI Pages ----

def test_home_page():
    status, body = get("/")
    assert status == 200, f"Expected 200, got {status}"
    assert "Petclinic" in body, "Home page should contain 'Petclinic'"


def test_info_page():
    status, body = get("/info")
    assert status == 200, f"Expected 200, got {status}"
    assert "Spring Boot" in body or "Petclinic" in body, "Info page content check"


def test_owners_page():
    status, body = get("/owners")
    assert status == 200, f"Expected 200, got {status}"
    assert "Owner" in body or "owner" in body, "Owners page should list owners"


def test_vets_page():
    status, body = get("/vets")
    assert status == 200, f"Expected 200, got {status}"
    assert "Vet" in body or "vet" in body, "Vets page should list vets"


def test_pet_types_page():
    status, body = get("/petTypes")
    assert status == 200, f"Expected 200, got {status}"
    assert "Pet Type" in body or "petType" in body or "type" in body.lower(), "PetTypes page check"


def test_specialties_page():
    status, body = get("/specialties")
    assert status == 200, f"Expected 200, got {status}"
    assert "Specialt" in body or "specialt" in body, "Specialties page check"


# ---- REST API Endpoints ----

def test_rest_owner_list():
    status, body = get_json("/rest/owner/list")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, (list, dict)), "Should return JSON list or object"


def test_rest_vet_list():
    status, body = get_json("/rest/vet/list")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, (list, dict)), "Should return JSON list or object"


def test_rest_pet_type_list():
    status, body = get_json("/rest/petType/list")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, (list, dict)), "Should return JSON list or object"


def test_rest_specialty_list():
    status, body = get_json("/rest/specialty/list")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, (list, dict)), "Should return JSON list or object"


def test_rest_pet_list():
    status, body = get_json("/rest/pet/list")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, (list, dict)), "Should return JSON list or object"


def test_rest_visit_list():
    status, body = get_json("/rest/visit/list")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, (list, dict)), "Should return JSON list or object"


# ---- REST API Individual Resource ----

def test_rest_owner_by_id():
    status, body = get_json("/rest/owner/1")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, dict), "Should return JSON object"


def test_rest_vet_by_id():
    status, body = get_json("/rest/vet/1")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, dict), "Should return JSON object"


def test_rest_pet_type_by_id():
    status, body = get_json("/rest/petType/1")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, dict), "Should return JSON object"


def test_rest_specialty_by_id():
    status, body = get_json("/rest/specialty/1")
    assert status == 200, f"Expected 200, got {status}"
    assert isinstance(body, dict), "Should return JSON object"


if __name__ == "__main__":
    print("=" * 60)
    print("Petclinic Spring Boot - Smoke Tests")
    print("=" * 60)

    if not wait_for_app():
        print("ABORT: Application did not become ready")
        sys.exit(1)

    print()
    print("--- Actuator / Health ---")
    test("Health endpoint returns UP", test_health_endpoint)
    test("Info endpoint accessible", test_info_endpoint)

    print()
    print("--- Web UI Pages ---")
    test("Home page loads", test_home_page)
    test("Info page loads", test_info_page)
    test("Owners page loads", test_owners_page)
    test("Vets page loads", test_vets_page)
    test("Pet Types page loads", test_pet_types_page)
    test("Specialties page loads", test_specialties_page)

    print()
    print("--- REST API List Endpoints ---")
    test("GET /rest/owner/list", test_rest_owner_list)
    test("GET /rest/vet/list", test_rest_vet_list)
    test("GET /rest/petType/list", test_rest_pet_type_list)
    test("GET /rest/specialty/list", test_rest_specialty_list)
    test("GET /rest/pet/list", test_rest_pet_list)
    test("GET /rest/visit/list", test_rest_visit_list)

    print()
    print("--- REST API Individual Resources ---")
    test("GET /rest/owner/1", test_rest_owner_by_id)
    test("GET /rest/vet/1", test_rest_vet_by_id)
    test("GET /rest/petType/1", test_rest_pet_type_by_id)
    test("GET /rest/specialty/1", test_rest_specialty_by_id)

    print()
    print("=" * 60)
    print(f"Results: {passed} passed, {failed} failed, {passed + failed} total")
    print("=" * 60)

    if errors:
        print()
        print("Failures:")
        for name, err in errors:
            print(f"  - {name}: {err}")

    sys.exit(0 if failed == 0 else 1)
