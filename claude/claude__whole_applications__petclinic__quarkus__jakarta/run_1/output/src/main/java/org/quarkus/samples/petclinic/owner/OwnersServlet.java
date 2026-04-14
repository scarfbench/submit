package org.quarkus.samples.petclinic.owner;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.quarkus.samples.petclinic.visit.Visit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = "/app/owners/*")
public class OwnersServlet extends HttpServlet {

    private static final Pattern OWNER_ID_PATTERN = Pattern.compile("^/(\\d+)$");
    private static final Pattern OWNER_EDIT_PATTERN = Pattern.compile("^/(\\d+)/edit$");
    private static final Pattern PET_NEW_PATTERN = Pattern.compile("^/(\\d+)/pets/new$");
    private static final Pattern PET_EDIT_PATTERN = Pattern.compile("^/(\\d+)/pets/(\\d+)/edit$");
    private static final Pattern VISIT_NEW_PATTERN = Pattern.compile("^/(\\d+)/pets/(\\d+)/visits/new$");

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Validator validator;

    @Inject
    private UserTransaction utx;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        if ("/find".equals(pathInfo)) {
            request.setAttribute("errors", Collections.emptyList());
            request.getRequestDispatcher("/WEB-INF/jsp/findOwners.jsp").forward(request, response);
        } else if ("/new".equals(pathInfo)) {
            request.setAttribute("owner", null);
            request.setAttribute("errors", new HashMap<>());
            request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(request, response);
        } else if (pathInfo.isEmpty() || "/".equals(pathInfo)) {
            // List owners or process find form
            processFindForm(request, response);
        } else {
            Matcher ownerIdMatcher = OWNER_ID_PATTERN.matcher(pathInfo);
            Matcher ownerEditMatcher = OWNER_EDIT_PATTERN.matcher(pathInfo);
            Matcher petNewMatcher = PET_NEW_PATTERN.matcher(pathInfo);
            Matcher petEditMatcher = PET_EDIT_PATTERN.matcher(pathInfo);
            Matcher visitNewMatcher = VISIT_NEW_PATTERN.matcher(pathInfo);

            if (ownerIdMatcher.matches()) {
                Long ownerId = Long.parseLong(ownerIdMatcher.group(1));
                showOwner(ownerId, request, response);
            } else if (ownerEditMatcher.matches()) {
                Long ownerId = Long.parseLong(ownerEditMatcher.group(1));
                Owner owner = em.find(Owner.class, ownerId);
                request.setAttribute("owner", owner);
                request.setAttribute("errors", new HashMap<>());
                request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(request, response);
            } else if (petNewMatcher.matches()) {
                Long ownerId = Long.parseLong(petNewMatcher.group(1));
                Owner owner = em.find(Owner.class, ownerId);
                List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
                request.setAttribute("owner", owner);
                request.setAttribute("pet", null);
                request.setAttribute("petTypes", petTypes);
                request.setAttribute("errors", new HashMap<>());
                request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(request, response);
            } else if (petEditMatcher.matches()) {
                Long ownerId = Long.parseLong(petEditMatcher.group(1));
                Long petId = Long.parseLong(petEditMatcher.group(2));
                Owner owner = em.find(Owner.class, ownerId);
                Pet pet = em.find(Pet.class, petId);
                List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
                request.setAttribute("owner", owner);
                request.setAttribute("pet", pet);
                request.setAttribute("petTypes", petTypes);
                request.setAttribute("errors", new HashMap<>());
                request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(request, response);
            } else if (visitNewMatcher.matches()) {
                Long petId = Long.parseLong(visitNewMatcher.group(2));
                Pet pet = em.find(Pet.class, petId);
                request.setAttribute("pet", pet);
                request.setAttribute("visit", null);
                request.setAttribute("errors", new HashMap<>());
                request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateVisitForm.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        Matcher petNewMatcher = PET_NEW_PATTERN.matcher(pathInfo);
        Matcher petEditMatcher = PET_EDIT_PATTERN.matcher(pathInfo);
        Matcher visitNewMatcher = VISIT_NEW_PATTERN.matcher(pathInfo);

        try {
            if ("/new".equals(pathInfo)) {
                processOwnerCreation(request, response);
            } else if (OWNER_EDIT_PATTERN.matcher(pathInfo).matches()) {
                Matcher m = OWNER_EDIT_PATTERN.matcher(pathInfo);
                m.matches();
                Long ownerId = Long.parseLong(m.group(1));
                processOwnerUpdate(ownerId, request, response);
            } else if (petNewMatcher.matches()) {
                Long ownerId = Long.parseLong(petNewMatcher.group(1));
                processPetCreation(ownerId, request, response);
            } else if (petEditMatcher.matches()) {
                Long ownerId = Long.parseLong(petEditMatcher.group(1));
                Long petId = Long.parseLong(petEditMatcher.group(2));
                processPetUpdate(ownerId, petId, request, response);
            } else if (visitNewMatcher.matches()) {
                Long petId = Long.parseLong(visitNewMatcher.group(2));
                processVisitCreation(petId, request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void processFindForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lastName = request.getParameter("lastName");
        List<Owner> owners;

        if (lastName == null || "".equals(lastName.trim())) {
            owners = em.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
        } else {
            owners = em.createQuery("SELECT o FROM Owner o WHERE o.lastName = :lastName", Owner.class)
                    .setParameter("lastName", lastName)
                    .getResultList();
        }

        if (owners.isEmpty()) {
            request.setAttribute("errors", Arrays.asList("lastName not found"));
            request.getRequestDispatcher("/WEB-INF/jsp/findOwners.jsp").forward(request, response);
            return;
        }
        if (owners.size() == 1) {
            Owner owner = owners.get(0);
            setVisits(owner);
            request.setAttribute("owner", owner);
            request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
            return;
        }

        request.setAttribute("owners", owners);
        request.getRequestDispatcher("/WEB-INF/jsp/ownersList.jsp").forward(request, response);
    }

    private void showOwner(Long ownerId, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Owner owner = em.find(Owner.class, ownerId);
        setVisits(owner);
        request.setAttribute("owner", owner);
        request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
    }

    private void processOwnerCreation(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Owner owner = new Owner();
        owner.firstName = request.getParameter("firstName");
        owner.lastName = request.getParameter("lastName");
        owner.address = request.getParameter("address");
        owner.city = request.getParameter("city");
        owner.telephone = request.getParameter("telephone");

        final Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            request.setAttribute("owner", null);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(request, response);
        } else {
            utx.begin();
            em.persist(owner);
            em.flush();
            utx.commit();
            setVisits(owner);
            request.setAttribute("owner", owner);
            request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
        }
    }

    private void processOwnerUpdate(Long ownerId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Owner owner = new Owner();
        owner.id = ownerId;
        owner.firstName = request.getParameter("firstName");
        owner.lastName = request.getParameter("lastName");
        owner.address = request.getParameter("address");
        owner.city = request.getParameter("city");
        owner.telephone = request.getParameter("telephone");

        final Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            request.setAttribute("owner", owner);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateOwnerForm.jsp").forward(request, response);
        } else {
            utx.begin();
            Owner merged = em.merge(owner);
            utx.commit();
            setVisits(merged);
            request.setAttribute("owner", merged);
            request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
        }
    }

    private void processPetCreation(Long ownerId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Owner owner = em.find(Owner.class, ownerId);

        Pet pet = new Pet();
        String birthDateStr = request.getParameter("birthDate");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            pet.birthDate = java.time.LocalDate.parse(birthDateStr);
        }
        pet.name = request.getParameter("name");
        String typeStr = request.getParameter("type");
        if (typeStr != null) {
            pet.type = parsePetType(typeStr);
        }

        final Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            request.setAttribute("owner", owner);
            request.setAttribute("pet", null);
            request.setAttribute("petTypes", petTypes);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(request, response);
        } else {
            utx.begin();
            pet.owner = owner;
            em.persist(pet);
            owner.addPet(pet);
            utx.commit();
            request.setAttribute("owner", owner);
            request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
        }
    }

