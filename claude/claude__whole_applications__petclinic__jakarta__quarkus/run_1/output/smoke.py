#!/usr/bin/env python3
"""
Smoke tests for PetClinic Quarkus REST API.
Tests all REST endpoints to verify the migration from Jakarta EE to Quarkus.
"""

import json
import os
import sys
import time
import urllib.request
import urllib.error

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")
REST_BASE = f"{BASE_URL}/rest"

passed = 0
failed = 0
errors = []


def test(name, url, expected_status=200, expected_content_type="application/json", check_body=None):
    global passed, failed, errors
    full_url = url if url.startswith("http") else f"{REST_BASE}{url}"
    try:
        req = urllib.request.Request(full_url)
        req.add_header("Accept", expected_content_type)
        resp = urllib.request.urlopen(req, timeout=30)
        status = resp.getcode()
        body = resp.read().decode("utf-8")
        content_type = resp.headers.get("Content-Type", "")

        if status != expected_status:
            failed += 1
            errors.append(f"FAIL [{name}]: Expected status {expected_status}, got {status}")
            print(f"  FAIL [{name}]: Expected status {expected_status}, got {status}")
            return False

        if check_body and not check_body(body):
            failed += 1
            errors.append(f"FAIL [{name}]: Body check failed. Body: {body[:200]}")
            print(f"  FAIL [{name}]: Body check failed. Body: {body[:200]}")
            return False

        passed += 1
        print(f"  PASS [{name}]")
        return True
    except urllib.error.HTTPError as e:
        if e.code == expected_status:
            passed += 1
            print(f"  PASS [{name}] (expected {expected_status})")
            return True
        failed += 1
        errors.append(f"FAIL [{name}]: HTTP {e.code} - {e.reason}")
        print(f"  FAIL [{name}]: HTTP {e.code} - {e.reason}")
        return False
    except Exception as e:
        failed += 1
        errors.append(f"FAIL [{name}]: {str(e)}")
        print(f"  FAIL [{name}]: {str(e)}")
        return False


def is_json_list(body):
    try:
        data = json.loads(body)
        return isinstance(data, list)
    except:
        return False


def is_json_object(body):
    try:
        data = json.loads(body)
        return isinstance(data, dict)
    except:
        return False


def is_non_empty_json_list(body):
    try:
        data = json.loads(body)
        return isinstance(data, list) and len(data) > 0
    except:
        return False


def has_xml_content(body):
    return "<?xml" in body or "<" in body


def wait_for_server(max_retries=60, delay=2):
    """Wait for the server to be ready."""
    print(f"Waiting for server at {BASE_URL}...")
    for i in range(max_retries):
        try:
            req = urllib.request.Request(f"{BASE_URL}/q/health/ready")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print("Server is ready!")
                return True
        except:
            pass
        # Also try a simple endpoint
        try:
            req = urllib.request.Request(f"{REST_BASE}/petType/list")
            resp = urllib.request.urlopen(req, timeout=5)
            if resp.getcode() == 200:
                print("Server is ready!")
                return True
        except:
            pass
        if i % 10 == 0:
            print(f"  Attempt {i+1}/{max_retries}...")
        time.sleep(delay)
    print("Server did not become ready in time!")
    return False


def main():
    global passed, failed, errors

    if not wait_for_server():
        print("ERROR: Server not ready, aborting smoke tests.")
        sys.exit(1)

    print("\n=== PetClinic Quarkus Smoke Tests ===\n")

    # ---- Health check ----
    print("-- Health Checks --")
    test("Health endpoint", f"{BASE_URL}/q/health", check_body=lambda b: "UP" in b)

    # ---- PetType endpoints ----
    print("\n-- PetType Endpoints --")
    test("PetType list (JSON)", "/petType/list", check_body=is_json_list)
    test("PetType list+json", "/petType/list+json", check_body=is_json_list)
    test("PetType list+xml", "/petType/list+xml", expected_content_type="application/xml", check_body=has_xml_content)
    test("PetType by id (JSON)", "/petType/1", check_body=is_json_object)
    test("PetType by id+json", "/petType/1+json", check_body=is_json_object)
    test("PetType by id+xml", "/petType/1+xml", expected_content_type="application/xml", check_body=has_xml_content)

    # ---- Specialty endpoints ----
    print("\n-- Specialty Endpoints --")
    test("Specialty list (JSON)", "/specialty/list", check_body=is_json_list)
    test("Specialty list+json", "/specialty/list+json", check_body=is_json_list)
    test("Specialty list+xml", "/specialty/list+xml", expected_content_type="application/xml", check_body=has_xml_content)
    test("Specialty by id (JSON)", "/specialty/1", check_body=is_json_object)
    test("Specialty by id+json", "/specialty/1+json", check_body=is_json_object)
    test("Specialty by id+xml", "/specialty/1+xml", expected_content_type="application/xml", check_body=has_xml_content)

    # ---- Vet endpoints ----
    print("\n-- Vet Endpoints --")
    test("Vet list (JSON)", "/vet/list", check_body=is_json_list)
    test("Vet list+json", "/vet/list+json", check_body=is_json_list)
    test("Vet list+xml", "/vet/list+xml", expected_content_type="application/xml", check_body=has_xml_content)
    test("Vet by id (JSON)", "/vet/1", check_body=is_json_object)
    test("Vet by id+json", "/vet/1+json", check_body=is_json_object)
    test("Vet by id+xml", "/vet/1+xml", expected_content_type="application/xml", check_body=has_xml_content)

    # ---- Owner endpoints ----
    print("\n-- Owner Endpoints --")
    test("Owner list (JSON)", "/owner/list", check_body=is_json_list)
    test("Owner list+json", "/owner/list+json", check_body=is_json_list)
    test("Owner list+xml", "/owner/list+xml", expected_content_type="application/xml", check_body=has_xml_content)
    test("Owner by id (JSON)", "/owner/1", check_body=is_json_object)
    test("Owner by id+json", "/owner/1+json", check_body=is_json_object)
    test("Owner by id+xml", "/owner/1+xml", expected_content_type="application/xml", check_body=has_xml_content)

    # ---- Pet endpoints ----
    print("\n-- Pet Endpoints --")
    test("Pet list (JSON)", "/pet/list", check_body=is_json_list)
    test("Pet list+json", "/pet/list+json", check_body=is_json_list)
    test("Pet list+xml", "/pet/list+xml", expected_content_type="application/xml", check_body=has_xml_content)
    test("Pet by id (JSON)", "/pet/1", check_body=is_json_object)
    test("Pet by id+json", "/pet/1+json", check_body=is_json_object)
    test("Pet by id+xml", "/pet/1+xml", expected_content_type="application/xml", check_body=has_xml_content)

    # ---- Visit endpoints ----
    print("\n-- Visit Endpoints --")
    test("Visit list (JSON)", "/visit/list", check_body=is_json_list)
    test("Visit list+json", "/visit/list+json", check_body=is_json_list)
    test("Visit list+xml", "/visit/list+xml", expected_content_type="application/xml", check_body=has_xml_content)
    test("Visit by id (JSON)", "/visit/1", check_body=is_json_object)
    test("Visit by id+json", "/visit/1+json", check_body=is_json_object)
    test("Visit by id+xml", "/visit/1+xml", expected_content_type="application/xml", check_body=has_xml_content)

    # ---- Summary ----
    print(f"\n=== Results: {passed} passed, {failed} failed out of {passed + failed} tests ===")
    if errors:
        print("\nErrors:")
        for e in errors:
            print(f"  - {e}")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
