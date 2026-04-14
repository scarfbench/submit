#!/usr/bin/env python3
"""
Smoke tests for the PetClinic Quarkus application.
Tests the REST API endpoints to verify functionality after migration.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
PASS = 0
FAIL = 0

def test(name, method, path, expected_status=200, check_json=True, check_fn=None):
    global PASS, FAIL
    url = f"{BASE_URL}{path}"
    try:
        req = urllib.request.Request(url, method=method)
        req.add_header("Accept", "application/json")
        resp = urllib.request.urlopen(req, timeout=15)
        status = resp.getcode()
        body = resp.read().decode("utf-8")

        if status != expected_status:
            print(f"  FAIL {name}: expected status {expected_status}, got {status}")
            FAIL += 1
            return

        if check_json and body:
            try:
                data = json.loads(body)
            except json.JSONDecodeError:
                print(f"  FAIL {name}: response is not valid JSON")
                FAIL += 1
                return

            if check_fn:
                result = check_fn(data)
                if result is not True:
                    print(f"  FAIL {name}: {result}")
                    FAIL += 1
                    return

        print(f"  PASS {name}")
        PASS += 1
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            print(f"  PASS {name}")
            PASS += 1
        else:
            print(f"  FAIL {name}: HTTP {e.code} - {e.reason}")
            FAIL += 1
    except Exception as e:
        print(f"  FAIL {name}: {e}")
        FAIL += 1


def wait_for_app(timeout=120):
    """Wait for the application to be ready."""
    print(f"Waiting for application at {BASE_URL} ...")
    start = time.time()
    while time.time() - start < timeout:
        try:
            req = urllib.request.Request(f"{BASE_URL}/q/health/ready", method="GET")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print("Application is ready!")
                return True
        except:
            pass
        time.sleep(2)

    # Try a simpler endpoint as fallback
    try:
        req = urllib.request.Request(f"{BASE_URL}/rest/owner/list", method="GET")
        resp = urllib.request.urlopen(req, timeout=5)
        if resp.getcode() == 200:
            print("Application is ready (health endpoint not available, but REST works)!")
            return True
    except:
        pass

    print("Timed out waiting for application.")
    return False


def run_tests():
    print("\n=== PetClinic Quarkus Smoke Tests ===\n")

    # Owner endpoints
    print("--- Owner Endpoints ---")
    test("GET /rest/owner/list returns JSON array", "GET", "/rest/owner/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 1 else f"Expected non-empty list, got {type(d).__name__} len={len(d) if isinstance(d, list) else 'N/A'}")

    test("GET /rest/owner/1 returns a single owner", "GET", "/rest/owner/1",
         check_fn=lambda d: True if isinstance(d, dict) and "firstName" in d else f"Expected dict with firstName, got {d}")

    test("GET /rest/owner/list+json returns JSON array", "GET", "/rest/owner/list+json",
         check_fn=lambda d: True if isinstance(d, list) else f"Expected list, got {type(d).__name__}")

    test("GET /rest/owner/1+json returns JSON owner", "GET", "/rest/owner/1+json",
         check_fn=lambda d: True if isinstance(d, dict) and d.get("firstName") == "Thomas" else f"Expected Thomas, got {d.get('firstName', 'missing')}")

    # Pet endpoints
    print("\n--- Pet Endpoints ---")
    test("GET /rest/pet/list returns JSON array", "GET", "/rest/pet/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 1 else f"Expected non-empty list, got len={len(d) if isinstance(d, list) else 'N/A'}")

    test("GET /rest/pet/1 returns a single pet", "GET", "/rest/pet/1",
         check_fn=lambda d: True if isinstance(d, dict) and "name" in d else f"Expected dict with name")

    test("GET /rest/pet/list+json returns JSON array", "GET", "/rest/pet/list+json",
         check_fn=lambda d: True if isinstance(d, list) else f"Expected list")

    # PetType endpoints
    print("\n--- PetType Endpoints ---")
    test("GET /rest/petType/list returns JSON array", "GET", "/rest/petType/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 6 else f"Expected list with >= 6 items, got {len(d) if isinstance(d, list) else 'N/A'}")

    test("GET /rest/petType/1 returns a single petType", "GET", "/rest/petType/1",
         check_fn=lambda d: True if isinstance(d, dict) and "name" in d else f"Expected dict with name")

    test("GET /rest/petType/list+json returns JSON array", "GET", "/rest/petType/list+json",
         check_fn=lambda d: True if isinstance(d, list) else f"Expected list")

    # Specialty endpoints
    print("\n--- Specialty Endpoints ---")
    test("GET /rest/specialty/list returns JSON array", "GET", "/rest/specialty/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 1 else f"Expected non-empty list")

    test("GET /rest/specialty/1 returns a single specialty", "GET", "/rest/specialty/1",
         check_fn=lambda d: True if isinstance(d, dict) and "name" in d else f"Expected dict with name")

    test("GET /rest/specialty/list+json returns JSON array", "GET", "/rest/specialty/list+json",
         check_fn=lambda d: True if isinstance(d, list) else f"Expected list")

    # Vet endpoints
    print("\n--- Vet Endpoints ---")
    test("GET /rest/vet/list returns JSON array", "GET", "/rest/vet/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 1 else f"Expected non-empty list")

    test("GET /rest/vet/1 returns a single vet", "GET", "/rest/vet/1",
         check_fn=lambda d: True if isinstance(d, dict) and "firstName" in d else f"Expected dict with firstName")

    test("GET /rest/vet/list+json returns JSON array", "GET", "/rest/vet/list+json",
         check_fn=lambda d: True if isinstance(d, list) else f"Expected list")

    # Visit endpoints
    print("\n--- Visit Endpoints ---")
    test("GET /rest/visit/list returns JSON array", "GET", "/rest/visit/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 1 else f"Expected non-empty list")

    test("GET /rest/visit/1 returns a single visit", "GET", "/rest/visit/1",
         check_fn=lambda d: True if isinstance(d, dict) and "description" in d else f"Expected dict with description")

    test("GET /rest/visit/list+json returns JSON array", "GET", "/rest/visit/list+json",
         check_fn=lambda d: True if isinstance(d, list) else f"Expected list")

    # Data integrity checks
    print("\n--- Data Integrity Checks ---")
    test("Owner list has at least 2 owners", "GET", "/rest/owner/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 2 else f"Expected >= 2 owners")

    test("PetType list has at least 12 pet types", "GET", "/rest/petType/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 12 else f"Expected >= 12 pet types, got {len(d) if isinstance(d, list) else 'N/A'}")

    test("Specialty list has at least 7 specialties", "GET", "/rest/specialty/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 7 else f"Expected >= 7 specialties")

    test("Vet list has at least 2 vets", "GET", "/rest/vet/list",
         check_fn=lambda d: True if isinstance(d, list) and len(d) >= 2 else f"Expected >= 2 vets")

    # Summary
    print(f"\n=== Results: {PASS} passed, {FAIL} failed out of {PASS + FAIL} tests ===\n")
    return FAIL == 0


if __name__ == "__main__":
    if not wait_for_app():
        print("Application did not start in time. Exiting.")
        sys.exit(1)

    success = run_tests()
    sys.exit(0 if success else 1)