    private void processPetUpdate(Long ownerId, Long petId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Pet pet = em.find(Pet.class, petId);
        String birthDateStr = request.getParameter("birthDate");
        if (birthDateStr != null && !birthDateStr.trim().isEmpty()) {
            pet.birthDate = java.time.LocalDate.parse(birthDateStr);
        }
        pet.name = request.getParameter("name");
        String typeStr = request.getParameter("type");
        if (typeStr != null) {
            pet.type = parsePetType(typeStr);
        }

        final Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            Pet oldPet = em.find(Pet.class, petId);
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            request.setAttribute("owner", oldPet.owner);
            request.setAttribute("pet", oldPet);
            request.setAttribute("petTypes", petTypes);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdatePetForm.jsp").forward(request, response);
        } else {
            utx.begin();
            Pet merged = em.merge(pet);
            utx.commit();
            request.setAttribute("owner", merged.owner);
            request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
        }
    }

    private void processVisitCreation(Long petId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Pet pet = em.find(Pet.class, petId);

        Visit visit = new Visit();
        String dateStr = request.getParameter("date");
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            visit.date = java.time.LocalDate.parse(dateStr);
        }
        visit.description = request.getParameter("description");

        final Set<ConstraintViolation<Visit>> violations = validator.validate(visit);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Visit> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            request.setAttribute("pet", pet);
            request.setAttribute("visit", visit);
            request.setAttribute("errors", errors);
            request.getRequestDispatcher("/WEB-INF/jsp/createOrUpdateVisitForm.jsp").forward(request, response);
        } else {
            utx.begin();
            visit.petId = pet.id;
            em.persist(visit);
            pet.addVisit(visit);
            utx.commit();
            request.setAttribute("owner", pet.owner);
            request.getRequestDispatcher("/WEB-INF/jsp/ownerDetails.jsp").forward(request, response);
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

    private PetType parsePetType(String text) {
        List<PetType> types = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
        for (PetType pt : types) {
            if (pt.name.equals(text)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("type not found: " + text);
    }
}
