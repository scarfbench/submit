package org.petclinic.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.petclinic.model.Owner;
import org.petclinic.model.Pet;
import org.petclinic.model.PetType;
import org.petclinic.repository.OwnerRepository;
import org.petclinic.repository.PetTypeRepository;
import org.thymeleaf.context.WebContext;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/pets/*")
public class PetServlet extends BaseServlet {

    @Inject
    private OwnerRepository ownerRepository;

    @Inject
    private PetTypeRepository petTypeRepository;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (pathInfo.matches("/\\d+/pets/new")) {
            showCreateForm(request, response, pathInfo);
        } else if (pathInfo.matches("/\\d+/pets/\\d+/edit")) {
            showEditForm(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (pathInfo.matches("/\\d+/pets/new")) {
            processCreateForm(request, response, pathInfo);
        } else if (pathInfo.matches("/\\d+/pets/\\d+/edit")) {
            processEditForm(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private int extractOwnerId(String pathInfo) {
        String[] parts = pathInfo.split("/");
        return Integer.parseInt(parts[1]);
    }

    private int extractPetId(String pathInfo) {
        String[] parts = pathInfo.split("/");
        // /ownerId/pets/petId/edit
        return Integer.parseInt(parts[3]);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        int ownerId = extractOwnerId(pathInfo);
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);
        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Collection<PetType> types = petTypeRepository.findPetTypes();
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", optOwner.get());
        ctx.setVariable("pet", new Pet());
        ctx.setVariable("types", types);
        processTemplate("pets/createOrUpdatePetForm", ctx, response);
    }

    private void processCreateForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        int ownerId = extractOwnerId(pathInfo);
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);
        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Owner owner = optOwner.get();
        Pet pet = new Pet();
        populatePet(request, pet);

        List<Map<String, String>> errors = validatePet(pet, owner, true);
        if (!errors.isEmpty()) {
            Collection<PetType> types = petTypeRepository.findPetTypes();
            WebContext ctx = createWebContext(request, response);
            ctx.setVariable("owner", owner);
            ctx.setVariable("pet", pet);
            ctx.setVariable("types", types);
            ctx.setVariable("errors", errors);
            processTemplate("pets/createOrUpdatePetForm", ctx, response);
            return;
        }

        owner.addPet(pet);
        ownerRepository.save(owner);
        request.getSession().setAttribute("flash_message", "New Pet has been Added");
        response.sendRedirect(request.getContextPath() + "/owners/" + ownerId);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        int ownerId = extractOwnerId(pathInfo);
        int petId = extractPetId(pathInfo);
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);
        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Owner owner = optOwner.get();
        Pet pet = owner.getPet(petId);
        if (pet == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Collection<PetType> types = petTypeRepository.findPetTypes();
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", owner);
        ctx.setVariable("pet", pet);
        ctx.setVariable("types", types);
        processTemplate("pets/createOrUpdatePetForm", ctx, response);
    }

    private void processEditForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        int ownerId = extractOwnerId(pathInfo);
        int petId = extractPetId(pathInfo);
        Optional<Owner> optOwner = ownerRepository.findById(ownerId);
        if (optOwner.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Owner owner = optOwner.get();
        Pet existingPet = owner.getPet(petId);
        if (existingPet == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        populatePet(request, existingPet);

        List<Map<String, String>> errors = validatePet(existingPet, owner, false);
        if (!errors.isEmpty()) {
            Collection<PetType> types = petTypeRepository.findPetTypes();
            WebContext ctx = createWebContext(request, response);
            ctx.setVariable("owner", owner);
            ctx.setVariable("pet", existingPet);
            ctx.setVariable("types", types);
            ctx.setVariable("errors", errors);
            processTemplate("pets/createOrUpdatePetForm", ctx, response);
            return;
        }

        ownerRepository.save(owner);
        request.getSession().setAttribute("flash_message", "Pet details has been edited");
        response.sendRedirect(request.getContextPath() + "/owners/" + ownerId);
    }

    private void populatePet(HttpServletRequest request, Pet pet) {
        String name = getParam(request, "name");
        if (name != null) pet.setName(name);

        LocalDate birthDate = parseDateParam(request, "birthDate");
        if (birthDate != null) pet.setBirthDate(birthDate);

        String typeStr = getParam(request, "type");
        if (typeStr != null) {
            List<PetType> types = petTypeRepository.findPetTypes();
            for (PetType type : types) {
                if (type.getName().equals(typeStr)) {
                    pet.setType(type);
                    break;
                }
            }
        }
    }

    private List<Map<String, String>> validatePet(Pet pet, Owner owner, boolean isNew) {
        List<Map<String, String>> errors = new ArrayList<>();
        if (!hasText(pet.getName())) {
            errors.add(createError("name", "is required"));
        }
        if (isNew && pet.getType() == null) {
            errors.add(createError("type", "is required"));
        }
        if (pet.getBirthDate() == null) {
            errors.add(createError("birthDate", "is required"));
        }
        if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now())) {
            errors.add(createError("birthDate", "invalid date"));
        }
        if (hasText(pet.getName()) && isNew && owner.getPet(pet.getName(), true) != null) {
            errors.add(createError("name", "already exists"));
        }
        return errors;
    }

    private Map<String, String> createError(String field, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("field", field);
        error.put("message", message);
        return error;
    }
}
