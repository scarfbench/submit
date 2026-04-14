#!/usr/bin/env python3
"""
Smoke tests for Petclinic Quarkus REST API.
Tests all REST endpoints to verify the migration from Jakarta EE to Quarkus.
"""
import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("SMOKE_BASE_URL", "http://localhost:8080")
REST_BASE = f"{BASE_URL}/rest"

passed = 0
failed = 0
errors = []


def test(name, url, expected_status=200, check_fn=None, min_items=None):
    """Run a single smoke test."""
    global passed, failed, errors
    try:
        req = urllib.request.Request(url)
        req.add_header("Accept", "application/json")
        resp = urllib.request.urlopen(req, timeout=30)
        status = resp.getcode()
        body = resp.read().decode("utf-8")

        if status != expected_status:
            failed += 1
            msg = f"FAIL {name}: expected status {expected_status}, got {status}"
            errors.append(msg)
            print(msg)
            return False

        if check_fn:
            data = json.loads(body)
            result = check_fn(data)
            if not result:
                failed += 1
                msg = f"FAIL {name}: check function failed on response: {body[:200]}"
                errors.append(msg)
                print(msg)
                return False

        if min_items is not None:
            data = json.loads(body)
            if isinstance(data, list):
                if len(data) < min_items:
                    failed += 1
                    msg = f"FAIL {name}: expected at least {min_items} items, got {len(data)}"
                    errors.append(msg)
                    print(msg)
                    return False

        passed += 1
        print(f"PASS {name}")
        return True

    except urllib.error.HTTPError as e:
        failed += 1
        msg = f"FAIL {name}: HTTP {e.code} - {e.reason}"
        errors.append(msg)
        print(msg)
        return False
    except Exception as e:
        failed += 1
        msg = f"FAIL {name}: {type(e).__name__}: {e}"
        errors.append(msg)
        print(msg)
        return False


