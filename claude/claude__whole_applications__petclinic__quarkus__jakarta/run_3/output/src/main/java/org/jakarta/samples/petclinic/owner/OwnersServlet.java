package org.jakarta.samples.petclinic.owner;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.jakarta.samples.petclinic.visit.Visit;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/owners/*")
public class OwnersServlet extends HttpServlet {

    @Inject
    EntityManager em;

    @Inject
    Validator validator;

    @Resource
    UserTransaction utx;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Process find form: GET /owners?lastName=
            processFindForm(req, resp);
        } else if (pathInfo.equals("/find")) {
            // Show find form
            req.setAttribute("errors", java.util.Collections.emptyList());
            req.getRequestDispatcher("/WEB-INF/jsp/findOwners.jsp").forward(req, resp);
        } else if (pathInfo.equals("/new")) {
            // Show create form
            req.setAttribute("owner", null);
            req.setAttribute("errors", new HashMap<String, String>());
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(req, resp);
        } else if (pathInfo.matches("/\\d+/edit")) {
            Long ownerId = extractId(pathInfo, 1);
            Owner owner = em.find(Owner.class, ownerId);
            req.setAttribute("owner", owner);
            req.setAttribute("errors", new HashMap<String, String>());
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(req, resp);
        } else if (pathInfo.matches("/\\d+/pets/new")) {
            Long ownerId = extractId(pathInfo, 1);
            Owner owner = em.find(Owner.class, ownerId);
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            req.setAttribute("owner", owner);
            req.setAttribute("pet", null);
            req.setAttribute("petTypes", petTypes);
            req.setAttribute("errors", new HashMap<String, String>());
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(req, resp);
        } else if (pathInfo.matches("/\\d+/pets/\\d+/edit")) {
            String[] parts = pathInfo.split("/");
            Long ownerId = Long.parseLong(parts[1]);
            Long petId = Long.parseLong(parts[3]);
            Owner owner = em.find(Owner.class, ownerId);
            Pet pet = em.find(Pet.class, petId);
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            req.setAttribute("owner", owner);
            req.setAttribute("pet", pet);
            req.setAttribute("petTypes", petTypes);
            req.setAttribute("errors", new HashMap<String, String>());
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(req, resp);
        } else if (pathInfo.matches("/\\d+/pets/\\d+/visits/new")) {
            String[] parts = pathInfo.split("/");
            Long petId = Long.parseLong(parts[3]);
            Pet pet = em.find(Pet.class, petId);
            req.setAttribute("pet", pet);
            req.setAttribute("visit", null);
            req.setAttribute("errors", new HashMap<String, String>());
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateVisitForm.jsp").forward(req, resp);
        } else if (pathInfo.matches("/api/list")) {
            // JSON API - delegate to JAX-RS
            resp.sendRedirect(req.getContextPath() + "/api/owners/list");
        } else if (pathInfo.matches("/\\d+")) {
            Long ownerId = extractId(pathInfo, 1);
            Owner owner = em.find(Owner.class, ownerId);
            if (owner == null) {
                req.setAttribute("message", "Owner not found");
                resp.setHeader("x-error", "true");
                req.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(req, resp);
                return;
            }
            setVisits(owner);
            req.setAttribute("owner", owner);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/new")) {
            processCreateOwner(req, resp);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/edit")) {
            Long ownerId = extractId(pathInfo, 1);
            processUpdateOwner(req, resp, ownerId);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/pets/new")) {
            Long ownerId = extractId(pathInfo, 1);
            processCreatePet(req, resp, ownerId);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/pets/\\d+/edit")) {
            String[] parts = pathInfo.split("/");
            Long ownerId = Long.parseLong(parts[1]);
            Long petId = Long.parseLong(parts[3]);
            processUpdatePet(req, resp, ownerId, petId);
        } else if (pathInfo != null && pathInfo.matches("/\\d+/pets/\\d+/visits/new")) {
            String[] parts = pathInfo.split("/");
            Long ownerId = Long.parseLong(parts[1]);
            Long petId = Long.parseLong(parts[3]);
            processCreateVisit(req, resp, ownerId, petId);
        } else {
            resp.sendError(404);
        }
    }

    private void processFindForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String lastName = req.getParameter("lastName");

        Collection<Owner> owners;
        if (lastName == null || lastName.trim().isEmpty()) {
            owners = em.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
        } else {
            owners = em.createQuery("SELECT o FROM Owner o WHERE o.lastName = :lastName", Owner.class)
                    .setParameter("lastName", lastName)
                    .getResultList();
        }

        if (owners.isEmpty()) {
            req.setAttribute("errors", java.util.Arrays.asList("lastName not found"));
            req.getRequestDispatcher("/WEB-INF/jsp/findOwners.jsp").forward(req, resp);
        } else if (owners.size() == 1) {
            Owner owner = owners.iterator().next();
            setVisits(owner);
            req.setAttribute("owner", owner);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        } else {
            req.setAttribute("owners", owners);
            req.getRequestDispatcher("/WEB-INF/jsp/ownersList.jsp").forward(req, resp);
        }
    }

    private void processCreateOwner(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Owner owner = new Owner();
        owner.firstName = req.getParameter("firstName");
        owner.lastName = req.getParameter("lastName");
        owner.address = req.getParameter("address");
        owner.city = req.getParameter("city");
        owner.telephone = req.getParameter("telephone");

        Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            req.setAttribute("owner", null);
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(req, resp);
        } else {
            try {
                utx.begin();
                em.persist(owner);
                utx.commit();
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception re) { /* ignore */ }
                throw new ServletException("Failed to create owner", e);
            }
            setVisits(owner);
            req.setAttribute("owner", owner);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        }
    }

    private void processUpdateOwner(HttpServletRequest req, HttpServletResponse resp, Long ownerId) throws ServletException, IOException {
        Owner owner = new Owner();
        owner.id = ownerId;
        owner.firstName = req.getParameter("firstName");
        owner.lastName = req.getParameter("lastName");
        owner.address = req.getParameter("address");
        owner.city = req.getParameter("city");
        owner.telephone = req.getParameter("telephone");

        Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            req.setAttribute("owner", owner);
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(req, resp);
        } else {
            Owner merged;
            try {
                utx.begin();
                merged = em.merge(owner);
                utx.commit();
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception re) { /* ignore */ }
                throw new ServletException("Failed to update owner", e);
            }
            setVisits(merged);
            req.setAttribute("owner", merged);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        }
    }

    private void processCreatePet(HttpServletRequest req, HttpServletResponse resp, Long ownerId) throws ServletException, IOException {
        Owner owner = em.find(Owner.class, ownerId);
        Pet pet = new Pet();
        pet.name = req.getParameter("name");
        String birthDateStr = req.getParameter("birthDate");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            pet.birthDate = java.time.LocalDate.parse(birthDateStr);
        }
        String typeName = req.getParameter("type");
        pet.type = findPetType(typeName);

        Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            req.setAttribute("owner", owner);
            req.setAttribute("pet", null);
            req.setAttribute("petTypes", petTypes);
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(req, resp);
        } else {
            try {
                utx.begin();
                pet.owner = owner;
                em.persist(pet);
                utx.commit();
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception re) { /* ignore */ }
                throw new ServletException("Failed to create pet", e);
            }
            owner.addPet(pet);
            setVisits(owner);
            req.setAttribute("owner", owner);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        }
    }

    private void processUpdatePet(HttpServletRequest req, HttpServletResponse resp, Long ownerId, Long petId) throws ServletException, IOException {
        Pet pet = new Pet();
        pet.id = petId;
        pet.name = req.getParameter("name");
        String birthDateStr = req.getParameter("birthDate");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            pet.birthDate = java.time.LocalDate.parse(birthDateStr);
        }
        String typeName = req.getParameter("type");
        pet.type = findPetType(typeName);

        Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            Pet oldPet = em.find(Pet.class, petId);
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            req.setAttribute("owner", oldPet.owner);
            req.setAttribute("pet", oldPet);
            req.setAttribute("petTypes", petTypes);
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(req, resp);
        } else {
            try {
                utx.begin();
                em.merge(pet);
                utx.commit();
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception re) { /* ignore */ }
                throw new ServletException("Failed to update pet", e);
            }
            Owner owner = em.find(Owner.class, ownerId);
            setVisits(owner);
            req.setAttribute("owner", owner);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        }
    }

    private void processCreateVisit(HttpServletRequest req, HttpServletResponse resp, Long ownerId, Long petId) throws ServletException, IOException {
        Pet pet = em.find(Pet.class, petId);
        Visit visit = new Visit();
        String dateStr = req.getParameter("date");
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            visit.date = java.time.LocalDate.parse(dateStr);
        }
        visit.description = req.getParameter("description");
        visit.petId = petId;

        Set<ConstraintViolation<Visit>> violations = validator.validate(visit);
        Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Visit> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            req.setAttribute("pet", pet);
            req.setAttribute("visit", visit);
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateVisitForm.jsp").forward(req, resp);
        } else {
            try {
                utx.begin();
                em.persist(visit);
                utx.commit();
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception re) { /* ignore */ }
                throw new ServletException("Failed to create visit", e);
            }
            pet.addVisit(visit);
            Owner owner = em.find(Owner.class, ownerId);
            setVisits(owner);
            req.setAttribute("owner", owner);
            req.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(req, resp);
        }
    }

    private void setVisits(Owner owner) {
        if (owner != null && owner.pets != null) {
            for (Pet pet : owner.pets) {
                List<Visit> visits = em.createQuery("SELECT v FROM Visit v WHERE v.petId = :petId", Visit.class)
                        .setParameter("petId", pet.id)
                        .getResultList();
                pet.setVisitsInternal(visits);
            }
        }
    }

    private PetType findPetType(String name) {
        List<PetType> types = em.createQuery("SELECT pt FROM PetType pt WHERE pt.name = :name", PetType.class)
                .setParameter("name", name)
                .getResultList();
        if (types.isEmpty()) {
            throw new IllegalArgumentException("type not found: " + name);
        }
        return types.get(0);
    }

    private Long extractId(String pathInfo, int position) {
        String[] parts = pathInfo.split("/");
        return Long.parseLong(parts[position]);
    }
}
