"""
Smoke tests for Petclinic Spring Boot application.
Tests REST API endpoints to verify the migration from Jakarta EE is complete and functional.
"""
import pytest
import requests
import json
import sys
import os

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080")


def get(path, expected_status=200):
    """Helper to make GET requests and check status."""
    url = f"{BASE_URL}{path}"
    resp = requests.get(url, timeout=10)
    assert resp.status_code == expected_status, (
        f"GET {path} returned {resp.status_code}, expected {expected_status}. Body: {resp.text[:500]}"
    )
    return resp


class TestHealthAndActuator:
    """Test that the application is running and healthy."""

    def test_actuator_health(self):
        resp = get("/actuator/health")
        data = resp.json()
        assert data["status"] == "UP", f"Health check failed: {data}"

    def test_actuator_info(self):
        resp = get("/actuator/info")
        assert resp.status_code == 200


class TestOwnerEndpoint:
    """Test Owner REST API endpoints."""

    def test_list_owners(self):
        resp = get("/rest/owner/list")
        data = resp.json()
        assert "owners" in data or "ownerList" in data or isinstance(data, list) or isinstance(data, dict)

    def test_get_owner_by_id(self):
        resp = get("/rest/owner/1")
        data = resp.json()
        assert data.get("id") == 1 or data.get("id") == "1" or "firstName" in data

    def test_get_owner_not_found(self):
        get("/rest/owner/999", expected_status=404)

    def test_list_owners_json(self):
        resp = get("/rest/owner/list+json")
        assert "application/json" in resp.headers.get("Content-Type", "")

    def test_get_owner_json(self):
        resp = get("/rest/owner/1+json")
        assert "application/json" in resp.headers.get("Content-Type", "")


class TestPetEndpoint:
    """Test Pet REST API endpoints."""

    def test_list_pets(self):
        resp = get("/rest/pet/list")
        data = resp.json()
        assert isinstance(data, (dict, list))

    def test_get_pet_by_id(self):
        resp = get("/rest/pet/1")
        data = resp.json()
        assert "name" in data or "id" in data

    def test_get_pet_not_found(self):
        get("/rest/pet/999", expected_status=404)


class TestPetTypeEndpoint:
    """Test PetType REST API endpoints."""

    def test_list_pet_types(self):
        resp = get("/rest/petType/list")
        data = resp.json()
        assert isinstance(data, (dict, list))

    def test_get_pet_type_by_id(self):
        resp = get("/rest/petType/1")
        data = resp.json()
        assert "name" in data or "id" in data

    def test_get_pet_type_not_found(self):
        get("/rest/petType/999", expected_status=404)


class TestSpecialtyEndpoint:
    """Test Specialty REST API endpoints."""

    def test_list_specialties(self):
        resp = get("/rest/specialty/list")
        data = resp.json()
        assert isinstance(data, (dict, list))

    def test_get_specialty_by_id(self):
        resp = get("/rest/specialty/1")
        data = resp.json()
        assert "name" in data or "id" in data

    def test_get_specialty_not_found(self):
        get("/rest/specialty/999", expected_status=404)


class TestVetEndpoint:
    """Test Vet REST API endpoints."""

    def test_list_vets(self):
        resp = get("/rest/vet/list")
        data = resp.json()
        assert isinstance(data, (dict, list))

    def test_get_vet_by_id(self):
        resp = get("/rest/vet/1")
        data = resp.json()
        assert "firstName" in data or "id" in data

    def test_get_vet_not_found(self):
        get("/rest/vet/999", expected_status=404)


class TestVisitEndpoint:
    """Test Visit REST API endpoints."""

    def test_list_visits(self):
        resp = get("/rest/visit/list")
        data = resp.json()
        assert isinstance(data, (dict, list))

    def test_get_visit_by_id(self):
        resp = get("/rest/visit/1")
        data = resp.json()
        assert "description" in data or "id" in data

    def test_get_visit_not_found(self):
        get("/rest/visit/999", expected_status=404)


class TestDataIntegrity:
    """Test that seed data is properly loaded."""

    def test_owner_data_loaded(self):
        resp = get("/rest/owner/list")
        data = resp.json()
        # There should be at least 2 owners from seed data
        if isinstance(data, list):
            owners = data
        else:
            owners = data.get("owners", data.get("ownerList", []))
        assert len(owners) >= 2, f"Expected at least 2 owners, got {len(owners)}"

    def test_pet_type_data_loaded(self):
        resp = get("/rest/petType/list")
        data = resp.json()
        if isinstance(data, list):
            pet_types = data
        else:
            pet_types = data.get("petTypes", data.get("petTypeList", []))
        assert len(pet_types) >= 10, f"Expected at least 10 pet types, got {len(pet_types)}"

    def test_specialty_data_loaded(self):
        resp = get("/rest/specialty/list")
        data = resp.json()
        if isinstance(data, list):
            specialties = data
        else:
            specialties = data.get("specialties", data.get("specialtyList", []))
        assert len(specialties) >= 7, f"Expected at least 7 specialties, got {len(specialties)}"

    def test_vet_data_loaded(self):
        resp = get("/rest/vet/list")
        data = resp.json()
        if isinstance(data, list):
            vets = data
        else:
            vets = data.get("vets", data.get("vetList", []))
        assert len(vets) >= 2, f"Expected at least 2 vets, got {len(vets)}"


class TestXmlSupport:
    """Test XML content negotiation."""

    def test_owner_list_xml(self):
        resp = get("/rest/owner/list+xml")
        content_type = resp.headers.get("Content-Type", "")
        assert "xml" in content_type.lower(), f"Expected XML content type, got {content_type}"

    def test_specialty_list_xml(self):
        resp = get("/rest/specialty/list+xml")
        content_type = resp.headers.get("Content-Type", "")
        assert "xml" in content_type.lower(), f"Expected XML content type, got {content_type}"


if __name__ == "__main__":
    sys.exit(pytest.main([__file__, "-v", "--tb=short"]))