def wait_for_server(url, timeout=120):
    """Wait for the server to become available."""
    print(f"Waiting for server at {url} ...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(url)
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print(f"Server is ready (took {time.time() - start:.1f}s)")
                return True
        except Exception:
            pass
        time.sleep(2)
    print(f"Server did not become ready within {timeout}s")
    return False


def main():
    global passed, failed

    # Wait for server
    # Try health endpoint first, then fall back to a REST endpoint
    health_url = f"{BASE_URL}/q/health/ready"
    rest_check = f"{REST_BASE}/specialty/list"

    if not wait_for_server(health_url, timeout=120):
        # Try REST endpoint directly
        if not wait_for_server(rest_check, timeout=30):
            print("ERROR: Server not available")
            sys.exit(1)

    print("\n=== Petclinic Quarkus Smoke Tests ===\n")

    # --- Specialty Endpoints ---
    test("GET /rest/specialty/list returns specialties",
         f"{REST_BASE}/specialty/list",
         min_items=1)

    test("GET /rest/specialty/list items have expected fields",
         f"{REST_BASE}/specialty/list",
         check_fn=lambda data: isinstance(data, list) and len(data) > 0 and
                               all(k in data[0] for k in ["id", "name"]))

    test("GET /rest/specialty/1 returns a specialty",
         f"{REST_BASE}/specialty/1",
         check_fn=lambda d: "id" in d and "name" in d)

    test("GET /rest/specialty/list+json returns JSON list",
         f"{REST_BASE}/specialty/list+json",
         min_items=1)

    # --- PetType Endpoints ---
    test("GET /rest/petType/list returns pet types",
         f"{REST_BASE}/petType/list",
         min_items=1)

    test("GET /rest/petType/list items have expected fields",
         f"{REST_BASE}/petType/list",
         check_fn=lambda data: isinstance(data, list) and len(data) > 0 and
                               all(k in data[0] for k in ["id", "name"]))

    test("GET /rest/petType/1 returns a pet type",
         f"{REST_BASE}/petType/1",
         check_fn=lambda d: "id" in d and "name" in d)

    # --- Vet Endpoints ---
    test("GET /rest/vet/list returns vets",
         f"{REST_BASE}/vet/list",
         min_items=1)

    test("GET /rest/vet/list items have expected fields",
         f"{REST_BASE}/vet/list",
         check_fn=lambda data: isinstance(data, list) and len(data) > 0 and
                               all(k in data[0] for k in ["id", "firstName", "lastName"]))

    test("GET /rest/vet/1 returns a vet",
         f"{REST_BASE}/vet/1",
         check_fn=lambda d: "id" in d and "firstName" in d and "lastName" in d)

    test("GET /rest/vet/1 vet has specialties",
         f"{REST_BASE}/vet/1",
         check_fn=lambda d: "specialtyList" in d and
                            isinstance(d["specialtyList"], dict) and
                            "specialty" in d["specialtyList"] and
                            len(d["specialtyList"]["specialty"]) > 0)

    # --- Owner Endpoints ---
    test("GET /rest/owner/list returns owners",
         f"{REST_BASE}/owner/list",
         min_items=1)

    test("GET /rest/owner/list items have expected fields",
         f"{REST_BASE}/owner/list",
         check_fn=lambda data: isinstance(data, list) and len(data) > 0 and
                               all(k in data[0] for k in ["id", "firstName", "lastName"]))

    test("GET /rest/owner/1 returns an owner",
         f"{REST_BASE}/owner/1",
         check_fn=lambda d: "id" in d and "firstName" in d and "lastName" in d)

    test("GET /rest/owner/1 owner has pet list",
         f"{REST_BASE}/owner/1",
         check_fn=lambda d: "petList" in d)

    # --- Pet Endpoints ---
    test("GET /rest/pet/list returns pets",
         f"{REST_BASE}/pet/list",
         min_items=1)

    test("GET /rest/pet/list items have expected fields",
         f"{REST_BASE}/pet/list",
         check_fn=lambda data: isinstance(data, list) and len(data) > 0 and
                               all(k in data[0] for k in ["id", "name"]))

    test("GET /rest/pet/1 returns a pet",
         f"{REST_BASE}/pet/1",
         check_fn=lambda d: "id" in d and "name" in d)

    # --- Visit Endpoints ---
    test("GET /rest/visit/list returns visits",
         f"{REST_BASE}/visit/list",
         min_items=1)

    test("GET /rest/visit/list items have expected fields",
         f"{REST_BASE}/visit/list",
         check_fn=lambda data: isinstance(data, list) and len(data) > 0 and
                               all(k in data[0] for k in ["id", "description"]))

    test("GET /rest/visit/1 returns a visit",
         f"{REST_BASE}/visit/1",
         check_fn=lambda d: "id" in d and "description" in d)

    # --- Data Integrity Checks ---
    test("Specialty count matches seed data (7 specialties)",
         f"{REST_BASE}/specialty/list",
         check_fn=lambda data: len(data) == 7)

    test("PetType count matches seed data (12 pet types)",
         f"{REST_BASE}/petType/list",
         check_fn=lambda data: len(data) == 12)

    test("Vet count matches seed data (2 vets)",
         f"{REST_BASE}/vet/list",
         check_fn=lambda data: len(data) == 2)

    test("Owner count matches seed data (2 owners)",
         f"{REST_BASE}/owner/list",
         check_fn=lambda data: len(data) == 2)

    test("Pet count matches seed data (5 pets)",
         f"{REST_BASE}/pet/list",
         check_fn=lambda data: len(data) == 5)

    test("Visit count matches seed data (7 visits)",
         f"{REST_BASE}/visit/list",
         check_fn=lambda data: len(data) == 7)

    # --- Cross-reference checks ---
    test("Owner 2 (Justus Jonas) has correct name",
         f"{REST_BASE}/owner/2",
         check_fn=lambda d: d.get("firstName") == "Justus" and d.get("lastName") == "Jonas")

    test("Pet 1 (Roger) is a rabbit",
         f"{REST_BASE}/pet/1",
         check_fn=lambda d: d.get("name") == "Roger" and
                            d.get("petType", {}).get("name") == "Rabbit")

    # --- Summary ---
    print(f"\n=== Results: {passed} passed, {failed} failed ===\n")

    if errors:
        print("Failures:")
        for e in errors:
            print(f"  - {e}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
