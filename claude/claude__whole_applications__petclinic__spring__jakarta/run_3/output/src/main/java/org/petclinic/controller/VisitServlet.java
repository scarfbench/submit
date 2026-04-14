package org.petclinic.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.petclinic.model.Owner;
import org.petclinic.model.Pet;
import org.petclinic.model.Visit;
import org.petclinic.repository.OwnerRepository;
import org.thymeleaf.context.WebContext;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/visits/*")
public class VisitServlet extends BaseServlet {

    @Inject
    private OwnerRepository ownerRepository;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/\\d+/pets/\\d+/visits/new")) {
            showNewVisitForm(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.matches("/\\d+/pets/\\d+/visits/new")) {
            processNewVisitForm(request, response, pathInfo);
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
        return Integer.parseInt(parts[3]);
    }

    private void showNewVisitForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
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

        Visit visit = new Visit();
        WebContext ctx = createWebContext(request, response);
        ctx.setVariable("owner", owner);
        ctx.setVariable("pet", pet);
        ctx.setVariable("visit", visit);
        processTemplate("pets/createOrUpdateVisitForm", ctx, response);
    }

    private void processNewVisitForm(HttpServletRequest request, HttpServletResponse response, String pathInfo)
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

        Visit visit = new Visit();
        LocalDate date = parseDateParam(request, "date");
        if (date != null) visit.setDate(date);
        String description = getParam(request, "description");
        if (description != null) visit.setDescription(description);

        List<Map<String, String>> errors = new ArrayList<>();
        if (!hasText(visit.getDescription())) {
            Map<String, String> error = new HashMap<>();
            error.put("field", "description");
            error.put("message", "must not be blank");
            errors.add(error);
        }

        if (!errors.isEmpty()) {
            WebContext ctx = createWebContext(request, response);
            ctx.setVariable("owner", owner);
            ctx.setVariable("pet", pet);
            ctx.setVariable("visit", visit);
            ctx.setVariable("errors", errors);
            processTemplate("pets/createOrUpdateVisitForm", ctx, response);
            return;
        }

        pet.addVisit(visit);
        owner.addVisit(petId, visit);
        ownerRepository.save(owner);
        request.getSession().setAttribute("flash_message", "Your visit has been booked");
        response.sendRedirect(request.getContextPath() + "/owners/" + ownerId);
    }
}
